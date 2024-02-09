package com.bijlipay.ATFFileGeneration.Model.dto;

import java.util.Date;

public class AtfFileExportDTO {
    private String terminalId;

    private String merchantId;

    private String posDeviceId;

    private String batchNumber;

    private String cardHolderName;

    private String maskedCardNumber;

    private String transactionMode;

    private String invoiceNumber;

    private String acquireBank;

    private String cardType;

    private String cardNetwork;

    private String cardIssuerCountryCode;

    private String amount;

    private String responseCode;

    private String rrn;

    private String transactionAuthCode;

    private Date transactionDate;

    private Date responseDate;

    private String transactionId;

    private String orgTransactionId;

    private String transactionType;

    private String status;

    private String stan;

    private String settlementMode;

    private String settlementStatus;

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getPosDeviceId() {
        return posDeviceId;
    }

    public void setPosDeviceId(String posDeviceId) {
        this.posDeviceId = posDeviceId;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }

    public String getTransactionMode() {
        return transactionMode;
    }

    public void setTransactionMode(String transactionMode) {
        this.transactionMode = transactionMode;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getAcquireBank() {
        return acquireBank;
    }

    public void setAcquireBank(String acquireBank) {
        this.acquireBank = acquireBank;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCardNetwork() {
        return cardNetwork;
    }

    public void setCardNetwork(String cardNetwork) {
        this.cardNetwork = cardNetwork;
    }

    public String getCardIssuerCountryCode() {
        return cardIssuerCountryCode;
    }

    public void setCardIssuerCountryCode(String cardIssuerCountryCode) {
        this.cardIssuerCountryCode = cardIssuerCountryCode;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getTransactionAuthCode() {
        return transactionAuthCode;
    }

    public void setTransactionAuthCode(String transactionAuthCode) {
        this.transactionAuthCode = transactionAuthCode;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Date getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(Date responseDate) {
        this.responseDate = responseDate;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getOrgTransactionId() {
        return orgTransactionId;
    }

    public void setOrgTransactionId(String orgTransactionId) {
        this.orgTransactionId = orgTransactionId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String getSettlementMode() {
        return settlementMode;
    }

    public void setSettlementMode(String settlementMode) {
        this.settlementMode = settlementMode;
    }

    public String getSettlementStatus() {
        return settlementStatus;
    }

    public void setSettlementStatus(String settlementStatus) {
        this.settlementStatus = settlementStatus;
    }

    @Override
    public String toString() {
        return "AtfFileExportDTO{" +
                "terminalId='" + terminalId + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", posDeviceId='" + posDeviceId + '\'' +
                ", batchNumber='" + batchNumber + '\'' +
                ", cardHolderName='" + cardHolderName + '\'' +
                ", maskedCardNumber='" + maskedCardNumber + '\'' +
                ", transactionMode='" + transactionMode + '\'' +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", acquireBank='" + acquireBank + '\'' +
                ", cardType='" + cardType + '\'' +
                ", cardNetwork='" + cardNetwork + '\'' +
                ", cardIssuerCountryCode='" + cardIssuerCountryCode + '\'' +
                ", amount='" + amount + '\'' +
                ", responseCode='" + responseCode + '\'' +
                ", rrn='" + rrn + '\'' +
                ", transactionAuthCode='" + transactionAuthCode + '\'' +
                ", transactionDate=" + transactionDate +
                ", responseDate=" + responseDate +
                ", transactionId='" + transactionId + '\'' +
                ", orgTransactionId='" + orgTransactionId + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", status='" + status + '\'' +
                ", stan='" + stan + '\'' +
                ", settlementMode='" + settlementMode + '\'' +
                ", settlementStatus='" + settlementStatus + '\'' +
                '}';
    }
}
