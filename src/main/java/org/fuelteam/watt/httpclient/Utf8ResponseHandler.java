package org.fuelteam.watt.httpclient;

import java.io.IOException;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import com.google.common.collect.Lists;

public class Utf8ResponseHandler implements ResponseHandler<List<Object>> {

    public static final ResponseHandler<List<Object>> INSTANCE = new Utf8ResponseHandler();

    @Override
    public List<Object> handleResponse(final HttpResponse response) throws IOException {
        final StatusLine statusLine = response.getStatusLine();
        //final HttpEntity httpEntity = response.getEntity();
        /*
        if (statusLine.getStatusCode() >= 300) {
            EntityUtils.consume(httpEntity);
            throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }
        */
        return Lists.newArrayList(statusLine.getStatusCode(), EntityUtils.toString(response.getEntity(), Consts.UTF_8));
        //return httpEntity == null ? null : EntityUtils.toString(httpEntity, Consts.UTF_8);
    }
}