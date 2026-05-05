package io.deliverywise.core.route.service;

import io.deliverywise.core.route.model.DeliveryPoint;

import java.util.List;

/**
 * Математическое ядро для расчета матрицы расстояний.
 * На текущем этапе использует Евклидову метрику (Пифагор) для расчета кратчайшего пути
 * между точками в декартовой системе координат.
 */

public class DistanceService implements DistanceProvider {
    /**
     * Создает матрицу расстояний (в условных единицах/метрах).
     * OR-Tools требует long для стабильности расчетов.
     */

    public long[][] createDistanceMatrix(List<DeliveryPoint> points) {
        int size = points.size();
        long[][] matrix = new long[size][size];
        for (int i = 0; i < size; i++){
            for (int j = 0; j < size; j++){
                if (i == j){
                    matrix[i][j] = 0; // Distance == 0 is distance to itself
                } else {
                    matrix[i][j] = calculateEuclidean(points.get(i), points.get(j));
                }
            }
        }
        return  matrix;
    }

    private long calculateEuclidean(DeliveryPoint p1, DeliveryPoint p2) {
        // Pythagoras formula ^ sqrt((x1 - x2)^2 + (y1 - y2)^2)
        double dx = p1.x() - p2.x();
        double dy = p1.y() - p2.y();
        return Math.round(Math.sqrt(dx * dx + dy * dy));
    }

}
