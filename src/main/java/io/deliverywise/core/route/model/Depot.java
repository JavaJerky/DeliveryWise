package io.deliverywise.core.route.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * <p>Represents the central distribution hub (Depot) for a routing session.</p>
 * <p>Представляє центральний розподільчий склад (Депо) для сесії маршрутизації.</p>
 *
 * @author Ihor Herasymenko
 * @since 23.06.2026
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Depot {

    private int id = 0; // Для Google OR-Tools депо завжди має індекс 0
    private String name;
    private String address;
    private double latitude;
    private double longitude;

    /**
     * Converts the depot properties into a standard DeliveryPoint structure
     * required by the routing matrix and solver.
     */
    public DeliveryPoint toDeliveryPoint() {
        DeliveryPoint point = new DeliveryPoint();
        point.setId(this.id);
        point.setCustomerName(this.name);
        point.setDeliveryAddress(this.address);
        point.setLatitude(this.latitude);
        point.setLongitude(this.longitude);
        point.setWeightKg(0.0);
        point.setMainWarehousePlaces(0);
        return point;
    }
}