package uk.ac.ebi.quickgo.annotation.model;

import uk.ac.ebi.quickgo.annotation.common.document.AnnotationFields;
import uk.ac.ebi.quickgo.rest.search.filter.RequestFilter;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

/**
 * A data structure for the annotation filtering parameters passed in from the client.
 *
 * Once the comma separated values have been set, then turn then into an object (SimpleFilter) that
 * encapsulates the list and solr field name to use for that argument.
 *
 * @author Tony Wardell
 * Date: 25/04/2016
 * Time: 11:23
 * Created with IntelliJ IDEA.
 */
public class AnnotationRequest {
    public static final int DEFAULT_ENTRIES_PER_PAGE = 25;
    public static final int MAX_ENTRIES_PER_PAGE = 100;

    private static final String COMMA = ",";
    private static final int DEFAULT_PAGE_NUMBER = 1;

    //Non-data parameters
    @Min(0)
    @Max(MAX_ENTRIES_PER_PAGE)
    private int limit = DEFAULT_ENTRIES_PER_PAGE;

    @Min(1)
    private int page = DEFAULT_PAGE_NUMBER;

    private final Map<String, String> filters = new HashMap<>();

    /**
     *  E.g. ASPGD,Agbase,..
     *  In the format assignedBy=ASPGD,Agbase
     */
    public void setAssignedBy(String assignedBy) {
        filters.put(AnnotationFields.ASSIGNED_BY, assignedBy);
    }

    @Pattern(regexp = "^[A-Za-z][A-Za-z\\-_]+(,[A-Za-z][A-Za-z\\-_]+)*")
    public String getAssignedBy() {
        return filters.get(AnnotationFields.ASSIGNED_BY);
    }

    /**
     * A list of with/from values, separated by commas
     * In the format withFrom=PomBase:SPBP23A10.14c,RGD:621207 etc
     * Users can supply just the id (e.g. PomBase) or id SPBP23A10.14c
     * @param withFrom
     */
    public void setWithFrom(String withFrom){
        filters.put(AnnotationFields.WITH_FROM_SEARCH, withFrom);
    }

    /**
     * Return a list of with/from values, separated by commas
     * @return
     */
    public  String getWithFrom(){
        return filters.get(AnnotationFields.WITH_FROM_SEARCH);
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }

    public int getPage() {
        return page;
    }

    public Stream<RequestFilter> convertToFilters() {
        return filters.entrySet().stream().map(filter -> new RequestFilter(filter.getKey(),
                splitFilterValues(filter.getValue())));
    }

    private String[] splitFilterValues(String values) {
        return values.split(COMMA);
    }
}
