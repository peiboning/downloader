package com.download.net;

import java.io.InputStream;

/**
 * Created by peiboning on 2018/3/13.
 */

public class Response {
    private ResponseHeader header;
    private InputStream inputStream;

    public static class ResponseHeader {
        public int responseCode;
        public int contentLength;

        @Override
        public String toString() {
            return "ResponseHeader{" +
                    "responseCode=" + responseCode +
                    ", contentLength=" + contentLength +
                    '}';
        }
    }

    @Override
    public String toString() {
        return header.toString();
    }

    public ResponseHeader getHeader() {
        return header;
    }

    public void setHeader(ResponseHeader header) {
        this.header = header;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
