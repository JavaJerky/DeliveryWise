package io.deliverywise.core.route.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * <p>Core web infrastructure configuration class.</p>
 * <p>Головний клас конфігурації веб-інфраструктури додатка.</p>
 *
 * <p>Establishes centralized Spring Beans for outbound HTTP transport and integration pipelines.</p>
 * <p>Створює централізовані компоненти (Spring Beans) для вихідного HTTP-транспорту та інтеграційних каналів.</p>
 *
 * @author Ihor Herasymenko
 * @since 08.06.2026
 */
@Configuration
public class WebConfig {

    /**
     * <p>Target gateway root URL injected from active environment property source.</p>
     * <p>Коренева URL-адреса цільового шлюзу, що інжектується з активного джерела властивостей оточення.</p>
     */
    @Value("${carrier.api.base-url}")
    private String baseUrl;

    /**
     * Creates a pre-configured, reusable {@link RestClient} bean for stateful corporate API integration.
     * Створює попередньо налаштований компонент {@link RestClient} багаторазового використання для інтеграції з корпоративним API.
     *
     * @return Initialized RestClient instance bound to the enterprise base URL /
     * Ініціалізований екземпляр RestClient, прив'язаний до корпоративної базової URL-адреси.
     */
    @Bean
    public RestClient carrierRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}