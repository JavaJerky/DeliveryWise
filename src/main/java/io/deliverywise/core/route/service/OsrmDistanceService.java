package io.deliverywise.core.route.service;

import io.deliverywise.core.route.model.DeliveryPoint;

import java.util.List;

public class OsrmDistanceService implements DistanceProvider{
    @Override
    public long[][] createDistanceMatrix(List<DeliveryPoint> points) {
        //TODO: Реализовать HTTP запрос к OSRM API
        return new long[points.size()][points.size()];
    }
}
