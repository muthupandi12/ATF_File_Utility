package com.bijlipay.ATFFileGeneration.Config;

import org.springframework.http.HttpStatus;

public class ApiResponse {

    private HttpStatus status;

    private Object data;

    private Object message;

    public ApiResponse(HttpStatus status, Object data, Object message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public ApiResponse(HttpStatus status, Object data) {
        this.status = status;
        this.data = data;
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
