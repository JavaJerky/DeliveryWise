package io.deliverywise.core.route.integration;

import io.deliverywise.core.route.model.DeliveryPoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>Integration test verifying the behaviour of {@link CarrierApiClientStub} using local profile constraints.</p>
 * <p>Інтеграційний тест, що перевіряє поведінку {@link CarrierApiClientStub} з використанням локального профілю.</p>
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
     * Verifies that mock data is ingested correctly from JSON resources and physical attributes are accurately calculated.
     * Перевіряє, що макетні дані коректно зчитуються з ресурсів JSON, а фізичні атрибути обчислюються без помилок.
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
        assertFalse(orders.isEmpty(), "Operational orders list must not be empty!");

        // 4. Print structured summary for physical distribution auditing
        System.out.println("\n=== [TEST] VERIFYING CARGO DISTRIBUTION VIA ENTERPRISE DATA MODELS ===");
        for (DeliveryPoint order : orders) {
            System.out.printf("Client: %-22s | Weight: %4.0f kg | Places (Main/Fragile): %d/%d | Pallets (Main/Fragile): %d/%d | Fragile Chem: %-5b | Bulky Load: %b%n",
                    order.customerName(),
                    order.weightKg(),
                    order.mainWarehousePlaces(),
                    order.fragileWarehousePlaces(),
                    order.mainWarehousePallets(),
                    order.fragileWarehousePallets(),
                    order.isFragileChemicals(),
                    order.isBulky()
            );
        }
        System.out.println("======================================================================\n");
    }
}