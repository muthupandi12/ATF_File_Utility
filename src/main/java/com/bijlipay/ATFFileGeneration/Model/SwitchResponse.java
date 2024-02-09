package com.bijlipay.ATFFileGeneration.Model;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "switch_response")
public class SwitchResponse {

    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TxnCorrelationId", length = 41)
    private String txnCorrelationId;

    @Column(name = "MTI", length = 4)
    private String MTI;

    @Column(name = "Stan", length = 6)
    private String stan;

    @Column(name = "TxnDateTimeGMT", length = 10)
    private String TxnDateTimeGMT;

    @Column(name = "RRNumber", length = 12)
    private String RRNumber;

    @Column(name = "TxnResponseCode", length = 2)
    private String TxnResponseCode;

    @Column(name = "AuthResponseCode", length = 6)
    private String AuthResponseCode;

    @Column(name = "AdditionalResponseData", length = 100)
    private String AdditionalResponseData;

    @Column(name = "TxnIdentifier", length = 20)
    private String TxnIdentifier;

    @Column(name = "TxnResponseDescription", length = 100)
    private String TxnResponseDescription;

    @Column(name = "TxnFailureCode", length = 4)
    private String TxnFailureCode;

    @Column(name = "TxnProcessedBy", length = 1)
    private String TxnProcessedBy;

    @Column(name = "ResponseReceivedTime")
    private Date ResponseReceivedTime;

    public String getTxnCorrelationId() {
        return txnCorrelationId;
    }

    public void setTxnCorrelationId(String txnCorrelationId) {
        this.txnCorrelationId = txnCorrelationId;
    }

    public String getMTI() {
        return MTI;
    }

    public void setMTI(String MTI) {
        this.MTI = MTI;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String getTxnDateTimeGMT() {
        return TxnDateTimeGMT;
    }

    public void setTxnDateTimeGMT(String txnDateTimeGMT) {
        TxnDateTimeGMT = txnDateTimeGMT;
    }

    public String getRRNumber() {
        return RRNumber;
    }

    public void setRRNumber(String RRNumber) {
        this.RRNumber = RRNumber;
    }

    public String getTxnResponseCode() {
        return TxnResponseCode;
    }

    public void setTxnResponseCode(String txnResponseCode) {
        TxnResponseCode = txnResponseCode;
    }

    public String getAuthResponseCode() {
        return AuthResponseCode;
    }

    public void setAuthResponseCode(String authResponseCode) {
        AuthResponseCode = authResponseCode;
    }

    public String getAdditionalResponseData() {
        return AdditionalResponseData;
    }

    public void setAdditionalResponseData(String additionalResponseData) {
        AdditionalResponseData = additionalResponseData;
    }

    public String getTxnIdentifier() {
        return TxnIdentifier;
    }

    public void setTxnIdentifier(String txnIdentifier) {
        TxnIdentifier = txnIdentifier;
    }

    public String getTxnResponseDescription() {
        return TxnResponseDescription;
    }

    public void setTxnResponseDescription(String txnResponseDescription) {
        TxnResponseDescription = txnResponseDescription;
    }

    public String getTxnFailureCode() {
        return TxnFailureCode;
    }

    public void setTxnFailureCode(String txnFailureCode) {
        TxnFailureCode = txnFailureCode;
    }

    public String getTxnProcessedBy() {
        return TxnProcessedBy;
    }

    public void setTxnProcessedBy(String txnProcessedBy) {
        TxnProcessedBy = txnProcessedBy;
    }

    public Date getResponseReceivedTime() {
        return ResponseReceivedTime;
    }

    public void setResponseReceivedTime(Date responseReceivedTime) {
        ResponseReceivedTime = responseReceivedTime;
    }
}
