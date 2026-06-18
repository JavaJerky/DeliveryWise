package io.deliverywise.core.route.model;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

/**
 * <p>Represents a vehicle within the fleet optimization system.</p>
 * <p>Представляє транспортний засіб у системі оптимізації автопарку.</p>
 *
 *
 * @author Ihor Herasymenko
 * @since 08.06.2026
 */
public class Vehicle {

    /**
     * id                        Unique identifier of the vehicle / Унікальний ідентифікатор ТЗ.
     * vehicleModel              Model of the vehicle (e.g., Mercedes Sprinter) / Модель автомобіля.
     * vehicleRegistrationNumber License plate number / Державній реєстраційний номер.
     * driverName                Full name of the assigned driver / ПІБ закріпленого водія.
     * phoneNumber               Driver's contact phone number / Контактний номер телефону водія.
     * vehicleWeightCapacity     Maximum payload capacity in kilograms / Максимальна вантажопідйомність у кг.
     * vehiclePalletCapacity     Maximum capacity in standard pallets / Максимальна місткість у стандартних палетах.
     * fleetType                 The operational category of the fleet member / Операційна категорія приналежності ТЗ.
     * status                    Current operational lifecycle and availability status of the vehicle. / Поточний операційний статус життєвого циклу та доступності транспортного засобу.
     */

    private int id;
        private String vehicleModel;
        private String vehicleRegistrationNumber;
        private String driverName;
        private String phoneNumber;
        private double vehicleWeightCapacity;
        private int vehiclePalletCapacity;
        @Enumerated(EnumType.STRING)
        private FleetType fleetType;
        @Enumerated(EnumType.STRING)
        private VehicleStatus status;

    /**
     * Forms a concise summary of the vehicle for routing sheets.
     * Формує короткий рядок інформації про автомобіль для маршрутного листа.
     *
     * @return Formatted summary string / Форматований рядок з інформацією.
     */
    public String getVehicleInfo() {
        String typeMarker = fleetType == FleetType.OWN ? "" : "[" + fleetType + "] ";
        return String.format("%s%s (%s) | Driver: %s | Max Weight: %.0f kg | Pallets: %d | Active: %-5b",
                typeMarker,
                vehicleModel,
                vehicleRegistrationNumber,
                driverName,
                vehicleWeightCapacity,
                vehiclePalletCapacity,
                status);
    }

    /**
     * Checks if the vehicle is fully operational and available for today's routes.
     * Перевіряє, чи автомобіль повністю справний та доступний для сьогоднішніх маршрутів.
     *
     * @return true if status is READY / true якщо статус READY
     */
    public boolean isAvailableForRouting() {
        return this.status == VehicleStatus.READY;
    }
}