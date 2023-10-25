package com.bijlipay.ATFFileGeneration.Util;

public class Constants {

    public static final String DateFormat ="yyyy-MM-dd";

    public static final String DateFormat1 ="yyyyddMM";


//    public static final String[] ATF_FILE_HEADER = {"terminalId","merchantId","posDeviceId","batchNumber","cardHolderName","maskedCardNumber","transactionMode","invoiceNumber","acquirerBank","cardType","cardNetwork","cardIssuerCountryCode","amount","responseCode","rrn","transactionAuthCode","transactionDate","responseDate","transactionId","orgTransactionID","transactionType","status","Stan","settlementMode","settlementStatus","ackStatus","initStatus","hostStatus","reversalStatus","voidStatus","responseDateCheck","voidAndSaleTxnIdCheckWithDate","reversalAndSaleTxnIdCheckWithDate","upiAndSaleTxnIdCheck","voidTxnResponseCodeCheck","reversalAndAckStatus","settledTxnWrongStatus","notSettledTxnWrongStatus","voidTxnOtherThanHostStatus","saleUpiNullValueStatus","voidReversalNullValueStatus","upiTxnIdEqualStatus","saleTxnOnlyInitStatus"};

    public static final String[] TXN_FILE_HEADER = {"MTI","TxnType","TerminalId","MerchantId","TxnDate","TxnTime","TxnAmount","TxnResponseCode","ResponseReceivedTime","RRNumber","Stan","InvoiceNumber","BatchNumber","Urn","AuthResponseCode","TxnAdditionalAmount","InstitutionId"};

    public static final String[] TXN_SETTLEMENT_FILE_HEADER = {"terminalId","merchantId","posDeviceId","batchNumber","cardHolderName","maskedCardNumber","transactionMode","invoiceNumber","acquirerBank","cardType","cardNetwork","cardIssuerCountryCode","amount","responseCode","rrn","transactionAuthCode","transactionDate","responseDate","transactionId","orgTransactionID","transactionType","status","Stan","settlementMode","settlementStatus"};


    public static final String[] ATF_FILE_HEADER1 = {"transactionId"};

    public static final String Sale = "Sale";

    public static final String UPI = "UPI";

    public static final String Void = "Void";

    public static final String Reversal = "Reversal";


    public static final String[] ATF_FILE_HEADER2 = {"transactionId","responseDateCheck","voidAndSaleTxnIdCheckWithDate","reversalAndSaleTxnIdCheckWithDate","voidTxnResponseCodeCheck","reversalAndAckStatus","settledTxnWrongStatus","notSettledTxnWrongStatus","voidTxnOtherThanHostStatus","saleUpiNullValueStatus","voidReversalNullValueStatus","upiAndReversalTxnIdEqualStatus","saleTxnOnlyInitStatus"};

    public static final String[] SENT_TO = {"muthupandi@bijlipay.co.in","txn.support@bijlipay.co.in","rameshkumarm@bijlipay.co.in","madhusuthanan@bijlipay.co.in","sarvesh@bijlipay.co.in"};

    public static final String[] SENT_TOTAL_MAIL = {"muthupandi@bijlipay.co.in","txn.support@bijlipay.co.in"};



}
