package org.fuelteam.watt.httpclient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;

public class StreamResponseHandler implements ResponseHandler<InputStream> {

    public static final ResponseHandler<InputStream> INSTANCE = new StreamResponseHandler();

    @Override
    public InputStream handleResponse(final HttpResponse response) throws IOException {
        final StatusLine statusLine = response.getStatusLine();
        final HttpEntity httpEntity = response.getEntity();
        if (statusLine.getStatusCode() >= 300) {
            EntityUtils.consume(httpEntity);
            throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }
        return httpEntity == null ? null : httpEntity.getContent();
    }
}