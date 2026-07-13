package io.deliverywise.core.route.util;

import io.deliverywise.core.route.model.DeliveryPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CargoSpacesParser}.
 *
 * @author Ihor Herasymenko
 * @since 25.06.2026
 */
class CargoSpacesParserTest {

    private DeliveryPoint point;

    @BeforeEach
    void setUp() {
        point = new DeliveryPoint();
        point.setId(42);
    }

    // =========================================================================
    // HAPPY PATH
    // =========================================================================

    @Test
    @DisplayName("Standard format: '2+s1/1+s0' → correct field values")
    void parse_standardFormat_correctValues() {
        CargoSpacesParser.parse("2+s1/1+s0", point);

        assertThat(point.getMainWarehousePlaces()).isEqualTo(2);
        assertThat(point.getFragileWarehousePlaces()).isEqualTo(1);
        assertThat(point.getMainWarehousePallets()).isEqualTo(1);
        assertThat(point.getFragileWarehousePallets()).isEqualTo(0);
        assertThat(point.getCargoSpacesRaw()).isEqualTo("2+s1/1+s0");
    }

    @Test
    @DisplayName("All zeros: '0+s0/0+s0' → all fields zero")
    void parse_allZeros_allFieldsZero() {
        CargoSpacesParser.parse("0+s0/0+s0", point);

        assertThat(point.getMainWarehousePlaces()).isZero();
        assertThat(point.getFragileWarehousePlaces()).isZero();
        assertThat(point.getMainWarehousePallets()).isZero();
        assertThat(point.getFragileWarehousePallets()).isZero();
    }

    @Test
    @DisplayName("Large values: '12+s5/3+s2' → correct field values")
    void parse_largeValues_correctValues() {
        CargoSpacesParser.parse("12+s5/3+s2", point);

        assertThat(point.getMainWarehousePlaces()).isEqualTo(12);
        assertThat(point.getFragileWarehousePlaces()).isEqualTo(5);
        assertThat(point.getMainWarehousePallets()).isEqualTo(3);
        assertThat(point.getFragileWarehousePallets()).isEqualTo(2);
    }

    // =========================================================================
    // NORMALIZATION
    // =========================================================================

    @Test
    @DisplayName("Uppercase S: '2+S1/1+S0' → normalized and parsed correctly")
    void parse_uppercaseS_normalizedAndParsed() {
        CargoSpacesParser.parse("2+S1/1+S0", point);

        assertThat(point.getMainWarehousePlaces()).isEqualTo(2);
        assertThat(point.getFragileWarehousePlaces()).isEqualTo(1);
        assertThat(point.getMainWarehousePallets()).isEqualTo(1);
        assertThat(point.getFragileWarehousePallets()).isEqualTo(0);
    }

    @Test
    @DisplayName("Spaces around separators: '2 +s 1 / 1+s0' → normalized and parsed correctly")
    void parse_spacesAroundSeparators_normalizedAndParsed() {
        CargoSpacesParser.parse("2 +s 1 / 1+s0", point);

        assertThat(point.getMainWarehousePlaces()).isEqualTo(2);
        assertThat(point.getFragileWarehousePlaces()).isEqualTo(1);
        assertThat(point.getMainWarehousePallets()).isEqualTo(1);
        assertThat(point.getFragileWarehousePallets()).isEqualTo(0);
    }

    @Test
    @DisplayName("Mixed case with spaces: '2 +S 1 / 1+s0' → normalized and parsed correctly")
    void parse_mixedCaseWithSpaces_normalizedAndParsed() {
        CargoSpacesParser.parse("2 +S 1 / 1+s0", point);

        assertThat(point.getMainWarehousePlaces()).isEqualTo(2);
        assertThat(point.getFragileWarehousePlaces()).isEqualTo(1);
        assertThat(point.getMainWarehousePallets()).isEqualTo(1);
        assertThat(point.getFragileWarehousePallets()).isEqualTo(0);
    }

    // =========================================================================
    // EDGE CASES — NULL / BLANK
    // =========================================================================

    @Test
    @DisplayName("Null input → all fields zero")
    void parse_nullInput_allFieldsZero() {
        CargoSpacesParser.parse(null, point);

        assertAllFieldsZero();
    }

    @Test
    @DisplayName("Empty string → all fields zero")
    void parse_emptyString_allFieldsZero() {
        CargoSpacesParser.parse("", point);

        assertAllFieldsZero();
    }

    @Test
    @DisplayName("Blank string (spaces only) → all fields zero")
    void parse_blankString_allFieldsZero() {
        CargoSpacesParser.parse("   ", point);

        assertAllFieldsZero();
    }

    // =========================================================================
    // EDGE CASES — MALFORMED FORMAT
    // =========================================================================

    @Test
    @DisplayName("Missing slash: '2+s1' → all fields zero")
    void parse_missingSlash_allFieldsZero() {
        CargoSpacesParser.parse("2+s1", point);

        assertAllFieldsZero();
    }

    @Test
    @DisplayName("Missing +s separator: '2/1' → all fields zero")
    void parse_missingFragileSeparator_allFieldsZero() {
        CargoSpacesParser.parse("2/1", point);

        assertAllFieldsZero();
    }

    @Test
    @DisplayName("Non-numeric value: 'X+s1/1+s0' → all fields zero")
    void parse_nonNumericValue_allFieldsZero() {
        CargoSpacesParser.parse("X+s1/1+s0", point);

        assertAllFieldsZero();
    }

    @Test
    @DisplayName("Raw string is always saved even when malformed")
    void parse_malformedInput_rawStringStillSaved() {
        CargoSpacesParser.parse("BROKEN_FORMAT", point);

        assertThat(point.getCargoSpacesRaw()).isEqualTo("BROKEN_FORMAT");
        assertAllFieldsZero();
    }

    // =========================================================================
    // HELPER
    // =========================================================================

    private void assertAllFieldsZero() {
        assertThat(point.getMainWarehousePlaces()).isZero();
        assertThat(point.getFragileWarehousePlaces()).isZero();
        assertThat(point.getMainWarehousePallets()).isZero();
        assertThat(point.getFragileWarehousePallets()).isZero();
    }
}