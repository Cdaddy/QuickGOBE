package uk.ac.ebi.quickgo.rest.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import org.slf4j.Logger;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created 18/04/16
 * @author Edd
 */
public class ControllerValidationHelperImpl implements ControllerValidationHelper {

    private static final Logger LOGGER = getLogger(ControllerValidationHelperImpl.class);
    public static final int MAX_PAGE_RESULTS = 100;

    private static final String COMMA = ",";
    private final Predicate<String> validIdCondition;
    private final Predicate<Integer> validNumberOfPageResults;

    public ControllerValidationHelperImpl(int maxPageResults, Predicate<String> validIDCondition) {
        this.validNumberOfPageResults = pageResults -> pageResults <= maxPageResults;
        this.validIdCondition = validIDCondition;
    }

    public ControllerValidationHelperImpl(int maxPageResults) {
        this(
                maxPageResults,
                anyId -> true);
    }

    public ControllerValidationHelperImpl() {
        this(
                MAX_PAGE_RESULTS,
                anyId -> true);
    }

    @Override public List<String> validateCSVIds(String ids) {
        List<String> idList = csvToList(ids);

        validateRequestedResults(idList.size());

        idList.stream().filter(validIdCondition.negate())
                .forEach(badId -> {
                    String errorMessage = "Provided ID: '" + badId + "' is invalid";
                    LOGGER.error(errorMessage);
                    throw new IllegalArgumentException(errorMessage);
                });

        return idList;
    }

    @Override public void validateRequestedResults(int requestedResultsSize) {
        if (validNumberOfPageResults.negate().test(requestedResultsSize)) {
            String errorMessage = "Cannot retrieve retrieve the requested number of results. Must satisfy the " +
                    "Predicate: " + validNumberOfPageResults.toString() + ". Please consider using end-points that " +
                    "return paged results.";
            LOGGER.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Creates a list of items from a scalar representation of a list, in CSV format. If the
     * parameter is null, an empty list is returned.
     *
     * @param csv a CSV list of items
     * @return a list of values originally comprising the CSV input String
     */
    List<String> csvToList(String csv) {
        if (!isNullOrEmpty(csv)) {
            return Arrays.asList(csv.split(COMMA));
        } else {
            return Collections.emptyList();
        }
    }
}