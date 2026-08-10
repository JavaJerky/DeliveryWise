package io.deliverywise.core.route.service;


import io.deliverywise.core.route.model.OverrideCoordinates;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

/**
 * <p>
 * Matches a normalized delivery address against the manually curated "override" table — known problem points (markets,
 * construction sites, large ex-industrial zones) where plain address-string geocoding does not work.
 * </p>
 * <p>
 * Матчить нормалізовану адресу доставки проти вручну складеної override-таблиці — відомих проблемних точок (ринки,
 * будівництва, великі колишні промзони), де звичайний геокодинг рядка адреси не працює.
 * </p>
 *
 * <p>
 * This is the FIRST link in the coordinate priority chain (see session 3): override table &gt; sysadmin API (TODO) &gt;
 * address-string geocoding (TODO). A miss here is not an error — most addresses are simple and are expected to fall
 * through to the next source. Це ПЕРШИЙ рівень пріоритету джерел координат (сесія 3): override-таблиця &gt; API
 * сисадміна (TODO) &gt; геокодинг рядка адреси (TODO). Промах тут — не помилка, більшість адрес прості і мають
 * провалюватись далі по ланцюжку.
 * </p>
 *
 * <p>
 * The override file lives OUTSIDE the repository ({@code .gitignore}) — it references real client locations and must
 * never reach the public GitHub remote. Its path is configured via {@code geocoding.override.config-path} (see
 * {@code application-local.properties}). The file is OPTIONAL by design: if it is missing (fresh checkout, new machine,
 * wrong path), we log a warning and continue with an empty override list rather than failing application startup — the
 * whole point of the priority chain is that override coverage is never expected to be complete. Override-файл лежить
 * ПОЗА репозиторієм ({@code .gitignore}) — він посилається на реальні локації клієнтів і ніколи не повинен потрапити в
 * публічний GitHub. Шлях налаштовується через {@code geocoding.override.config-path} (див.
 * {@code application-local.properties}). Файл ОПЦІОНАЛЬНИЙ за задумом: якщо він відсутній (свіжий чекаут, новий
 * комп'ютер, невірний шлях) — логуємо попередження і продовжуємо з порожнім списком override, а не валимо старт
 * застосунку — сенс всього ланцюжка пріоритетів саме в тому, що override ніколи не покриває все.
 * </p>
 *
 * <p>
 * Each entry's coordinates are sanity-checked against a Kyiv/oblast bounding box at load time. A plain
 * [-90,90]/[-180,180] range check would not catch a swapped latitude/longitude — Kyiv's own latitude (~50) and
 * longitude (~30) both fall well inside that generic range. Entries outside the bounding box, or with incomplete
 * (TODO/{@code null}) coordinates, are logged and skipped — not fatal, just excluded from matching. Координати кожного
 * запису перевіряються на bounding box Києва/області під час завантаження. Звичайна перевірка діапазону
 * [-90,90]/[-180,180] не зловить переплутані місцями широту/довготу — і широта (~50), і довгота (~30) Києва прекрасно
 * вписуються в цей загальний діапазон. Записи поза bounding box або з незаповненими (TODO/{@code null}) координатами
 * логуються і пропускаються — не фатально, просто не беруть участі в матчингу.
 * </p>
 *
 * <p>
 * Coordinates are returned as {@link OverrideCoordinates} — a temporary type, independent of the still-open decision on
 * {@code DeliveryPoint}'s own coordinate representation (Jira SCRUM-15). Координати повертаються як
 * {@link OverrideCoordinates} — тимчасовий тип, незалежний від ще не прийнятого рішення щодо представлення координат у
 * {@code DeliveryPoint} (Jira SCRUM-15).
 * </p>
 *
 * @author Ihor Herasymenko
 * @since 10.08.2026
 */
@Service
public class GeocodingOverrideMatcher {

    private static final Logger log = LoggerFactory.getLogger(
            GeocodingOverrideMatcher.class);

    // Kyiv / Kyiv oblast bounding box — sanity check only, deliberately generous (not a precise
    // administrative border). Values are degrees.
    // Bounding box Києва/області — лише перевірка на здоровий глузд, свідомо із запасом (не точний
    // адміністративний кордон). Значення в градусах.
    private static final double MIN_LATITUDE = 49.0;
    private static final double MAX_LATITUDE = 52.5;
    private static final double MIN_LONGITUDE = 28.5;
    private static final double MAX_LONGITUDE = 32.5;

    private final List<OverrideEntry> entries;

    /**
     * Loads and compiles the override table from disk once, at construction time. Завантажує і компілює
     * override-таблицю з диска один раз, при створенні біна.
     *
     * @param configPath Path to the override YAML file. Defaults to the project's own {@code local-data/} convention if
     *                   {@code geocoding.override.config-path} is not set — see class javadoc for why a missing file is
     *                   not fatal / Шлях до override YAML-файлу. За замовчуванням — конвенція {@code local-data/}
     *                   проєкту, якщо {@code geocoding.override.config-path} не задано — див. javadoc класу, чому
     *                   відсутній файл не фатальний.
     */
    public GeocodingOverrideMatcher(
            @Value("${geocoding.override.config-path:local-data/geocoding-overrides.yaml}") String configPath) {
        this.entries = loadEntries(configPath);
        log.info("GeocodingOverrideMatcher: loaded {} valid override entr{} from '{}'.", entries.size(),
                entries.size() == 1 ? "y" : "ies", configPath);
    }

    /**
     * Finds the first override entry whose pattern matches the given (already normalized) address. Order matters —
     * entries are matched in file order, first match wins, so specific patterns must precede general ones in the YAML
     * file (see {@code local-data/geocoding-overrides.yaml} header). Знаходить перший override-запис, чий паттерн
     * збігається із заданою (вже нормалізованою) адресою. Порядок важливий — записи матчаться в порядку файлу,
     * перемагає перший збіг, тому специфічні паттерни мають йти РАНІШЕ загальних у YAML-файлі (див. шапку
     * {@code local-data/geocoding-overrides.yaml}).
     *
     * @param normalizedAddress Address string already processed by {@code AddressNormalizer#normalize} / Рядок адреси,
     *                          вже оброблений {@code AddressNormalizer#normalize}.
     * @return Coordinates of the first matching entry, or empty if nothing matched (not an error — caller should fall
     *         through to the next source in the priority chain) / Координати першого збіглого запису, або порожньо,
     *         якщо нічого не збіглось (не помилка — виклик має провалитись до наступного джерела в ланцюжку
     *         пріоритетів).
     */
    public Optional<OverrideCoordinates> match(String normalizedAddress) {
        if (normalizedAddress == null || normalizedAddress.isBlank()) {
            return Optional.empty();
        }

        for (OverrideEntry entry : entries) {
            if (entry.pattern().matcher(normalizedAddress).matches()) {
                return Optional.of(entry.coordinates());
            }
        }
        return Optional.empty();
    }

    /**
     * Number of successfully loaded override entries — exposed for tests, not meant for production logic. Кількість
     * успішно завантажених override-записів — для тестів, не для продакшн-логіки.
     */
    int entryCount() {
        return entries.size();
    }

    // Raw YAML → Map/List structures below (no schema-bound library, same hand-written-over-generated
    // philosophy already used for OrderMapper): parsing YAML config for ~10 entries doesn't justify pulling
    // in a mapping framework, and unchecked casts are inherent to any raw Map-based YAML/JSON reading.
    // Сирі YAML → структури Map/List нижче (та сама філософія "написано вручну, не згенеровано", що й
    // OrderMapper): парсинг YAML-конфігу на ~10 записів не виправдовує підключення мапінг-фреймворку, а
    // unchecked-приведення типів — неминучі при будь-якому читанні YAML/JSON через сирі Map.
    @SuppressWarnings("unchecked")
    private List<OverrideEntry> loadEntries(String configPath) {
        Path path = Path.of(configPath);
        if (!Files.exists(path)) {
            log.warn(
                    "GeocodingOverrideMatcher: override file not found at '{}'. This file is optional by design "
                            + "(see class javadoc) — continuing with an empty override list.",
                    configPath);
            return List.of();
        }

        Map<String, Object> root;
        try (InputStream in = Files.newInputStream(path)) {
            root = new Yaml().load(in);
        } catch (IOException e) {
            log.warn(
                    "GeocodingOverrideMatcher: failed to read override file at '{}'. Continuing with an empty "
                            + "override list.",
                    configPath, e);
            return List.of();
        }

        Object rawOverrides = root == null ? null : root.get("overrides");
        if (!(rawOverrides instanceof List<?> rawEntries)) {
            log.warn(
                    "GeocodingOverrideMatcher: '{}' has no top-level 'overrides' list. Continuing with an empty "
                            + "override list.",
                    configPath);
            return List.of();
        }

        List<OverrideEntry> result = new ArrayList<>();
        for (Object rawEntry : rawEntries) {
            if (!(rawEntry instanceof Map<?, ?> entryMap)) {
                continue;
            }
            parseEntry((Map<String, Object>) entryMap).ifPresent(result::add);
        }
        return List.copyOf(result);
    }

    @SuppressWarnings("unchecked")
    private Optional<OverrideEntry> parseEntry(Map<String, Object> entryMap) {
        Object patternRaw = entryMap.get("address_pattern");
        if (!(patternRaw instanceof String patternText) || patternText.isBlank()) {
            log.warn(
                    "GeocodingOverrideMatcher: entry without a valid 'address_pattern', skipping: {}", entryMap);
            return Optional.empty();
        }

        Object coordsRaw = entryMap.get("coordinates");
        if (!(coordsRaw instanceof Map<?, ?> rawCoords)) {
            log.warn(
                    "GeocodingOverrideMatcher: entry '{}' has no 'coordinates' block, skipping.", patternText);
            return Optional.empty();
        }
        Map<String, Object> coordsMap = (Map<String, Object>) rawCoords;

        Double latitude = asDouble(coordsMap.get("latitude"));
        Double longitude = asDouble(coordsMap.get("longitude"));
        if (latitude == null || longitude == null) {
            log.warn(
                    "GeocodingOverrideMatcher: entry '{}' has incomplete coordinates (TODO not filled in yet?), "
                            + "skipping.",
                    patternText);
            return Optional.empty();
        }

        if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE
                || longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
            log.warn(
                    "GeocodingOverrideMatcher: entry '{}' has coordinates ({}, {}) outside the expected "
                            + "Kyiv/oblast bounding box — likely a data entry mistake (swapped lat/lon?). "
                            + "Skipping.",
                    patternText, latitude, longitude);
            return Optional.empty();
        }

        return Optional.of(new OverrideEntry(compileGlobPattern(patternText),
                new OverrideCoordinates(latitude, longitude)));
    }

    private static Double asDouble(Object value) {
        return value instanceof Number number ? number.doubleValue() : null;
    }

    /**
     * Compiles a glob-style pattern (only {@code *} is special, meaning "any characters") into a {@link Pattern}
     * requiring a full-string match. Компілює glob-подібний паттерн (лише {@code *} — спецсимвол, "будь-які символи") у
     * {@link Pattern}, що вимагає повного збігу рядка.
     */
    private static Pattern compileGlobPattern(String glob) {
        String[] literalParts = glob.split("\\*", -1);
        StringBuilder regex = new StringBuilder("^");
        for (int i = 0; i < literalParts.length; i++) {
            regex.append(Pattern.quote(literalParts[i]));
            if (i < literalParts.length - 1) {
                regex.append(".*");
            }
        }
        regex.append("$");
        return Pattern.compile(regex.toString());
    }

    private record OverrideEntry(Pattern pattern, OverrideCoordinates coordinates) {
    }

}
