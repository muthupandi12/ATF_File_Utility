package com.bijlipay.ATFFileGeneration.Model;

import javax.persistence.*;

@Entity
@Table(name = "notification_fields")
public class NotificationFields {

    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY )
    @Column(name = "Id")
    private Long id;

    @Column(name = "rrn")
    private String rrn;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "card_holder_name")
    private String cardHolderName;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "amount")
    private String amount;

    @Column(name = "masked_card_number")
    private String maskedCardNumber;

    @Column(name = "pos_device_id")
    private String posDeviceId;

    @Column(name = "transaction_mode")
    private String transactionMode;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "card_network")
    private String cardNetwork;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "acquirer_bank")
    private String acquirerBank;

    @Column(name = "card_issuer_country_code")
    private String cardIssuerCountryCode;

    @Column(name = "card_type")
    private String cardType;

    @Column(name = "transaction_date_time")
    private String transactionDateTime;

    @Column(name = "settlement_mode")
    private String settlementMode;

    @Column(name = "TerminalId")
    private String terminalId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
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

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }

    public String getPosDeviceId() {
        return posDeviceId;
    }

    public void setPosDeviceId(String posDeviceId) {
        this.posDeviceId = posDeviceId;
    }

    public String getTransactionMode() {
        return transactionMode;
    }

    public void setTransactionMode(String transactionMode) {
        this.transactionMode = transactionMode;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getCardNetwork() {
        return cardNetwork;
    }

    public void setCardNetwork(String cardNetwork) {
        this.cardNetwork = cardNetwork;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getAcquirerBank() {
        return acquirerBank;
    }

    public void setAcquirerBank(String acquirerBank) {
        this.acquirerBank = acquirerBank;
    }

    public String getCardIssuerCountryCode() {
        return cardIssuerCountryCode;
    }

    public void setCardIssuerCountryCode(String cardIssuerCountryCode) {
        this.cardIssuerCountryCode = cardIssuerCountryCode;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getTransactionDateTime() {
        return transactionDateTime;
    }

    public void setTransactionDateTime(String transactionDateTime) {
        this.transactionDateTime = transactionDateTime;
    }

    public String getSettlementMode() {
        return settlementMode;
    }

    public void setSettlementMode(String settlementMode) {
        this.settlementMode = settlementMode;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }
}
