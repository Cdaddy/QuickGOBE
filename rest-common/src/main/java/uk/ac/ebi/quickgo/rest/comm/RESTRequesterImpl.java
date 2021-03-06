package uk.ac.ebi.quickgo.rest.comm;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.springframework.web.client.RestOperations;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created 31/05/16
 * @author Edd
 */
public class RESTRequesterImpl implements RESTRequester {
    private static final Logger LOGGER = getLogger(RESTRequesterImpl.class);
    private final String url;
    private final RestOperations restOperations;
    private Map<String, String> requestParameters;

    private RESTRequesterImpl(Builder builder) {
        this.url = builder.url;
        this.requestParameters = Collections.unmodifiableMap(builder.requestParameters);
        this.restOperations = builder.restOperations;
    }

    @Override
    public <T> CompletableFuture<T> get(Class<T> responseType) {
        return get(restOperations, responseType);
    }

    public static Builder newBuilder(RestOperations restOperations, String url) {
        return new Builder(restOperations, url);
    }

    <T> CompletableFuture<T> get(RestOperations template, Class<T> responseType) {
        return CompletableFuture.supplyAsync(() ->
                template.getForObject(url, responseType, requestParameters));
    }

    public static class Builder {

        private String url;
        private Map<String, String> requestParameters;
        private RestOperations restOperations;

        Builder(RestOperations restOperations, String url) {
            checkURL(url);
            Preconditions.checkArgument(restOperations != null, "RestOperations cannot be null");

            this.url = url;
            this.restOperations = restOperations;
            this.requestParameters = new LinkedHashMap<>();
        }

        public RESTRequesterImpl build() {
            return new RESTRequesterImpl(this);
        }

        public Builder resetURL(String url) {
            checkURL(url);

            this.url = url;
            return this;
        }

        public Builder addRequestParameter(String name, String value) {
            Preconditions.checkArgument(name != null && !name.trim().isEmpty(), "Name cannot be null or empty");
            Preconditions.checkArgument(value != null, "Value cannot be null");

            if (!requestParameters.containsKey(name)) {
                requestParameters.put(name, value);
            }

            return this;
        }

        private void checkURL(String url) {
            Preconditions.checkArgument(url != null && !url.trim().isEmpty(), "URL cannot be null or empty");
        }
    }

    @Override public String toString() {
        return "RESTRequesterImpl{" +
                "url='" + url + '\'' +
                ", restOperations=" + restOperations +
                ", requestParameters=" + requestParameters +
                '}';
    }
}
