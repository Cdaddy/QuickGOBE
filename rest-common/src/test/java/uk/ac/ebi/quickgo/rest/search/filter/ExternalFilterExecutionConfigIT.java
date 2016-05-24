package uk.ac.ebi.quickgo.rest.search.filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static uk.ac.ebi.quickgo.rest.search.filter.FieldExecutionConfig.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ExternalFilterExecutionConfigIT.TestApplication.class,
        initializers = ConfigFileApplicationContextInitializer.class)
public class ExternalFilterExecutionConfigIT {
    @Configuration
    @ComponentScan
    @EnableConfigurationProperties
    public static class TestApplication {
        public static void main(String[] args) {
            SpringApplication.run(TestApplication.class);
        }
    }

    @Autowired
    private ExternalFilterExecutionConfig externalFilterExecutionConfig;

    @Test
    public void yamlPropertiesLoadedCorrectlyIntoBean() {
        List<FieldExecutionConfig> fieldConfigs = externalFilterExecutionConfig.getFields();

        FieldExecutionConfig aspectField = createStubAspectField();
        FieldExecutionConfig usageField = createStubUsageField();

        assertThat(fieldConfigs, hasSize(2));
        assertThat(fieldConfigs, containsInAnyOrder(aspectField, usageField));
    }

    private FieldExecutionConfig createStubAspectField() {
        Map<String, String> map = new HashMap<>();
        map.put("fromTable", "ontology");
        map.put("fromAttribute", "id");
        map.put("toTable", "annotation");
        map.put("toAttribute", "id");

        return FilterUtil.createExecutionConfigWithProps("aspect", ExecutionType.JOIN, map);
    }

    private FieldExecutionConfig createStubUsageField() {
        Map<String, String> map = new HashMap<>();
        map.put("ip", "123.456.789");
        map.put("endpoint", "endpoint");

        return FilterUtil.createExecutionConfigWithProps("usage", ExecutionType.REST_COMM, map);
    }
}