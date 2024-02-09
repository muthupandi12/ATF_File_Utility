package com.bijlipay.ATFFileGeneration.Model;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "notification_data")
public class NotificationData {

    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",nullable = false)
    private Long id;

    @Column(name = "TxnCorrelationId")
    private String txnCorrelationId;

    @Column(name = "TransactionId")
    private String transactionId;

    @Column(name = "OrgTransactionId")
    private String orgTransactionId;

    @Column(name = "NotificationType")
    private String notificationType;

    @Column(name = "TerminalId")
    private String terminalId;

    @Column(name = "MTI")
    private String MTI;

    @Column(name = "TransactionType")
    private String transactionType;

    @Column(name = "ProcessingCode")
    private String processingCode;

    @Column(name = "Description")
    private String description;

    @Column(name = "NotificationStatus")
    private String notificationStatus;

    @Column(name = "DateTime")
    private Date dateTime;

    @Column(name = "Rrn")
    private String rrn;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "transaction_date_time")
    private String transactionDateTime;

    @Column(name = "notification_fields_id")
    private Long notificationFieldsId;

    @Column(name = "transaction_auth_code")
    private String transactionAuthCode;

    @Column(name = "response_date_time")
    private String responseDateTime;

    @Column(name = "HostResponseDateTime")
    private String hostResponseDateTime;

    @Column(name = "response_code")
    private String responseCode;

    @Column(name = "Stan")
    private String stan;

    @Column(name = "settlement_status")
    private String settlementStatus;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "NotificationRecipient")
    private String notificationRecipient;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTxnCorrelationId() {
        return txnCorrelationId;
    }

    public void setTxnCorrelationId(String txnCorrelationId) {
        this.txnCorrelationId = txnCorrelationId;
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

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getMTI() {
        return MTI;
    }

    public void setMTI(String MTI) {
        this.MTI = MTI;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getProcessingCode() {
        return processingCode;
    }

    public void setProcessingCode(String processingCode) {
        this.processingCode = processingCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotificationStatus() {
        return notificationStatus;
    }

    public void setNotificationStatus(String notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getTransactionDateTime() {
        return transactionDateTime;
    }

    public void setTransactionDateTime(String transactionDateTime) {
        this.transactionDateTime = transactionDateTime;
    }

    public Long getNotificationFieldsId() {
        return notificationFieldsId;
    }

    public void setNotificationFieldsId(Long notificationFieldsId) {
        this.notificationFieldsId = notificationFieldsId;
    }

    public String getTransactionAuthCode() {
        return transactionAuthCode;
    }

    public void setTransactionAuthCode(String transactionAuthCode) {
        this.transactionAuthCode = transactionAuthCode;
    }

    public String getResponseDateTime() {
        return responseDateTime;
    }

    public void setResponseDateTime(String responseDateTime) {
        this.responseDateTime = responseDateTime;
    }

    public String getHostResponseDateTime() {
        return hostResponseDateTime;
    }

    public void setHostResponseDateTime(String hostResponseDateTime) {
        this.hostResponseDateTime = hostResponseDateTime;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String getSettlementStatus() {
        return settlementStatus;
    }

    public void setSettlementStatus(String settlementStatus) {
        this.settlementStatus = settlementStatus;
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

    public String getNotificationRecipient() {
        return notificationRecipient;
    }

    public void setNotificationRecipient(String notificationRecipient) {
        this.notificationRecipient = notificationRecipient;
    }
}
