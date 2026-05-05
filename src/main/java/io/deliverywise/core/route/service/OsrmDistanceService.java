package ua.foxminded.mentoring.route.service;

import ua.foxminded.mentoring.route.model.DeliveryPoint;

import java.util.List;

public class OsrmDistanceService implements DistanceProvider{
    @Override
    public long[][] createDistanceMatrix(List<DeliveryPoint> points) {
        //TODO: Реализовать HTTP запрос к OSRM API
        return new long[points.size()][points.size()];
    }
}
