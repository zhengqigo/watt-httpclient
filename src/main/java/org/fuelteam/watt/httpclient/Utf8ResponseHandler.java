package org.fuelteam.watt.httpclient;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

public class Utf8ResponseHandler implements ResponseHandler<Pair<Integer, String>> {

    public static final ResponseHandler<Pair<Integer, String>> INSTANCE = new Utf8ResponseHandler();

    @Override
    public Pair<Integer, String> handleResponse(final HttpResponse response) throws IOException {
        final StatusLine statusLine = response.getStatusLine();
        //final HttpEntity httpEntity = response.getEntity();
        /*
        if (statusLine.getStatusCode() >= 300) {
            EntityUtils.consume(httpEntity);
            throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }
        */
        return Pair.of(statusLine.getStatusCode(), EntityUtils.toString(response.getEntity(), Consts.UTF_8));
        //return httpEntity == null ? null : EntityUtils.toString(httpEntity, Consts.UTF_8);
    }
}