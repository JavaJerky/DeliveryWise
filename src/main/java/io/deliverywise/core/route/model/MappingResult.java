package io.deliverywise.core.route.model;

import java.util.List;

/**
 * <p>Result of mapping a batch of raw carrier orders into delivery points.</p>
 * <p>Результат маппінгу пакету сирих замовлень перевізника у точки доставки.</p>
 *
 * <p>{@code readyForRouting} — points with all required data present, ready for OR-Tools.
 * {@code incomplete} — rows missing required data, kept together with the reason(s) so an
 * operator can fix the source table and the logist can re-run the pipeline.
 * {@code readyForRouting} — точки з усіма необхідними даними, готові для OR-Tools.
 * {@code incomplete} — рядки з відсутніми даними, разом із причинами, щоб оператор міг
 * виправити джерело, а логіст — перезапустити пайплайн.</p>
 *
 * @author Ihor Herasymenko
 * @date 21.07.2026
 */
public record MappingResult(
        List<DeliveryPoint> readyForRouting,
        List<UnmappedOrder> incomplete
) {
    public MappingResult {
        readyForRouting = List.copyOf(readyForRouting);
        incomplete = List.copyOf(incomplete);
    }
}
