package uk.ac.ebi.quickgo.annotation.download.http;

import uk.ac.ebi.quickgo.annotation.model.Annotation;
import uk.ac.ebi.quickgo.rest.search.results.QueryResult;

import java.io.IOException;
import java.io.OutputStream;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import static uk.ac.ebi.quickgo.annotation.download.http.MediaTypeFactory.GPAD_MEDIA_TYPE;

/**
 * GPAD message converter that writes a stream of {@link QueryResult} containing {@link Annotation} instances,
 * to a response's output stream.
 *
 * Created 19/01/17
 * @author Edd
 */
public class GPADHttpMessageConverter extends AbstractHttpMessageConverter<Object> {
    private final DispatchWriter helper;

    public GPADHttpMessageConverter(DispatchWriter helper) {
        super(GPAD_MEDIA_TYPE);
        this.helper = helper;
    }

    @Override protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        return null;
    }

    @Override protected void writeInternal(Object downloadPackage, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        OutputStream out = outputMessage.getBody();
        helper.write(downloadPackage, out);
    }
}
