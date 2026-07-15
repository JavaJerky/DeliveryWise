package io.deliverywise.core.route.integration;

import io.deliverywise.core.route.model.RawOrderDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * <p>Utility class for parsing the carrier HTML table into raw order DTOs.</p>
 * <p>Утилітарний клас для парсингу HTML-таблиці перевізника в сирі DTO замовлень.</p>
 *
 * <p>Uses JSoup to extract rows from the delivery table.
 * Використовує JSoup для витягування рядків з таблиці доставок.</p>
 * <p>Pipeline: HTML string → JSoup → List&lt;RawOrderDto&gt;</p>
 *
 * @author Ihor Herasymenko
 * @date 13.07.2026
 */
public class HtmlTableParser {

    private static final Logger log = LoggerFactory.getLogger(HtmlTableParser.class);

    private HtmlTableParser (){
        // Utility class — no instantiation
    }

    /**
     * Parses the carrier HTML table and returns a list of raw order DTOs.
     * Парсить HTML-таблицю перевізника та повертає список сирих DTO замовлень.
     *
     * @param html Raw HTML string from the carrier web interface.
     * @return List of {@link RawOrderDto}, one per table row with status "Готовий до доставки".
     */

    public static List<RawOrderDto> parse(String html) {

        List<RawOrderDto> orderDtos = new ArrayList<>();

        if (html == null || html.isBlank()) {
            log.warn("HtmlTableParser: html is null or blank,  Returning empty list.");
            return orderDtos;
        }

        Document doc = Jsoup.parse(html);

        // 1/ First - build headers
        Map<String, Integer> headers = new LinkedHashMap<>();
        Elements headerCells = doc.select("table.deliverystable th");
        for (int i = 0; i < headerCells.size(); i++) {
            headers.put(headerCells.get(i).text().trim(), i);
        }

        if (headers.isEmpty()) {
            log.warn("HtmlTableParser: no header columns found. Check table selector 'table.deliverystable th'.");
            return orderDtos;
        }

        //2. Then - parsing rows
        Elements rows = doc.select("tr.deliverylist");
        for (Element row : rows){

            RawOrderDto dto = parse(row, headers);
            if("Готовий до доставки".equals(dto.getStatus())){
                orderDtos.add(dto);
            }
        }


        return orderDtos;
    }

    /**
     * Parses a single table row into a RawOrderDto.
     * Парсить один рядок таблиці в RawOrderDto.
     *
     * @param row     Table row element / Елемент рядка таблиці.
     * @param headers Map of column name to index / Мапа назва колонки → індекс.
     * @return Populated {@link RawOrderDto} with raw string values.
     */
    private static RawOrderDto parse(Element row, Map<String, Integer> headers){

        Elements cells = row.select("td");
        RawOrderDto dto = new RawOrderDto();

        dto.setId(cells.get(columnIndex("ID", headers)).text());
        dto.setManagerName(cells.get(columnIndex("Відповідальний", headers)).text());
        dto.setSenderName(cells.get(columnIndex("Відправник", headers)).text());
        dto.setSenderAddress(cells.get(columnIndex("Адреса відправки", headers)).text());
        dto.setCustomerName(cells.get(columnIndex("Клієнт/Отримувач", headers)).text());
        dto.setDeliveryAddress(cells.get(columnIndex("Адреса", headers)).text());
        dto.setInvoices(cells.get(columnIndex("Рахунки", headers)).text());
        dto.setIssueOrders(cells.get(columnIndex("Вид.ордери", headers)).text());
        dto.setDeliveryNotes(cells.get(columnIndex("Накладні", headers)).text());
        dto.setWeightKg(cells.get(columnIndex("Вага", headers)).text());
        dto.setOrderAmount(cells.get(columnIndex("Сума", headers)).text());
        dto.setCargoSpacesRaw(cells.get(columnIndex("М/П", headers)).text());
        dto.setCategory(cells.get(columnIndex("Кат.", headers)).text());
        dto.setDriverRaw(cells.get(columnIndex("Водій", headers)).text());
        dto.setSpecialNotes(cells.get(columnIndex("Примітки", headers)).text());
        dto.setStatus(cells.get(columnIndex("Статус", headers)).text());

        return dto;
    }

    /**
     * Returns the index of a column by its header name.
     * Повертає індекс колонки за її назвою в заголовку таблиці.
     *
     * @param columnName Column header name as it appears in the HTML table / Назва колонки як в HTML.
     * @param headers    Map of column name to index / Мапа назва колонки → індекс.
     * @return Column index.
     * @throws IllegalArgumentException if the column is not found / якщо колонка відсутня.
     */
    private static int columnIndex(String columnName, Map<String, Integer> headers){
        Integer idx = headers.get(columnName);
        if (idx == null) {
            throw new IllegalArgumentException("Column not found" + columnName);
        }
        return idx;
    }

}
