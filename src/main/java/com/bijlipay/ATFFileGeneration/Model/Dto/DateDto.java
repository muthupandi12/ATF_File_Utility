package com.bijlipay.ATFFileGeneration.Model.Dto;

public class DateDto {

    private String minDate;
    private String maxDate;


    public DateDto(String minDate, String maxDate) {
        this.minDate = minDate;
        this.maxDate = maxDate;
    }

    public String getMinDate() {
        return minDate;
    }

    public void setMinDate(String minDate) {
        this.minDate = minDate;
    }

    public String getMaxDate() {
        return maxDate;
    }

    public void setMaxDate(String maxDate) {
        this.maxDate = maxDate;
    }
}
