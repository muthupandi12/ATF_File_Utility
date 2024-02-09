package com.bijlipay.ATFFileGeneration.Model;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "switch_request")
public class SwitchRequest {

    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TxnCorrelationId", length = 41)
    private String txnCorrelationId;

    @Column(name = "CardholderPan", length = 64)
    private String cardholderPan;

    @Column(name = "MerchantPan", length = 64)
    private String merchantPan;

    @Column(name = "BeneficiaryPan",length = 64 )
    private String beneficiaryPan;

    @Column(name = "CardholderAccNo",length = 64)
    private String cardholderAccNo;

    @Column(name = "BeneficiaryAccNo",length = 64)
    private String beneficiaryAccNo;

    @Column(name = "MTI",length = 4)
    private String MTI;

    @Column(name = "TxnType", length = 2)
    private String txnType;

    @Column(name = "CardholderAccType", length = 2)
    private String cardholderAccType;

    @Column(name = "BeneficiaryAccType", length = 2)
    private String beneficiaryAccType;

    @Column(name = "Stan", length = 6)
    private String stan;

    @Column(name = "EMV", length = 500)
    private String EMV;

    @Column(name = "ExpiryDate", length = 4)
    private Long expiryDate;

    @Column(name = "ServiceCode", length = 3)
    private String serviceCode;

    @Column(name = "AadhaarNumber", length = 64)
    private String aadhaarNumber;

    @Column(name = "SuperInstId", length = 15)
    private String SuperInstId;

    @Column(name = "InstitutionId", length = 11)
    private String InstitutionId;

    @Column(name = "SponsorBankId", length = 11)
    private String SponsorBankId;

    @Column(name = "SuperMerchantId", length = 15)
    private String SuperMerchantId;

    @Column(name = "GroupMerchantId", length = 15)
    private String GroupMerchantId;

    @Column(name = "MerchantId", length = 15)
    private String MerchantId;

    @Column(name = "SubMerchantId", length = 15)
    private String SubMerchantId;

    @Column(name = "TerminalId", length = 8)
    private String TerminalId;

    @Column(name = "CardHolderCountryCode", length = 3)
    private String CardHolderCountryCode;

    @Column(name = "TerminalAccNo", length = 64)
    private String TerminalAccNo;

    @Column(name = "TerminalCardReadCapability", length = 1)
    private String TerminalCardReadCapability;

    @Column(name = "TerminalAuthReadCapability", length = 1)
    private String TerminalAuthReadCapability;

    @Column(name = "TerminalCardInputMode", length = 1)
    private String TerminalCardInputMode;

    @Column(name = "TerminalOpMode", length = 1)
    private String TerminalOpMode;

    @Column(name = "TerminalOpEnv", length = 1)
    private String TerminalOpEnv;

    @Column(name = "TerminalCardCaptureCapability", length = 1)
    private String TerminalCardCaptureCapability;

    @Column(name = "TerminalCardPresentStatus", length = 1)
    private String TerminalCardPresentStatus;

    @Column(name = "TerminalCardHolderPresentStatus", length = 1)
    private String TerminalCardHolderPresentStatus;

    @Column(name = "TerminalOuptutCapability", length = 1)
    private String TerminalOuptutCapability;

    @Column(name = "TerminalPINCaptureCapability", length = 2)
    private String TerminalPINCaptureCapability;

    @Column(name = "TerminalAddress", length = 25)
    private String TerminalAddress;

    @Column(name = "TerminalCity", length = 12)
    private String TerminalCity;

    @Column(name = "TerminalStateCode", length = 2)
    private String TerminalStateCode;

    @Column(name = "TerminalCountryCode", length = 3)
    private String TerminalCountryCode;

    @Column(name = "TerminalType", length = 1)
    private String TerminalType;

    @Column(name = "PANEntryMode", length = 2)
    private String PANEntryMode;

    @Column(name = "PINEntryCapability", length = 1)
    private String PINEntryCapability;

    @Column(name = "POSConditionCode", length = 2)
    private String POSConditionCode;

    @Column(name = "MerchantName", length = 40)
    private String MerchantName;

    @Column(name = "MerchantCity", length = 12)
    private String MerchantCity;

    @Column(name = "MerchantStateCode", length = 2)
    private String MerchantStateCode;

    @Column(name = "MerchantCountryCode", length = 3)
    private String MerchantCountryCode;

    @Column(name = "MerchantCategoryCode", length = 4)
    private String MerchantCategoryCode;

    @Column(name = "MerchantType", length = 1)
    private String MerchantType;

    @Column(name = "MerchantFraudScore", length = 10)
    private String MerchantFraudScore;

    @Column(name = "BillOrInvoiceNumber", length = 10)
    private String BillOrInvoiceNumber;

    @Column(name = "TxnDateTime", length = 10)
    private String TxnDateTime;

    @Column(name = "TxnDate", length = 4)
    private String TxnDate;

    @Column(name = "TxnTime", length = 6)
    private String TxnTime;

    @Column(name = "RRNumber", length = 12)
    private String RRNumber;

    @Column(name = "BatchNumber", length = 6)
    private String BatchNumber;

    @Column(name = "InvoiceNumber", length = 30)
    private String InvoiceNumber;

    @Column(name = "TxnCurrencyCode", length = 3)
    private String TxnCurrencyCode;

    @Column(name = "TxnAmount", length = 12)
    private String TxnAmount;

    @Column(name = "TxnBillingAmount", length = 12)
    private String TxnBillingAmount;

    @Column(name = "TxnFeeAmount", length = 12)
    private String TxnFeeAmount;

    @Column(name = "TxnFeeCurrencyCode", length = 3)
    private String TxnFeeCurrencyCode;

    @Column(name = "TxnFeeAmountType", length = 1)
    private String TxnFeeAmountType;

    @Column(name = "SettlementAmount", length = 30)
    private String SettlementAmount;

    @Column(name = "SettlementCurrencyCode", length = 3)
    private String SettlementCurrencyCode;

    @Column(name = "SettlementDate", length = 4)
    private String SettlementDate;

    @Column(name = "TxnAdditionalAmount", length = 12)
    private String TxnAdditionalAmount;

    @Column(name = "TxnAdditionalAmountType", length = 1)
    private String TxnAdditionalAmountType;

    @Column(name = "TxnReversalAmount", length = 12)
    private String TxnReversalAmount;

    @Column(name = "AdviseReasonCode", length = 4)
    private String AdviseReasonCode;

    @Column(name = "AcqInstitutionId", length = 11)
    private String AcqInstitutionId;

    @Column(name = "FwdInstitutionId", length = 11)
    private String FwdInstitutionId;

    @Column(name = "NetworkSourceId", length = 6)
    private String NetworkSourceId;

    @Column(name = "NetworkDestId", length = 6)
    private String NetworkDestId;

    @Column(name = "TxnIdentifier", length = 20)
    private String TxnIdentifier;

    @Column(name = "AuthResponseCode", length = 6)
    private String AuthResponseCode;

    @Column(name = "SrcChannelType", length = 1)
    private String SrcChannelType;

    @Column(name = "SrcRoutingId", length = 10)
    private String SrcRoutingId;

    @Column(name = "DestRoutingId", length = 10)
    private String DestRoutingId;

    @Column(name = "MandatoryCheckStatus", length = 1)
    private String MandatoryCheckStatus;

    @Column(name = "MerchantValidationStatus", length = 1)
    private String MerchantValidationStatus;

    @Column(name = "PVVVerficationStatus", length = 1)
    private String PVVVerficationStatus;

    @Column(name = "CVVVerficationStatus", length = 1)
    private String CVVVerficationStatus;

    @Column(name = "CVV2VerficationStatus", length = 1)
    private String CVV2VerficationStatus;

    @Column(name = "EMVValidationStatus", length = 1)
    private String EMVValidationStatus;

    @Column(name = "SwitchMode", length = 1)
    private String SwitchMode;

    @Column(name = "BinId", length = 10)
    private String BinId;

    @Column(name = "BIN", length = 6)
    private String BIN;

    @Column(name = "OriginalMTI", length = 4)
    private String OriginalMTI;

    @Column(name = "OriginalDateTime", length = 10)
    private String OriginalDateTime;

    @Column(name = "OriginalStan", length = 6)
    private String OriginalStan;

    @Column(name = "OriginalAcqInstId", length = 11)
    private String OriginalAcqInstId;

    @Column(name = "OriginalFwdInstId", length = 11)
    private String OriginalFwdInstId;

    @Column(name = "AuthIndicator", length = 1)
    private String AuthIndicator;

    @Column(name = "ECIndicator", length = 1)
    private String ECIndicator;

    @Column(name = "PaySecureIssuerId", length = 20)
    private String PaySecureIssuerId;

    @Column(name = "NetworkId")
    private Long NetworkId;

    @Column(name = "PreAuthTimeLimit", length = 10)
    private String PreAuthTimeLimit;

    @Column(name = "TerminalIssueId", length = 20)
    private String TerminalIssueId;

    @Column(name = "TerminalComment", length = 15)
    private String TerminalComment;

    @Column(name = "MerchantMobileNo", length = 20)
    private String MerchantMobileNo;

    @Column(name = "CardInfo", length = 4)
    private String CardInfo;

    @Column(name = "MatrixData", length = 100)
    private String MatrixData;

    @Column(name = "IntRefNo", length = 66)
    private String IntRefNo;

    @Column(name = "Urn", length = 15)
    private String Urn;

    @Column(name = "RefundId", length = 20)
    private String RefundId;

    @Column(name = "DeviceInvoiceNumber", length = 45)
    private String DeviceInvoiceNumber;

    @Column(name = "RequestRouteTime")
    private Date RequestRouteTime;

    public String getTxnCorrelationId() {
        return txnCorrelationId;
    }

    public void setTxnCorrelationId(String txnCorrelationId) {
        this.txnCorrelationId = txnCorrelationId;
    }

    public String getCardholderPan() {
        return cardholderPan;
    }

    public void setCardholderPan(String cardholderPan) {
        this.cardholderPan = cardholderPan;
    }

    public String getMerchantPan() {
        return merchantPan;
    }

    public void setMerchantPan(String merchantPan) {
        this.merchantPan = merchantPan;
    }

    public String getBeneficiaryPan() {
        return beneficiaryPan;
    }

    public void setBeneficiaryPan(String beneficiaryPan) {
        this.beneficiaryPan = beneficiaryPan;
    }

    public String getCardholderAccNo() {
        return cardholderAccNo;
    }

    public void setCardholderAccNo(String cardholderAccNo) {
        this.cardholderAccNo = cardholderAccNo;
    }

    public String getBeneficiaryAccNo() {
        return beneficiaryAccNo;
    }

    public void setBeneficiaryAccNo(String beneficiaryAccNo) {
        this.beneficiaryAccNo = beneficiaryAccNo;
    }

    public String getMTI() {
        return MTI;
    }

    public void setMTI(String MTI) {
        this.MTI = MTI;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getCardholderAccType() {
        return cardholderAccType;
    }

    public void setCardholderAccType(String cardholderAccType) {
        this.cardholderAccType = cardholderAccType;
    }

    public String getBeneficiaryAccType() {
        return beneficiaryAccType;
    }

    public void setBeneficiaryAccType(String beneficiaryAccType) {
        this.beneficiaryAccType = beneficiaryAccType;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String getEMV() {
        return EMV;
    }

    public void setEMV(String EMV) {
        this.EMV = EMV;
    }

    public Long getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Long expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getAadhaarNumber() {
        return aadhaarNumber;
    }

    public void setAadhaarNumber(String aadhaarNumber) {
        this.aadhaarNumber = aadhaarNumber;
    }

    public String getSuperInstId() {
        return SuperInstId;
    }

    public void setSuperInstId(String superInstId) {
        SuperInstId = superInstId;
    }

    public String getInstitutionId() {
        return InstitutionId;
    }

    public void setInstitutionId(String institutionId) {
        InstitutionId = institutionId;
    }

    public String getSponsorBankId() {
        return SponsorBankId;
    }

    public void setSponsorBankId(String sponsorBankId) {
        SponsorBankId = sponsorBankId;
    }

    public String getSuperMerchantId() {
        return SuperMerchantId;
    }

    public void setSuperMerchantId(String superMerchantId) {
        SuperMerchantId = superMerchantId;
    }

    public String getGroupMerchantId() {
        return GroupMerchantId;
    }

    public void setGroupMerchantId(String groupMerchantId) {
        GroupMerchantId = groupMerchantId;
    }

    public String getMerchantId() {
        return MerchantId;
    }

    public void setMerchantId(String merchantId) {
        MerchantId = merchantId;
    }

    public String getSubMerchantId() {
        return SubMerchantId;
    }

    public void setSubMerchantId(String subMerchantId) {
        SubMerchantId = subMerchantId;
    }

    public String getTerminalId() {
        return TerminalId;
    }

    public void setTerminalId(String terminalId) {
        TerminalId = terminalId;
    }

    public String getCardHolderCountryCode() {
        return CardHolderCountryCode;
    }

    public void setCardHolderCountryCode(String cardHolderCountryCode) {
        CardHolderCountryCode = cardHolderCountryCode;
    }

    public String getTerminalAccNo() {
        return TerminalAccNo;
    }

    public void setTerminalAccNo(String terminalAccNo) {
        TerminalAccNo = terminalAccNo;
    }

    public String getTerminalCardReadCapability() {
        return TerminalCardReadCapability;
    }

    public void setTerminalCardReadCapability(String terminalCardReadCapability) {
        TerminalCardReadCapability = terminalCardReadCapability;
    }

    public String getTerminalAuthReadCapability() {
        return TerminalAuthReadCapability;
    }

    public void setTerminalAuthReadCapability(String terminalAuthReadCapability) {
        TerminalAuthReadCapability = terminalAuthReadCapability;
    }

    public String getTerminalCardInputMode() {
        return TerminalCardInputMode;
    }

    public void setTerminalCardInputMode(String terminalCardInputMode) {
        TerminalCardInputMode = terminalCardInputMode;
    }

    public String getTerminalOpMode() {
        return TerminalOpMode;
    }

    public void setTerminalOpMode(String terminalOpMode) {
        TerminalOpMode = terminalOpMode;
    }

    public String getTerminalOpEnv() {
        return TerminalOpEnv;
    }

    public void setTerminalOpEnv(String terminalOpEnv) {
        TerminalOpEnv = terminalOpEnv;
    }

    public String getTerminalCardCaptureCapability() {
        return TerminalCardCaptureCapability;
    }

    public void setTerminalCardCaptureCapability(String terminalCardCaptureCapability) {
        TerminalCardCaptureCapability = terminalCardCaptureCapability;
    }

    public String getTerminalCardPresentStatus() {
        return TerminalCardPresentStatus;
    }

    public void setTerminalCardPresentStatus(String terminalCardPresentStatus) {
        TerminalCardPresentStatus = terminalCardPresentStatus;
    }

    public String getTerminalCardHolderPresentStatus() {
        return TerminalCardHolderPresentStatus;
    }

    public void setTerminalCardHolderPresentStatus(String terminalCardHolderPresentStatus) {
        TerminalCardHolderPresentStatus = terminalCardHolderPresentStatus;
    }

    public String getTerminalOuptutCapability() {
        return TerminalOuptutCapability;
    }

    public void setTerminalOuptutCapability(String terminalOuptutCapability) {
        TerminalOuptutCapability = terminalOuptutCapability;
    }

    public String getTerminalPINCaptureCapability() {
        return TerminalPINCaptureCapability;
    }

    public void setTerminalPINCaptureCapability(String terminalPINCaptureCapability) {
        TerminalPINCaptureCapability = terminalPINCaptureCapability;
    }

    public String getTerminalAddress() {
        return TerminalAddress;
    }

    public void setTerminalAddress(String terminalAddress) {
        TerminalAddress = terminalAddress;
    }

    public String getTerminalCity() {
        return TerminalCity;
    }

    public void setTerminalCity(String terminalCity) {
        TerminalCity = terminalCity;
    }

    public String getTerminalStateCode() {
        return TerminalStateCode;
    }

    public void setTerminalStateCode(String terminalStateCode) {
        TerminalStateCode = terminalStateCode;
    }

    public String getTerminalCountryCode() {
        return TerminalCountryCode;
    }

    public void setTerminalCountryCode(String terminalCountryCode) {
        TerminalCountryCode = terminalCountryCode;
    }

    public String getTerminalType() {
        return TerminalType;
    }

    public void setTerminalType(String terminalType) {
        TerminalType = terminalType;
    }

    public String getPANEntryMode() {
        return PANEntryMode;
    }

    public void setPANEntryMode(String PANEntryMode) {
        this.PANEntryMode = PANEntryMode;
    }

    public String getPINEntryCapability() {
        return PINEntryCapability;
    }

    public void setPINEntryCapability(String PINEntryCapability) {
        this.PINEntryCapability = PINEntryCapability;
    }

    public String getPOSConditionCode() {
        return POSConditionCode;
    }

    public void setPOSConditionCode(String POSConditionCode) {
        this.POSConditionCode = POSConditionCode;
    }

    public String getMerchantName() {
        return MerchantName;
    }

    public void setMerchantName(String merchantName) {
        MerchantName = merchantName;
    }

    public String getMerchantCity() {
        return MerchantCity;
    }

    public void setMerchantCity(String merchantCity) {
        MerchantCity = merchantCity;
    }

    public String getMerchantStateCode() {
        return MerchantStateCode;
    }

    public void setMerchantStateCode(String merchantStateCode) {
        MerchantStateCode = merchantStateCode;
    }

    public String getMerchantCountryCode() {
        return MerchantCountryCode;
    }

    public void setMerchantCountryCode(String merchantCountryCode) {
        MerchantCountryCode = merchantCountryCode;
    }

    public String getMerchantCategoryCode() {
        return MerchantCategoryCode;
    }

    public void setMerchantCategoryCode(String merchantCategoryCode) {
        MerchantCategoryCode = merchantCategoryCode;
    }

    public String getMerchantType() {
        return MerchantType;
    }

    public void setMerchantType(String merchantType) {
        MerchantType = merchantType;
    }

    public String getMerchantFraudScore() {
        return MerchantFraudScore;
    }

    public void setMerchantFraudScore(String merchantFraudScore) {
        MerchantFraudScore = merchantFraudScore;
    }

    public String getBillOrInvoiceNumber() {
        return BillOrInvoiceNumber;
    }

    public void setBillOrInvoiceNumber(String billOrInvoiceNumber) {
        BillOrInvoiceNumber = billOrInvoiceNumber;
    }

    public String getTxnDateTime() {
        return TxnDateTime;
    }

    public void setTxnDateTime(String txnDateTime) {
        TxnDateTime = txnDateTime;
    }

    public String getTxnDate() {
        return TxnDate;
    }

    public void setTxnDate(String txnDate) {
        TxnDate = txnDate;
    }

    public String getTxnTime() {
        return TxnTime;
    }

    public void setTxnTime(String txnTime) {
        TxnTime = txnTime;
    }

    public String getRRNumber() {
        return RRNumber;
    }

    public void setRRNumber(String RRNumber) {
        this.RRNumber = RRNumber;
    }

    public String getBatchNumber() {
        return BatchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        BatchNumber = batchNumber;
    }

    public String getInvoiceNumber() {
        return InvoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        InvoiceNumber = invoiceNumber;
    }

    public String getTxnCurrencyCode() {
        return TxnCurrencyCode;
    }

    public void setTxnCurrencyCode(String txnCurrencyCode) {
        TxnCurrencyCode = txnCurrencyCode;
    }

    public String getTxnAmount() {
        return TxnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        TxnAmount = txnAmount;
    }

    public String getTxnBillingAmount() {
        return TxnBillingAmount;
    }

    public void setTxnBillingAmount(String txnBillingAmount) {
        TxnBillingAmount = txnBillingAmount;
    }

    public String getTxnFeeAmount() {
        return TxnFeeAmount;
    }

    public void setTxnFeeAmount(String txnFeeAmount) {
        TxnFeeAmount = txnFeeAmount;
    }

    public String getTxnFeeCurrencyCode() {
        return TxnFeeCurrencyCode;
    }

    public void setTxnFeeCurrencyCode(String txnFeeCurrencyCode) {
        TxnFeeCurrencyCode = txnFeeCurrencyCode;
    }

    public String getTxnFeeAmountType() {
        return TxnFeeAmountType;
    }

    public void setTxnFeeAmountType(String txnFeeAmountType) {
        TxnFeeAmountType = txnFeeAmountType;
    }

    public String getSettlementAmount() {
        return SettlementAmount;
    }

    public void setSettlementAmount(String settlementAmount) {
        SettlementAmount = settlementAmount;
    }

    public String getSettlementCurrencyCode() {
        return SettlementCurrencyCode;
    }

    public void setSettlementCurrencyCode(String settlementCurrencyCode) {
        SettlementCurrencyCode = settlementCurrencyCode;
    }

    public String getSettlementDate() {
        return SettlementDate;
    }

    public void setSettlementDate(String settlementDate) {
        SettlementDate = settlementDate;
    }

    public String getTxnAdditionalAmount() {
        return TxnAdditionalAmount;
    }

    public void setTxnAdditionalAmount(String txnAdditionalAmount) {
        TxnAdditionalAmount = txnAdditionalAmount;
    }

    public String getTxnAdditionalAmountType() {
        return TxnAdditionalAmountType;
    }

    public void setTxnAdditionalAmountType(String txnAdditionalAmountType) {
        TxnAdditionalAmountType = txnAdditionalAmountType;
    }

    public String getTxnReversalAmount() {
        return TxnReversalAmount;
    }

    public void setTxnReversalAmount(String txnReversalAmount) {
        TxnReversalAmount = txnReversalAmount;
    }

    public String getAdviseReasonCode() {
        return AdviseReasonCode;
    }

    public void setAdviseReasonCode(String adviseReasonCode) {
        AdviseReasonCode = adviseReasonCode;
    }

    public String getAcqInstitutionId() {
        return AcqInstitutionId;
    }

    public void setAcqInstitutionId(String acqInstitutionId) {
        AcqInstitutionId = acqInstitutionId;
    }

    public String getFwdInstitutionId() {
        return FwdInstitutionId;
    }

    public void setFwdInstitutionId(String fwdInstitutionId) {
        FwdInstitutionId = fwdInstitutionId;
    }

    public String getNetworkSourceId() {
        return NetworkSourceId;
    }

    public void setNetworkSourceId(String networkSourceId) {
        NetworkSourceId = networkSourceId;
    }

    public String getNetworkDestId() {
        return NetworkDestId;
    }

    public void setNetworkDestId(String networkDestId) {
        NetworkDestId = networkDestId;
    }

    public String getTxnIdentifier() {
        return TxnIdentifier;
    }

    public void setTxnIdentifier(String txnIdentifier) {
        TxnIdentifier = txnIdentifier;
    }

    public String getAuthResponseCode() {
        return AuthResponseCode;
    }

    public void setAuthResponseCode(String authResponseCode) {
        AuthResponseCode = authResponseCode;
    }

    public String getSrcChannelType() {
        return SrcChannelType;
    }

    public void setSrcChannelType(String srcChannelType) {
        SrcChannelType = srcChannelType;
    }

    public String getSrcRoutingId() {
        return SrcRoutingId;
    }

    public void setSrcRoutingId(String srcRoutingId) {
        SrcRoutingId = srcRoutingId;
    }

    public String getDestRoutingId() {
        return DestRoutingId;
    }

    public void setDestRoutingId(String destRoutingId) {
        DestRoutingId = destRoutingId;
    }

    public String getMandatoryCheckStatus() {
        return MandatoryCheckStatus;
    }

    public void setMandatoryCheckStatus(String mandatoryCheckStatus) {
        MandatoryCheckStatus = mandatoryCheckStatus;
    }

    public String getMerchantValidationStatus() {
        return MerchantValidationStatus;
    }

    public void setMerchantValidationStatus(String merchantValidationStatus) {
        MerchantValidationStatus = merchantValidationStatus;
    }

    public String getPVVVerficationStatus() {
        return PVVVerficationStatus;
    }

    public void setPVVVerficationStatus(String PVVVerficationStatus) {
        this.PVVVerficationStatus = PVVVerficationStatus;
    }

    public String getCVVVerficationStatus() {
        return CVVVerficationStatus;
    }

    public void setCVVVerficationStatus(String CVVVerficationStatus) {
        this.CVVVerficationStatus = CVVVerficationStatus;
    }

    public String getCVV2VerficationStatus() {
        return CVV2VerficationStatus;
    }

    public void setCVV2VerficationStatus(String CVV2VerficationStatus) {
        this.CVV2VerficationStatus = CVV2VerficationStatus;
    }

    public String getEMVValidationStatus() {
        return EMVValidationStatus;
    }

    public void setEMVValidationStatus(String EMVValidationStatus) {
        this.EMVValidationStatus = EMVValidationStatus;
    }

    public String getSwitchMode() {
        return SwitchMode;
    }

    public void setSwitchMode(String switchMode) {
        SwitchMode = switchMode;
    }

    public String getBinId() {
        return BinId;
    }

    public void setBinId(String binId) {
        BinId = binId;
    }

    public String getBIN() {
        return BIN;
    }

    public void setBIN(String BIN) {
        this.BIN = BIN;
    }

    public String getOriginalMTI() {
        return OriginalMTI;
    }

    public void setOriginalMTI(String originalMTI) {
        OriginalMTI = originalMTI;
    }

    public String getOriginalDateTime() {
        return OriginalDateTime;
    }

    public void setOriginalDateTime(String originalDateTime) {
        OriginalDateTime = originalDateTime;
    }

    public String getOriginalStan() {
        return OriginalStan;
    }

    public void setOriginalStan(String originalStan) {
        OriginalStan = originalStan;
    }

    public String getOriginalAcqInstId() {
        return OriginalAcqInstId;
    }

    public void setOriginalAcqInstId(String originalAcqInstId) {
        OriginalAcqInstId = originalAcqInstId;
    }

    public String getOriginalFwdInstId() {
        return OriginalFwdInstId;
    }

    public void setOriginalFwdInstId(String originalFwdInstId) {
        OriginalFwdInstId = originalFwdInstId;
    }

    public String getAuthIndicator() {
        return AuthIndicator;
    }

    public void setAuthIndicator(String authIndicator) {
        AuthIndicator = authIndicator;
    }

    public String getECIndicator() {
        return ECIndicator;
    }

    public void setECIndicator(String ECIndicator) {
        this.ECIndicator = ECIndicator;
    }

    public String getPaySecureIssuerId() {
        return PaySecureIssuerId;
    }

    public void setPaySecureIssuerId(String paySecureIssuerId) {
        PaySecureIssuerId = paySecureIssuerId;
    }

    public Long getNetworkId() {
        return NetworkId;
    }

    public void setNetworkId(Long networkId) {
        NetworkId = networkId;
    }

    public String getPreAuthTimeLimit() {
        return PreAuthTimeLimit;
    }

    public void setPreAuthTimeLimit(String preAuthTimeLimit) {
        PreAuthTimeLimit = preAuthTimeLimit;
    }

    public String getTerminalIssueId() {
        return TerminalIssueId;
    }

    public void setTerminalIssueId(String terminalIssueId) {
        TerminalIssueId = terminalIssueId;
    }

    public String getTerminalComment() {
        return TerminalComment;
    }

    public void setTerminalComment(String terminalComment) {
        TerminalComment = terminalComment;
    }

    public String getMerchantMobileNo() {
        return MerchantMobileNo;
    }

    public void setMerchantMobileNo(String merchantMobileNo) {
        MerchantMobileNo = merchantMobileNo;
    }

    public String getCardInfo() {
        return CardInfo;
    }

    public void setCardInfo(String cardInfo) {
        CardInfo = cardInfo;
    }

    public String getMatrixData() {
        return MatrixData;
    }

    public void setMatrixData(String matrixData) {
        MatrixData = matrixData;
    }

    public String getIntRefNo() {
        return IntRefNo;
    }

    public void setIntRefNo(String intRefNo) {
        IntRefNo = intRefNo;
    }

    public String getUrn() {
        return Urn;
    }

    public void setUrn(String urn) {
        Urn = urn;
    }

    public String getRefundId() {
        return RefundId;
    }

    public void setRefundId(String refundId) {
        RefundId = refundId;
    }

    public String getDeviceInvoiceNumber() {
        return DeviceInvoiceNumber;
    }

    public void setDeviceInvoiceNumber(String deviceInvoiceNumber) {
        DeviceInvoiceNumber = deviceInvoiceNumber;
    }

    public Date getRequestRouteTime() {
        return RequestRouteTime;
    }

    public void setRequestRouteTime(Date requestRouteTime) {
        RequestRouteTime = requestRouteTime;
    }
}
