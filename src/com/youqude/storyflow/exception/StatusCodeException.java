package com.youqude.storyflow.exception;

public class StatusCodeException extends Exception{
    
    private int statusCode = -1;
    private String url = null;
    
    public StatusCodeException(String url,int statusCode) {
        super(url);
        this.url = url;
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }


    public String getUrl() {
        return url;
    }
}
