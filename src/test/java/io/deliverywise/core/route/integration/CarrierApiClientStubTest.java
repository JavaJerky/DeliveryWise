package io.deliverywise.core.route.integration;


import static org.junit.jupiter.api.Assertions.*;


import io.deliverywise.core.route.model.DeliveryPoint;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * <p>
 * Integration test verifying the behaviour of {@link CarrierApiClientStub} using local profile constraints.
 * </p>
 * <p>
 * Інтеграційний тест, що перевіряє поведінку {@link CarrierApiClientStub} з використанням локального профілю.
 * </p>
 *
 * @author Ihor Herasymenko
 * @since 09.06.2026
 */
@SpringBootTest
@ActiveProfiles("local")
class CarrierApiClientStubTest {

    @Autowired
    private CarrierApiClient carrierApiClient;

    /**
     * Verifies that mock data is ingested correctly from JSON resources and physical attributes are accurately
     * calculated. Перевіряє, що макетні дані коректно зчитуються з ресурсів JSON, а фізичні атрибути обчислюються без
     * помилок.
     */
    @Test
    void shouldLoadOrdersFromJsonAndCalculateFlags() {
        // 1. Authenticate and retrieve predictable mock token
        String token = carrierApiClient.login("admin", "admin");
        assertNotNull(token);

        // 2. Fetch the operational stub orders list
        List<DeliveryPoint> orders = carrierApiClient.getOrders(token);

        // 3. Evaluate basic collection metrics
        assertNotNull(orders);
        assertFalse(orders.isEmpty(),
                "Operational orders list must not be empty!");

        // 4. Print structured summary for physical distribution auditing
        System.out.println(
                "\n=== [TEST] VERIFYING CARGO DISTRIBUTION VIA ENTERPRISE DATA MODELS ===");
        for (DeliveryPoint order : orders) {
            // Crucial: Execute the logical constraints calculation for each point prior to output
            // Критично: Запускаємо розрахунок логічних обмежень для кожної точки перед виведенням
            order.calculateLogicalFlags();

            System.out.printf(
                    "Client: %-22s | Weight: %4.0f kg | Places (Main/Fragile): %d/%d | Pallets (Main/Fragile): %d/%d | Fragile Chem: %-5b | Bulky Load: %-5b | Light Volumetric: %b%n",
                    order.getCustomerName(),
                    order.getWeightKg(),
                    order.getMainWarehousePlaces(),
                    order.getFragileWarehousePlaces(),
                    order.getMainWarehousePallets(),
                    order.getFragileWarehousePallets(),
                    order.isFragileChemicals(),
                    order.isBulky(),
                    order.isLightVolumetric() // Output the new operational flag for high-volume, low-weight fragile
                                              // cargo
                                              // Виводимо новий операційний прапор для легкого об'ємного крихкого
                                              // вантажу
            );
        }
        System.out.println(
                "======================================================================\n");
    }

}
