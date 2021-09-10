package performance;

public class Run {

    public static void main(String[] args) {
        PerformanceService performanceService =
                new PerformanceService("10.0.9.103",
                        10000,
                        2,
                        1000,
                        30);
        performanceService.init();
        performanceService.run();
    }
}
