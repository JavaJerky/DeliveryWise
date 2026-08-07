package io.deliverywise.core.route.util;


import java.util.Locale;

/**
 * Normalizes the raw "Адреса" (delivery address) string from the HTML/Excel export before it is compared against
 * {@code GeocodingService} override patterns. Нормалізує сирий рядок колонки "Адреса" з HTML/Excel-вивантаження перед
 * порівнянням з паттернами override-таблиці {@code GeocodingService}.
 *
 * <p>
 * Real export data (see {@code local-data/geocoding-overrides.yaml} header, session 4 analysis of a real 86-row export)
 * contains: mixed case, a trailing junk artifact {@code "[ ]"}, inconsistent comma/space punctuation around the house
 * number, and a repeated {@code "Київ,м.Київ,м.Київ"}-style city prefix (sometimes 3, sometimes 4 times — do not rely
 * on counting occurrences). Реальні дані (див. шапку {@code local-data/geocoding-overrides.yaml}, аналіз сесії 4 на
 * реальних 86 рядках) містять: змішаний регістр, хвостовий сміттєвий артефакт {@code "[ ]"}, нестабільну кому/пробіл
 * навколо номера будинку, і повторюваний міський префікс {@code "Київ,м.Київ,м.Київ"} (то 3, то 4 рази — не покладатись
 * на підрахунок повторів).
 * </p>
 *
 * <p>
 * <b>Deliberately does NOT strip parenthesized landmark notes</b> (e.g. {@code "(со стороны трамвайных путей)"},
 * {@code "(зі сторони моста на Братиславській)"}) — those are exactly the text the override patterns match on.
 * Stripping parentheses is a separate concern for the future geocoding fallback path (TODO), not for override matching.
 * <b>Свідомо НЕ прибирає текст у дужках</b> (напр. {@code "(со стороны трамвайных путей)"},
 * {@code "(зі сторони моста на Братиславській)"}) — саме на цей текст зав'язані override-паттерни. Обрізка дужок —
 * окрема відповідальність для майбутнього geocoding fallback (TODO), не для override-матчингу.
 * </p>
 *
 * <p>
 * Does NOT resolve Ukrainian/Russian spelling differences of the same street name (e.g. {@code "миропольская"} vs
 * {@code "миропільська"}) — stemming is unreliable here because case endings eat the shared root. Callers (override
 * entries) handle this by listing both spellings explicitly rather than expecting the normalizer to unify them. НЕ
 * вирішує різницю укр./рос. написання однієї вулиці (напр. {@code "миропольская"} проти {@code "миропільська"}) —
 * стеммінг тут ненадійний, бо закінчення відмінків з'їдають спільний корінь. Виклики (записи override) вирішують це
 * явним дублюванням обох варіантів написання, а не очікуванням, що нормалізатор їх об'єднає.
 * </p>
 *
 * @author Ihor Herasymenko
 * @since 05.08.2026
 */
public final class AddressNormalizer {

    // Trailing junk artifact from the export, e.g. "[ ]", "[ ]", "[]", optionally preceded by a
    // comma/space: "...Промислова 7,[ ]" → "...Промислова 7"
    private static final String TRAILING_BRACKET_PATTERN = "[,\\s]*\\[\\s*]\\s*$";

    private AddressNormalizer() {
        // Utility class — no instantiation
    }

    /**
     * Normalizes a raw address string for override-pattern matching. Returns an empty string for {@code null} input
     * (callers should treat that as "no match possible", same as any other override miss — not a special case to handle
     * separately). Нормалізує сирий рядок адреси для матчингу override-паттернів. Для {@code null} повертає порожній
     * рядок (виклики мають трактувати це як "матч неможливий", так само як звичайний промах override — не окремий
     * випадок для обробки).
     *
     * @param rawAddress Raw "Адреса" string from the HTML/Excel export / Сирий рядок "Адреса" з
     *                   HTML/Excel-вивантаження.
     * @return Normalized string, safe to compare against override patterns / Нормалізований рядок, безпечний для
     *         порівняння з override-паттернами.
     */
    public static String normalize(String rawAddress) {
        if (rawAddress == null) {
            return "";
        }

        String result = rawAddress.toLowerCase(Locale.ROOT);

        // Strip the trailing "[ ]" export artifact.
        // Прибираємо хвостовий артефакт експорту "[ ]".
        result = result.replaceAll(TRAILING_BRACKET_PATTERN, "");

        // Comma between street name and house number → space, so "миропольская,2" and
        // "миропольская 2" normalize to the same thing.
        // Кома між назвою вулиці й номером будинку → пробіл, щоб "миропольская,2" і
        // "миропольская 2" нормалізувались однаково.
        result = result.replace(',', ' ');

        // Collapse repeated whitespace, trim edges.
        // Схлопуємо повторні пробіли, обрізаємо краї.
        result = result.replaceAll("\\s+", " ").trim();

        return result;
    }

}
