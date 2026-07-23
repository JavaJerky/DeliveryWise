package io.deliverywise.core.route.integration;


import static org.junit.jupiter.api.Assertions.*;


import io.deliverywise.core.route.model.RawOrderDto;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit test verifying the behaviour of {@link HtmlTableParser} using HTML-fixture. Unit тест, що перевіряє поведінку
 * {@link HtmlTableParser} з використанням HTML-фікстура.
 *
 * @author Ihor Herasymenko
 */
class HtmlTableParserTest {

    private static final String SAMPLE_TABLE_PATH = "src/test/resources/html/sample-table.html";

    @Test
    void shouldParseOnlyRowsWithReadyForDeliveryStatus() throws IOException {
        String html = readFixture(SAMPLE_TABLE_PATH);

        List<RawOrderDto> result = HtmlTableParser.parse(html);

        assertEquals(4, result.size());
    }

    @Test
    void shouldMapColumnsByHeaderNameCorrectly() throws IOException {
        String html = readFixture(SAMPLE_TABLE_PATH);

        List<RawOrderDto> result = HtmlTableParser.parse(html);
        RawOrderDto first = result.get(0);

        assertEquals("1001", first.getId());
        assertEquals("Клієнт №1", first.getCustomerName());
        assertEquals("0+s0/0+s0", first.getCargoSpacesRaw());
        assertEquals("Готовий до доставки", first.getStatus());
    }

    @Test
    void shouldReturnEmptyListWhenHtmlIsBlank() {
        List<RawOrderDto> result = HtmlTableParser.parse("");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenHtmlIsNull() {
        List<RawOrderDto> result = HtmlTableParser.parse(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenRowHasFewerCellsThanHeaders() {
        // Manually crafted HTML with a row missing the М/П cell (15 <td> instead of 16),
        // simulating accidental column removal/reordering during future table changes.
        String brokenHtml = """
                <html>
                <body>
                <table class="deliverystable">
                  <thead>
                    <tr>
                      <th>ID</th><th>Відповідальний</th><th>Відправник</th>
                      <th>Адреса відправки</th><th>Клієнт/Отримувач</th><th>Адреса</th>
                      <th>Рахунки</th><th>Вид.ордери</th><th>Накладні</th>
                      <th>Вага</th><th>Сума</th><th>М/П</th><th>Кат.</th>
                      <th>Водій</th><th>Примітки</th><th>Статус</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr class="deliverylist">
                      <td>1005</td><td>Іванов</td><td>Склад №1</td>
                      <td>вул. Сирецька, 10</td><td>Клієнт №5</td><td>вул. Старовокзальна, 6</td>
                      <td>INV-005</td><td>ORD-005</td><td>DN-005</td>
                      <td>555</td><td>8000</td><td>B</td>
                      <td>Сидоренко</td><td>повернення тари</td><td>Готовий до доставки</td>
                    </tr>
                  </tbody>
                </table>
                </body>
                </html>
                """;

        assertThrows(IndexOutOfBoundsException.class,
                () -> HtmlTableParser.parse(brokenHtml));
    }

    private String readFixture(String path) throws IOException {
        return Files.readString(Path.of(path));
    }

}
