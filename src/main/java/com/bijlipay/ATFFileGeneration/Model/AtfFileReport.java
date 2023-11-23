package com.bijlipay.ATFFileGeneration.Model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "atf_file_report_main")
public class AtfFileReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",nullable = false)
    private long id;

    @Column(name = "terminal_id")
    private String terminalId;
    @Column(name = "merchant_id")
    private String merchantId;
    @Column(name = "pos_device_id")
    private String posDeviceId;
    @Column(name = "batch_number")
    private String batchNumber;
    @Column(name = "card_holder_name")
    private String cardHolderName;
    @Column(name = "masked_card_number")
    private String maskedCardNumber;
    @Column(name = "transaction_mode")
    private String transactionMode;
    @Column(name = "invoice_number")
    private String invoiceNumber;
    @Column(name = "acquire_bank")
    private String acquireBank;
    @Column(name = "card_type")
    private String cardType;
    @Column(name = "card_network")
    private String cardNetwork;
    @Column(name = "card_issuer_country_code")
    private String cardIssuerCountryCode;
    @Column(name = "amount")
    private String amount;
    @Column(name = "response_code")
    private String responseCode;
    @Column(name = "rrn")
    private String rrn;
    @Column(name = "transaction_auth_code")
    private String transactionAuthCode;
    @Column(name = "transaction_date")
    private Date transactionDate;
    @Column(name = "response_date")
    private Date responseDate;
    @Column(name = "transaction_id")
    private String transactionId;
    @Column(name = "org_transaction_id")
    private String orgTransactionId;
    @Column(name = "transaction_type")
    private String transactionType;
    @Column(name = "status")
    private String status;
    @Column(name = "stan")
    private String stan;
    @Column(name = "settlement_mode")
    private String settlementMode;
    @Column(name = "settlement_status")
    private String settlementStatus;
    @Column(name = "ack_status",columnDefinition = "tinyint(1) default 0")
    private boolean ackStatus;
    @Column(name = "init_status",columnDefinition = "tinyint(1) default 0")
    private boolean initStatus;
    @Column(name = "host_status",columnDefinition = "tinyint(1) default 0")
    private boolean hostStatus;
    @Column(name = "reversal_status",columnDefinition = "tinyint(1) default 0")
    private boolean reversalStatus;
    @Column(name = "void_status",columnDefinition = "tinyint(1) default 0")
    private boolean voidStatus;

    @Column(name = "response_date_check",columnDefinition = "tinyint(1) default 0")
    private boolean responseDateCheck;

    @Column(name = "void_and_sale_txn_id_check",columnDefinition = "tinyint(1) default 0")
    private boolean voidAndSaleTxnIdCheckWithDate;

    @Column(name = "reversal_and_sale_txn_id_check",columnDefinition = "tinyint(1) default 0")
    private boolean reversalAndSaleTxnIdCheckWithDate;
    @Column(name = "void_txn_response_code_check",columnDefinition = "tinyint(1) default 0")
    private boolean voidTxnResponseCodeCheck;

    @Column(name = "reversal_and_ack_status",columnDefinition = "tinyint(1) default 0")
    private boolean reversalAndAckStatus;
    @Column(name = "settled_txn_wrong_status",columnDefinition = "tinyint(1) default 0")
    private boolean settledTxnWrongStatus;

    @Column(name = "not_settled_txn_wrong_status",columnDefinition = "tinyint(1) default 0")
    private boolean notSettledTxnWrongStatus;

    @Column(name = "void_txn_other_than_host_status",columnDefinition = "tinyint(1) default 0")
    private boolean voidTxnOtherThanHostStatus;
    @Column(name = "sale_upi_null_value_status",columnDefinition = "tinyint(1) default 0")
    private boolean saleUpiNullValueStatus;
    @Column(name = "void_reversal_null_value_status",columnDefinition = "tinyint(1) default 0")
    private boolean voidReversalNullValueStatus;

    @Column(name = "upi_and_reversal_txn_id_equal_status",columnDefinition = "tinyint(1) default 0")
    private boolean upiAndReversalTxnIdEqualStatus;

    @Column(name = "sale_txn_only_init_status",columnDefinition = "tinyint(1) default 0")
    private boolean saleTxnOnlyInitStatus;

    @Column(name = "rules_verified_status",columnDefinition = "tinyint(1) default 0")
    private boolean rulesVerifiedStatus;


    @Column(name = "not_settled_txn_wrong_corresponding_void_or_reversal",columnDefinition = "tinyint(1) default 0")
    private boolean notSettledTxnWrongCorrespondingVoidOrReversal;

    @Column(name = "only_void_reversal_without_sale_or_upi",columnDefinition = "tinyint(1) default 0")
    private boolean onlyVoidReversalWithoutSaleOrUPI;

    @Column(name = "response_date_mismatch",columnDefinition = "tinyint(1) default 0")
    private boolean responseDateMismatch;

    @Column(name = "settled_txn_wrong_corresponding_void_or_reversal",columnDefinition = "tinyint(1) default 0")
    private boolean settledTxnWrongCorrespondingVoidOrReversal;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public boolean isAckStatus() {
        return ackStatus;
    }

    public void setAckStatus(boolean ackStatus) {
        this.ackStatus = ackStatus;
    }

    public boolean isInitStatus() {
        return initStatus;
    }

    public void setInitStatus(boolean initStatus) {
        this.initStatus = initStatus;
    }

    public boolean isHostStatus() {
        return hostStatus;
    }

    public void setHostStatus(boolean hostStatus) {
        this.hostStatus = hostStatus;
    }

    public boolean isReversalStatus() {
        return reversalStatus;
    }

    public void setReversalStatus(boolean reversalStatus) {
        this.reversalStatus = reversalStatus;
    }

    public boolean isResponseDateCheck() {
        return responseDateCheck;
    }

    public void setResponseDateCheck(boolean responseDateCheck) {
        this.responseDateCheck = responseDateCheck;
    }


    public boolean isVoidAndSaleTxnIdCheckWithDate() {
        return voidAndSaleTxnIdCheckWithDate;
    }

    public void setVoidAndSaleTxnIdCheckWithDate(boolean voidAndSaleTxnIdCheckWithDate) {
        this.voidAndSaleTxnIdCheckWithDate = voidAndSaleTxnIdCheckWithDate;
    }

    public boolean isReversalAndSaleTxnIdCheckWithDate() {
        return reversalAndSaleTxnIdCheckWithDate;
    }

    public void setReversalAndSaleTxnIdCheckWithDate(boolean reversalAndSaleTxnIdCheckWithDate) {
        this.reversalAndSaleTxnIdCheckWithDate = reversalAndSaleTxnIdCheckWithDate;
    }

    public boolean isVoidTxnResponseCodeCheck() {
        return voidTxnResponseCodeCheck;
    }

    public void setVoidTxnResponseCodeCheck(boolean voidTxnResponseCodeCheck) {
        this.voidTxnResponseCodeCheck = voidTxnResponseCodeCheck;
    }

    public boolean isReversalAndAckStatus() {
        return reversalAndAckStatus;
    }

    public void setReversalAndAckStatus(boolean reversalAndAckStatus) {
        this.reversalAndAckStatus = reversalAndAckStatus;
    }

    public boolean isSettledTxnWrongStatus() {
        return settledTxnWrongStatus;
    }

    public void setSettledTxnWrongStatus(boolean settledTxnWrongStatus) {
        this.settledTxnWrongStatus = settledTxnWrongStatus;
    }

    public boolean isNotSettledTxnWrongStatus() {
        return notSettledTxnWrongStatus;
    }

    public void setNotSettledTxnWrongStatus(boolean notSettledTxnWrongStatus) {
        this.notSettledTxnWrongStatus = notSettledTxnWrongStatus;
    }

    public boolean isVoidTxnOtherThanHostStatus() {
        return voidTxnOtherThanHostStatus;
    }

    public void setVoidTxnOtherThanHostStatus(boolean voidTxnOtherThanHostStatus) {
        this.voidTxnOtherThanHostStatus = voidTxnOtherThanHostStatus;
    }

    public boolean isSaleUpiNullValueStatus() {
        return saleUpiNullValueStatus;
    }

    public void setSaleUpiNullValueStatus(boolean saleUpiNullValueStatus) {
        this.saleUpiNullValueStatus = saleUpiNullValueStatus;
    }

    public boolean isVoidReversalNullValueStatus() {
        return voidReversalNullValueStatus;
    }

    public void setVoidReversalNullValueStatus(boolean voidReversalNullValueStatus) {
        this.voidReversalNullValueStatus = voidReversalNullValueStatus;
    }

    public boolean isUpiAndReversalTxnIdEqualStatus() {
        return upiAndReversalTxnIdEqualStatus;
    }

    public void setUpiAndReversalTxnIdEqualStatus(boolean upiAndReversalTxnIdEqualStatus) {
        this.upiAndReversalTxnIdEqualStatus = upiAndReversalTxnIdEqualStatus;
    }

    public boolean isSaleTxnOnlyInitStatus() {
        return saleTxnOnlyInitStatus;
    }

    public void setSaleTxnOnlyInitStatus(boolean saleTxnOnlyInitStatus) {
        this.saleTxnOnlyInitStatus = saleTxnOnlyInitStatus;
    }

    public boolean isVoidStatus() {
        return voidStatus;
    }

    public void setVoidStatus(boolean voidStatus) {
        this.voidStatus = voidStatus;
    }

    public boolean isRulesVerifiedStatus() {
        return rulesVerifiedStatus;
    }

    public void setRulesVerifiedStatus(boolean rulesVerifiedStatus) {
        this.rulesVerifiedStatus = rulesVerifiedStatus;
    }

    public boolean isNotSettledTxnWrongCorrespondingVoidOrReversal() {
        return notSettledTxnWrongCorrespondingVoidOrReversal;
    }

    public void setNotSettledTxnWrongCorrespondingVoidOrReversal(boolean notSettledTxnWrongCorrespondingVoidOrReversal) {
        this.notSettledTxnWrongCorrespondingVoidOrReversal = notSettledTxnWrongCorrespondingVoidOrReversal;
    }

    public boolean isOnlyVoidReversalWithoutSaleOrUPI() {
        return onlyVoidReversalWithoutSaleOrUPI;
    }

    public void setOnlyVoidReversalWithoutSaleOrUPI(boolean onlyVoidReversalWithoutSaleOrUPI) {
        this.onlyVoidReversalWithoutSaleOrUPI = onlyVoidReversalWithoutSaleOrUPI;
    }

    public boolean isResponseDateMismatch() {
        return responseDateMismatch;
    }

    public void setResponseDateMismatch(boolean responseDateMismatch) {
        this.responseDateMismatch = responseDateMismatch;
    }

    public boolean isSettledTxnWrongCorrespondingVoidOrReversal() {
        return settledTxnWrongCorrespondingVoidOrReversal;
    }

    public void setSettledTxnWrongCorrespondingVoidOrReversal(boolean settledTxnWrongCorrespondingVoidOrReversal) {
        this.settledTxnWrongCorrespondingVoidOrReversal = settledTxnWrongCorrespondingVoidOrReversal;
    }
}
