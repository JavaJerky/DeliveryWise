package io.deliverywise.core.route.service;


import static org.assertj.core.api.Assertions.assertThat;


import io.deliverywise.core.route.model.DeliveryPoint;
import io.deliverywise.core.route.model.MappingResult;
import io.deliverywise.core.route.model.OperationType;
import io.deliverywise.core.route.model.RawOrderDto;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


/**
 * Unit tests for {@link OrderMapper}.
 *
 * @author Ihor Herasymenko
 * @date 21.07.2026
 */
class OrderMapperTest {

    private static RawOrderDto validRawOrder() {
        RawOrderDto dto = new RawOrderDto();

        dto.setId("1025");
        dto.setDeliveryAddress("Київ, вул.Сирецька 30 [Іван Петренко 0501234567]");
        dto.setWeightKg("120.5");
        dto.setOrderAmount("7550");
        dto.setCargoSpacesRaw("2+s1/1+s0");
        dto.setSpecialNotes("шпилька 2м");

        dto.setCustomerName("Афіна, ТОВ");
        dto.setSenderName("Підприємство, ТОВ");
        dto.setSenderAddress("Київ, вул.Сирецька 10");
        dto.setManagerName("Боровко Іван");
        dto.setInvoices("0100-184569");
        dto.setIssueOrders("00-00236958");
        dto.setDeliveryNotes("шпилька 2 м");
        dto.setStatus("Готовий до доставки");
        dto.setCategory("Delivery");
        dto.setDriverRaw("Степанчук Василь 0672153322");

        return dto;

    }


    // =========================================================================
    // HAPPY PATH
    // =========================================================================

    @Test
    @DisplayName("All fields valid → row lands in readyForRouting, incomplete is empty")
    void mapAll_allFieldsValid_goesToReady() {
        // - result.incomplete() порожній
        // - result.readyForRouting() містить 1 DeliveryPoint з очікуваними полями
        RawOrderDto raw = validRawOrder();

        MappingResult result = OrderMapper.mapAll(List.of(raw));
        assertThat(result.readyForRouting()).hasSize(1);
        assertThat(result.incomplete()).isEmpty();

        DeliveryPoint point = result.readyForRouting().get(0);

        assertThat(point.getId()).isEqualTo(1025);
        assertThat(point.getSenderAddress()).isEqualTo("Київ, вул.Сирецька 10");
        assertThat(point.getDeliveryAddress()).isEqualTo("Київ, вул.Сирецька 30");
        assertThat(point.getClientContacts()).isEqualTo("Іван Петренко 0501234567");
        assertThat(point.getWeightKg()).isEqualTo(120.5);
        assertThat(point.getOrderAmount()).isEqualByComparingTo("7550");

        // распарсенное М/П: "2+s1/1+s0"
        assertThat(point.getMainWarehousePlaces()).isEqualTo(2);
        assertThat(point.getFragileWarehousePlaces()).isEqualTo(1);
        assertThat(point.getMainWarehousePallets()).isEqualTo(1);
        assertThat(point.getFragileWarehousePallets()).isEqualTo(0);

        assertThat(point.getOperationType()).isEqualTo(OperationType.DELIVERY);

    }

    // =========================================================================
    // ADDRESS / CONTACTS SPLIT
    // =========================================================================

    @Test
    @DisplayName("Address with trailing [contacts] → split into deliveryAddress + clientContacts")
    void mapAll_addressWithContacts_splitsCorrectly() {
        RawOrderDto raw = validRawOrder();
        raw.setDeliveryAddress("КИЇВ вул. Сирецька, 28/2 [Іван Петренко 0501234567]");

        MappingResult result = OrderMapper.mapAll(List.of(raw));

        assertThat(result.incomplete()).isEmpty();
        DeliveryPoint point = result.readyForRouting().get(0);
        assertThat(point.getDeliveryAddress()).isEqualTo("КИЇВ вул. Сирецька, 28/2");
        assertThat(point.getClientContacts()).isEqualTo("Іван Петренко 0501234567");
    }

    @Test
    @DisplayName("Address without brackets → stays as-is, clientContacts stays null")
    void mapAll_addressWithoutBrackets_keptAsIs() {
        RawOrderDto raw = validRawOrder();
        raw.setDeliveryAddress("Київ, вул. Хмельницького, 15");

        MappingResult result = OrderMapper.mapAll(List.of(raw));

        assertThat(result.incomplete()).isEmpty();
        DeliveryPoint point = result.readyForRouting().get(0);
        assertThat(point.getDeliveryAddress()).isEqualTo("Київ, вул. Хмельницького, 15");
        assertThat(point.getClientContacts()).isNull();
    }

    @Test
    @DisplayName("Blank address → row goes to incomplete with 'deliveryAddress' reason")
    void mapAll_blankAddress_goesToIncomplete() {
        RawOrderDto raw = validRawOrder();
        raw.setDeliveryAddress("   ");

        MappingResult result = OrderMapper.mapAll(List.of(raw));

        assertThat(result.readyForRouting()).isEmpty();
        assertThat(result.incomplete()).hasSize(1);
        assertThat(result.incomplete().get(0).missingFields())
                                                              .anyMatch(s -> s.startsWith("deliveryAddress"));
    }

    // =========================================================================
    // WEIGHT / AMOUNT
    // =========================================================================

    @Test
    @DisplayName("Blank weight, no document marker in specialNotes → incomplete")
    void mapAll_blankWeightNoDocumentMarker_goesToIncomplete() {
        RawOrderDto raw = validRawOrder();
        raw.setWeightKg("");

        MappingResult result = OrderMapper.mapAll(List.of(raw));

        assertThat(result.readyForRouting()).isEmpty();
        assertThat(result.incomplete()).hasSize(1);
        assertThat(result.incomplete().get(0).missingFields())
                                                              .anyMatch(s -> s.startsWith("weightKg"));
    }


    @Test
    @DisplayName("Blank weight AND amount, specialNotes contains 'документ' → ready, both 0")
    void mapAll_blankWeightWithDocumentMarker_goesToReadyWithZero() {
        RawOrderDto raw = validRawOrder();
        raw.setSpecialNotes("Пересилка документів");
        raw.setWeightKg("");
        raw.setOrderAmount("");

        MappingResult result = OrderMapper.mapAll(List.of(raw));

        assertThat(result.incomplete()).isEmpty();
        DeliveryPoint point = result.readyForRouting().get(0);
        assertThat(point.getWeightKg()).isEqualTo(0.0);
        assertThat(point.getOrderAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Comma as decimal separator: '41,76' → parsed as 41.76")
    void mapAll_commaDecimalSeparator_parsedCorrectly() {
        RawOrderDto raw = validRawOrder();
        raw.setWeightKg("41,76");

        MappingResult result = OrderMapper.mapAll(List.of(raw));

        assertThat(result.incomplete()).isEmpty();
        DeliveryPoint point = result.readyForRouting().get(0);
        assertThat(point.getWeightKg()).isEqualTo(41.76);
    }

    @Test
    @DisplayName("Non-numeric weight → incomplete with 'weightKg' reason")
    void mapAll_nonNumericWeight_goesToIncomplete() {
        RawOrderDto raw = validRawOrder();
        raw.setWeightKg("abc");

        MappingResult result = OrderMapper.mapAll(List.of(raw));

        assertThat(result.readyForRouting()).isEmpty();
        assertThat(result.incomplete()).hasSize(1);
        assertThat(result.incomplete().get(0).missingFields())
                                                              .anyMatch(s -> s.startsWith("weightKg"));
    }

    // =========================================================================
    // CARGO SPACES INTEGRATION
    // =========================================================================

    @Test
    @DisplayName("Malformed М/П, no document marker → incomplete with 'cargoSpaces' reason")
    void mapAll_malformedCargoSpaces_goesToIncomplete() {
        RawOrderDto raw = validRawOrder();
        raw.setCargoSpacesRaw("2+1/1+0");

        MappingResult result = OrderMapper.mapAll(List.of(raw));

        assertThat(result.readyForRouting()).isEmpty();
        assertThat(result.incomplete()).hasSize(1);
        assertThat(result.incomplete().get(0).missingFields())
                                                              .anyMatch(s -> s.startsWith("cargoSpaces"));
    }

    @Test
    @DisplayName("Valid М/П with all zeros ('0+s0/0+s0') → still ready (legitimate zero)")
    void mapAll_validZeroCargoSpaces_staysReady() {
        RawOrderDto raw = validRawOrder();
        raw.setCargoSpacesRaw("0+s0/0+s0");

        MappingResult result = OrderMapper.mapAll(List.of(raw));

        assertThat(result.incomplete()).isEmpty();
        DeliveryPoint point = result.readyForRouting().get(0);
        assertThat(point.getMainWarehousePlaces()).isZero();
        assertThat(point.getFragileWarehousePlaces()).isZero();
        assertThat(point.getMainWarehousePallets()).isZero();
        assertThat(point.getFragileWarehousePallets()).isZero();
    }

    // =========================================================================
    // ID
    // =========================================================================

    @Test
    @DisplayName("Blank id → incomplete with 'id' reason")
    void mapAll_blankId_goesToIncomplete() {
        RawOrderDto raw = validRawOrder();
        raw.setId("");

        MappingResult result = OrderMapper.mapAll(List.of(raw));

        assertThat(result.readyForRouting()).isEmpty();
        assertThat(result.incomplete()).hasSize(1);
        assertThat(result.incomplete().get(0).missingFields())
                                                              .anyMatch(s -> s.startsWith("id"));
    }

    @Test
    @DisplayName("Non-numeric id → incomplete with 'id' reason")
    void mapAll_nonNumericId_goesToIncomplete() {
        RawOrderDto raw = validRawOrder();
        raw.setId("ID-abc");

        MappingResult result = OrderMapper.mapAll(List.of(raw));

        assertThat(result.readyForRouting()).isEmpty();
        assertThat(result.incomplete()).hasSize(1);
        assertThat(result.incomplete().get(0).missingFields())
                                                              .anyMatch(s -> s.startsWith("id"));
    }

    // =========================================================================
    // "ALL ISSUES AT ONCE"
    // =========================================================================

    @Test
    @DisplayName("Multiple broken fields in one row → all reasons collected, not just the first")
    void mapAll_multipleBrokenFields_collectsAllIssues() {
        RawOrderDto raw = validRawOrder();
        raw.setWeightKg("not-a-number");
        raw.setCargoSpacesRaw("broken-format");

        MappingResult result = OrderMapper.mapAll(List.of(raw));

        assertThat(result.readyForRouting()).isEmpty();
        assertThat(result.incomplete()).hasSize(1);

        List<String> issues = result.incomplete().get(0).missingFields();
        assertThat(issues).hasSize(2);
        assertThat(issues).anyMatch(s -> s.startsWith("weightKg"));
        assertThat(issues).anyMatch(s -> s.startsWith("cargoSpaces"));
    }

    // =========================================================================
    // BATCH BEHAVIOR
    // =========================================================================

    @Test
    @DisplayName("Mixed batch: some rows ready, some incomplete → both lists correct")
    void mapAll_mixedBatch_splitsCorrectly() {
        RawOrderDto readyRaw = validRawOrder();

        RawOrderDto brokenId = validRawOrder();
        brokenId.setId("");

        RawOrderDto brokenWeight = validRawOrder();
        brokenWeight.setWeightKg("bad");

        MappingResult result = OrderMapper.mapAll(
                List.of(readyRaw, brokenId, brokenWeight));

        assertThat(result.readyForRouting()).hasSize(1);
        assertThat(result.incomplete()).hasSize(2);
    }

}
