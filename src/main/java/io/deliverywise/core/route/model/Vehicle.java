package io.deliverywise.core.route.model;

/**
 * Модель транспортного средства.
 * Содержит технические характеристики и идентификаторы автомобиля.
 */
public record Vehicle(
        int id,
        String modelName,
        String licensePlate,
        String driverName,
        String phoneNumber, // is type String or other?
        long capacity,
        int maxPallets, // возможное количество паллет на борту
        boolean isOwnFleet // true - own, false для Новой Почты или наемного транспорта
        // if we will use some types of vehicle (own, rent, Нова пошта, etc. - create enum
) {
    /**
     * Возвращает краткое описание машины для отчета.
     */
    public String getVehicleInfo(){
        // Если машина не своя, добавляем метку [НАЙМ]
        String fleetMarker = isOwnFleet ? "" : "[НАЙМ] ";

        return String.format("%s (%s) | Водій: %s (%s) | Вантажопідйомність: %d кг | Палет: %d",
                modelName, licensePlate, driverName, phoneNumber, capacity, maxPallets);
    }

}
