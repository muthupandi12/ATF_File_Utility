package com.bijlipay.ATFFileGeneration.Config;

import org.springframework.http.HttpStatus;

public class ApiResponse {

    private HttpStatus status;

    private Object data;

    private Object message;

    public ApiResponse(HttpStatus status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;

    }

    public ApiResponse(HttpStatus status, Object message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }
}
