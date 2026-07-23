package io.deliverywise.core.route.infrastructure;


import io.deliverywise.core.route.model.DeliveryPoint;
import io.deliverywise.core.route.model.Vehicle;
import java.util.List;

/**
 * Contract for decoupling data ingestion (HTML/1C/WMS) from the core routing engine. Контракт для ізоляції джерел даних
 * (HTML/1C/WMS) від ядра оптимізації маршрутів.
 *
 * @author Ihor Herasymenko
 * @date 16.06.2026
 */
public interface RouteDataProvider {

    /**
     * Retrieves and anonymizes all active delivery points for the current routing session.
     */
    List<DeliveryPoint> getAnonymizedDeliveryPoints();

    /**
     * Retrieves the list of available corporate and hired vehicles with their capacities.
     */
    List<Vehicle> getAvailableFleet();

}
