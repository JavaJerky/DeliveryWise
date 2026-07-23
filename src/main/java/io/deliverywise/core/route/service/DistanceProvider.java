package io.deliverywise.core.route.service;


import io.deliverywise.core.route.model.DeliveryPoint;
import java.util.List;

/**
 * <p>
 * Strategy interface defining the contract for distance matrix generation.
 * </p>
 * <p>
 * Стратегічний інтерфейс, що визначає контракт для генерації матриці відстаней.
 * </p>
 *
 * <p>
 * Decouples the core routing optimization engine (Google OR-Tools) from specific geospatial providers or mapping
 * services (e.g., OSRM, GraphHopper, Google Maps API).
 * </p>
 * <p>
 * Вiдділяє головний двигун оптимізації маршрутів (Google OR-Tools) від конкретних геопросторових провайдерів або
 * картографічних сервісів (наприклад, OSRM, GraphHopper, Google Maps API).
 * </p>
 *
 * @author Ihor Herasymenko
 * @since 08.06.2026
 */
public interface DistanceProvider {

    /**
     * Generates a square cost matrix representing distances or travel times between all pairs of delivery points.
     * Генерація квадратної матриці вартості, яка представляє відстані або час у дорозі між усіма парами точок доставки.
     *
     * @param points A list of anonymized delivery points, where index 0 is typically the depot / Список анонімізованих
     *               точок доставки, де індекс 0 зазвичай є депо (складом).
     * @return A 2D array [i][j] where the value represents the cost (e.g., distance in meters or time in seconds) from
     *         point i to point j / Двовимірний масив [i][j], де значення представляє вартість (наприклад, відстань у
     *         метрах або час у секундах) від точки i до точки j.
     */
    long[][] createDistanceMatrix(List<DeliveryPoint> points);

}
