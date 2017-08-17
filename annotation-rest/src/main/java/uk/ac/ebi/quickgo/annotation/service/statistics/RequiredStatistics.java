package uk.ac.ebi.quickgo.annotation.service.statistics;

import uk.ac.ebi.quickgo.annotation.common.AnnotationFields;
import uk.ac.ebi.quickgo.rest.search.AggregateFunction;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static uk.ac.ebi.quickgo.annotation.service.statistics.RequiredStatisticType.statsType;

/**
 * Represents the required statistics that must be shown about annotations. Comprises
 * a list of {@link RequiredStatistic} entities, each of which captures particular details
 * of the annotations over which they have been calculated.
 *
 * The {@link RequiredStatistics} include statistics over annotations and gene products. Within each
 * statistic, by default of 10 items will be displayed for each statistic type (e.g., evidences, taxons, etc.),
 * except for GO terms, which by default displayed 200 ids.
 *
 * Created 16/08/17
 * @author Edd
 */
public class RequiredStatistics {
    static final int DEFAULT_GO_TERM_LIMIT = 200;
    private static final List<RequiredStatisticType> STATS_TYPES;
    private static final String ANNOTATION = "annotation";
    private static final String GENE_PRODUCT = "geneProduct";

    static {
        STATS_TYPES = asList(
                statsType(AnnotationFields.Facetable.GO_ID, DEFAULT_GO_TERM_LIMIT),
                statsType(AnnotationFields.Facetable.TAXON_ID),
                statsType(AnnotationFields.Facetable.REFERENCE),
                statsType(AnnotationFields.Facetable.EVIDENCE_CODE),
                statsType(AnnotationFields.Facetable.ASSIGNED_BY),
                statsType(AnnotationFields.Facetable.GO_ASPECT)
        );
    }

    private final List<RequiredStatistic> requiredStats;

    RequiredStatistics(StatisticsTypeConfigurer statsConfigurer) {
        List<RequiredStatistic> requests = asList(annotationStats(), geneProductStats());
        statsConfigurer.configureStatsRequests(requests);
        requiredStats = Collections.unmodifiableList(requests);
    }

    public List<RequiredStatistic> getStats() {
        return requiredStats;
    }
    
    private RequiredStatistic annotationStats() {
        return new RequiredStatistic(ANNOTATION, AnnotationFields.Facetable.ID, AggregateFunction
                .COUNT.getName(), STATS_TYPES);
    }

    private RequiredStatistic geneProductStats() {
        return new RequiredStatistic(GENE_PRODUCT, AnnotationFields.Facetable.GENE_PRODUCT_ID,
                AggregateFunction.UNIQUE.getName(), STATS_TYPES);
    }
}
