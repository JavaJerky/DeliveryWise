package io.deliverywise.core.route.analytics;

/**
 * <h1>Route Benchmark Engine</h1>
 * <p>
 * ENG: Core interface for the analytical module responsible for evaluating routing efficiency.
 * It compares automated optimization results (AI / Google OR-Tools) against historical or manual dispatcher routes.
 * Used to generate business intelligence reports for corporate executives.
 * </p>
 * <p>
 * UKR: Основний інтерфейс аналітичного модуля, що відповідає за оцінку ефективності маршрутизації.
 * Порівнює результати автоматичної оптимізації (AI / Google OR-Tools) з історичними або ручними маршрутами диспетчера.
 * Використовується для формування звітів бізнес-аналітики для керівництва компанії.
 * </p>
 *
 * @since 2026-06-25
 */
public interface RouteBenchmarkEngine {

    /**
     * <p>
     * ENG: Performs a comparative analysis between manual dispatch metrics and DeliveryWise optimized metrics.
     * </p>
     * <p>
     * UKR: Виконує порівняльний аналіз між метриками ручного диспетчерського керування та оптимізованими метриками DeliveryWise.
     * </p>
     *
     * @param manualFacts     ENG: Historical data from manual planning. / UKR: Історичні дані ручного планування.
     * @param optimizedPlan   ENG: Result produced by the optimization core. / UKR: Результат, згенерований ядром оптимізації.
     * @return {@link BenchmarkReport} ENG: Summary comparison report. / UKR: Зведений порівняльний звіт.
     */
    BenchmarkReport compare(Object manualFacts, Object optimizedPlan);
}