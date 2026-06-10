package io.deliverywise.core.route.service;

import io.deliverywise.core.route.model.DeliveryPoint;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>Data security tool designed to comply with data privacy standards and corporate non-disclosure agreements (NDAs).</p>
 * <p>Інструмент забезпечення безпеки даних (відповідність стандартам приватності та корпоративному захисту комерційної таємниці).</p>
 *
 * <p>Transforms a list of real warehouse orders into an anonymized structure, obfuscating PII (Personally Identifiable Information)
 * of managers, clients, and addresses, while preserving critical geometric and weight constraints for Google OR-Tools engine.</p>
 * <p>Перетворює список реальних замовлень в анонімний вигляд, маскуючи персональні дані менеджерів, клієнтів та адреси,
 * зберігаючи критичні геометричні та вагові параметри для двигуна Google OR-Tools.</p>
 *
 * @author Ihor Herasymenko
 * @since 08.06.2026
 */
public class Anonymizer {

    /**
     * Creates a secure copy of delivery points, obfuscating sensitive data but retaining routing coordinates and payload dimensions.
     * Створює безпечну копію точок доставки, приховуючи конфіденційну інформацію, але зберігаючи гео-координати та вагово-габаритні характеристики.
     *
     * @param realPoints Source list of operational delivery points / Вихідний список реальних точок доставки.
     * @return Anonymized list safe for logging or processing / Анонімізований список, безпечний для логування та обробки.
     */
    public List<DeliveryPoint> anonymize(List<DeliveryPoint> realPoints) {
        if (realPoints == null) {
            return Collections.emptyList();
        }

        return realPoints.stream()
                .map(p -> new DeliveryPoint(
                        p.id(),
                        "Manager_ID_" + Math.abs(p.managerName().hashCode() % 100), // Obfuscate manager identity
                        "Enterprise_Warehouse_Origin",                             // Mask specific origin facility
                        "Client_Hash_" + Integer.toHexString(p.customerName().hashCode()).toUpperCase(), // Hash client name
                        "REDACTED_STREET_ADDRESS",                                 // Address text removed; coordinates remain for math
                        "HIDDEN_CONTACTS",                                         // Strip phone numbers
                        p.weightKg(),
                        BigDecimal.ZERO,                                           // Prices/amounts are hidden (not needed for vehicle routing)

                        p.cargoSpacesRaw(),                                        // Retain distribution string for capacity analytics
                        p.mainWarehousePlaces(),
                        p.fragileWarehousePlaces(),
                        p.mainWarehousePallets(),
                        p.fragileWarehousePallets(),

                        Collections.emptyList(),                                   // Strip invoice metadata
                        Collections.emptyList(),                                   // Strip warehouse release documents
                        Collections.emptyList(),                                   // Strip consignment delivery notes

                        p.isFragileChemicals(),                                    // Retain calculated physical constraints
                        p.isBulky(),                                               // Retain oversized markers (e.g., long items)

                        p.timeStart(),                                             // Time windows must remain intact for OR-Tools
                        p.timeEnd(),

                        "Optimized secure route notes",                            // Strip operational dispatch remarks
                        p.deliveryStatus(),

                        p.latitude(),                                              // CRITICAL: retain coordinates for OSRM matrix
                        p.longitude()                                              // CRITICAL: retain coordinates for OSRM matrix
                ))
                .collect(Collectors.toList());
    }

    /**
     * Generates a sanitized console summary suitable for safe debugging, open team channels, or support tickets.
     * Формує безпечний звіт для логування, який можна передавати у відкриті чати або техпідтримку.
     *
     * @param points List of delivery points to print / Список точок доставки для виведення.
     */
    public void printSafeReport(List<DeliveryPoint> points) {
        if (points == null) return;

        System.out.println("=== Safe Enterprise Logistics Report ===");
        points.forEach(p -> System.out.printf(
                "Point ID: %d | Client: %s | Weight: %.2f kg | Pallets (Main/Fragile): %d/%d | Coords: [%.6f, %.6f]%n",
                p.id(),
                p.customerName(),
                p.weightKg(),
                p.mainWarehousePallets(),
                p.fragileWarehousePallets(),
                p.latitude(),
                p.longitude()
        ));
    }
}