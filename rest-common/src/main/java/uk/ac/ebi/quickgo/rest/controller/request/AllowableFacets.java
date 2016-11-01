package uk.ac.ebi.quickgo.rest.controller.request;

import uk.ac.ebi.quickgo.common.SearchableField;

import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {AllowableFacetsImpl.class})
@Documented
public @interface AllowableFacets {
    String DEFAULT_ERROR_MESSAGE = "The facet parameter contains invalid values: %s";

    String message() default DEFAULT_ERROR_MESSAGE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<? extends SearchableField> searchableFields();
}
