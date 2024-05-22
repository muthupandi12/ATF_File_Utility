package com.bijlipay.ATFFileGeneration.Model.Dto;

import java.util.Arrays;
import java.util.List;

public class UPICountDto {

    private String status;
    private String settled;
    private String notSettled;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSettled() {
        return settled;
    }

    public void setSettled(String settled) {
        this.settled = settled;
    }

    public String getNotSettled() {
        return notSettled;
    }

    public void setNotSettled(String notSettled) {
        this.notSettled = notSettled;
    }

    public List<String> getAsList() {
        return  Arrays.asList(status, settled, notSettled);
    }
}
