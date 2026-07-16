package io.deliverywise.core.route.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <p>Raw Data Transfer Object for a single row from the carrier HTML table.</p>
 * <p>Сирий DTO для одного рядка з HTML-таблиці перевізника.</p>
 *
 * <p>All fields are stored as raw strings — no parsing or interpretation at this stage.
 * Всі поля зберігаються як рядки — без парсингу та інтерпретації на цьому етапі.</p>
 *
 * <p>Mapping pipeline: HTML row → RawOrderDto → CargoSpacesParser → DeliveryPoint</p>
 *
 * @author Ihor Herasymenko
 * @since 13.07.2026
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RawOrderDto {

    private String id;
    private String managerName;
    private String senderName;
    private String senderAddress;
    private String customerName;
    private String deliveryAddress;
    private String clientContacts;
    private String invoices;
    private String issueOrders;
    private String deliveryNotes;
    private String weightKg;
    private String orderAmount;
    private String cargoSpacesRaw;
    private String category;
    private String driverRaw;
    private String specialNotes;
    private String status;

}
