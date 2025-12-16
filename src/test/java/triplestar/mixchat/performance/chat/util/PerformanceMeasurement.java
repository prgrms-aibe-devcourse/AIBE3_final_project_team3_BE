package triplestar.mixchat.performance.chat.util;

import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

/**
 * 성능 측정 결과를 담는 클래스
 */
public class PerformanceMeasurement {

    private static final Logger log = LoggerFactory.getLogger(PerformanceMeasurement.class);

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
        log.info("=".repeat(80));
        log.info("Performance Test: {}", testName);
        log.info("=".repeat(80));
        log.info(String.format("⏱️  Total Execution Time : %,d ms", executionTimeMs));
        log.info(String.format("🔍 Total Query Count    : %,d queries", queryCount));
        log.info(String.format("⚡ Max Query Time       : %,d ms", queryExecutionMaxTime));
        log.info(String.format("🔌 DB Connection Count  : %,d connections", connectionCount));
        log.info("=".repeat(80));
    }

    /**
     * 두 측정 결과 비교 출력
     */
    public static void compareResults(PerformanceMeasurement before,
                                      PerformanceMeasurement after) {
        log.info("=".repeat(80));
        log.info("Performance Comparison: {} vs {}", before.testName, after.testName);
        log.info("=".repeat(80));

        // 실행 시간 비교
        long timeDiff = before.executionTimeMs - after.executionTimeMs;
        double timeImprovement = ((double) timeDiff / before.executionTimeMs) * 100;
        log.info("⏱️  Execution Time");
        log.info(String.format("   Before: %,d ms", before.executionTimeMs));
        log.info(String.format("   After : %,d ms", after.executionTimeMs));
        log.info(String.format("   Diff  : %,d ms (%.1f%% %s)",
            Math.abs(timeDiff),
            Math.abs(timeImprovement),
            timeImprovement > 0 ? "FASTER ⚡" : "SLOWER ⚠️"));
        log.info(""); // Empty line for spacing

        // 쿼리 수 비교
        long queryDiff = before.queryCount - after.queryCount;
        double queryImprovement = before.queryCount > 0
            ? ((double) queryDiff / before.queryCount) * 100
            : 0;
        log.info("🔍 Query Count");
        log.info(String.format("   Before: %,d queries", before.queryCount));
        log.info(String.format("   After : %,d queries", after.queryCount));
        log.info(String.format("   Diff  : %,d queries (%.1f%% %s)",
            Math.abs(queryDiff),
            Math.abs(queryImprovement),
            queryImprovement > 0 ? "REDUCED ✅" : "INCREASED ⚠️"));
        log.info(""); // Empty line for spacing

        // 최대 쿼리 시간 비교
        long maxQueryDiff = before.queryExecutionMaxTime - after.queryExecutionMaxTime;
        log.info("⚡ Max Query Time");
        log.info(String.format("   Before: %,d ms", before.queryExecutionMaxTime));
        log.info(String.format("   After : %,d ms", after.queryExecutionMaxTime));
        log.info(String.format("   Diff  : %,d ms", Math.abs(maxQueryDiff)));
        log.info(""); // Empty line for spacing

        // 종합 평가
        log.info("📊 Overall Assessment");
        if (timeImprovement > 0 && queryImprovement >= 0) {
            log.info("   ✅ PERFORMANCE IMPROVED!");
            log.info(String.format("   💡 %.1fx faster execution",
                (double) before.executionTimeMs / after.executionTimeMs));
        } else if (timeImprovement < -50) {
            log.info("   ⚠️  PERFORMANCE DEGRADED SIGNIFICANTLY");
        } else {
            log.info("   ⚖️  PERFORMANCE SIMILAR");
        }

        log.info("=".repeat(80));
    }

    // Getters
    public String getTestName() { return testName; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    public long getQueryCount() { return queryCount; }
    public long getQueryExecutionMaxTime() { return queryExecutionMaxTime; }
    public long getConnectionCount() { return connectionCount; }
}
