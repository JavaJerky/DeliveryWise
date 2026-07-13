package io.deliverywise.core.route.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ihor Herasymenko
 * @date 09.07.2026
 */
public class DeliveryPointDeserializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeAllSixPoints() throws Exception {
        // 1. Читаємо файл з resources
        InputStream inputStream = getClass()
                .getResourceAsStream("/mock-data/kyiv-test-orders.json");

        assertNotNull(inputStream, "Файл kyiv-test-orders.json не знайдено в mock-data/");

        // 2. Десеріалізуємо в List<DeliveryPoint>
        List<DeliveryPoint> points = objectMapper.readValue(
                inputStream,
                objectMapper.getTypeFactory().constructCollectionType(List.class, DeliveryPoint.class)
        );

        // 3. Перевіряємо кількість точок
        assertEquals(6, points.size());
    }

    @Test
    void shouldParseOperationTypeEnum() throws Exception {
        List<DeliveryPoint> points = deserialize();

        // id=1..5 — DELIVERY, id=6 — RETURN
        assertEquals(OperationType.DELIVERY, points.get(0).getOperationType());
        assertEquals(OperationType.RETURN,   points.get(5).getOperationType());
    }

    @Test
    void shouldParseBigDecimalWithoutPrecisionLoss() throws Exception {
        List<DeliveryPoint> points = deserialize();

        // id=1: orderAmount = 32000.00
        assertEquals(new BigDecimal("32000.00"), points.get(0).getOrderAmount());

        // id=6 (RETURN): orderAmount = 0.00
        assertEquals(new BigDecimal("0.00"), points.get(5).getOrderAmount());
    }

    @Test
    void shouldHaveNullRouteAssignmentFields() throws Exception {
        List<DeliveryPoint> points = deserialize();

        // Всі точки до оптимізації — routeId і sequenceNumber = null
        for (DeliveryPoint point : points) {
            assertNull(point.getRouteId(),        "routeId має бути null для point id=" + point.getId());
            assertNull(point.getSequenceNumber(), "sequenceNumber має бути null для point id=" + point.getId());
        }
    }

    @Test
    void shouldParseBooleanFlagsCorrectly() throws Exception {
        List<DeliveryPoint> points = deserialize();

        // id=1: шпильки 3м → isBulky=true
        assertTrue(points.get(0).isBulky());

        // id=2: хімія → isFragileChemicals=true
        assertTrue(points.get(1).isFragileChemicals());

        // id=4: дюбель-парасолька → isLightVolumetric=true
        assertTrue(points.get(3).isLightVolumetric());
    }

    // --- helper ---
    private List<DeliveryPoint> deserialize() throws Exception {
        InputStream inputStream = getClass()
                .getResourceAsStream("/mock-data/kyiv-test-orders.json");
        return objectMapper.readValue(
                inputStream,
                objectMapper.getTypeFactory().constructCollectionType(List.class, DeliveryPoint.class)
        );
    }
}