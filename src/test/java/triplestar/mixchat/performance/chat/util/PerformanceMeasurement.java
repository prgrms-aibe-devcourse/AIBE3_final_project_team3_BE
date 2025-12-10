package triplestar.mixchat.performance.chat.util;

import org.hibernate.stat.Statistics;
import org.springframework.util.StopWatch;

/**
 * 성능 측정 결과를 담는 클래스
 */
public class PerformanceMeasurement {

    private final String testName;
    private final long executionTimeMs;
    private final long queryCount;
    private final long queryExecutionMaxTime;
    private final long connectionCount;

    private PerformanceMeasurement(String testName, long executionTimeMs,
                                   long queryCount, long queryExecutionMaxTime,
                                   long connectionCount) {
        this.testName = testName;
        this.executionTimeMs = executionTimeMs;
        this.queryCount = queryCount;
        this.queryExecutionMaxTime = queryExecutionMaxTime;
        this.connectionCount = connectionCount;
    }

    /**
     * 성능 측정 실행 및 결과 반환
     */
    public static PerformanceMeasurement measure(String testName,
                                                  Statistics stats,
                                                  Runnable task) {
        // Statistics 초기화
        stats.clear();

        // 실행 시간 측정
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        task.run();

        stopWatch.stop();

        // 결과 수집
        return new PerformanceMeasurement(
            testName,
            stopWatch.getTotalTimeMillis(),
            stats.getQueryExecutionCount(),
            stats.getQueryExecutionMaxTime(),
            stats.getConnectCount()
        );
    }

    /**
     * 측정 결과 출력 (콘솔)
     */
    public void printResult() {
        System.out.println("=".repeat(80));
        System.out.println("Performance Test: " + testName);
        System.out.println("=".repeat(80));
        System.out.printf("⏱️  Total Execution Time : %,d ms%n", executionTimeMs);
        System.out.printf("🔍 Total Query Count    : %,d queries%n", queryCount);
        System.out.printf("⚡ Max Query Time       : %,d ms%n", queryExecutionMaxTime);
        System.out.printf("🔌 DB Connection Count  : %,d connections%n", connectionCount);
        System.out.println("=".repeat(80));
    }

    /**
     * 두 측정 결과 비교 출력
     */
    public static void compareResults(PerformanceMeasurement before,
                                      PerformanceMeasurement after) {
        System.out.println("=".repeat(80));
        System.out.println("Performance Comparison: " + before.testName + " vs " + after.testName);
        System.out.println("=".repeat(80));

        // 실행 시간 비교
        long timeDiff = before.executionTimeMs - after.executionTimeMs;
        double timeImprovement = ((double) timeDiff / before.executionTimeMs) * 100;
        System.out.printf("⏱️  Execution Time%n");
        System.out.printf("   Before: %,d ms%n", before.executionTimeMs);
        System.out.printf("   After : %,d ms%n", after.executionTimeMs);
        System.out.printf("   Diff  : %,d ms (%.1f%% %s)%n%n",
            Math.abs(timeDiff),
            Math.abs(timeImprovement),
            timeImprovement > 0 ? "FASTER ⚡" : "SLOWER ⚠️");

        // 쿼리 수 비교
        long queryDiff = before.queryCount - after.queryCount;
        double queryImprovement = before.queryCount > 0
            ? ((double) queryDiff / before.queryCount) * 100
            : 0;
        System.out.printf("🔍 Query Count%n");
        System.out.printf("   Before: %,d queries%n", before.queryCount);
        System.out.printf("   After : %,d queries%n", after.queryCount);
        System.out.printf("   Diff  : %,d queries (%.1f%% %s)%n%n",
            Math.abs(queryDiff),
            Math.abs(queryImprovement),
            queryImprovement > 0 ? "REDUCED ✅" : "INCREASED ⚠️");

        // 최대 쿼리 시간 비교
        long maxQueryDiff = before.queryExecutionMaxTime - after.queryExecutionMaxTime;
        System.out.printf("⚡ Max Query Time%n");
        System.out.printf("   Before: %,d ms%n", before.queryExecutionMaxTime);
        System.out.printf("   After : %,d ms%n", after.queryExecutionMaxTime);
        System.out.printf("   Diff  : %,d ms%n%n", Math.abs(maxQueryDiff));

        // 종합 평가
        System.out.println("📊 Overall Assessment");
        if (timeImprovement > 0 && queryImprovement >= 0) {
            System.out.println("   ✅ PERFORMANCE IMPROVED!");
            System.out.printf("   💡 %.1fx faster execution%n",
                (double) before.executionTimeMs / after.executionTimeMs);
        } else if (timeImprovement < -50) {
            System.out.println("   ⚠️  PERFORMANCE DEGRADED SIGNIFICANTLY");
        } else {
            System.out.println("   ⚖️  PERFORMANCE SIMILAR");
        }

        System.out.println("=".repeat(80));
    }

    // Getters
    public String getTestName() { return testName; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    public long getQueryCount() { return queryCount; }
    public long getQueryExecutionMaxTime() { return queryExecutionMaxTime; }
    public long getConnectionCount() { return connectionCount; }
}
