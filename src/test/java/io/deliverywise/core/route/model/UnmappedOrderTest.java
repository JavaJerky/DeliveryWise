package io.deliverywise.core.route.model;


import static org.assertj.core.api.Assertions.assertThat;


import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link UnmappedOrder} record.
 *
 * @author Ihor Herasymenko
 * @date 22.07.2026
 */
class UnmappedOrderTest {

    @Test
    @DisplayName("Constructor stores source and missingFields as given")
    void creation_storesSourceAndMissingFields() {
        RawOrderDto source = rawOrder("42");
        List<String> missing = List.of(
                "weightKg: відсутнє значення",
                "cargoSpaces (М/П): некоректний формат 'x'");

        UnmappedOrder order = new UnmappedOrder(source, missing);

        assertThat(order.source()).isSameAs(source);
        assertThat(order.missingFields()).containsExactly(
                "weightKg: відсутнє значення",
                "cargoSpaces (М/П): некоректний формат 'x'");
    }

    @Test
    @DisplayName("Empty missingFields list is a valid state")
    void emptyMissingFields_isValidState() {
        UnmappedOrder order = new UnmappedOrder(rawOrder("1"), List.of());

        assertThat(order.missingFields()).isEmpty();
    }

    @Nested
    @DisplayName("equals / hashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("Same source reference + equal missingFields content → equal, same hashCode")
        void sameSourceReference_equalContent_areEqual() {
            RawOrderDto source = rawOrder("7");

            UnmappedOrder first = new UnmappedOrder(source,
                    List.of("id: відсутній"));
            UnmappedOrder second = new UnmappedOrder(source,
                    new ArrayList<>(List.of("id: відсутній")));

            assertThat(first).isEqualTo(second);
            assertThat(first.hashCode()).isEqualTo(second.hashCode());
        }

        @Test
        @DisplayName("Different source instances with identical field values → NOT equal (RawOrderDto has no @EqualsAndHashCode)")
        void differentSourceInstances_areNotEqual() {
            // RawOrderDto — Lombok @Getter/@Setter клас БЕЗ @EqualsAndHashCode, тож
            // порівнюється за посиланням (Object.equals). Два різних RawOrderDto з
            // однаковими полями — це НЕ "однаковий" source для запису UnmappedOrder.
            // Якщо колись додати @EqualsAndHashCode до RawOrderDto, цей тест зловить
            // зміну поведінки.
            RawOrderDto first = rawOrder("7");
            RawOrderDto second = rawOrder("7");

            UnmappedOrder firstOrder = new UnmappedOrder(first,
                    List.of("id: відсутній"));
            UnmappedOrder secondOrder = new UnmappedOrder(second,
                    List.of("id: відсутній"));

            assertThat(firstOrder).isNotEqualTo(secondOrder);
        }

        @Test
        @DisplayName("Same source, different missingFields → not equal")
        void sameSourceDifferentMissingFields_areNotEqual() {
            RawOrderDto source = rawOrder("7");

            UnmappedOrder first = new UnmappedOrder(source,
                    List.of("id: відсутній"));
            UnmappedOrder second = new UnmappedOrder(source,
                    List.of("weightKg: відсутнє значення"));

            assertThat(first).isNotEqualTo(second);
        }

    }

    @Test
    @DisplayName("toString contains missingFields entries (readability in logs)")
    void toString_containsMissingFieldsForLogs() {
        UnmappedOrder order = new UnmappedOrder(rawOrder("99"),
                List.of("weightKg: відсутнє значення"));

        assertThat(order.toString()).contains("weightKg: відсутнє значення");
    }

    @Test
    @DisplayName("missingFields() must not let external mutation of the source list leak back in")
    void missingFields_isNotMutableFromOutside() {
        // Контракт: compact constructor робить List.copyOf(...), тому зовнішня
        // мутація списку, переданого в конструктор, не повинна відбиватись на record.
        List<String> mutableSource = new ArrayList<>(List.of("id: відсутній"));
        UnmappedOrder order = new UnmappedOrder(rawOrder("1"), mutableSource);

        mutableSource.add("weightKg: відсутнє значення");

        assertThat(order.missingFields()).hasSize(1);
    }

    // --- helper ---
    private static RawOrderDto rawOrder(String id) {
        RawOrderDto dto = new RawOrderDto();
        dto.setId(id);
        return dto;
    }

}
