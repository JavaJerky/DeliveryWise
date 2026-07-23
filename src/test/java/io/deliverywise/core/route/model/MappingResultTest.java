package io.deliverywise.core.route.model;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link MappingResult} record.
 *
 * @author Ihor Herasymenko
 * @date 22.07.2026
 */
class MappingResultTest {

    @Test
    @DisplayName("Constructor stores readyForRouting and incomplete as given")
    void creation_storesBothLists() {
        DeliveryPoint point = deliveryPoint(1);
        UnmappedOrder unmapped = new UnmappedOrder(new RawOrderDto(),
                List.of("id: відсутній"));

        MappingResult result = new MappingResult(List.of(point),
                List.of(unmapped));

        assertThat(result.readyForRouting()).containsExactly(point);
        assertThat(result.incomplete()).containsExactly(unmapped);
    }

    @Test
    @DisplayName("Both lists empty → valid state, no crash")
    void bothListsEmpty_isValidState() {
        MappingResult result = new MappingResult(List.of(), List.of());

        assertThat(result.readyForRouting()).isEmpty();
        assertThat(result.incomplete()).isEmpty();
    }

    @Test
    @DisplayName("readyForRouting() must not let external mutation of the source list leak back in")
    void readyForRouting_isNotMutableFromOutside() {
        // Контракт: compact constructor робить List.copyOf(...), тому зовнішня
        // мутація списку, переданого в конструктор, не повинна відбиватись на record.
        List<DeliveryPoint> mutableSource = new ArrayList<>(
                List.of(deliveryPoint(1)));
        MappingResult result = new MappingResult(mutableSource, List.of());

        mutableSource.add(deliveryPoint(2));

        assertThat(result.readyForRouting()).hasSize(1);
    }

    @Test
    @DisplayName("incomplete() returned list must be unmodifiable regardless of what was passed in")
    void incomplete_returnedListIsUnmodifiable() {
        // Якщо конструктор просто зберігає передане посилання (а не робить
        // List.copyOf/List.of всередині), передача звичайного ArrayList зробить
        // incomplete() зовні модифіковним — цей тест це й ловить.
        List<UnmappedOrder> mutableSource = new ArrayList<>();
        MappingResult result = new MappingResult(List.of(), mutableSource);
        UnmappedOrder extra = new UnmappedOrder(new RawOrderDto(), List.of());

        assertThatThrownBy(() -> result.incomplete().add(extra))
                                                                .isInstanceOf(UnsupportedOperationException.class);
    }

    private static DeliveryPoint deliveryPoint(int id) {
        DeliveryPoint point = new DeliveryPoint();
        point.setId(id);
        return point;
    }

}
