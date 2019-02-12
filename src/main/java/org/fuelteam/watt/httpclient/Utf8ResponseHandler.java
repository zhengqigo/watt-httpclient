package org.fuelteam.watt.httpclient;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class Utf8ResponseHandler implements ResponseHandler<String> {

    public static final ResponseHandler<String> INSTANCE = new Utf8ResponseHandler();

    @Override
    public String handleResponse(final HttpResponse response) throws IOException {
        //final StatusLine statusLine = response.getStatusLine();
        //final HttpEntity httpEntity = response.getEntity();
        /*
        if (statusLine.getStatusCode() >= 300) {
            EntityUtils.consume(httpEntity);
            throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }
        */
        return EntityUtils.toString(response.getEntity(), Consts.UTF_8);
        //return httpEntity == null ? null : EntityUtils.toString(httpEntity, Consts.UTF_8);
    }
}