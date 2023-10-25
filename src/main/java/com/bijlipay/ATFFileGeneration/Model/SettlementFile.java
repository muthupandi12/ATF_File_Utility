package com.bijlipay.ATFFileGeneration.Model;

import javax.persistence.*;

@Entity
@Table(name = "settlement_file_main")
public class SettlementFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settle_id", nullable = false)
    private long settleId;
    @Column(name = "mid",nullable = true)
    private String mid;
    @Column(name = "tid",nullable = true)
    private String tid;
    @Column(name = "batch_number",nullable = true)
    private String batchNumber;
    @Column(name = "invoice_number",nullable = true)
    private String invoiceNumber;
    @Column(name = "stan",nullable = true)
    private String stan;
    @Column(name = "rrn",nullable = true)
    private String rrn;
    @Column(name = "auth_code",nullable = true)
    private String authCode;
    @Column(name = "amount",nullable = true)
    private String amount;
    @Column(name = "to_char",nullable = true)
    private String toChar;
    @Column(name = "additional_amount",nullable = true)
    private String additionalAmount;
    @Column(name = "status",nullable = true)
    private String status;
//    @Column(name = "date",nullable = true)
//    private Date date;

    @Column(name = "date",nullable = true)
    private String date;
    @Column(name = "file_name",nullable = true)
    private String filename;

    public long getSettleId() {
        return settleId;
    }

    public void setSettleId(long settleId) {
        this.settleId = settleId;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getToChar() {
        return toChar;
    }

    public void setToChar(String toChar) {
        this.toChar = toChar;
    }

    public String getAdditionalAmount() {
        return additionalAmount;
    }

    public void setAdditionalAmount(String additionalAmount) {
        this.additionalAmount = additionalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
