package uk.ac.ebi.quickgo.rest.search.filter;

import com.google.common.base.Preconditions;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Holds the definitions of both the {@link InternalFilterExecutionConfig} and the
 * {@link ExternalFilterExecutionConfig}. It is then capable of providing information for all types of fields.
 *
 * @author Ricardo Antunes
 */
class GlobalFilterExecutionConfig implements FilterExecutionConfig {
    private final InternalFilterExecutionConfig internalExecutionConfig;
    private final ExternalFilterExecutionConfig externalExecutionConfig;

    @Autowired
    public GlobalFilterExecutionConfig(InternalFilterExecutionConfig internalExecutionConfig,
            ExternalFilterExecutionConfig externalExecutionConfig) {
        Preconditions.checkArgument(internalExecutionConfig != null, "InternalExecutionConfiguration cannot be null.");
        Preconditions.checkArgument(externalExecutionConfig != null, "ExternalExecutionConfiguration cannot be null.");

        this.internalExecutionConfig = internalExecutionConfig;
        this.externalExecutionConfig = externalExecutionConfig;
    }

    @Override public Optional<FieldExecutionConfig> getField(String fieldName) {
        Preconditions.checkArgument(fieldName != null && !fieldName.trim().isEmpty(),
                "Field name cannot be null or empty");

        Optional<FieldExecutionConfig> config = internalExecutionConfig.getField(fieldName);

        if (!config.isPresent()) {
            config = externalExecutionConfig.getField(fieldName);
        }

        return config;
    }
}