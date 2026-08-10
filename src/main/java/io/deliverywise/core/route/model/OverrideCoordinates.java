package io.deliverywise.core.route.model;


/**
 * <p>
 * Coordinates resolved by {@code GeocodingOverrideMatcher} for a single matched override pattern.
 * </p>
 * <p>
 * Координати, знайдені {@code GeocodingOverrideMatcher} для одного збіглого override-паттерну.
 * </p>
 *
 * <p>
 * <b>Temporary type.</b> {@link DeliveryPoint} already carries {@code latitude}/{@code longitude} as plain
 * {@code double} fields, but whether that stays a simple pair of doubles or becomes a real geometric type (PostGIS
 * {@code geography}/{@code geometry} via {@code org.locationtech.jts.geom.Point}) is a separate, still-open decision
 * (see Jira SCRUM-15). This record exists so the matcher doesn't need to wait on that decision — once SCRUM-15 lands,
 * the call site converts this into whatever {@code DeliveryPoint} ends up using.
 * </p>
 * <p>
 * <b>Тимчасовий тип.</b> {@link DeliveryPoint} вже має {@code latitude}/{@code longitude} як прості поля
 * {@code double}, але чи лишиться це парою {@code double}, чи стане справжнім геометричним типом (PostGIS
 * {@code geography}/{@code geometry} через {@code org.locationtech.jts.geom.Point}) — окреме, ще не прийняте рішення
 * (див. Jira SCRUM-15). Цей record існує, щоб матчер не чекав на це рішення — коли SCRUM-15 буде закрито, виклик просто
 * сконвертує це значення у те, що використовує {@code DeliveryPoint}.
 * </p>
 *
 * @author Ihor Herasymenko
 * @since 10.08.2026
 */
public record OverrideCoordinates(double latitude, double longitude) {
}
