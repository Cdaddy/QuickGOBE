package uk.ac.ebi.quickgo.geneproduct.common;

import java.util.List;
import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.repository.SolrCrudRepository;

import static uk.ac.ebi.quickgo.geneproduct.common.GeneProductFields.*;

/**
 * Gene product repository interface exposing methods for performing searches over its contents.
 * <p>
 * See here how to change:
 * https://github.com/spring-projects/spring-data-solr/blob/master/README.md
 *
 * @author Ricardo Antunes; Tony Wardell
 */
public interface GeneProductRepository extends SolrCrudRepository<GeneProductDocument, String> {

    @Query(value = ID + ":?0",
            fields = {ID, DATABASE, NAME, SYMBOL, SYNONYM, TYPE, TAXON_ID, DATABASE_SUBSET,
                    COMPLETE_PROTEOME, REFERENCE_PROTEOME, IS_ISOFORM, IS_ANNOTATED, PARENT_ID})
    List<GeneProductDocument> findById(List<String> ids);

    List<GeneProductDocument> findByTargetSet(String name);
}
