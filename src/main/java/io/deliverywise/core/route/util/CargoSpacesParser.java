package io.deliverywise.core.route.util;

import io.deliverywise.core.route.model.DeliveryPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for parsing the raw cargo spaces string from the logistics table.
 * Утилітарний клас для парсингу рядка розподілу вантажних місць з логістичної таблиці.
 *
 * <p>Format / Формат: {@code "mainPlaces+sFragilePlaces/mainPallets+sFragilePallets"}</p>
 * <p>Example / Приклад: {@code "2+s1/1+s0"} →
 *   mainWarehousePlaces=2, fragileWarehousePlaces=1,
 *   mainWarehousePallets=1, fragileWarehousePallets=0
 * </p>
 *
 * <p>The 's' prefix on the second term of each group marks Soudal (fragile/chemical) cargo.
 * Префікс 's' перед другим числом у кожній групі позначає продукцію Soudal (крихке/хімія).</p>
 *
 * <p>Parsing is atomic: if either group fails to parse, all four fields are reset to zero.
 * Парсинг атомарний: якщо будь-яка група не розпарсилась — всі чотири поля скидаються в 0.</p>
 *
 * <p>Returns {@code true} if parsing succeeded, {@code false} if the atomic reset was
 * triggered — callers that need to distinguish "legitimately zero" from "malformed input"
 * (e.g. {@code OrderMapper}) should check the return value instead of inferring it from
 * all-zero fields.
 * Повертає {@code true}, якщо парсинг вдався, {@code false} — якщо спрацювало атомарне
 * скидання. Викликачі, яким потрібно відрізнити "легітимний нуль" від "битий формат"
 * (напр. {@code OrderMapper}), мають перевіряти значення, що повертається, а не робити
 * висновок з нульових полів.</p>
 *
 * @author Ihor Herasymenko
 * @since 25.06.2026
 */
public class CargoSpacesParser {

    private static final Logger log = LoggerFactory.getLogger(CargoSpacesParser.class);

    // Format: "mainPlaces+sFragilePlaces/mainPallets+sFragilePallets"
    // Separators: '/' splits places from pallets, '+s' splits main from fragile within each group
    private static final String GROUP_SEPARATOR = "/";
    private static final String FRAGILE_SEPARATOR = "\\+s";

    // Sentinel value returned by parseGroup() to signal a parse failure
    // Сигнальне значення, яке повертає parseGroup() при помилці парсингу
    private static final int[] PARSE_FAILURE = null;

    private CargoSpacesParser() {
        // Utility class — no instantiation
    }

    /**
     * Parses the raw cargo spaces string and populates the corresponding fields on the delivery point.
     * Парсить рядок розподілу місць та заповнює відповідні поля об'єкта точки доставки.
     *
     * <p>Parsing is atomic: if either group fails, all four fields are reset to zero.
     * Парсинг атомарний: якщо будь-яка група не розпарсилась — всі поля скидаються в 0.</p>
     *
     * @param raw   Raw cargo spaces string from the logistics table / Сирий рядок з таблиці логістики.
     * @param point DeliveryPoint to populate / Точка доставки для заповнення.
     * @return {@code true} if both groups parsed successfully, {@code false} if the atomic
     *         reset was triggered (fields were zeroed) / {@code true}, якщо обидві групи
     *         розпарсились успішно, {@code false} — якщо спрацювало атомарне скидання.
     */
    public static boolean parse(String raw, DeliveryPoint point) {
        point.setCargoSpacesRaw(raw);

        if (raw == null || raw.isBlank()) {
            log.warn("CargoSpacesParser: raw string is null or blank for point id={}. Setting all cargo fields to 0.",
                    point.getId());
            resetCargoFields(point);
            return false;
        }

        // Normalize: remove all whitespace, convert to lowercase
        // Нормалізація: видаляємо всі пробіли, приводимо до нижнього регістру
        // "2 +S 1 / 1+s0" → "2+s1/1+s0"
        String normalized = raw.strip().toLowerCase().replaceAll("\\s+", "");

        String[] groups = normalized.split(GROUP_SEPARATOR, -1);

        if (groups.length != 2) {
            log.warn("CargoSpacesParser: unexpected format '{}' (normalized: '{}') for point id={}. Expected 'X+sY/X+sY'. Setting all to 0.",
                    raw, normalized, point.getId());
            resetCargoFields(point);
            return false;
        }

        int[] places  = parseGroup(raw, groups[0], point.getId(), "places");
        int[] pallets = parseGroup(raw, groups[1], point.getId(), "pallets");

        // Atomic commit: both groups must succeed, otherwise reset everything
        // Атомарний коміт: обидві групи мають бути валідні, інакше — скидаємо всі поля
        if (places == PARSE_FAILURE || pallets == PARSE_FAILURE) {
            log.warn("CargoSpacesParser: atomic reset triggered for point id={}. Raw: '{}'", point.getId(), raw);
            resetCargoFields(point);
            return false;
        }

        point.setMainWarehousePlaces(places[0]);
        point.setFragileWarehousePlaces(places[1]);
        point.setMainWarehousePallets(pallets[0]);
        point.setFragileWarehousePallets(pallets[1]);
        return true;
    }

    /**
     * Parses a single group token (e.g. "2+s1") into [main, fragile] counts.
     * Парсить одну групу токена (напр. "2+s1") у масив [основне, крихке].
     *
     * @param raw       Full raw string for logging context / Повний сирий рядок для контексту логу.
     * @param group     Group token to parse / Токен групи для парсингу.
     * @param pointId   Delivery point id for logging / ID точки для логування.
     * @param groupName Human-readable group label for logging / Назва групи для логування.
     * @return int[2]: [mainCount, fragileCount], or PARSE_FAILURE (null) on error.
     */
    private static int[] parseGroup(String raw, String group, int pointId, String groupName) {
        String[] parts = group.split(FRAGILE_SEPARATOR, -1);

        if (parts.length != 2) {
            log.warn("CargoSpacesParser: cannot parse {} group '{}' in '{}' for point id={}.",
                    groupName, group, raw, pointId);
            return PARSE_FAILURE;
        }

        try {
            int main    = Integer.parseInt(parts[0].trim());
            int fragile = Integer.parseInt(parts[1].trim());
            return new int[]{main, fragile};
        } catch (NumberFormatException e) {
            log.warn("CargoSpacesParser: non-numeric value in {} group '{}' in '{}' for point id={}.",
                    groupName, group, raw, pointId);
            return PARSE_FAILURE;
        }
    }

    /**
     * Resets all cargo count fields to zero on the delivery point.
     * Скидає всі поля кількості вантажних місць до нуля.
     */
    private static void resetCargoFields(DeliveryPoint point) {
        point.setMainWarehousePlaces(0);
        point.setFragileWarehousePlaces(0);
        point.setMainWarehousePallets(0);
        point.setFragileWarehousePallets(0);
    }
}