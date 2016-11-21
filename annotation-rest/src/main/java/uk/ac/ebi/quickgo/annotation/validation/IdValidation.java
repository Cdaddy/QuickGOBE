package uk.ac.ebi.quickgo.annotation.validation;

import com.google.common.base.Preconditions;
import java.util.Objects;

/**
 * @author Tony Wardell
 * Date: 10/11/2016
 * Time: 13:41
 * Created with IntelliJ IDEA.
 */
class IdValidation {

    private static final String DELIMITER = ":";

    public static String db(final String idWithDb) {
        Preconditions.checkArgument(Objects.nonNull(idWithDb), "The id should not be null");
        Preconditions.checkArgument(idWithDb.contains(DELIMITER), "The id should contain the delimiter %s", DELIMITER);
        return idWithDb.substring(0, idWithDb.indexOf(":")).toLowerCase().trim();
    }

    static String id(final String idWithDb){
        Preconditions.checkArgument(Objects.nonNull(idWithDb), "The id should not be null");
        Preconditions.checkArgument(idWithDb.contains(DELIMITER), "The id should contain the delimiter %s", DELIMITER);
        return idWithDb.substring(idWithDb.indexOf(":") + 1).trim();
    }
}
