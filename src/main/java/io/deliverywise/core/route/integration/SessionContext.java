package io.deliverywise.core.route.integration;

import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>Thread-safe container designed for managing the active corporate session authorization token.</p>
 * <p>Потокобезпечний контейнер, призначений для керування активним токеном корпоративної сесії авторизації.</p>
 *
 * <p>Utilizes {@link AtomicReference} to provide non-blocking, atomic read/write operations on the volatile
 * authentication key across concurrent execution threads within the Spring container.</p>
 * <p>Використовує {@link AtomicReference} для забезпечення неблокуючих атомарних операцій читання/запису
 * ключів автентифікації між паралельними потоками виконання всередині контейнера Spring.</p>
 *
 * @author Ihor Herasymenko
 * @since 08.06.2026
 */
@Component
public class SessionContext {

    private final AtomicReference<String> authKey = new AtomicReference<>();

    /**
     * Atomically saves the new authentication key received from the remote dispatching gateway.
     * Атомарно зберігає новий ключ автентифікації, отриманий від віддаленого шлюзу диспетчеризації.
     *
     * @param key The active session token to store / Активний сесійний токен для збереження.
     */
    public void setAuthKey(String key) {
        this.authKey.set(key);
    }

    /**
     * Retrieves the current session token for outgoing corporate HTTP requests.
     * Повертає поточний сесійний токен для вихідних корпоративних HTTP-запитів.
     *
     * @return Active authorization string, or {@code null} if not authenticated /
     * Рядок активного ключа авторизації, або {@code null}, якщо автентифікацію не пройдено.
     */
    public String getAuthKey() {
        return this.authKey.get();
    }

    /**
     * Verifies if the application context currently holds an active, non-null session token.
     * Перевіряє, чи містить контекст додатка активний, непустий токен сесії.
     *
     * @return {@code true} if authenticated, {@code false} otherwise /
     * {@code true}, якщо автентифікацію пройдено успішно, інакше {@code false}.
     */
    public boolean isAuthenticated() {
        return this.authKey.get() != null;
    }
}