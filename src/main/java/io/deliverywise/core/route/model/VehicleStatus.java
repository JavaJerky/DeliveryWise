package io.deliverywise.core.route.model;


/**
 * <p>
 * Enumerates the operational statuses of vehicles in the fleet.
 * </p>
 * <p>
 * Перелічує операційні статуси транспортних засобів в автопаркуу.
 * </p>
 *
 * @author Ihor Herasymenko
 * @date 18.06.2026
 */
public enum VehicleStatus {

    /** Available for immediate routing sessions / Готовий до рейсу. */
    READY,
    /** Vehicle is undergoing technical maintenance / На технічному обслуговуванні (СТО). */
    MAINTENANCE,
    /** Assigned driver is currently unavailable due to illness / Водій на лікарняному. */
    DRIVER_SICK,
    /** Temporarily or permanently removed from operations / Виведений з експлуатації. */
    OUT_OF_SERVICE
}
