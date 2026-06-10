package io.deliverywise.core.route.integration;

import io.deliverywise.core.route.model.DeliveryPoint;
import java.util.List;

/**
 * <p>Integration gateway contract defining operations with the external Carrier logistics API.</p>
 * <p>Інтеграційний контракт шлюзу, що визначає операції із зовнішнім API логістичного оператора (Carrier API).</p>
 *
 * <p>Abstrahates authorization and data ingestion pipelines from the core vehicle routing optimizer.</p>
 * <p>Абстрагує процеси авторизації та отримання даних від головного двигуна оптимізації маршрутів.</p>
 *
 * @author Ihor Herasymenko
 * @since 08.06.2026
 */
public interface CarrierApiClient {

    /**
     * Authenticates against the external carrier service using enterprise credentials.
     * Виконує автентифікацію на зовнішньому сервері логістичного оператора за допомогою корпоративних облікових даних.
     *
     * @param login    Corporate system login identity / Логін користувача корпоративної системи.
     * @param password Corporate system security credential / Пароль користувача.
     * @return An active session token or cookie string for stateful operations /
     * Рядок активного сесійного токена або cookie для подальших запитів.
     */
    String login(String login, String password);

    /**
     * Fetches the operational list of delivery points (orders) prepared for fleet routing optimization.
     * Отримує оперативний список точок доставки (замовлень), підготовлених для оптимізації маршрутів автопарку.
     *
     * @param authToken Valid authorization session token or cookie / Валідний токен авторизації, отриманий після login.
     * @return List of delivery orders extracted from the external system / Список замовлень, витягнутих із зовнішньої системи.
     */
    List<DeliveryPoint> getOrders(String authToken);
}