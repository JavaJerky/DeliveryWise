package ua.foxminded.mentoring.route;

import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.*;
import ua.foxminded.mentoring.route.model.DeliveryPoint;
import ua.foxminded.mentoring.route.model.Vehicle;
import ua.foxminded.mentoring.route.service.*;

import java.util.List;

/**
 * Главный оркестратор системы SoldiRoute.
 * Связывает воедино подготовку данных, расчет матрицы расстояний и
 * запуск поискового движка Google OR-Tools для решения задачи маршрутизации (VRP).
 */

public class RouteApp {

    public static void main(String[] args) {
        // Подгружаем нативный код (C++) через Java
        //Loader.loadNativeLibraries(); // change loadNativeLibraries to full path com.google.ortools.Loader.loadNativeLibraries();
        com.google.ortools.Loader.loadNativeLibraries();
        System.out.println("Статус системи: Систему SoldiRoute запущено.");
        System.out.println("Библіотеки Google OR-Tools успішно завантажено.");

        // 2. Prepare data
        DataGenerator generator = new DataGenerator();
        List<DeliveryPoint> realPoints = generator.generatePoints(20); // 250 точок

        // Реальный автопарк
        List<Vehicle> vehicles = generator.generateVehicles();

        // 3.  Counte matrix
        // Переключатель "Генеративная база / Реальные карты OSRM"
        DistanceProvider distanceProvider = new DistanceService();
        //DistanceProvider distanceProvider = new OsrmDistanceService();
        long[][] matrix = distanceProvider.createDistanceMatrix(realPoints);

        // 4. Solving the task
        // We will create the `solve` method
        RouteApp app = new RouteApp();
        app.solve(matrix, realPoints, vehicles);

    }

    public void solve(long[][] distanceMatrix, List<DeliveryPoint> points, List<Vehicle> vehicles) {

        System.out.println("Починаю налаштування моделі..."); // ЛОГ 1
        // 1. Создаем менеджер индексов:
        // количество точек (размер матрицы), кол-во машин - реальное, склад (индекс 0)
        RoutingIndexManager manager = new RoutingIndexManager(distanceMatrix.length, vehicles.size(), 0);

        // 2. Создаем саму модель маршрутизации
        RoutingModel routing = new RoutingModel(manager);

        // 3. Регистрируем функцию расстояний (Transit Callback)
        // Она говорит системе, как считать "стоимость" проезда между узлами
        int transitCallbackIndex = routing.registerTransitCallback((long fromIndex, long toIndex) -> {
            int fromNode = manager.indexToNode(fromIndex);
            int toNode = manager.indexToNode(toIndex);
            return distanceMatrix[fromNode][toNode];
        });

        // Регистрируем веса точек (Demand)
        int demandCallbackIndex = routing.registerUnaryTransitCallback((long fromIndex) -> {
            int node = manager.indexToNode(fromIndex);
            return (long) points.get(node).weight(); // Берем вес из конкретной точки
        });

        // Динамически создаем массив из лимитов нашего списка машин
        long[] capacities = vehicles.stream()
                .mapToLong(Vehicle::capacity)
                        .toArray();

        routing.addDimensionWithVehicleCapacity(
                demandCallbackIndex,
                0,
                capacities,
                true,
                "Capacity"
        );

        // Устанавливаем стоимость использования каждой машины
        // Определяем, что в первую очередь загружаются свои машины, а потом наемные и Новая почта
        for (int i = 0; i < vehicles.size(); i++) {
            if (vehicles.get(i).isOwnFleet()) {
                // Свои машины "бесплатные" для старта
                routing.setFixedCostOfVehicle(0, i);
            } else {
                // Наемные машины имеют огромный штраф за выезд.
                // ИИ возьмет их только если в свои физически не влезет товар.
                routing.setFixedCostOfVehicle(100_000, i);
            }
        }

        // 5. Настраиваем параметры поиска (алгоритм "Первое дешевое ребро")
        RoutingSearchParameters searchParameters = main.defaultRoutingSearchParameters()
                .toBuilder()
                .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                .build();



        System.out.println("Запускаю пошук рішення (Solver)..."); // ЛОГ 2
        // 6. ЗАПУСК РЕШЕНИЯ
        Assignment solution = routing.solveWithParameters(searchParameters);

        System.out.println("Пошук виконано."); // ЛОГ 3

        // 7. ВЫВОД РЕЗУЛЬТАТА (твой новый PrintService)
        if (solution != null) {
            PrintService printer = new PrintService();
            printer.printSolution(manager, routing, solution, points, vehicles);
        } else {
            System.out.println("На жаль, рішення не знайдено. Перевірте вагу та ліміти автівок.");
        }
    }
}
