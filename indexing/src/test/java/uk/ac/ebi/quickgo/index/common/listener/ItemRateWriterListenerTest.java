package uk.ac.ebi.quickgo.index.common.listener;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.quickgo.index.common.listener.ItemRateWriterListener.WRITE_RATE_DOCUMENT_INTERVAL;

/**
 * Created 27/04/16
 * @author Edd
 */
@RunWith(MockitoJUnitRunner.class)
public class ItemRateWriterListenerTest {

    private Instant start;
    private ItemRateWriterListener<Object> itemRateWriterListener;

    @Mock
    private List<Object> mockedWrittenDocList;

    @Before
    public void setUp() {
        start = Instant.now();
        itemRateWriterListener = new ItemRateWriterListener<>(start);
    }

    @Test
    public void computesZeroDocsPerSecond() {
        Instant now = start.plusSeconds(10);
        Duration duration = Duration.between(start, now);
        float docsPerSecond = itemRateWriterListener.getItemsPerSecond(duration, new AtomicInteger(0));
        assertThat(docsPerSecond, is(0.0F));
    }

    @Test
    public void computes1DocPerSecond() {
        Instant now = start.plusSeconds(44);
        Duration duration = Duration.between(start, now);
        float docsPerSecond = itemRateWriterListener.getItemsPerSecond(duration, new AtomicInteger(44));
        assertThat(docsPerSecond, is(1.0F));
    }

    @Test
    public void computes8DocPerSecond() {
        Instant now = start.plusSeconds(5);
        Duration duration = Duration.between(start, now);
        float docsPerSecond = itemRateWriterListener.getItemsPerSecond(duration, new AtomicInteger(40));
        assertThat(docsPerSecond, is(8.0F));
    }

    @Test
    public void demonstrateGoingBackInTimeDoesntKillGetItemsPerSecond() {
        Instant now = start.minusSeconds(5); // minus (hopefully this doesn't happen, Marty)
        Duration duration = Duration.between(start, now);
        float docsPerSecond = itemRateWriterListener.getItemsPerSecond(duration, new AtomicInteger(40));
        assertThat(docsPerSecond, is(-8.0F));
    }

    @Test
    public void computesRateAfterOneWrite() throws Exception {
        int numDocs = 40;
        Instant fiveSecsAfterStart = start.plusSeconds(5);

        when(mockedWrittenDocList.size()).thenReturn(numDocs);

        itemRateWriterListener.afterWrite(mockedWrittenDocList);
        ItemRateWriterListener.StatsInfo statsInfo = itemRateWriterListener.computeWriteRateStats(fiveSecsAfterStart);

        System.out.println(statsInfo.toString());
        assertThat(statsInfo.totalSeconds, is(5L));
        assertThat(statsInfo.totalWriteCount, is(numDocs));
    }

    @Test
    public void computesRateAfterMultipleWrites() throws Exception {
        int tenDocs = 10;
        long twoSeconds = 2L;
        Instant twoSecsAfterStart = start.plusSeconds(twoSeconds);

        when(mockedWrittenDocList.size()).thenReturn(tenDocs);
        itemRateWriterListener.afterWrite(mockedWrittenDocList);

        // add lots of docs to trigger a new delta
        when(mockedWrittenDocList.size()).thenReturn(WRITE_RATE_DOCUMENT_INTERVAL);
        itemRateWriterListener.afterWrite(mockedWrittenDocList);

        when(mockedWrittenDocList.size()).thenReturn(tenDocs);
        itemRateWriterListener.afterWrite(mockedWrittenDocList);

        ItemRateWriterListener.StatsInfo statsInfo = itemRateWriterListener.computeWriteRateStats(twoSecsAfterStart);

        System.out.println(statsInfo);
        assertThat(statsInfo.deltaWriteCount, is(tenDocs));
        // do not test delta time, because it internally uses
        assertThat(statsInfo.totalSeconds, is(twoSeconds));
        assertThat(statsInfo.totalWriteCount, is(tenDocs + tenDocs + WRITE_RATE_DOCUMENT_INTERVAL));
    }
}