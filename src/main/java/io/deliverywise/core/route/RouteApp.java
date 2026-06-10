package io.deliverywise.core.route;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>Main entry point for the DeliveryWise routing and fleet optimization engine.</p>
 * <p>Головна точка входу для системи маршрутизації та оптимізації автопарку DeliveryWise.</p>
 *
 * <p>Activates the Spring Boot context and prepares the infrastructure for operational logistics calculation.</p>
 * <p>Активує контекст Spring Boot та готує інфраструктуру для розрахунку оперативної логістики.</p>
 *
 * @author Ihor Herasymenko
 * @since 08.06.2026
 */
@SpringBootApplication
public class RouteApp {

    /**
     * Bootstrap method to launch the Spring Boot application.
     * Метод запуску для старту Spring Boot додатку.
     *
     * @param args Command line arguments / Аргументи командного рядка.
     */
    public static void main(String[] args) {
        SpringApplication.run(RouteApp.class, args);
    }
}

