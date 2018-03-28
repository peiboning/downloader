package com.download.net;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

/**
 * Created by peiboning on 2018/3/13.
 */

public class HttpUrlNetWork implements INetWork {
    private URL url;



    @Override
    public Response perform(Request request) {
        HttpURLConnection connection = null;
        try {
            url = new URL(request.getUrl());
            connection = (HttpURLConnection) url.openConnection();

            setHeaders(connection, request.getHeaders());


            Response.ResponseHeader header = new Response.ResponseHeader();
            header.responseCode = connection.getResponseCode();
            header.contentLength = connection.getContentLength();

            Response response = new Response();
            response.setHeader(header);
            return response;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null != connection){
                connection.disconnect();
            }
        }
        return null;
    }

    private void setHeaders(HttpURLConnection connection, Map<String, String> headers) throws ProtocolException {
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10 * 1000);
        connection.setReadTimeout(100 * 1000);
    }
}
