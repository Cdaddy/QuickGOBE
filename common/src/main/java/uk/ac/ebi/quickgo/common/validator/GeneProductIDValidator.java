package uk.ac.ebi.quickgo.common.validator;

import uk.ac.ebi.quickgo.common.loader.DbXRefLoader;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author Tony Wardell
 * Date: 14/06/2016
 * Time: 13:32
 * Created with IntelliJ IDEA.
 */
public class GeneProductIDValidator implements ConstraintValidator<GeneProductIDList,String>{

    //@Value("${geneproduct.db.xref.valid.regexes}") //todo define in common
    private String xrefValidationRegexFile="src/test/resources/DB_XREFS_ENTITIES.dat.gz"; //todo remove
    Predicate<String> idValidator;

    @Override public void initialize(GeneProductIDList constraintAnnotation) {
        GeneProductDbXRefIDFormats
                dbXrefEntities = GeneProductDbXRefIDFormats.createWithData(geneProductLoader().load());
        idValidator = dbXrefEntities::isValidId;
    }

    @Override public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        List invalidGeneProdIDs = Arrays.stream(s.split(",")).filter(idValidator.negate()).collect(Collectors.toList());

        return invalidGeneProdIDs.size() == 0;
    }
    private DbXRefLoader geneProductLoader() {
        return new DbXRefLoader(this.xrefValidationRegexFile);
    }
}
