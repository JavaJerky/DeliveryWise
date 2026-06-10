package io.deliverywise.core.route.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>Domain model representing a delivery point or a customer order.</p>
 * <p>Доменна модель, що представляє точку доставки або замовлення клієнта.</p>
 *
 * @param id                         Unique identifier of the delivery point / Унікальний ідентифікатор точки.
 * @param managerName                Name of the responsible manager / Ім'я відповідального менеджера.
 * @param senderName                 Name of the sending warehouse or entity / Назва відправника.
 * @param customerName               Anonymized customer name or ID / Анонімізоване ім'я або ID клієнта.
 * @param deliveryAddress            Delivery address (cleansed or masked for privacy) / Адреса доставки.
 * @param clientContacts             Driver-facing contact information / Контактні дані клієнта.
 * @param weightKg                   Total weight of the order in kilograms / Загальна вага замовлення в кг.
 * @param orderAmount                Total monetary value of the order using BigDecimal to prevent rounding errors / Точна сума замовлення (використовує BigDecimal для запобігання помилок округлення).
 * @param cargoSpacesRaw             Raw logistical zone distribution string / Сирий рядок розподілу за зонами.
 * @param mainWarehousePlaces        Number of standard boxes from the main warehouse / Кількість місць (коробок) з основного складу.
 * @param fragileWarehousePlaces     Number of fragile/chemical boxes requiring special care / Кількість місць (коробок) з термо-складу (хрупке/хімія).
 * @param mainWarehousePallets       Number of standard heavy pallets / Кількість палет з основного складу (метизи/важке).
 * @param fragileWarehousePallets    Number of pallets with fragile/chemical goods / Кількість палет з продукцією термо-складу.
 * @param invoices                   List of attached commercial invoices / Список комерційних рахунків-фактур.
 * @param issueOrders                List of warehouse release orders / Список видаткових ордерів.
 * @param deliveryNotes              List of consignment/delivery notes / Список товарно-транспортних накладних.
 * @param isFragileChemicals         Flag indicating special storage/loading constraints / Прапор потреби в особливих умовах навантаження (крихке/хімія).
 * @param isBulky                    Flag indicating oversized or long cargo (e.g., 2-3m profiles) / Прапор негабаритного або довгомірного грузу.
 * @param timeStart                  Delivery time window start (minutes from midnight) / Початок тимчасового вікна доставки (хв від початку доби).
 * @param timeEnd                    Delivery time window end (minutes from midnight) / Кінець тимчасового вікна доставки (хв від початку доби).
 * @param specialNotes               Special instructions or comments from logistics / Спеціальні примітки або коментарі логіста.
 * @param deliveryStatus             Current operational status of the delivery / Поточний статус доставки.
 * @param latitude                   Geographic latitude for the routing engine / Географічна широта для картографічного двигуна.
 * @param longitude                  Geographic longitude for the routing engine / Географічна довгота для картографічного двигуна.
 * * @author Ihor Herasymenko
 * @since 08.06.2026
 */
public record DeliveryPoint(
        int id,
        String managerName,
        String senderName,
        String customerName,
        String deliveryAddress,
        String clientContacts,
        double weightKg,
        BigDecimal orderAmount,

        // Warehouse allocation zones (Enterprise standard)
        String cargoSpacesRaw,
        int mainWarehousePlaces,
        int fragileWarehousePlaces,
        int mainWarehousePallets,
        int fragileWarehousePallets,

        List<String> invoices,
        List<String> issueOrders,
        List<String> deliveryNotes,

        boolean isFragileChemicals,
        boolean isBulky,

        int timeStart,
        int timeEnd,

        String specialNotes,
        String deliveryStatus,

        double latitude,
        double longitude
) {
    /**
     * Compact constructor to automatically derive logical constraints based on order composition.
     * Компактний конструктор для автоматичного визначення логічних обмежень на основі складу замовлення.
     */
    public DeliveryPoint {
        // Automatically determine fragility based on specialized warehouse items
        isFragileChemicals = (fragileWarehousePlaces > 0 || fragileWarehousePallets > 0);

        // Safe case-insensitive scan for bulky items or long profiles in special notes
        if (specialNotes != null) {
            String lowerNotes = specialNotes.toLowerCase();
            isBulky = lowerNotes.contains("шпил")    // шпилька, шпилка
                    || lowerNotes.contains("шпіл")   // шпілька
                    || lowerNotes.contains("шпл")    // шплька (опечатка)
                    || lowerNotes.contains("проф")   // профіль, профиль
                    || lowerNotes.contains("стенд")  // виставковий стенд
                    || lowerNotes.contains("метр")   // 2 метри, 3 метра
                    || lowerNotes.contains(" 3м")    // маркер довжини 3м
                    || lowerNotes.contains(" 2м");   // маркер довжини 2м
        } else {
            isBulky = false;
        }
    }
}