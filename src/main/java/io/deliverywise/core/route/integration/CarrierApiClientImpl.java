package io.deliverywise.core.route.integration;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.deliverywise.core.route.model.DeliveryPoint;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
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
 * Utilizes Spring's {@link RestClient} for stateless HTTP transport and encapsulates legacy md5 hashing requirements.
 * </p>
 * <p>
 * Використовує Spring {@link RestClient} для HTTP-транспорту та інкапсулює застарілі вимоги щодо хешування md5.
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

    /**
     * Internal constructor initializing the HTTP transport client layers. Внутрішній конструктор, що ініціалізує шари
     * транспортного HTTP-клієнта.
     */
    private CarrierApiClientImpl() {
        this.client = RestClient.create();
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
                                    .uri("transcore.php")
                                    .contentType(
                                            MediaType.APPLICATION_FORM_URLENCODED)
                                    .body("command=newauth&username=" + login
                                            + "&userpassword=" + md5(password))
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
     * Generates an MD5 hexadecimal hash representation of the input string. Гexecutable утиліта для генерації
     * шістнадцяткового представлення MD5-хешу вхідного рядка.
     *
     * @param input Source raw string / Вихідний сирий рядок.
     * @return Hashed hexadecimal string / Хешований шістнадцятковий рядок.
     */
    private String md5(String input) {
        // // TODO: Legacy compatibility layer
        // Cryptographic MD5 digest is enforced by the legacy compliance constraints of the target transcore.php router.
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(
                    "Critical security provider error: MD5 digest algorithm not found",
                    e);
        }
        return sb.toString();
    }

}
