package io.deliverywise.core.route.integration;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.deliverywise.core.route.model.DeliveryPoint;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * <p>
 * Production implementation of the {@link CarrierApiClient} interacting with the remote dispatching gateway.
 * </p>
 * <p>
 * Промислова реалізація інтерфейсу {@link CarrierApiClient}, що взаємодіє з віддаленим шлюзом диспетчеризації.
 * </p>
 *
 * <p>
 * Utilizes Spring's {@link RestClient} for stateless HTTP transport. The exact shape of the legacy auth handshake
 * (endpoint name, form field names, digest algorithm) is intentionally NOT hardcoded here — it is loaded at runtime
 * from an external properties file living outside this repository (see {@code carrier.auth.config-path}). These are
 * implementation details of a third-party legacy system we do not control, not part of DeliveryWise's own security
 * design, and should not be visible in the public source tree.
 * </p>
 * <p>
 * Використовує Spring {@link RestClient} для HTTP-транспорту. Точна форма legacy-хендшейку авторизації (ім'я ендпоінта,
 * назви полів форми, алгоритм хешування) свідомо НЕ захардкожена тут — завантажується в рантаймі із зовнішнього
 * properties-файлу поза цим репозиторієм (див. {@code carrier.auth.config-path}). Це деталі реалізації чужої
 * legacy-системи, яку ми не контролюємо, а не елемент власної моделі безпеки DeliveryWise, і їм не місце у публічному
 * вихідному коді.
 * </p>
 *
 * @author Ihor Herasymenko
 * @since 08.06.2026
 */
@Component
@Profile("!local")
public class CarrierApiClientImpl implements CarrierApiClient {

    private final RestClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    private final String endpoint;
    private final String command;
    private final String usernameField;
    private final String passwordField;
    private final String hashAlgorithm;

    /**
     * Initializes the HTTP transport client and loads the legacy auth protocol shape from an external config file.
     * Ініціалізує HTTP-клієнт та завантажує форму legacy-протоколу авторизації із зовнішнього конфіг-файлу.
     *
     * @param configPath Absolute path to the external carrier-auth properties file (outside the repo), injected via
     *                   {@code carrier.auth.config-path} / Абсолютний шлях до зовнішнього properties-файлу (поза
     *                   репозиторієм), інжектиться через {@code carrier.auth.config-path}.
     */
    public CarrierApiClientImpl(@Value("${carrier.auth.config-path}") String configPath) {
        this.client = RestClient.create();

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(Path.of(configPath))) {
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Carrier auth config not found at '" + configPath + "'. This file lives outside the "
                            + "repository by design (see carrier.auth.config-path in application-local.properties) "
                            + "— create it before activating a non-local profile.",
                    e);
        }

        this.endpoint = requireProperty(props, "carrier.auth.endpoint");
        this.command = requireProperty(props, "carrier.auth.command");
        this.usernameField = requireProperty(props, "carrier.auth.username-field");
        this.passwordField = requireProperty(props, "carrier.auth.password-field");
        this.hashAlgorithm = requireProperty(props, "carrier.auth.hash-algorithm");
    }

    private static String requireProperty(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing required key '" + key + "' in the external carrier auth config");
        }
        return value;
    }

    /**
     * Executes security handshake against the remote script and extracts the session token. Виконує автентифікацію на
     * віддаленому скрипті та витягує сесійний токен доступу.
     *
     * @param login    Corporate system login identity / Логін користувача.
     * @param password Corporate system security credential / Пароль користувача.
     * @return Extracted alpha-numeric auth key / Витягнутий альфа-нумеричний ключ авторизації.
     */
    @Override
    public String login(String login, String password) {
        String responseBody = client.post()
                                    .uri(endpoint)
                                    .contentType(
                                            MediaType.APPLICATION_FORM_URLENCODED)
                                    .body("command=" + command
                                            + "&" + usernameField + "=" + login
                                            + "&" + passwordField + "=" + digest(password))
                                    .retrieve()
                                    .body(String.class);

        try {
            JsonNode root = mapper.readTree(responseBody);
            if (root.get("authstatus").asInt() != 1) {
                throw new RuntimeException(
                        "External authentication rejected by carrier server");
            }
            return root.get("authkey").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                    "Failed to parse carrier authorization response payload",
                    e);
        }
    }

    /**
     * Operational placeholder for future direct enterprise endpoint ingestion. Операційний маркер для майбутньої прямої
     * інтеграції з кінцевою точкою.
     *
     * @throws UnsupportedOperationException Target integration pipeline is under active deployment / Цільовий канал
     *                                       інтеграції знаходиться в процесі розгортання.
     */
    @Override
    public List<DeliveryPoint> getOrders(String authToken) {
        // // TODO: Legacy compatibility layer / Planned infrastructure upgrade
        // Implement reactive order ingestion via RestClient once the enterprise endpoint goes live.
        throw new UnsupportedOperationException(
                "Real-world data ingestion via external carrier API layout is currently under deployment. Please use CarrierApiClientStub for local testing.");
    }

    /**
     * Generates a hexadecimal digest of the input string using the algorithm loaded from the external carrier auth
     * config. Генерує шістнадцятковий дайджест вхідного рядка алгоритмом, завантаженим із зовнішнього конфігу
     * авторизації перевізника.
     *
     * @param input Source raw string / Вихідний сирий рядок.
     * @return Hashed hexadecimal string / Хешований шістнадцятковий рядок.
     */
    private String digest(String input) {
        // Algorithm is NOT our choice — it's dictated by the target legacy endpoint's compliance
        // constraints (see carrier.auth.config-path). Loaded, not hardcoded, so this file never
        // states in public source which (weak) algorithm the third-party system requires.
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
            byte[] hashBytes = md.digest(input.getBytes());
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(
                    "Carrier auth config specifies an unsupported digest algorithm: " + hashAlgorithm,
                    e);
        }
        return sb.toString();
    }

}
