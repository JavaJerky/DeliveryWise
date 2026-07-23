package io.deliverywise.core.route.model;


/**
 *
 * <p>
 * Enumerates the operational types of logistics fleet vehicles.
 * </p>
 * <p>
 * Перелічує операційні типи транспортних засобів у логістиці.
 * </p>
 *
 * @author Ihor Herasymenko
 * @date 17.06.2026
 */
public enum FleetType {

    /** Own corporate fleet / Власний автопарк підприємства. */
    OWN,
    /** Long-term or short-term rented vehicle / Найманий (залучений) транспорт. */
    RE_HIRED,
    /** External third-party logistics provider / Зовнішній логістичний оператор (Нова Пошта). */
    NOVA_POSHTA

}
