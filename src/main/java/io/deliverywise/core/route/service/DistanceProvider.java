package ua.foxminded.mentoring.route.service;

import ua.foxminded.mentoring.route.model.DeliveryPoint;

import java.util.List;

public interface DistanceProvider {
    long[][] createDistanceMatrix(List<DeliveryPoint> points);
}
