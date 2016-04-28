package uk.ac.ebi.quickgo.index.common.listener;

import uk.ac.ebi.quickgo.index.common.SolrServerWriter;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.springframework.batch.core.ItemWriteListener;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Class used to log statistics of the rate of writing. The primary purpose is to provide
 * a basis for reviewing, comparing and tuning batch run performances.
 *
 * Created 27/04/16
 * @author Edd
 */
public class ItemRateWriterListener<O> implements ItemWriteListener<O> {
    private static final Logger LOGGER = getLogger(SolrServerWriter.class);
    static final int WRITE_RATE_DOCUMENT_INTERVAL = 10000;
    private final Instant startOfWriting;
    private AtomicInteger totalWriteCount = new AtomicInteger(0);
    private AtomicInteger deltaWriteCount = new AtomicInteger(0);
    private Instant startOfDelta;

    public ItemRateWriterListener(Instant now) {
        startOfWriting = startOfDelta = now;
    }

    @Override public void beforeWrite(List<? extends O> list) {

    }

    @Override public void afterWrite(List<? extends O> list) {
        deltaWriteCount.addAndGet(list.size());

        if (deltaWriteCount.get() >= WRITE_RATE_DOCUMENT_INTERVAL) {
            LOGGER.info(computeWriteRateStats(Instant.now()).toString());
            resetDelta();
        }
    }

    @Override public void onWriteError(Exception e, List<? extends O> list) {

    }

    /**
     * Computes the rate of items written per second, from instance creation time
     * to a specified time-point, {@code now}.
     *
     * @param duration the duration for which the rate should be computed
     * @param writeCount the number of items written
     * @return a floating point number representing the rate of writing
     */
    float getItemsPerSecond(Duration duration, AtomicInteger writeCount) {
        return (float) writeCount.get() / duration.get(ChronoUnit.SECONDS);
    }

    /**
     * Compute writing rate statistics and return a formatted {@link String},
     * ready for printing.
     *
     * @param now the time point at which the statistics should be computed
     * @return a formatted {@link String} representing the write rate statistics
     */
    StatsInfo computeWriteRateStats(Instant now) {
        totalWriteCount.addAndGet(deltaWriteCount.get());

        StatsInfo statsInfo = new StatsInfo();
        statsInfo.totalWriteCount = totalWriteCount.get();
        statsInfo.totalSeconds = Duration.between(startOfWriting, now).getSeconds();
        statsInfo.deltaWriteCount = deltaWriteCount.get();
        statsInfo.deltaSeconds = Duration.between(startOfDelta, now).getSeconds();

        return statsInfo;
    }

    private void resetDelta() {
        deltaWriteCount.set(0);
        startOfDelta = Instant.now();
    }

    static class StatsInfo {
        private static final int SECONDS_IN_AN_HOUR = 3600;

        int deltaWriteCount;
        long deltaSeconds;

        int totalWriteCount;
        long totalSeconds;

        @Override public String toString() {
            float deltaDocsPerSecond = (float) deltaWriteCount / deltaSeconds;
            float totalDocsPerSecond = (float) totalWriteCount / totalSeconds;
            return
                            "\tWrite statistics {\n" +
                            "\t\tLatest delta:\n" +
                            String.format("\t\t\t# docs\t\t:\t%d\n", deltaWriteCount) +
                            String.format("\t\t\ttime (sec)\t:\t%d\n", deltaSeconds) +
                            String.format("\t\t\tdocs/sec\t:\t%.2f\n", deltaDocsPerSecond) +
                            String.format("\t\t\tdocs/hour\t:\t%.0f\t(projected from docs/sec)\n", deltaDocsPerSecond
                                    * SECONDS_IN_AN_HOUR) +
                            "\t\tOverall:\n" +
                            String.format("\t\t\t# docs\t\t:\t%d\n", totalWriteCount) +
                            String.format("\t\t\ttime (sec)\t:\t%d\n", totalSeconds) +
                            String.format("\t\t\tdocs/sec\t:\t%.2f\n", totalDocsPerSecond) +
                            String.format("\t\t\tdocs/hour\t:\t%.0f\t(projected from docs/sec)\n", totalDocsPerSecond *
                            SECONDS_IN_AN_HOUR) +
                            "\t}\n";
        }
    }


}
