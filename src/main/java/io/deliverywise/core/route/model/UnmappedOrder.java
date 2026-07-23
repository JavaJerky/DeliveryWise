package io.deliverywise.core.route.model;


import java.util.List;

/**
 * <p>
 * A raw order that could not be fully mapped to a {@link DeliveryPoint}.
 * </p>
 * <p>
 * Сире замовлення, яке не вдалося повністю змапити у {@link DeliveryPoint}.
 * </p>
 *
 * <p>
 * {@code missingFields} lists every problem found in this row (not just the first one), so an operator sees everything
 * to fix in one pass instead of one error at a time.
 * </p>
 *
 * @author Ihor Herasymenko
 * @date 21.07.2026
 */
public record UnmappedOrder(
        RawOrderDto source,
        List<String> missingFields) {

    public UnmappedOrder {
        missingFields = List.copyOf(missingFields);
    }

}
