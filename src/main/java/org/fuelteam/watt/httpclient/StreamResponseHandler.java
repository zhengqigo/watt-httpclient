package org.fuelteam.watt.httpclient;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;

import com.google.common.collect.Lists;

public class StreamResponseHandler implements ResponseHandler<List<Object>> {

    public static final ResponseHandler<List<Object>> INSTANCE = new StreamResponseHandler();

    @Override
    public List<Object> handleResponse(final HttpResponse response) throws IOException {
        final StatusLine statusLine = response.getStatusLine();
        final HttpEntity httpEntity = response.getEntity();
        /*
        if (statusLine.getStatusCode() >= 300) {
            EntityUtils.consume(httpEntity);
            throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }
        */
        return Lists.newArrayList(statusLine.getStatusCode(), httpEntity == null ? null : httpEntity.getContent());
        //return httpEntity == null ? null : httpEntity.getContent();
    }
}