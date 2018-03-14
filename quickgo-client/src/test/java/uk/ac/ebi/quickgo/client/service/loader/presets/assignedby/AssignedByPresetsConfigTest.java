package uk.ac.ebi.quickgo.client.service.loader.presets.assignedby;

import uk.ac.ebi.quickgo.client.service.loader.presets.ff.RawNamedPreset;

import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.item.ItemProcessor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * @author Tony Wardell
 * Date: 02/08/2017
 * Time: 17:06
 * Created with IntelliJ IDEA.
 */
public class AssignedByPresetsConfigTest {

    private AssignedByPresetsConfig assignedByPresetsConfig;

    @Before
    public void setup() {
        assignedByPresetsConfig = new AssignedByPresetsConfig();
    }

    @Test
    public void preventDuplicates() throws Exception {
        final ItemProcessor<RawNamedPreset, RawNamedPreset> itemProcessor =
                assignedByPresetsConfig.duplicateChecker();
        RawNamedPreset rawNamedPreset1 = new RawNamedPreset();
        rawNamedPreset1.name = "AgBase";
        RawNamedPreset rawNamedPreset2 = new RawNamedPreset();
        rawNamedPreset2.name = "AspGD";
        RawNamedPreset rawNamedPreset3 = new RawNamedPreset();
        rawNamedPreset3.name = "ASPGD";
        RawNamedPreset rawNamedPreset4 = new RawNamedPreset();
        rawNamedPreset4.name = "Alzheimers_University_of_Toronto";

        assertThat(itemProcessor.process(rawNamedPreset1), notNullValue());
        assertThat(itemProcessor.process(rawNamedPreset2), notNullValue());
        assertThat(itemProcessor.process(rawNamedPreset3), nullValue());
        assertThat(itemProcessor.process(rawNamedPreset4), notNullValue());
    }
}
