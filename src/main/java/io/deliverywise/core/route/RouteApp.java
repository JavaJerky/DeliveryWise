package io.deliverywise.core.route;

import io.deliverywise.core.route.service.RouteOptimizationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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

    /**
     * Тестовий раннер, який автоматично запускає оптимізацію маршрутів після старту Spring Boot.
     * Забезпечує миттєву перевірку математичного ядра OR-Tools на mock-даних.
     */
    @Bean
    public CommandLineRunner runOptimizationTest(RouteOptimizationService optimizationService) {
        return args -> {
            System.out.println("\n🚀 [SYSTEM START] Initializing MVP Routing Optimization Session...");

            // Викликаємо наше ядро
            optimizationService.calculateOptimalRoutes();

            System.out.println("🏁 [SYSTEM END] Optimization Session Finished.\n");
        };
    }
}

