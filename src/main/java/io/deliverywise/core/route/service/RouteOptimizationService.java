package io.deliverywise.core.route.service;

import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.Assignment;
import com.google.ortools.constraintsolver.FirstSolutionStrategy;
import com.google.ortools.constraintsolver.IntVar;
import com.google.ortools.constraintsolver.RoutingIndexManager;
import com.google.ortools.constraintsolver.RoutingModel;
import com.google.ortools.constraintsolver.RoutingSearchParameters;
import com.google.ortools.constraintsolver.main;
import io.deliverywise.core.route.infrastructure.RouteDataProvider;
import io.deliverywise.core.route.model.DeliveryPoint;
import io.deliverywise.core.route.model.Depot;
import io.deliverywise.core.route.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Core engine for fleet routing optimization using Google OR-Tools.</p>
 * <p>Головний двигун оптимізації маршрутів автопарку на базі Google OR-Tools.</p>
 *
 * @author Ihor Herasymenko
 * @since 18.06.2026
 */
@Service
public class RouteOptimizationService {

    private static final Logger log = LoggerFactory.getLogger(RouteOptimizationService.class);

    private final RouteDataProvider routeDataProvider;
    private final DistanceProvider distanceProvider;
    private final Depot defaultDepot;

    static {
        log.info("Initializing Google OR-Tools native libraries...");
        Loader.loadNativeLibraries();
    }

    /**
     * Constructs the optimization service with required data providers and default SaaS depot properties.
     * Конструктор сервісу оптимізації з необхідними провайдерами даних та параметрами депо за замовчуванням.
     */
    public RouteOptimizationService(
            RouteDataProvider routeDataProvider,
            DistanceProvider distanceProvider,
            @Value("${deliverywise.routing.default-depot.name}") String depotName,
            @Value("${deliverywise.routing.default-depot.address}") String depotAddress,
            @Value("${deliverywise.routing.default-depot.latitude}") double depotLatitude,
            @Value("${deliverywise.routing.default-depot.longitude}") double depotLongitude) {
        this.routeDataProvider = routeDataProvider;
        this.distanceProvider = distanceProvider;
        this.defaultDepot = new Depot(0, depotName, depotAddress, depotLatitude, depotLongitude);
    }

    /**
     * Core orchestration method for routing calculation and VRP solving.
     * Основний метод оркестрації розрахунку маршрутів та розв'язання задачі VRP.
     */
    public void calculateOptimalRoutes() {
        List<DeliveryPoint> clientPoints = routeDataProvider.getAnonymizedDeliveryPoints();
        List<Vehicle> fleet = routeDataProvider.getAvailableFleet();

        // [EN] Safe guard check for data availability
        // [UA] Захисна перевірка на наявність вхідних даних
        if (clientPoints.isEmpty() || fleet.isEmpty()) {
            log.warn("Optimization aborted: No customer points or active vehicles available.");
            return;
        }

        // [EN] Form full location list where Index 0 is always the Depot
        // [UA] Формуємо повний список локацій, де першим (Індекс 0) завжди йде Депо
        List<DeliveryPoint> allLocations = prepareLocationsWithDepot(this.defaultDepot, clientPoints);

        int numLocations = allLocations.size();
        int numVehicles = fleet.size();
        int depotIndex = this.defaultDepot.getId();

        log.info("Starting VRP Optimization. Locations: {} (inc. Depot), Active Fleet: {}", numLocations, numVehicles);

        // [EN] Delegate matrix building to the distance service
        // [UA] Делегуємо побудову матриці нашому картографічному сервісу
        long[][] distanceMatrix = distanceProvider.createDistanceMatrix(allLocations);

        // [EN] Initialize OR-Tools index manager and routing mathematical model
        // [UA] Ініціалізуємо менеджер індексів та математичну модель Google OR-Tools
        RoutingIndexManager manager = new RoutingIndexManager(numLocations, numVehicles, depotIndex);
        RoutingModel routing = new RoutingModel(manager);

        // [EN] Register distance transit callback for the native C++ core
        // [UA] Реєструємо зворотний виклик (Callback) відстаней для нативного ядра C++
        final int transitCallbackIndex = routing.registerTransitCallback((long fromIndex, long toIndex) -> {
            int fromNode = manager.indexToNode(fromIndex);
            int toNode = manager.indexToNode(toIndex);
            return distanceMatrix[fromNode][toNode];
        });

        // [EN] Set global arc cost evaluator (minimize total fleet mileage)
        // [UA] Встановлюємо глобальний критерій вартості (мінімізація загального кілометражу автопарку)
        routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

        // ===================================================================
        // [EN] WEIGHT CONSTRAINT APPLICATION
        // [UA] ВПРОВАДЖЕННЯ ОБМЕЖЕННЯ ПО ВАЗІ
        // ===================================================================
        final int weightCallbackIndex = routing.registerUnaryTransitCallback((long fromIndex) -> {
            int fromNode = manager.indexToNode(fromIndex);
            return (long) allLocations.get(fromNode).getWeightKg();
        });

        long[] vehicleWeightCapacities = fleet.stream()
                .mapToLong(v -> (long) v.getVehicleWeightCapacity())
                .toArray();

        routing.addDimensionWithVehicleCapacity(
                weightCallbackIndex,
                0, // [EN] Zero slack [UA] Нульовий люфт
                vehicleWeightCapacities,
                true, // [EN] Start with zero cumulative weight [UA] Старт з нульовою вагою
                "Weight"
        );

        // ===================================================================
        // [EN] PALLET CONSTRAINT APPLICATION
        // [UA] ВПРОВАДЖЕННЯ ОБМЕЖЕННЯ ПО ПАЛЕТАХ
        // ===================================================================
        final int palletCallbackIndex = routing.registerUnaryTransitCallback((long fromIndex) -> {
            int fromNode = manager.indexToNode(fromIndex);
            return (long) allLocations.get(fromNode).getMainWarehousePlaces();
        });

        long[] vehiclePalletCapacities = fleet.stream()
                .mapToLong(Vehicle::getVehiclePalletCapacity)
                .toArray();

        routing.addDimensionWithVehicleCapacity(
                palletCallbackIndex,
                0,
                vehiclePalletCapacities,
                true,
                "Pallets"
        );

        // ===================================================================
        // [EN] MANDATORY NODES VISITATION (DISJUNCTIONS / PENALTIES)
        // [UA] ОБОВ'ЯЗКОВЕ ВІДВІДУВАННЯ ТОЧОК (ШТРАФИ ЗА ПРОПУСК)
        // ===================================================================
        long penalty = 20000000; // [EN] Huge virtual penalty cost [UA] Величезний віртуальний штраф
        for (int i = 1; i < numLocations; ++i) {
            long index = manager.nodeToIndex(i);
            routing.addDisjunction(new long[]{index}, penalty);
        }

        // ===================================================================
        // [EN] SEARCH PARAMETERS CONFIGURATION & SOLVER INVOCATION
        // [UA] НАЛАШТУВАННЯ ПАРАМЕТРІВ ПОШУКУ ТА ЗАПУСК SOLVER
        // ===================================================================
        RoutingSearchParameters searchParameters = main.defaultRoutingSearchParameters().toBuilder()
                .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                .build();

        log.info("Invoking Google OR-Tools Mathematical Solver...");
        Assignment solution = routing.solveWithParameters(searchParameters);

        // ===================================================================
        // [EN] INTERPRETATION AND RESULTS OUTPUT
        // [UA] ІНТЕРПРЕТАЦІЯ ТА ВИВЕДЕННЯ РЕЗУЛЬТАТІВ
        // ===================================================================
        if (solution != null) {
            log.info("=== OPTIMAL ROUTING PLAN FOUND ===");
            printSolution(routing, manager, solution, fleet, allLocations);
        } else {
            log.error("Solver failed to find an optimal routing configuration. Check weight/pallet capacities.");
        }
    }

    /**
     * Converts a Depot entity into a graph node and prepends it to the customer list.
     * Перетворює об'єкт Депо на вузол графа та ставить його першим у список клієнтів.
     */
    private List<DeliveryPoint> prepareLocationsWithDepot(Depot depot, List<DeliveryPoint> clientPoints) {
        List<DeliveryPoint> locations = new ArrayList<>();
        locations.add(depot.toDeliveryPoint());
        locations.addAll(clientPoints);
        return locations;
    }

    /**
     * Formats and logs the optimal routing layout found by the mathematical solver.
     * Форматовано виводить у лог результати оптимального планування маршрутів.
     */
    private void printSolution(RoutingModel routing, RoutingIndexManager manager, Assignment solution,
                               List<Vehicle> fleet, List<DeliveryPoint> locations) {
        long totalDistance = 0;

        for (int i = 0; i < fleet.size(); ++i) {
            long index = routing.start(i);
            StringBuilder routeStr = new StringBuilder();
            Vehicle vehicle = fleet.get(i);

            routeStr.append(String.format("Route for Vehicle #%d (%s, Max: %.0fkg/%dpl): Depot",
                    vehicle.getId(), vehicle.getVehicleModel(), vehicle.getVehicleWeightCapacity(), vehicle.getVehiclePalletCapacity()));

            long routeDistance = 0;
            long currentWeight = 0;
            long currentPallets = 0;

            while (!routing.isEnd(index)) {
                long previousIndex = index;
                IntVar nextVar = routing.nextVar(index);
                index = solution.value(nextVar);

                routeDistance += routing.getArcCostForVehicle(previousIndex, index, i);

                int nodeIndex = manager.indexToNode(index);
                if (nodeIndex != 0) {
                    DeliveryPoint point = locations.get(nodeIndex);
                    currentWeight += point.getWeightKg();
                    currentPallets += point.getMainWarehousePlaces();
                    routeStr.append(" -> ").append(point.getCustomerName())
                            .append(String.format("(w:%.0f, p:%d)", point.getWeightKg(), point.getMainWarehousePlaces()));
                }
            }
            routeStr.append(" -> Depot");

            if (routeDistance > 0) {
                log.info("{} | Distance: {} m | Load: {} kg, {} pallets",
                        routeStr, routeDistance, currentWeight, currentPallets);
                totalDistance += routeDistance;
            } else {
                log.info("Vehicle #{} ({}) - Unused (Stay at Depot)", vehicle.getId(), vehicle.getVehicleModel());
            }
        }
        log.info("=========================================");
        log.info("Total Fleet Distance: {} meters.", totalDistance);
    }
}