package uk.ac.ebi.quickgo.search.miscellaneous;

import uk.ac.ebi.quickgo.search.ontology.DocumentMocker;
import uk.ac.ebi.quickgo.search.ontology.OntologySearchEngine;
import uk.ac.ebi.quickgo.solr.model.miscellaneous.SolrMiscellaneous;
import uk.ac.ebi.quickgo.solr.model.ontology.SolrTerm;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.ac.ebi.quickgo.search.miscellaneous.DocumentMocker.Stats.createStats;
import static uk.ac.ebi.quickgo.search.miscellaneous.DocumentMocker.XRefDB.createXRefDB;
import static uk.ac.ebi.quickgo.search.ontology.DocumentMocker.Relation.createRelation;

/**
 * This class is used to show which queries are necessary to successfully search the index, e.g.,
 * how to search for a GO term.
 * <p/>
 * Changes made to the ontology core's schema.xml are instantly reflected by the tests
 * defined in this class.
 * <p/>
 * Example tests: queries sent by {@link TermRetrieval} can be tested here first, before needing
 * to move libraries/configurations re-indexing, then testing server-side.
 * <p/>
 * Please use {@link uk.ac.ebi.quickgo.search.miscellaneous.DocumentMocker} to add documents to the search engine, before trying
 * to search for them.
 *
 * @see DocumentMocker
 *
 * Created 02/11/15
 * @author Edd
 */
public class MiscellaneousSearchIT {
    @ClassRule
    public static final MiscellaneousSearchEngine searchEngine = new MiscellaneousSearchEngine();

    /**
     * Clean index before each test
     */
    @Before
    public void cleanIndex() {
        searchEngine.removeAllDocuments();
    }

    /**
     * See {@link MiscellaneousUtilImpl}
     */
    @Test
    public void shouldFindRelationParentWithExactQuery() {
        SolrMiscellaneous stats = createXRefDB();
        stats.setXrefAbbreviation("bioPIXIE_MEFIT");
        searchEngine.indexDocument(stats);

        QueryResponse queryResponse = searchEngine.getQueryResponse("docType:"+stats.getDocType()+" AND " +
                "xrefAbbreviation:bioPIXIE_MEFIT");
        assertThat(queryResponse.getResults().size(), is(1));
    }

}
