package uk.ac.ebi.quickgo.client.service.loader.presets.reference;

import uk.ac.ebi.quickgo.client.model.presets.PresetItem;
import uk.ac.ebi.quickgo.client.model.presets.impl.CompositePresetImpl;
import uk.ac.ebi.quickgo.client.model.presets.impl.PresetItemBuilder;
import uk.ac.ebi.quickgo.client.service.loader.presets.LogStepListener;
import uk.ac.ebi.quickgo.client.service.loader.presets.PresetsCommonConfig;
import uk.ac.ebi.quickgo.client.service.loader.presets.ff.RawNamedPreset;
import uk.ac.ebi.quickgo.client.service.loader.presets.ff.RawNamedPresetValidator;
import uk.ac.ebi.quickgo.client.service.loader.presets.ff.SourceColumnsFactory;
import uk.ac.ebi.quickgo.client.service.loader.presets.ff.StringToRawNamedPresetMapper;

import java.util.Set;
import java.util.function.Function;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;

import static uk.ac.ebi.quickgo.client.service.loader.presets.PresetsConfig.SKIP_LIMIT;
import static uk.ac.ebi.quickgo.client.service.loader.presets.PresetsConfigHelper.compositeItemProcessor;
import static uk.ac.ebi.quickgo.client.service.loader.presets.PresetsConfigHelper.fileReader;
import static uk.ac.ebi.quickgo.client.service.loader.presets.PresetsConfigHelper.rawPresetMultiFileReader;
import static uk.ac.ebi.quickgo.client.service.loader.presets.ff.SourceColumnsFactory.Source.DB_COLUMNS;
import static uk.ac.ebi.quickgo.client.service.loader.presets.ff.SourceColumnsFactory.Source.REF_COLUMNS;

/**
 * Exposes the {@link Step} bean that is used to read and populate information relating to the reference preset data.
 *
 * Created 01/09/16
 * @author Edd
 */
@Configuration
@Import({PresetsCommonConfig.class})
public class ReferencePresetsConfig {
    public static final String CORE_REFERENCE_DB_LOADING_STEP_NAME = "GenericReferenceDBReadingStep";
    public static final String SPECIFIC_REFERENCE_LOADING_STEP_NAME = "SpecificReferenceReadingStep";
    private static final String REFERENCE_DB_DEFAULTS = "DOI,GO_REF,PMID,REACTOME";
    private static final String REFERENCE_SPECIFIC_DB_DEFAULTS =
            "0000037,0000038,0000043,0000045,0000039,0000040,0000044,0000046,0000003,0000041,0000020,0000002," +
                    "0000042,0000019,0000035,0000049,0000104,0000108";
    private static final RawNamedPreset INVALID_PRESET = null;
    private static final String GO_REF_FORMAT = "GO_REF:%s";

    @Value("#{'${reference.db.preset.source:}'.split(',')}")
    private Resource[] dbResources;
    @Value("${reference.db.preset.header.lines:1}")
    private int dbHeaderLines;
    @Value("#{'${reference.db.preset.defaults:" + REFERENCE_DB_DEFAULTS + "}'.split(',')}")
    private Set<String> dbDefaults;

    @Value("#{'${reference.specific.db.preset.source:}'.split(',')}")
    private Resource[] specificResources;
    @Value("${reference.db.preset.header.lines:1}")
    private int specificDBHeaderLines;
    @Value("#{'${reference.specific.db.preset.defaults:" + REFERENCE_SPECIFIC_DB_DEFAULTS + "}'.split(',')}")
    private Set<String> specificDBDefaults;

    @Bean
    public Step referenceGenericDbStep(
            StepBuilderFactory stepBuilderFactory,
            Integer chunkSize,
            CompositePresetImpl presets) {
        FlatFileItemReader<RawNamedPreset> itemReader = fileReader(fieldSetMapper(DB_COLUMNS));
        itemReader.setLinesToSkip(dbHeaderLines);

        return stepBuilderFactory.get(CORE_REFERENCE_DB_LOADING_STEP_NAME)
                .<RawNamedPreset, RawNamedPreset>chunk(chunkSize)
                .faultTolerant()
                .skipLimit(SKIP_LIMIT)
                .<RawNamedPreset>reader(rawPresetMultiFileReader(dbResources, itemReader))
                .processor(compositeItemProcessor(
                        rawPresetValidator(),
                        rawPresetFilter(dbDefaults)))
                .writer(rawPresetWriter(
                        presets,
                        aRawPresetItem -> PresetItemBuilder.createWithName(aRawPresetItem.name)
                                .withDescription(aRawPresetItem.description)
                                .build()))
                .listener(new LogStepListener())
                .build();
    }

    @Bean
    public Step referenceSpecificDbStep(
            StepBuilderFactory stepBuilderFactory,
            Integer chunkSize,
            CompositePresetImpl presets) {
        FlatFileItemReader<RawNamedPreset> itemReader = fileReader(fieldSetMapper(REF_COLUMNS));
        itemReader.setLinesToSkip(dbHeaderLines);

        return stepBuilderFactory.get(SPECIFIC_REFERENCE_LOADING_STEP_NAME)
                .<RawNamedPreset, RawNamedPreset>chunk(chunkSize)
                .faultTolerant()
                .skipLimit(SKIP_LIMIT)
                .<RawNamedPreset>reader(rawPresetMultiFileReader(specificResources, itemReader))
                .processor(compositeItemProcessor(
                        rawPresetValidator(),
                        rawPresetFilter(specificDBDefaults)))
                .writer(rawPresetWriter(
                        presets,
                        aRawPresetItem -> PresetItemBuilder.createWithName(buildGORefID(aRawPresetItem.name))
                                .withDescription(aRawPresetItem.description)
                                .withRelevancy(aRawPresetItem.relevancy)
                                .build()))
                .listener(new LogStepListener())
                .build();
    }

    private ItemWriter<RawNamedPreset> rawPresetWriter(
            CompositePresetImpl presets,
            Function<RawNamedPreset, PresetItem> presetItemSupplier) {
        return rawItemList -> rawItemList.forEach(rawItem -> {
            presets.addPreset(CompositePresetImpl.PresetType.REFERENCES, presetItemSupplier.apply(rawItem));
        });
    }

    private String buildGORefID(String name) {
        return String.format(GO_REF_FORMAT, name);
    }

    private ItemProcessor<RawNamedPreset, RawNamedPreset> rawPresetFilter(Set<String> validPresetNames) {
        return rawNamedPreset ->
                validPresetNames.contains(rawNamedPreset.name) ? rawNamedPreset : INVALID_PRESET;
    }

    private FieldSetMapper<RawNamedPreset> fieldSetMapper(SourceColumnsFactory.Source source) {
        return new StringToRawNamedPresetMapper(SourceColumnsFactory.createFor(source));
    }

    private ItemProcessor<RawNamedPreset, RawNamedPreset> rawPresetValidator() {
        return new RawNamedPresetValidator();
    }
}
