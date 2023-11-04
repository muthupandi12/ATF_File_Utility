package com.bijlipay.ATFFileGeneration.Model;

import javax.persistence.*;

@Entity
@Table(name = "txn_list_main_total")
public class TxnListMainTotal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "txn_list_total_id", nullable = false)
    private long txnListId;

    @Column(name = "mti",nullable = true)
    private String mti;
    @Column(name = "txn_type",nullable = true)
    private String txnType;
    @Column(name = "terminal_id",nullable = true)
    private String terminalId;
    @Column(name = "merchant_id",nullable = true)
    private String merchantId;
    @Column(name = "txn_date",nullable = true)
    private String txnDate;
    @Column(name = "txn_time",nullable = true)
    private String txnTime;
    @Column(name = "txn_amount",nullable = true)
    private String txnAmount;
    @Column(name = "txn_response_code",nullable = true)
    private String txnResponseCode;
    @Column(name = "response_received_time",nullable = true)
    private String responseReceivedTime;
    @Column(name = "rrn",nullable = true)
    private String rrn;
    @Column(name = "stan",nullable = true)
    private String stan;
    @Column(name = "invoice_number",nullable = true)
    private String invoiceNumber;
    @Column(name = "batch_number",nullable = true)
    private String batchNumber;
    @Column(name = "urn",nullable = true)
    private String urn;
    @Column(name = "auth_response_code",nullable = true)
    private String authResponseCode;
    @Column(name = "txn_additional_amount",nullable = true)
    private String txnAdditionalAmount;
    @Column(name = "institution_id",nullable = true)
    private String institutionId;

    public long getTxnListId() {
        return txnListId;
    }

    public void setTxnListId(long txnListId) {
        this.txnListId = txnListId;
    }

    public String getMti() {
        return mti;
    }

    public void setMti(String mti) {
        this.mti = mti;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

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

    public String getTxnDate() {
        return txnDate;
    }

    public void setTxnDate(String txnDate) {
        this.txnDate = txnDate;
    }

    public String getTxnTime() {
        return txnTime;
    }

    public void setTxnTime(String txnTime) {
        this.txnTime = txnTime;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String getTxnResponseCode() {
        return txnResponseCode;
    }

    public void setTxnResponseCode(String txnResponseCode) {
        this.txnResponseCode = txnResponseCode;
    }

    public String getResponseReceivedTime() {
        return responseReceivedTime;
    }

    public void setResponseReceivedTime(String responseReceivedTime) {
        this.responseReceivedTime = responseReceivedTime;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }

    public String getAuthResponseCode() {
        return authResponseCode;
    }

    public void setAuthResponseCode(String authResponseCode) {
        this.authResponseCode = authResponseCode;
    }

    public String getTxnAdditionalAmount() {
        return txnAdditionalAmount;
    }

    public void setTxnAdditionalAmount(String txnAdditionalAmount) {
        this.txnAdditionalAmount = txnAdditionalAmount;
    }

    public String getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(String institutionId) {
        this.institutionId = institutionId;
    }
}
