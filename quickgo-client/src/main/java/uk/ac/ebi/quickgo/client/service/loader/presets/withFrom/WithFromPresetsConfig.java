package uk.ac.ebi.quickgo.client.service.loader.presets.withFrom;

import uk.ac.ebi.quickgo.client.model.presets.impl.CompositePresetImpl;
import uk.ac.ebi.quickgo.client.model.presets.impl.PresetItemBuilder;
import uk.ac.ebi.quickgo.client.service.loader.presets.LogStepListener;
import uk.ac.ebi.quickgo.client.service.loader.presets.PresetsCommonConfig;
import uk.ac.ebi.quickgo.client.service.loader.presets.ff.RawNamedPreset;
import uk.ac.ebi.quickgo.client.service.loader.presets.ff.RawNamedPresetValidator;
import uk.ac.ebi.quickgo.client.service.loader.presets.ff.SourceColumnsFactory;
import uk.ac.ebi.quickgo.client.service.loader.presets.ff.StringToRawNamedPresetMapper;

import java.util.List;
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

/**
 * Exposes the {@link Step} bean that is used to read and populate information relating to the with/from preset data.
 *
 * Created 01/09/16
 * @author Edd
 */
@Configuration
@Import({PresetsCommonConfig.class})
public class WithFromPresetsConfig {
    public static final String WITH_FROM_DB_LOADING_STEP_NAME = "WithFromDBReadingStep";
    private static final String DEFAULTS =
            "AGI_LocusCode,AspGD,CGD,CHEBI,EC,ECK,ECO,ECOGENE,EMBL,EchoBASE,EcoliWiki,Ensembl,EnsemblFungi," +
                    "EnsemblPlants,FB,GB,GO,GR,GR_PROTEIN,GR_protein,GenBank,GeneDB,HAMAP,HGNC,IntAct,InterPro," +
                    "JCVI,JCVI_CMR,JCVI_GenProp,KEGG,KEGG_LIGAND,MGI,MaizeGDB,MaizeGDB_Locus,NCBI,NCBI_GP,NCBI_Gene," +
                    "NCBI_gi,PANTHER,PDB,PIR,PR,Pfam,PomBase,PubChem_Compound,PubChem_Substance,RGD,RGDID,RNAcentral," +
                    "RefSeq,SGD,TAIR,TIGR,TIGR_GenProp,UniPathway,UniProt,UniProtKB,UniProtKB,UniProtKB,UniRule," +
                    "WB,ZFIN,dictyBase,protein_id";

    private static final RawNamedPreset INVALID_PRESET = null;

    @Value("#{'${withfrom.db.preset.source:}'.split(',')}")
    private Resource[] resources;
    @Value("${withfrom.db.preset.header.lines:1}")
    private int headerLines;
    @Value("#{'${withfrom.db.preset.defaults:" + DEFAULTS + "}'.split(',')}")
    private List<String> defaults;

    @Bean
    public Step withFromDbStep(
            StepBuilderFactory stepBuilderFactory,
            Integer chunkSize,
            CompositePresetImpl presets) {
        FlatFileItemReader<RawNamedPreset> itemReader = fileReader(rawPresetFieldSetMapper());
        itemReader.setLinesToSkip(headerLines);

        return stepBuilderFactory.get(WITH_FROM_DB_LOADING_STEP_NAME)
                .<RawNamedPreset, RawNamedPreset>chunk(chunkSize)
                .faultTolerant()
                .skipLimit(SKIP_LIMIT)
                .<RawNamedPreset>reader(rawPresetMultiFileReader(resources, itemReader))
                .processor(compositeItemProcessor(
                        rawPresetValidator(),
                        setPresetRelevancy(defaults)))
                .writer(rawPresetWriter(presets))
                .listener(new LogStepListener())
                .build();
    }

    /**
     * Write the list of {@link RawNamedPreset}s to the {@link CompositePresetImpl}
     * @param presets the presets to write to
     * @return the corresponding {@link ItemWriter}
     */
    private ItemWriter<RawNamedPreset> rawPresetWriter(CompositePresetImpl presets) {
        return rawItemList -> rawItemList.forEach(rawItem -> {
            presets.addPreset(CompositePresetImpl.PresetType.WITH_FROM,
                    PresetItemBuilder.createWithName(rawItem.name)
                            .withDescription(rawItem.description)
                            .withRelevancy(rawItem.relevancy)
                            .build());
        });
    }

    private ItemProcessor<RawNamedPreset, RawNamedPreset> setPresetRelevancy(List<String> validPresetNames) {
        return rawNamedPreset -> {
            int relevancy = validPresetNames.indexOf(rawNamedPreset.name);
            if (relevancy >= 0) {
                rawNamedPreset.relevancy = relevancy;
                return rawNamedPreset;
            } else {
                return INVALID_PRESET;
            }
        };
    }

    private FieldSetMapper<RawNamedPreset> rawPresetFieldSetMapper() {
        return new StringToRawNamedPresetMapper(SourceColumnsFactory.createFor(DB_COLUMNS));
    }

    private ItemProcessor<RawNamedPreset, RawNamedPreset> rawPresetValidator() {
        return new RawNamedPresetValidator();
    }
}
