В DistanceService. Мы используем Евклидову метрику (Пифагор) для расчета кратчайшего пути. 
Но мы не летаем на самолетах по прямой (гипотенузе). 
Мы нздим по криволинейным дорогам с их перекрестками, односторонним движением, типичными пробками etc.

Почему Пифагор, а не Google Maps? Это фундаментальный вопрос логистики. 
Почему мы выбрали Евклидову метрику? На этапе Phase 1 (MVP) нам нужно проверить, работает ли сам "движок" (OR-Tools).
Нам нужна быстрая, бесплатная и простая математика, чтобы заставить машины двигаться.
Как это работает в реальности? 
В настоящих задачах Евклидова метрика дает погрешность около 20-30% (коэффициент извилистости дорог). 
Для черновика этого достаточно, чтобы понять, какие точки "кучкуются" вместе.
Какие еще есть варианты?
 - Метрика Манхэттена: Сумма катетов ($|x1-x2| + |y1-y2|$). 
Идеальна для городов с сеткой улиц (как в Нью-Йорке или некоторых районах Киева).
 - Матрица из OSRM / GraphHopper: Это локальные серверы с картами OpenStreetMap. 
Они учитывают реальные дороги, но требуют настройки отдельного сервера.
 - Google Distance Matrix API: 
Самый точный вариант (учитывает пробки и знаки), но очень дорогой (каждый запрос стоит денег).
Сейчас мы используем Пифагора как "заглушку". 
Когда логика распределения по машинам будет идеальной, мы просто заменим DistanceService на сервис, 
который дергает реальные карты.

# Логіка маршрутизації CompanyRoute

## 1. Розрахунок відстаней
На поточному етапі (MVP) використовується **Евклідова метрика** (формула Піфагора).
* **Формула:** `sqrt((x1-x2)^2 + (y1-y2)^2)`
* **Причина вибору:** Швидкість розрахунків та відсутність потреби у зовнішніх API.

> **Важливо:** Це "повітряна" дистанція. Вона не враховує односторонній рух, пробки та реальну довжину доріг.

## 2. Модель автопарку
Ми використовуємо **Heterogeneous Fleet** (різнорідний парк):
- **Малі авто:** до 1500 кг (наприклад, буси).
- **Середні авто:** до 4000 кг (вантажівки).

## 3. Майбутні покращення (Backlog)
- [ ] Інтеграція з **OSRM** (Open Source Routing Machine) для реальних дорожніх маршрутів.
- [ ] Врахування часових вікон (Time Windows).
- [ ] Пріоритетність Soudal грузів.

## 4. Для "Real" (OSRM) режима скачать карту Украины 
 - * Прямая ссылка: download.geofabrik.de/europe/ukraine-latest.osm.pbf
 * 
# ______
### Прототип алгоритма (Евклид).
/*
import com.google.ortools.constraintsolver.*;
import io.deliverywise.core.route.model.DeliveryPoint;
import io.deliverywise.core.route.model.Vehicle;
import io.deliverywise.core.route.service.*;

import java.util.List;

/**
* Главный оркестратор системы CompanyRoute.
* Связывает воедино подготовку данных, расчет матрицы расстояний и
* запуск поискового движка Google OR-Tools для решения задачи маршрутизации (VRP).
  */

/*
import com.google.ortools.constraintsolver.*;

import io.deliverywise.core.route.model.DeliveryPoint;
import io.deliverywise.core.route.service.DataGenerator;
import io.deliverywise.core.route.service.DistanceProvider;
import io.deliverywise.core.route.service.DistanceService;

import java.util.List;
*/


/*
public class RouteApp {

    public static void main(String[] args) {
        // Подгружаем нативный код (C++) через Java
        //Loader.loadNativeLibraries(); // change loadNativeLibraries to full path com.google.ortools.Loader.loadNativeLibraries();
        com.google.ortools.Loader.loadNativeLibraries();
        System.out.println("Статус системи: Систему CompanyRoute запущено.");
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
*/





