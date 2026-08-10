package io.deliverywise.core.route.service;


import static org.assertj.core.api.Assertions.assertThat;


import io.deliverywise.core.route.model.OverrideCoordinates;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link GeocodingOverrideMatcher}, run against a fixture YAML file with invented data (not the real,
 * gitignored {@code local-data/geocoding-overrides.yaml}).
 *
 * @author Ihor Herasymenko
 * @since 10.08.2026
 */
class GeocodingOverrideMatcherTest {

    private static final String FIXTURE_PATH = "src/test/resources/geocoding/test-geocoding-overrides.yaml";
    private static final String MISSING_PATH = "src/test/resources/geocoding/does-not-exist.yaml";

    // =========================================================================
    // MATCHING
    // =========================================================================

    @Test
    @DisplayName("Специфічний паттерн перевіряється раніше загального (first-match-wins)")
    void match_specificPatternBeforeGeneral_specificWins() {
        GeocodingOverrideMatcher matcher = new GeocodingOverrideMatcher(FIXTURE_PATH);

        Optional<OverrideCoordinates> result = matcher.match(
                "тестовий ринок сторона а вхід №1");

        assertThat(result).isPresent();
        assertThat(result.get().latitude()).isEqualTo(50.4);
        assertThat(result.get().longitude()).isEqualTo(30.5);
    }

    @Test
    @DisplayName("Загальний паттерн ловить усе, що не впіймав специфічний")
    void match_generalPattern_catchesRemainder() {
        GeocodingOverrideMatcher matcher = new GeocodingOverrideMatcher(FIXTURE_PATH);

        Optional<OverrideCoordinates> result = matcher.match(
                "тестовий ринок без уточнення сторони");

        assertThat(result).isPresent();
        assertThat(result.get().latitude()).isEqualTo(50.45);
        assertThat(result.get().longitude()).isEqualTo(30.55);
    }

    @Test
    @DisplayName("Адреса без збігу -> Optional.empty()")
    void match_noPatternMatches_returnsEmpty() {
        GeocodingOverrideMatcher matcher = new GeocodingOverrideMatcher(FIXTURE_PATH);

        assertThat(matcher.match("звичайна вулиця 5")).isEmpty();
    }

    @Test
    @DisplayName("null/порожня адреса -> Optional.empty(), не виняток")
    void match_nullOrBlankAddress_returnsEmpty() {
        GeocodingOverrideMatcher matcher = new GeocodingOverrideMatcher(FIXTURE_PATH);

        assertThat(matcher.match(null)).isEmpty();
        assertThat(matcher.match("   ")).isEmpty();
    }

    // =========================================================================
    // LOADING / VALIDATION
    // =========================================================================

    @Test
    @DisplayName("Запис з незаповненими координатами (TODO: null) пропускається при завантаженні")
    void match_incompleteCoordinatesEntry_skippedSoNoMatch() {
        GeocodingOverrideMatcher matcher = new GeocodingOverrideMatcher(FIXTURE_PATH);

        assertThat(matcher.match("точка без координат тут")).isEmpty();
    }

    @Test
    @DisplayName("Запис з координатами поза bounding box Києва пропускається при завантаженні")
    void match_coordinatesOutsideKyivBoundingBox_skippedSoNoMatch() {
        GeocodingOverrideMatcher matcher = new GeocodingOverrideMatcher(FIXTURE_PATH);

        assertThat(matcher.match("точка поза києвом десь")).isEmpty();
    }

    @Test
    @DisplayName("Валідні записи фікстури завантажуються, невалідні (2 з 4) - пропущені")
    void entryCount_fixtureFile_onlyValidEntriesCounted() {
        GeocodingOverrideMatcher matcher = new GeocodingOverrideMatcher(FIXTURE_PATH);

        assertThat(matcher.entryCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Файл не знайдено на диску -> не падає, порожній список override")
    void constructor_missingFile_doesNotThrowAndLoadsEmpty() {
        GeocodingOverrideMatcher matcher = new GeocodingOverrideMatcher(MISSING_PATH);

        assertThat(matcher.entryCount()).isZero();
        assertThat(matcher.match("будь-яка адреса")).isEmpty();
    }

}
