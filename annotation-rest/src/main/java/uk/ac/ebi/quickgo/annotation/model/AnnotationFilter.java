package uk.ac.ebi.quickgo.annotation.model;

import uk.ac.ebi.quickgo.annotation.common.document.AnnotationFields;
import uk.ac.ebi.quickgo.rest.controller.ControllerValidationHelper;
import uk.ac.ebi.quickgo.rest.controller.ControllerValidationHelperImpl;
import uk.ac.ebi.quickgo.rest.search.query.FilterProvider;
import uk.ac.ebi.quickgo.rest.search.query.PrototypeFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * A data structure for the annotation filtering parameters passed in from the client.
 * Here are the list of parameters filtering will require. The values shown are the ones the FE currently uses
 * Nearly all the parameters can take multiple values, separated by commas. These types are named as plural.
 * Exceptions exists however
 *
 * Once the comma separated values have been set, then turn then into an object (PrototypeFilter) that
 * encapsulates the list and solr field name to use for that argument.
 *
 * @author Tony Wardell
 * Date: 25/04/2016
 * Time: 11:23
 * Created with IntelliJ IDEA.
 */
public class AnnotationFilter implements FilterProvider{

    public static final String DEFAULT_ENTRIES_PER_PAGE = "25";
    private static final String DEFAULT_PAGE_NUMBER = "1";
    private static final int MAX_PAGE_RESULTS = 100;


    //Non-data parameters
    private String limit = DEFAULT_ENTRIES_PER_PAGE;
    private String page = DEFAULT_PAGE_NUMBER;


    private final List<PrototypeFilter> prototypeFilters = new ArrayList<>();

    //todo @Autowired
    private final ControllerValidationHelper validationHelper = new ControllerValidationHelperImpl(MAX_PAGE_RESULTS);


    /**
     * After filters have been loaded, ensure values are valid and defaults are in place.
     */
    public void validation() {

        validationHelper.validateRequestedResults(Integer.parseInt(limit));
    }

    // E.g. ASPGD,Agbase,..
    public void setAssignedby(String assignedby) {

        if (!isNullOrEmpty(assignedby)) {
            prototypeFilters.add(PrototypeFilter.create(AnnotationFields.ASSIGNED_BY, assignedby));
        }

    }

    public void setPage(String page) {
        this.page = page;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public int getLimit() {
        return Integer.parseInt(limit);
    }

    public int getPage() {
        return Integer.parseInt(page);
    }

    public Stream<PrototypeFilter> stream(){
        return prototypeFilters.stream();
    }


    /**
     * Create a prototype filter using the passed argument
     * @param solrName
     * @param argIncludingDelimiters
     * @return
     */



}
