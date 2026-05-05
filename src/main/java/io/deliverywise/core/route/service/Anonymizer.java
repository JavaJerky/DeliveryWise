package io.deliverywise.core.route.service;

import io.deliverywise.core.route.model.DeliveryPoint;


import java.util.List;
import java.util.stream.Collectors;

/**
 * Инструмент обеспечения безопасности данных (GDPR/Коммерческая тайна).
 * Преобразует список реальных заказов в анонимный вид, заменяя персональные данные
 * хеш-кодами, сохраняя при этом физические параметры для работы алгоритма.
 */

public class Anonymizer {
    /**
     * Создает "облачную" копию точек, скрывая конфиденциальную информацию.
     * Вместо имен клиентов используется хеш или порядковый номер.
     */
    public List<DeliveryPoint> anonymize(List<DeliveryPoint> realPoints) {
        return realPoints.stream()
                .map(p -> new DeliveryPoint(
                        p.id(),
                        "Client_" + Integer.toHexString(p.clientName().hashCode()), // Скрываем имя
                        "REDACTED_ADDRESS", // Адрес не нужен для математики (нужны только координаты)
                        p.weight(),
                        p.boxes(),
                        p.pallets(),
                        p.isSoudal(),
                        p.isBulky(),
                        p.timeStart(),
                        p.timeEnd(),
                        "SECURE_NOTE", // Скрываем примечания
                        p.x(),
                        p.y()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Пример вывода для отладки, который можно безопасно копировать в чат
     */
    public void printSafeReport(List<DeliveryPoint> points) {
        System.out.println("=== Safe Logistics Report ===");
        points.forEach(p -> System.out.printf("Point ID: %d | Weight: %.2f | Coords: [%d, %d]%n",
                p.id(), p.weight(), p.x(), p.y()));
    }
}
