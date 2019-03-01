package org.fuelteam.watt.httpclient;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;

public class StreamResponseHandler implements ResponseHandler<Pair<Integer, Object>> {

    public static final ResponseHandler<Pair<Integer, Object>> INSTANCE = new StreamResponseHandler();

    @Override
    public Pair<Integer, Object> handleResponse(final HttpResponse response) throws IOException {
        final StatusLine statusLine = response.getStatusLine();
        final HttpEntity httpEntity = response.getEntity();
        /*
        if (statusLine.getStatusCode() >= 300) {
            EntityUtils.consume(httpEntity);
            throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }
        */
        return Pair.of(statusLine.getStatusCode(), httpEntity == null ? null : httpEntity.getContent());
        //return httpEntity == null ? null : httpEntity.getContent();
    }
}