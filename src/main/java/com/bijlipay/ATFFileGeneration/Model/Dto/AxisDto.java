package com.bijlipay.ATFFileGeneration.Model.Dto;

public class AxisDto {

    private String rrn;
    private String mid;
    private String latitude;
    private String longitude;
    private String aggrName;
    private String action;
    private String tid;
    private String dateOfCommissioning;


    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAggrName() {
        return aggrName;
    }

    public void setAggrName(String aggrName) {
        this.aggrName = aggrName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getDateOfCommissioning() {
        return dateOfCommissioning;
    }

    public void setDateOfCommissioning(String dateOfCommissioning) {
        this.dateOfCommissioning = dateOfCommissioning;
    }
}
