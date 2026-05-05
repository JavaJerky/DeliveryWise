package io.deliverywise.core.route.service;

import io.deliverywise.core.route.model.DeliveryPoint;

import java.util.List;

public interface DistanceProvider {
    long[][] createDistanceMatrix(List<DeliveryPoint> points);
}
