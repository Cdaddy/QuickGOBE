package uk.ac.ebi.quickgo.rest.search.query;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import org.springframework.validation.Errors;

/**
 * Holds the data required to specify a filter value
 *
 * @author Tony Wardell
 * Date: 03/05/2016
 * Time: 10:25
 * Created with IntelliJ IDEA.
 */
public class PrototypeFilter {

    private static final String COMMA = ",";
    private String solrName;
    private List<String> args;
    private Validator<String> validator;

    public String getSolrName() {
        return solrName;
    }

    public List<String> getArgs() {
        return args;
    }

    public static final PrototypeFilter create(String solrName, String argIncludingDelimiters, Validator<String> validator ){
        PrototypeFilter prototypeFilter = new PrototypeFilter();
        prototypeFilter.solrName = solrName;
        prototypeFilter.args = Arrays.asList(argIncludingDelimiters.split(COMMA));
        prototypeFilter.validator = validator;
        return prototypeFilter;
    }

    /**
     *
     * Test each argument in the list to see if its valid
     */
    public void validate(){
        args.stream().forEach(a -> validator.validate(a));
    }
}
