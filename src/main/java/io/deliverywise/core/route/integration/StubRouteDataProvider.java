package io.deliverywise.core.route.integration;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.deliverywise.core.route.infrastructure.RouteDataProvider;
import io.deliverywise.core.route.model.DeliveryPoint;
import io.deliverywise.core.route.model.FleetType;
import io.deliverywise.core.route.model.Vehicle;
import io.deliverywise.core.route.service.Anonymizer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>Stub implementation of {@link RouteDataProvider} for Phase 1 MVP demonstrations.</p>
 * <p>Стаб-реалізація {@link RouteDataProvider} для демонстрації MVP на першому етапі.</p>
 *
 * @author Ihor Herasymenko
 * @date 18.06.2026
 */

@Component
public class StubRouteDataProvider implements RouteDataProvider {

    private final ObjectMapper objectMapper;
    private final Anonymizer anonymizer;

    public StubRouteDataProvider(ObjectMapper objectMapper, Anonymizer anonymizer) {
        this.objectMapper = objectMapper;
        this.anonymizer = anonymizer;
    }

    /**
     * Завантажує точки з файлу kyiv-test-orders.json, розраховує логістичні прапори
     * та пакетно анонімізує весь список перед передачею в оптимізатор.
     *
     * @return List of processed and anonymized points / Список анонімізованих точок.
     */
    @Override
    public List<DeliveryPoint> getAnonymizedDeliveryPoints() {
        try {
            // 1. Зчитуємо сирий JSON-масив із нашої папки ресурсів mock-data
            InputStream inputStream = new ClassPathResource("mock-data/kyiv-test-orders.json").getInputStream();
            List<DeliveryPoint> rawPoints = objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<DeliveryPoint>>() {}
            );

            // 2. Спочатку розраховуємо логістичні прапори (шпильки, дюбелі) для кожної точки
            for (DeliveryPoint point : rawPoints) {
                point.calculateLogicalFlags();
            }

            // 3. ПАКЕТНО анонімізуємо весь список в один захід, як і вимагає архітектура Anonymizer
            // Метод повертає вже очищений від комерційних назв список точок
            return anonymizer.anonymize(rawPoints);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load and process stub delivery points from mock-data/kyiv-test-orders.json", e);
        }
    }

    /**
     * Завантажує список автомобілів з конфігураційного JSON-файлу стаб-даних
     * та відфільтровує лише ті ТЗ, які сьогодні готові до рейсу (статус READY).
     *
     * @return List of operational vehicles / Список готових до рейсу автомобілів.
     */
    @Override
    public List<Vehicle> getAvailableFleet() {
        try {
            // 1. Зчитуємо конфігурацію автопарку з нашої ізольованої папки mock-data
            InputStream inputStream = new ClassPathResource("mock-data/fleet-config.json").getInputStream();

            // 2. Десеріалізуємо JSON-масив у повноцінні Java-об'єкти класу Vehicle
            List<Vehicle> allVehicles = objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<Vehicle>>() {}
            );

            // 3. Бізнес-фільтрація: у двигун маршрутизації передаємо ТІЛЬКИ робочі машини
            return allVehicles.stream()
                    .filter(Vehicle::isAvailableForRouting)
                    .toList();

        } catch (Exception e) {
            // Захищаємо додаток від падіння, логуємо зрозумілу для розробника помилку
            throw new RuntimeException("Failed to load and filter fleet configuration from mock-data/fleet-config.json", e);
        }
    }
}
