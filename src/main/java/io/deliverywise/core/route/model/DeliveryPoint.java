package io.deliverywise.core.route.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;
//import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

/**
 * <p>Domain model representing a delivery point or a customer order.</p>
 * <p>Доменна модель, що представляє точку доставки або замовлення клієнта.</p>
 *
 * * @author Ihor Herasymenko
 * @since 08.06.2026
 */
public class DeliveryPoint {
    /**
     * id                         Unique identifier of the delivery point / Унікальний ідентифікатор точки.
     * managerName                Name of the responsible manager / Ім'я відповідального менеджера.
     * senderName                 Name of the sending warehouse or entity / Назва відправника.
     * customerName               Anonymized customer name or ID / Анонімізоване ім'я або ID клієнта.
     * deliveryAddress            Delivery address (cleansed or masked for privacy) / Адреса доставки.
     * clientContacts             Driver-facing contact information / Контактні дані клієнта.
     * weightKg                   Total weight of the order in kilograms / Загальна вага замовлення в кг.
     * orderAmount                Total monetary value of the order using BigDecimal to prevent rounding errors / Точна сума замовлення (використовує BigDecimal для запобігання помилок округлення).
     * cargoSpacesRaw             Raw logistical zone distribution string / Сирий рядок розподілу за зонами.
     * mainWarehousePlaces        Number of standard boxes from the main warehouse / Кількість місць (коробок) з основного складу.
     * fragileWarehousePlaces     Number of fragile/chemical boxes requiring special care / Кількість місць (коробок) з термо-складу (хрупке/хімія).
     * mainWarehousePallets       Number of standard heavy pallets / Кількість палет з основного складу (метизи/важке).
     * fragileWarehousePallets    Number of pallets with fragile/chemical goods / Кількість палет з продукцією термо-складу.
     * operationType              Operational category indicating whether it is a delivery,pickup, transfer or return / Операційна категорія: доставка клієнту, забір, перевезення вантажу з одного сскладу на інший, повернення від клієнта.
     * invoices                   Comma-separated list of attached commercial invoices / Список комерційних рахунків-фактур (номери через кому).
     * issueOrders                Comma-separated list of warehouse release orders / Список видаткових ордерів складу (номери через кому).
     * deliveryNotes              Comma-separated list of consignment/delivery notes / Список товарно-транспортних накладних (номери через кому).
     * isFragileChemicals         Flag indicating special storage/loading constraints / Прапор потреби в особливих умовах навантаження (крихке/хімія).
     * isBulky                    Flag indicating oversized or long cargo (e.g., 2-3m profiles) / Прапор негабаритного або довгомірного грузу.
     * LightVolumetric            Flag indicating low-weight, high-volume cargo requiring top-stacking (e.g., insulation plugs) / Прапор легкого, але об'ємного вантажу, що потребує верхнього розвантаження/розміщення (наприклад, дюбель-парасолька).
     * timeStart                  Delivery time window start (minutes from midnight) / Початок тимчасового вікна доставки (хв від початку доби).
     * timeEnd                    Delivery time window end (minutes from midnight) / Кінець тимчасового вікна доставки (хв від початку доби).
     * specialNotes               Special instructions or comments from logistics / Спеціальні примітки або коментарі логіста.
     * deliveryStatus             Current operational status of the delivery / Поточний статус доставки.
     * latitude                   Geographic latitude for the routing engine / Географічна широта для картографічного двигуна.
     * longitude                  Geographic longitude for the routing engine / Географічна довгота для картографічного двигуна.
     * routeId                    Unique identifier of the assigned route (null before optimization) / Унікальний ідентифікатор призначеного маршруту (null до оптимізації).
     * sequenceNumber             Sequence position index inside the final delivery chain / Порядковий номер (індекс) точки в ланцюжку доставки.
     */
    private int id;
    private String managerName;
    private String senderName;
    private String customerName;
    private String deliveryAddress;
    private String clientContacts;
    private double weightKg;
    private BigDecimal orderAmount;

        // Warehouse allocation zones (Enterprise standard)
    private String cargoSpacesRaw;
    private int mainWarehousePlaces;
    private int fragileWarehousePlaces;
    private int mainWarehousePallets;
    private int fragileWarehousePallets;

    @Enumerated(EnumType.STRING)
    private OperationType operationType;



    private String invoices;
    private String issueOrders;
    private String deliveryNotes;

    // Lombok генерує getter isFragileChemicals() → Jackson вирізає префікс 'is' → бачить "fragileChemicals".
    // @JsonProperty фіксує ім'я поля в JSON явно, щоб уникнути розбіжності між getter і JSON-ключем.
    // Та сама проблема: Lombok → isBulky() → Jackson бачить "bulky"  та Lombok → isLightVolumetric() → Jackson бачить "lightVolumetric".
    @JsonProperty("isFragileChemicals")
    private boolean isFragileChemicals;
    @JsonProperty("isBulky")
    private boolean isBulky;
    @JsonProperty("isLightVolumetric")
    private boolean isLightVolumetric;

    private int timeStart;
    private int timeEnd;

    private String specialNotes;
    private String deliveryStatus;

    private double latitude;
    private double longitude;

    private Integer routeId;
    private Integer sequenceNumber;

    /**
     * Compact constructor to automatically derive logical constraints based on order composition.
     * Компактний конструктор для автоматичного визначення логічних обмежень на основі складу замовлення.
     */
    /*public DeliveryPoint () {
    }*/

    public void calculateLogicalFlags() {
        // Automatically determine fragility based on specialized warehouse items
        isFragileChemicals = (fragileWarehousePlaces > 0 || fragileWarehousePallets > 0);

        if (specialNotes != null) {
            String lowerNotes = specialNotes.toLowerCase();

            // 1. Check for heavy oversized cargo / long profiles
            // Перевірка на важкий негабарит / довгомірний вантаж (шпильки, профілі)
            isBulky = lowerNotes.contains("шпил")
                    || lowerNotes.contains("шпіл")
                    || lowerNotes.contains("шпл")
                    || lowerNotes.contains("проф")
                    || lowerNotes.contains("метр")
                    || lowerNotes.contains(" 3м")
                    || lowerNotes.contains(" 2м");

            // 2. Check for trade display stands (require careful placement and securing)
            // Перевірка на торгові стенди (потребують дбайливого розміщення та фіксації)
            boolean hasStand = lowerNotes.contains("стен")
                    || lowerNotes.contains("вистав")
                    || lowerNotes.contains("выстав");

            // 3. Check for low-weight, high-volume cargo (insulation plugs, light plastic)
            // Перевірка на легкий об'ємний вантаж (дюбель-парасолька, легкий пластик)
            isLightVolumetric = lowerNotes.contains("парасол")
                    || lowerNotes.contains("зонт")
                    || lowerNotes.contains("тарел")
                    || lowerNotes.contains("таріл")
                    || lowerNotes.contains("обем")
                    || lowerNotes.contains("объем")
                    || lowerNotes.contains("об`єм")
                    || lowerNotes.contains("меш")
                    || lowerNotes.contains("пак")
                    || lowerNotes.contains("інструм")
                    || lowerNotes.contains("инструм")
                    || lowerNotes.contains("орган")
                    || lowerNotes.contains("конт")
                    || lowerNotes.contains("короб");

            // Стенд — это всегда частный случай негабарита (Bulky)
            if (hasStand) {
                isBulky = true;
            }

        } else {
            // If there are no special notes, forcibly reset only the derived operational flags
            // Якщо спеціальні примітки відсутні, примусово скидаємо лише розрахункові операційні прапори
            isBulky = false;
            isLightVolumetric = false;
        }
    }
}