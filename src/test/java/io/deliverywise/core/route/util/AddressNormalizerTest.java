package io.deliverywise.core.route.util;


import static org.assertj.core.api.Assertions.assertThat;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AddressNormalizer}. Test inputs are real "Адреса" strings taken from a real 86-row export
 * ({@code ТочкиАдресаФормулирПарсингShort.xlsx}, session 4 analysis) — not invented examples, to make sure the
 * normalizer holds up against the actual mess in the data.
 *
 * @author Ihor Herasymenko
 * @since 05.08.2026
 */
class AddressNormalizerTest {

    @Test
    @DisplayName("null input -> empty string")
    void normalize_nullInput_returnsEmptyString() {
        assertThat(AddressNormalizer.normalize(null)).isEmpty();
    }

    @Test
    @DisplayName("Uppercase and mixed case -> lowercase")
    void normalize_mixedCase_lowercased() {
        String raw = "КИЇВ,м.Київ,м.Київ Р-К ДАРНИЦА,УЛ.БОРИСПОЛЬСКАЯ, сектор №3[ ]";

        String result = AddressNormalizer.normalize(raw);

        assertThat(result).isLowerCase();
    }

    @Test
    @DisplayName("Trailing '[ ]' artifact is stripped")
    void normalize_trailingBracket_stripped() {
        String raw = "КИЇВ,м.Київ,м.Київ вул. Промислова 7,[ ]";

        String result = AddressNormalizer.normalize(raw);

        assertThat(result).doesNotContain("[").doesNotContain("]");
        assertThat(result).isEqualTo("київ м.київ м.київ вул. промислова 7");
    }

    @Test
    @DisplayName("Comma before house number becomes a space")
    void normalize_commaBeforeHouseNumber_becomesSpace() {
        // Same address, two punctuation styles from real data — must normalize to the same string.
        String withComma = "КИЇВ,м.Київ,м.Київ ул.Миропольская,2 р-к Юность[ ]";
        String withSpace = "КИЇВ,м.Київ,м.Київ Миропільська 2[ ]";

        String normalizedComma = AddressNormalizer.normalize(withComma);

        assertThat(normalizedComma).doesNotContain(",");
        assertThat(normalizedComma).contains("миропольская 2");
        // Not asserting equality between the two — different spelling of the street name
        // (миропольская vs миропільська) is a known, separate problem, not this test's concern.
        assertThat(AddressNormalizer.normalize(withSpace)).contains("миропільська 2");
    }

    @Test
    @DisplayName("Repeated whitespace is collapsed")
    void normalize_repeatedWhitespace_collapsed() {
        String raw = "КИЇВ,м.Київ,м.Київ  Київ.  вул  Перемоги,  18[ ]";

        String result = AddressNormalizer.normalize(raw);

        assertThat(result).doesNotContain("  "); // no double spaces
    }

    @Test
    @DisplayName("Parenthesized landmark text is preserved (needed for override matching)")
    void normalize_parenthesizedLandmark_preserved() {
        String raw = "КИЇВ,м.Київ,м.Київ вул.Миропольська,2 р-к Юность "
                + "(зі сторони моста на Братиславській)[ ]";

        String result = AddressNormalizer.normalize(raw);

        assertThat(result).contains("(зі сторони моста на братиславській)");
    }

    @Test
    @DisplayName("Full real row (id 74556): lowercase + comma->space + bracket stripped")
    void normalize_realRow_matchesExpectedNormalizedForm() {
        String raw = "КИЇВ,м.Київ,м.Київ ул.А.Ахматовой,45 (возле Биллы)[ ]";

        String result = AddressNormalizer.normalize(raw);

        assertThat(result).isEqualTo("київ м.київ м.київ ул.а.ахматовой 45 (возле биллы)");
    }

}
