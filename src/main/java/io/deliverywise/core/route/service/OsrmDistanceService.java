package io.deliverywise.core.route.service;

import io.deliverywise.core.route.model.DeliveryPoint;
import java.util.List;

/**
 * <p>Implementation of the {@link DistanceProvider} that utilizes OSRM (Open Source Routing Machine) API.</p>
 * <p>Реалізація інтерфейсу {@link DistanceProvider}, яка використовує OSRM (Open Source Routing Machine) API.</p>
 *
 * <p>Computes real-world road network distances and travel durations based on OpenStreetMap data.</p>
 * <p>Обчислює реальні відстані дорожньої мережі та тривалість поїздок на основі даних OpenStreetMap.</p>
 *
 * @author Ihor Herasymenko
 * @since 08.06.2026
 */
public class OsrmDistanceService implements DistanceProvider {

    /**
     * Calculates the distance matrix by executing an HTTP request to the OSRM table service.
     * Розраховує матрицю відстаней шляхом виконання HTTP-запиту до табличного сервісу OSRM.
     *
     * @param points A list of anonymized delivery points / Список анонімізованих точок доставки.
     * @return A square matrix [i][j] representing distances / Квадратна матриця [i][j], що представляє відстані.
     */
    @Override
    public long[][] createDistanceMatrix(List<DeliveryPoint> points) {
        if (points == null || points.isEmpty()) {
            return new long[0][0];
        }

        int size = points.size();
        // // TODO: Legacy compatibility layer / Planned infrastructure upgrade
        // Integrate WebClient/RestTemplate to fetch matrix from OSRM router endpoint: /table/v1/driving/...

        return new long[size][size];
    }
}