package io.deliverywise.core.route.model;

/**
 * <p>Represents a vehicle within the fleet optimization system.</p>
 * <p>Представляє транспортний засіб у системі оптимізації автопарку.</p>
 *
 * @param id                        Unique identifier of the vehicle / Унікальний ідентифікатор ТЗ.
 * @param vehicleModel              Model of the vehicle (e.g., Mercedes Sprinter) / Модель автомобіля.
 * @param vehicleRegistrationNumber License plate number / Державній реєстраційний номер.
 * @param driverName                Full name of the assigned driver / ПІБ закріпленого водія.
 * @param phoneNumber               Driver's contact phone number / Контактний номер телефону водія.
 * @param vehicleWeightCapacity     Maximum payload capacity in kilograms / Максимальна вантажопідйомність у кг.
 * @param vehiclePalletCapacity     Maximum capacity in standard pallets / Максимальна місткість у стандартних палетах.
 * @param fleetType                 The operational category of the fleet member / Операційна категорія приналежності ТЗ.
 * * @author Ihor Herasymenko
 * @since 08.06.2026
 */
public record Vehicle(
        int id,
        String vehicleModel,
        String vehicleRegistrationNumber,
        String driverName,
        String phoneNumber,
        double vehicleWeightCapacity,
        int vehiclePalletCapacity,
        FleetType fleetType
) {

    /**
     * <p>Enumerates the operational types of logistics fleet vehicles.</p>
     * <p>Перелічує операційні типи транспортних засобів у логістиці.</p>
     */
    public enum FleetType {
        /** Own corporate fleet / Власний автопарк підприємства. */
        OWN,
        /** Long-term or short-term rented vehicle / Найманий (залучений) транспорт. */
        RE_HIRED,
        /** External third-party logistics provider / Зовнішній логістичний оператор (Нова Пошта). */
        NOVA_POSHTA
    }

    /**
     * Forms a concise summary of the vehicle for routing sheets.
     * Формує короткий рядок інформації про автомобіль для маршрутного листа.
     *
     * @return Formatted summary string / Форматований рядок з інформацією.
     */
    public String getVehicleInfo() {
        String typeMarker = fleetType == FleetType.OWN ? "" : "[" + fleetType + "] ";
        return String.format("%s%s (%s) | Driver: %s | Max Weight: %.0f kg | Pallets: %d",
                typeMarker,
                vehicleModel,
                vehicleRegistrationNumber,
                driverName,
                vehicleWeightCapacity,
                vehiclePalletCapacity);
    }
}