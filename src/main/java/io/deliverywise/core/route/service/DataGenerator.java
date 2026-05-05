package ua.foxminded.mentoring.route.service; // Правильный пакет для генератора
import ua.foxminded.mentoring.route.model.DeliveryPoint;

import net.datafaker.Faker;
import ua.foxminded.mentoring.route.model.Vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

        /**
        * Сервис для генерации тестовых наборов данных.
        * Использует библиотеку DataFaker для создания реалистичных имен компаний и адресов в Украине.
        * Служит для имитации выгрузки из 1С на этапе разработки (Phase 1).
        *
         * Содержит список пеальных авто собственного автопарка
         * Содержит список наймных машин и машин Новой пошти
         * */

public class DataGenerator {

    // Используем локацию, чтобы адреса и названия были похожи на правду
    //Locale(java.lang.String)' is deprecated since version 19
    //private final Faker faker = new Faker(new Locale("uk"));
    private final net.datafaker.Faker faker = new net.datafaker.Faker(java.util.Locale.forLanguageTag("uk"));

    public List<DeliveryPoint> generatePoints(int count) {

        List<DeliveryPoint> points = new java.util.ArrayList<>();

        // 0. добавляем СКЛАД (Depot) - всегда точка старта всех машин (первая точка)
        points.add(new DeliveryPoint(
            0, "ПАТ СОЛДІ І КО", "вул. Сирецька, 28/2",
            0.0, 0, 0, false, false, 480, 1080, "СКЛАД", 500, 500
        ));

        //1. Генерация клиеентов
        // Generate clients
        for (int i = 1; i <= count; i++) {
            boolean isSoudal = faker.random().nextBoolean(); // 50/50 Soudal
            boolean isBulky = faker.random().nextInt(100) < 10; // 10% негабарит

            points.add(new DeliveryPoint(
                i,
                faker.company().name(),
                faker.address().streetAddress(),
                faker.number().randomDouble(1, 5, 800), // Вага до 800кг
                faker.number().numberBetween(1, 20),    // Мест
                faker.number().numberBetween(0, 3),     // Паллет
                isSoudal,
                isBulky,
                480, // Старт в 08:00
                faker.options().option(960, 1020, 1080), // Финиш в 16:00, 17:00 или 18:00
                isBulky ? "НЕГАБАРИТ! Шпилька!" : "Оплата на місці",
                faker.number().numberBetween(0, 1000),
                faker.number().numberBetween(0, 1000)
            ));
        }
        return points;
    }

    public List<Vehicle> generateVehicles() {
        List<Vehicle> vehicles= new ArrayList<>();
        vehicles.add(new Vehicle(0, "Citroën Jumper", "АА 1111 ВВ", "Макоїда Р.", "+380672169980", 1945, 4, true));
        vehicles.add(new Vehicle(0, "Citroën Jumper", "АА 1112 ВВ", "Червоний Е.", "+380674494757", 1945, 4, true));
        vehicles.add(new Vehicle(0, "Hyundai HD78", "АА 1113 ВВ", "Шкляревський І.", "+380675017873", 4500, 5, true));
        //vehicles.add(new Vehicle(0, "Hyundai HD78", "АА 1114 ВВ", "Василенко В.", "+380672434193", 4500, 5, true));

        // Найманий транспорт / Нова Пошта
        vehicles.add(new Vehicle(3, "Нова Пошта", "VIRTUAL", "Кур'єр НП", "-", 10000, 33, false));
        vehicles.add(new Vehicle(3, "Найманий", "VIRTUAL", "Найманий водій", "-", 5000, 5, false));

        return vehicles;
    }
}
