package io.deliverywise.core.route.service;

import io.deliverywise.core.route.model.DeliveryPoint;
import io.deliverywise.core.route.model.MappingResult;
import io.deliverywise.core.route.model.RawOrderDto;
import io.deliverywise.core.route.model.UnmappedOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link OrderMapper}.
 *
 * @author Ihor Herasymenko
 * @date 21.07.2026
 */
class OrderMapperTest {

    // =========================================================================
    // HAPPY PATH
    // =========================================================================

    @Test
    @DisplayName("All fields valid → row lands in readyForRouting, incomplete is empty")
    void mapAll_allFieldsValid_goesToReady() {
        // TODO: зібрати валідний RawOrderDto (усі поля заповнені, коректний
        // формат ваги/суми/М-П, адреса з [контактами] в кінці), передати
        // OrderMapper.mapAll(List.of(raw)), перевірити:
        //  - result.incomplete() порожній
        //  - result.readyForRouting() містить 1 DeliveryPoint з очікуваними полями
    }

    // =========================================================================
    // ADDRESS / CONTACTS SPLIT
    // =========================================================================

    @Test
    @DisplayName("Address with trailing [contacts] → split into deliveryAddress + clientContacts")
    void mapAll_addressWithContacts_splitsCorrectly() {
        // TODO: deliveryAddress = "КИЇВ вул. Сирецька, 28/2 [Іван Петренко 0501234567]"
        // очікується: point.getDeliveryAddress() == "КИЇВ вул. Сирецька, 28/2"
        //             point.getClientContacts() == "Іван Петренко 0501234567"
    }

    @Test
    @DisplayName("Address without brackets → stays as-is, clientContacts stays null")
    void mapAll_addressWithoutBrackets_keptAsIs() {
        // TODO: адреса без "[...]" в кінці — рідкісний, але легітимний випадок
    }

    @Test
    @DisplayName("Blank address → row goes to incomplete with 'deliveryAddress' reason")
    void mapAll_blankAddress_goesToIncomplete() {
        // TODO
    }

    // =========================================================================
    // WEIGHT / AMOUNT
    // =========================================================================

    @Test
    @DisplayName("Blank weight, no document marker in specialNotes → incomplete")
    void mapAll_blankWeightNoDocumentMarker_goesToIncomplete() {
        // TODO
    }

    @Test
    @DisplayName("Blank weight AND amount, specialNotes contains 'документ' → ready, both 0")
    void mapAll_blankWeightWithDocumentMarker_goesToReadyWithZero() {
        // TODO: перевірити саме що weightKg == 0.0 і orderAmount == BigDecimal.ZERO,
        // а не що рядок просто потрапив у incomplete
    }

    @Test
    @DisplayName("Comma as decimal separator: '41,76' → parsed as 41.76")
    void mapAll_commaDecimalSeparator_parsedCorrectly() {
        // TODO
    }

    @Test
    @DisplayName("Non-numeric weight → incomplete with 'weightKg' reason")
    void mapAll_nonNumericWeight_goesToIncomplete() {
        // TODO
    }

    // =========================================================================
    // CARGO SPACES INTEGRATION
    // =========================================================================

    @Test
    @DisplayName("Malformed М/П, no document marker → incomplete with 'cargoSpaces' reason")
    void mapAll_malformedCargoSpaces_goesToIncomplete() {
        // TODO: cargoSpacesRaw = щось битий формат типу "2+1/1+0" (без 's')
    }

    @Test
    @DisplayName("Valid М/П with all zeros ('0+s0/0+s0') → still ready (legitimate zero)")
    void mapAll_validZeroCargoSpaces_staysReady() {
        // TODO: це і є той кейс, заради якого CargoSpacesParser.parse() тепер
        // повертає boolean — валідний формат з нулями НЕ повинен йти в incomplete
    }

    // =========================================================================
    // ID
    // =========================================================================

    @Test
    @DisplayName("Blank id → incomplete with 'id' reason")
    void mapAll_blankId_goesToIncomplete() {
        // TODO
    }

    @Test
    @DisplayName("Non-numeric id → incomplete with 'id' reason")
    void mapAll_nonNumericId_goesToIncomplete() {
        // TODO
    }

    // =========================================================================
    // "ALL ISSUES AT ONCE"
    // =========================================================================

    @Test
    @DisplayName("Multiple broken fields in one row → all reasons collected, not just the first")
    void mapAll_multipleBrokenFields_collectsAllIssues() {
        // TODO: зробити рядок одразу з битою вагою І битим М/П (без document-маркера),
        // перевірити, що UnmappedOrder.missingFields() містить ОБИДВІ причини,
        // а не тільки першу знайдену
    }

    // =========================================================================
    // BATCH BEHAVIOR
    // =========================================================================

    @Test
    @DisplayName("Mixed batch: some rows ready, some incomplete → both lists correct")
    void mapAll_mixedBatch_splitsCorrectly() {
        // TODO: 2-3 рядки, різні результати, перевірити розмір обох списків
    }
}
