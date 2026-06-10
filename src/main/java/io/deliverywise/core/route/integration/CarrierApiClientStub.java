package io.deliverywise.core.route.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.deliverywise.core.route.model.DeliveryPoint;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * <p>Stub implementation of the {@link CarrierApiClient} dedicated for local testing and MVP simulations.</p>
 * <p>Заглушка-реалізація інтерфейсу {@link CarrierApiClient}, призначена для локального тестування та симуляцій MVP.</p>
 *
 * <p>Active exclusively within the "local" Spring profile. Simulates live data ingestion by reading
 * a static JSON resource file. The object mapping schema reflects the expected database structure.</p>
 * <p>Активна виключно у Spring-профілі "local". Імітує отримання оперативних даних шляхом зчитування
 * статичного файлу ресурсів JSON. Схема відображення об'єктів відповідає очікуваній структурі бази даних.</p>
 *
 * @author Ihor Herasymenko
 * @since 08.06.2026
 */
@Service
@Profile("local")
public class CarrierApiClientStub implements CarrierApiClient {

    private final ObjectMapper objectMapper;

    /**
     * Constructs the stub with a shared JSON object mapper.
     * Конструктор для ініціалізації заглушки спільним мапером об'єктів JSON.
     *
     * @param objectMapper Spring-managed Jackson mapper / Керований Spring мапер Jackson.
     */
    public CarrierApiClientStub(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Generates a predictable mock authentication token for local profile bypass.
     * Повертає передбачуваний фейковий токен для обходу фільтрів безпеки на локальному оточенні.
     *
     * @param login    Mock user identity / Логін користувача.
     * @param password Mock user credential / Пароль користувача.
     * @return Deterministic local authorization string / Детермінований рядок локальної авторизації.
     */
    @Override
    public String login(String login, String password) {
        return "mock-auth-key-kyiv-12345";
    }

    /**
     * Reads and parses static delivery points from local test resources.
     * Зчитує та десеріалізує статичні точки доставки з локальних тестових ресурсів.
     *
     * @param authToken Security token to evaluate / Токен авторизації для перевірки.
     * @return List of parsed delivery entities / Список зчитованих сутностей доставки.
     * @throws SecurityException If the mock token evaluation fails / Якщо перевірка фейкового токена завершилась невдачею.
     */
    @Override
    public List<DeliveryPoint> getOrders(String authToken) {
        if (authToken == null || !authToken.equals("mock-auth-key-kyiv-12345")) {
            throw new SecurityException("Access denied: Invalid mock authentication token provided");
        }

        try (InputStream inputStream = getClass().getResourceAsStream("/mock-data/kyiv-test-orders.json")) {
            if (inputStream == null) {
                throw new IOException("Target test data resource path not found: /mock-data/kyiv-test-orders.json");
            }
            return objectMapper.readValue(inputStream, new TypeReference<List<DeliveryPoint>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to read or parse local mock data ingestion pipeline", e);
        }
    }
}