package uk.ac.ebi.quickgo.rest.search.request.converter;

import uk.ac.ebi.quickgo.rest.search.query.QuickGOQuery;
import uk.ac.ebi.quickgo.rest.search.request.ClientRequest;
import uk.ac.ebi.quickgo.rest.search.request.config.RequestConfig;
import uk.ac.ebi.quickgo.rest.search.request.config.RequestConfigRetrieval;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.quickgo.rest.search.request.config.RequestConfig.ExecutionType.JOIN;
import static uk.ac.ebi.quickgo.rest.search.request.config.RequestConfig.ExecutionType.SIMPLE;
import static uk.ac.ebi.quickgo.rest.search.request.converter.JoinRequestConverter.FROM_ATTRIBUTE_NAME;
import static uk.ac.ebi.quickgo.rest.search.request.converter.JoinRequestConverter.FROM_TABLE_NAME;
import static uk.ac.ebi.quickgo.rest.search.request.converter.JoinRequestConverter.TO_ATTRIBUTE_NAME;
import static uk.ac.ebi.quickgo.rest.search.request.converter.JoinRequestConverter.TO_TABLE_NAME;

/**
 * Created 06/06/16
 * @author Edd
 */
@RunWith(MockitoJUnitRunner.class)
public class RequestConverterFactoryTest {
    @Mock
    private RequestConfigRetrieval requestConfigRetrievalMock;
    @Mock
    private RequestConfig requestConfigMock;

    private RequestConverterFactory converter;

    @Before
    public void setUp() {
        this.converter = new RequestConverterFactory(requestConfigRetrievalMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullConfigRetrievalThrowsException() {
        new RequestConverterFactory(null);
    }

    // simple request -> QuickGOQuery tests
    @Test
    public void createsQueryForCorrectlyConfiguredSimpleRequest() {
        String value = "valueX";
        String field = "fieldX";
        ClientRequest request = ClientRequest.newBuilder().addProperty(field, value).build();

        when(requestConfigRetrievalMock.getSignature(request.getSignature()))
               .thenReturn(Optional.of(requestConfigMock));
        when(requestConfigMock.getExecution()).thenReturn(SIMPLE);

        QuickGOQuery resultingQuery = converter.convert(request);
        QuickGOQuery expectedQuery = QuickGOQuery.createQuery(field, value);

        assertThat(resultingQuery, is(expectedQuery));
    }

    @Test(expected = IllegalStateException.class)
    public void missingSignatureInConfigForSimpleRequestCausesException() {
        String value = "valueX";
        String field = "fieldX";
        ClientRequest request = ClientRequest.newBuilder().addProperty(field, value).build();

        when(requestConfigRetrievalMock.getSignature(request.getSignature()))
                .thenReturn(Optional.empty());
        when(requestConfigMock.getExecution()).thenReturn(SIMPLE);

        converter.convert(request);
    }

    // join request -> QuickGOQuery tests
    @Test
    public void createsQueryForCorrectlyConfiguredJoinRequest() {
        String value = "valueX";
        String field = "fieldX";
        ClientRequest request = ClientRequest.newBuilder().addProperty(field, value).build();

        when(requestConfigRetrievalMock.getSignature(request.getSignature()))
                .thenReturn(Optional.of(requestConfigMock));
        when(requestConfigMock.getExecution()).thenReturn(JOIN);

        String fromTable = "from table";
        String fromAttribute = "from attribute";
        String toTable = "to table";
        String toAttribute = "to attribute";
        Map<String, String> configPropertiesMap = new HashMap<>();
        configPropertiesMap.put(FROM_TABLE_NAME, fromTable);
        configPropertiesMap.put(FROM_ATTRIBUTE_NAME, fromAttribute);
        configPropertiesMap.put(TO_TABLE_NAME, toTable);
        configPropertiesMap.put(TO_ATTRIBUTE_NAME, toAttribute);
        when(requestConfigMock.getProperties()).thenReturn(configPropertiesMap);

        QuickGOQuery resultingQuery = converter.convert(request);
        QuickGOQuery expectedQuery = QuickGOQuery.createJoinQueryWithFilter(
                fromTable,
                fromAttribute,
                toTable,
                toAttribute,
                QuickGOQuery.createQuery(field, value)
        );

        assertThat(resultingQuery, is(expectedQuery));
    }

    @Test(expected = IllegalStateException.class)
    public void missingSignatureInConfigForJoinRequestCausesException() {
        String value = "valueX";
        String field = "fieldX";
        ClientRequest request = ClientRequest.newBuilder().addProperty(field, value).build();

        when(requestConfigRetrievalMock.getSignature(request.getSignature()))
                .thenReturn(Optional.empty());
        when(requestConfigMock.getExecution()).thenReturn(JOIN);

        setConfigPropertiesMap();

        converter.convert(request);
    }

    private void setConfigPropertiesMap() {
        String fromTable = "from table";
        String fromAttribute = "from attribute";
        String toTable = "to table";
        String toAttribute = "to attribute";
        Map<String, String> configPropertiesMap = new HashMap<>();
        configPropertiesMap.put(FROM_TABLE_NAME, fromTable);
        configPropertiesMap.put(FROM_ATTRIBUTE_NAME, fromAttribute);
        configPropertiesMap.put(TO_TABLE_NAME, toTable);
        configPropertiesMap.put(TO_ATTRIBUTE_NAME, toAttribute);
        when(requestConfigMock.getProperties()).thenReturn(configPropertiesMap);
    }
}