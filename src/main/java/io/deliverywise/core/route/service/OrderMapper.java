package io.deliverywise.core.route.service;


import io.deliverywise.core.route.model.DeliveryPoint;
import io.deliverywise.core.route.model.MappingResult;
import io.deliverywise.core.route.model.OperationType;
import io.deliverywise.core.route.model.RawOrderDto;
import io.deliverywise.core.route.model.UnmappedOrder;
import io.deliverywise.core.route.util.CargoSpacesParser;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Maps raw HTML-table orders ({@link RawOrderDto}) into domain delivery points ({@link DeliveryPoint}), validating
 * business rules along the way.
 * </p>
 * <p>
 * Маппить сирі замовлення з HTML-таблиці ({@link RawOrderDto}) у доменні точки доставки ({@link DeliveryPoint}),
 * попутно перевіряючи бізнес-правила.
 * </p>
 *
 * <p>
 * <b>Розподіл відповідальності:</b> {@code HtmlTableParser} — суто структурний парсинг (HTML → рядки →
 * {@code RawOrderDto}), без бізнес-логіки. Весь семантичний розбір і валідація (число vs текст, обов'язковість полів,
 * виняток для документів) — тут.
 * </p>
 *
 * <p>
 * Кожен рядок або повністю потрапляє у {@code readyForRouting}, або йде у {@code incomplete} з переліком УСІХ знайдених
 * проблем одразу (не тільки першої) — щоб оператор побачив весь список за один прохід, а не виправляв по одній помилці.
 * </p>
 *
 * <p>
 * Статичний утилітарний клас (без {@code @Component}) — як {@code HtmlTableParser} і {@code CargoSpacesParser}, які він
 * композує; стану не має. Викликатиметься зі Spring-сервісу оркестрації пайплайну, коли той з'явиться.
 * </p>
 *
 * @author Ihor Herasymenko
 * @date 21.07.2026
 */
public final class OrderMapper {

    private static final Logger log = LoggerFactory.getLogger(
            OrderMapper.class);

    /**
     * Keywords in "Примітки" (specialNotes) that mark a document-only shipment — no cargo weight/amount required in
     * that case. Ключові слова в "Примітках", що позначають відправку суто документів — для таких рядків вага/сума не
     * обов'язкові.
     */
    private static final List<String> DOCUMENT_MARKERS = List.of("документ",
            "док");

    /**
     * "Адреса" колонка приходить у форматі "<адреса> [<контакти>]", контакти — довільний текст (телефон/ім'я у
     * будь-якому порядку, ручний ввід). Виділяємо блок у квадратних дужках в кінці рядка, решту лишаємо як адресу без
     * подальшого структурного розбору (вулиця/будинок тут НЕ парситься — це задача геокодера на Stage 3, вручну
     * регэкспом such variability не покрити).
     */
    private static final Pattern ADDRESS_CONTACTS_PATTERN = Pattern.compile(
            "^(.*?)\\s*\\[(.*)]\\s*$");

    private OrderMapper() {
        // Utility class — no instantiation
    }

    /**
     * Maps a full batch of raw orders, splitting the result into ready-for-routing and incomplete rows. Маппить весь
     * пакет сирих замовлень, розділяючи результат на готові до маршрутизації та неповні рядки.
     */
    public static MappingResult mapAll(List<RawOrderDto> rawOrders) {
        List<DeliveryPoint> ready = new ArrayList<>();
        List<UnmappedOrder> incomplete = new ArrayList<>();

        for (RawOrderDto raw : rawOrders) {
            List<String> issues = new ArrayList<>();
            DeliveryPoint point = mapSingle(raw, issues);

            if (issues.isEmpty()) {
                ready.add(point);
            } else {
                incomplete.add(new UnmappedOrder(raw, issues));
            }
        }

        log.info("OrderMapper: {} ready, {} incomplete out of {} rows",
                ready.size(), incomplete.size(), rawOrders.size());

        return new MappingResult(ready, incomplete);
    }

    /**
     * Maps one raw order into a {@link DeliveryPoint}, collecting every validation problem into {@code issues} instead
     * of stopping at the first one.
     */
    private static DeliveryPoint mapSingle(RawOrderDto raw,
            List<String> issues) {
        DeliveryPoint point = new DeliveryPoint();

        point.setId(parseId(raw.getId(), issues));
        point.setManagerName(raw.getManagerName());
        point.setSenderName(raw.getSenderName());
        point.setCustomerName(raw.getCustomerName());

        splitAddressAndContacts(raw.getDeliveryAddress(), point, issues);

        point.setInvoices(raw.getInvoices());
        point.setIssueOrders(raw.getIssueOrders());
        point.setDeliveryNotes(raw.getDeliveryNotes());
        point.setSpecialNotes(raw.getSpecialNotes());
        point.setDeliveryStatus(raw.getStatus());

        boolean isDocumentShipment = containsDocumentMarker(
                raw.getSpecialNotes());

        applyWeight(raw.getWeightKg(), point, issues, isDocumentShipment);
        applyAmount(raw.getOrderAmount(), point, issues, isDocumentShipment);
        applyCargoSpaces(raw.getCargoSpacesRaw(), point, issues,
                isDocumentShipment);

        // TODO: детект operationType з "Примітки" — відкладено, дефолт DELIVERY
        point.setOperationType(OperationType.DELIVERY);

        point.calculateLogicalFlags();

        return point;
    }

    private static int parseId(String rawId, List<String> issues) {
        if (rawId == null || rawId.isBlank()) {
            issues.add("id: відсутній");
            return -1;
        }
        try {
            return Integer.parseInt(rawId.trim());
        } catch (NumberFormatException e) {
            issues.add("id: не вдалося розпізнати номер '" + rawId + "'");
            return -1;
        }
    }

    private static void splitAddressAndContacts(String rawAddress,
            DeliveryPoint point, List<String> issues) {
        if (rawAddress == null || rawAddress.isBlank()) {
            issues.add("deliveryAddress: адреса відсутня");
            return;
        }

        Matcher matcher = ADDRESS_CONTACTS_PATTERN.matcher(rawAddress.trim());
        if (matcher.matches()) {
            point.setDeliveryAddress(matcher.group(1).trim());
            point.setClientContacts(matcher.group(2).trim());
        } else {
            // Немає блоку [контакти] — рідкісний випадок, але не помилка сама по собі
            point.setDeliveryAddress(rawAddress.trim());
        }

        if (point.getDeliveryAddress() == null
                || point.getDeliveryAddress().isBlank()) {
            issues.add(
                    "deliveryAddress: адреса порожня після відокремлення контактів");
        }
    }

    private static boolean containsDocumentMarker(String specialNotes) {
        if (specialNotes == null || specialNotes.isBlank()) {
            return false;
        }
        String lower = specialNotes.toLowerCase();
        return DOCUMENT_MARKERS.stream().anyMatch(lower::contains);
    }

    private static void applyWeight(String raw, DeliveryPoint point,
            List<String> issues, boolean documentShipment) {
        String normalized = normalizeNumber(raw);
        if (normalized == null) {
            if (documentShipment) {
                point.setWeightKg(0.0);
            } else {
                issues.add("weightKg: відсутнє значення");
            }
            return;
        }
        try {
            point.setWeightKg(Double.parseDouble(normalized));
        } catch (NumberFormatException e) {
            issues.add("weightKg: некоректний формат '" + raw + "'");
        }
    }

    private static void applyAmount(String raw, DeliveryPoint point,
            List<String> issues, boolean documentShipment) {
        String normalized = normalizeNumber(raw);
        if (normalized == null) {
            if (documentShipment) {
                point.setOrderAmount(BigDecimal.ZERO);
            } else {
                issues.add("orderAmount: відсутнє значення");
            }
            return;
        }
        try {
            // Парсимо рядок напряму в BigDecimal (не через double) — уникаємо
            // артефактів двійкового округлення для грошових сум.
            point.setOrderAmount(new BigDecimal(normalized));
        } catch (NumberFormatException e) {
            issues.add("orderAmount: некоректний формат '" + raw + "'");
        }
    }

    /**
     * Trims, replaces comma decimal separator with dot. Returns null for blank input (caller decides whether that's an
     * error or a valid document-shipment zero).
     */
    private static String normalizeNumber(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return raw.trim().replace(',', '.');
    }

    private static void applyCargoSpaces(String raw, DeliveryPoint point,
            List<String> issues, boolean documentShipment) {
        // CargoSpacesParser сам виставляє point.cargoSpacesRaw і 4 склад-поля,
        // атомарно зануляючи їх при помилці формату (поведінка не змінена,
        // існуючі 13 тестів проходять без змін). Тепер він додатково повертає
        // boolean — успішність парсингу, якраз для цього рішення.
        boolean valid = CargoSpacesParser.parse(raw, point);

        if (!valid && !documentShipment) {
            issues.add("cargoSpaces (М/П): некоректний формат '" + raw + "'");
        }
    }

    /**
     * Prints a human-readable summary to the console for quick visual verification during development — same idea as
     * {@code Anonymizer.printSafeReport()}, just for mapping results. Not a replacement for the {@code log.info(...)}
     * counts in {@link #mapAll}: that line stays as the permanent diagnostic trail, this method is for eyeballing the
     * actual rows while working on the pipeline. Друкує людинозрозумілий звіт у консоль для швидкої візуальної
     * перевірки під час розробки — та сама ідея, що й {@code Anonymizer.printSafeReport()}, тільки для результату
     * маппінгу.
     *
     * @param result Result of {@link #mapAll} to print / Результат {@link #mapAll} для друку.
     */
    public static void printReport(MappingResult result) {
        System.out.println("=== OrderMapper: " + result.readyForRouting().size()
                + " ready, " + result.incomplete().size() + " incomplete ===");

        System.out.println("--- READY ---");
        result.readyForRouting()
              .forEach(p -> System.out.printf(
                      "READY  id=%-6d %-25s %6.1f kg  %s%n",
                      p.getId(), p.getCustomerName(), p.getWeightKg(),
                      p.getDeliveryAddress()));

        System.out.println("--- INCOMPLETE ---");
        result.incomplete()
              .forEach(u -> System.out.printf("ISSUE  id=%-6s %s%n",
                      u.source().getId(),
                      String.join("; ", u.missingFields())));
    }

}
