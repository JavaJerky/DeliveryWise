package ua.foxminded.mentoring.route.service;

import com.google.ortools.constraintsolver.Assignment;
import com.google.ortools.constraintsolver.RoutingIndexManager;
import com.google.ortools.constraintsolver.RoutingModel;
import ua.foxminded.mentoring.route.model.DeliveryPoint;
import ua.foxminded.mentoring.route.model.Vehicle;

import java.util.List;

/**
 * Сервис визуализации результатов оптимизации.
 * Формирует читаемый отчет в консоли, разбивая общий пул заказов по конкретным автомобилям
 * с указанием веса, дистанции и последовательности посещения точек.
 */
public class PrintService {

    public void printSolution(
            RoutingIndexManager manager,
            RoutingModel routing,
            Assignment solution,
            List<DeliveryPoint> points,
            List<Vehicle> vehicles){
        System.out.println("\n=== Диспетчерський звіт ===");
        System.out.println("  Регіон: Київська область");
        System.out.println("===========================");
        long totalDistance = 0;
        long totalWeight = 0;

        // all vehicles
        for (int i = 0; i < vehicles.size(); i++){
            Vehicle vehicle = vehicles.get(i);
            long index = routing.start(i);
            System.out.println(vehicle.getVehicleInfo());  // описание авто,
                         // если вывод инфо об авто не устраивает, его можно изменить в рекорде Vehicle

            long routeDistance = 0;
            long routeWeight = 0;
            long dropOffs = 0;

            System.out.println("Маршрут: Склад");
            while (!routing.isEnd(index)){
                long previousIndex = index;
                index = solution.value(routing.nextVar(index));

                int nodeIndex = manager.indexToNode(index);

                // if vehicle didn't return to Depot, it's a point
                if (!routing.isEnd(index)){
                    DeliveryPoint point = points.get(nodeIndex);
                    routeWeight += point.weight();
                    dropOffs++;
                    System.out.print("\n-> Точка " + nodeIndex + " [" + point.clientName() + "] (" + (int)point.weight() + " кг) ");
                }
                //подсчет стоимости (дистанции) перехода
                routeDistance += routing.getArcCostForVehicle(previousIndex, index, i);
            }

            System.out.println(" -> Склад");
            System.out.println("  > Дистанція: " + routeDistance + " од.");
            System.out.println("  > Завантаження: " + routeWeight + " кг | Точок: " + dropOffs);
            System.out.println("--------------------------------------------------");

            totalDistance += routeDistance;
            totalWeight += routeWeight;
        }

        System.out.println("\nЗАГАЛЬНІ ПОКАЗНИКИ РЕЙСУ:");
        System.out.println("Всього доставлено: " + totalWeight + " кг");
        System.out.println("Загальний пробіг автопарку: " + totalDistance + " од.");
    }
}
