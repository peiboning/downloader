package com.download.net;

import java.util.Map;

/**
 * Created by peiboning on 2018/3/13.
 */

public class Request {
    private String url;
    private Map<String, String> headers;

    public Request(String url, Map<String, String> headers) {
        this.url = url;
        this.headers = headers;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
