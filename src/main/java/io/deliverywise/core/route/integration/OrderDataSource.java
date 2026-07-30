package io.deliverywise.core.route.integration;


import io.deliverywise.core.route.model.RawOrderDto;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * Abstraction over the different systems that can supply a day's raw delivery orders. Different implementations fetch
 * data from different upstream systems, but all of them return the same shape ({@link RawOrderDto}), so everything
 * downstream (OrderMapper, GeocodingService, OR-Tools, ...) does not need to know or care where the data actually came
 * from.
 * </p>
 * <p>
 * Абстракція над різними системами, що можуть постачати сирі замовлення на день. Різні реалізації дістають дані з
 * різних джерел, але всі повертають той самий формат ({@link RawOrderDto}), тому все нижче по потоку (OrderMapper,
 * GeocodingService, OR-Tools, ...) не повинно знати й дбати, звідки саме дані взялись.
 * </p>
 *
 * <p>
 * <b>Заплановані реалізації (Strategy pattern):</b>
 * </p>
 * <ul>
 * <li>{@code HtmlTableOrderDataSource} — поточне джерело "за замовчуванням". Забирає HTML-таблицю з carrier-порталу
 * (підприємства) на задану дату і парсить її через {@link HtmlTableParser}. Незалежне від сисадміна — карта колонок
 * фіксована ззовні, працює вже сьогодні, оскільки {@link HtmlTableParser} вже готовий і покритий тестами. Мінус: адреса
 * — вільний текст без гарантії коректних координат (див. блок "Ринки, стройки і промзони" в AI_CONTEXT.md).</li>
 * <li>{@code SysadminApiOrderDataSource} — заплановане, обіцяне сисадміном JSON API, дати немає. Ймовірно, віддає
 * структуровані дані, можливо разом з парою координат на точку. Координатам з цього джерела довіряти НЕ повністю (не
 * гарантовано коректні для нестандартних адрес — ринки, промзони, будівництва без адреси) — пріоритет нижчий за вручну
 * перевірену override-таблицю відомих проблемних точок.</li>
 * <li>{@code OwnFormOrderDataSource} — власний інтерфейс DeliveryWise для введення замовлень, заміна ручної
 * HTML-таблиці ("Далека перспектива" в AI_CONTEXT.md). Найвищий рівень довіри до даних, бо структура і валідація — під
 * нашим контролем із самого початку, а не успадковані від чужої системи.</li>
 * </ul>
 *
 * <p>
 * Наразі жодна реалізація ще не написана — {@code HtmlTableOrderDataSource} буде найпершою (тонка обгортка над вже
 * готовим {@link HtmlTableParser} + HTTP-запит на отримання HTML за дату, якого поки що немає). Інтерфейс вводиться
 * заздалегідь, щоб {@code OrderMapper} та все подальше не довелось переписувати, коли з'явиться друге чи третє джерело.
 * </p>
 *
 * @author Ihor Herasymenko
 * @date 30.07.2026
 */
public interface OrderDataSource {

    /**
     * Fetches raw orders for the given delivery date from the underlying system.
     * <p>
     * Забирає сирі замовлення на задану дату доставки з відповідної системи.
     *
     * @param deliveryDate The delivery date to fetch orders for / Дата доставки, на яку потрібні замовлення.
     * @return List of {@link RawOrderDto}, one per order. Empty list if none found for that date — implementations
     *         should not return {@code null}.
     */
    List<RawOrderDto> fetchOrders(LocalDate deliveryDate);

}
