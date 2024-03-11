package com.bijlipay.ATFFileGeneration.Util;

public class Constants {

    public static final String DateFormat ="yyyy-MM-dd";

    public static final String DateFormat1 ="yyyyddMM";

    public static final String DateFormat_Axis ="yyyy-MM-dd HH:mm:ss";



//    public static final String[] ATF_FILE_HEADER = {"terminalId","merchantId","posDeviceId","batchNumber","cardHolderName","maskedCardNumber","transactionMode","invoiceNumber","acquirerBank","cardType","cardNetwork","cardIssuerCountryCode","amount","responseCode","rrn","transactionAuthCode","transactionDate","responseDate","transactionId","orgTransactionID","transactionType","status","Stan","settlementMode","settlementStatus","ackStatus","initStatus","hostStatus","reversalStatus","voidStatus","responseDateCheck","voidAndSaleTxnIdCheckWithDate","reversalAndSaleTxnIdCheckWithDate","upiAndSaleTxnIdCheck","voidTxnResponseCodeCheck","reversalAndAckStatus","settledTxnWrongStatus","notSettledTxnWrongStatus","voidTxnOtherThanHostStatus","saleUpiNullValueStatus","voidReversalNullValueStatus","upiTxnIdEqualStatus","saleTxnOnlyInitStatus"};

    public static final String[] ATF_FILE_HEADER = {"terminalId","merchantId","posDeviceId","batchNumber","cardHolderName","maskedCardNumber","transactionMode","invoiceNumber","acquirerBank","cardType","cardNetwork","cardIssuerCountryCode","amount","responseCode","rrn","transactionAuthCode","transactionDate","responseDate","transactionId","orgTransactionID","transactionType","status","Stan","settlementMode","settlementStatus"};


    public static final String[] TXN_FILE_HEADER = {"MTI","TxnType","TerminalId","MerchantId","TxnDate","TxnTime","TxnAmount","TxnResponseCode","ResponseReceivedTime","RRNumber","Stan","InvoiceNumber","BatchNumber","Urn","AuthResponseCode","TxnAdditionalAmount","InstitutionId"};

    public static final String[] TXN_SETTLEMENT_FILE_HEADER = {"terminalId","merchantId","posDeviceId","batchNumber","cardHolderName","maskedCardNumber","transactionMode","invoiceNumber","acquirerBank","cardType","cardNetwork","cardIssuerCountryCode","amount","responseCode","rrn","transactionAuthCode","transactionDate","responseDate","transactionId","orgTransactionID","transactionType","status","Stan","settlementMode","settlementStatus"};

    public static final String[] TXN_SETTLEMENT_MISSING_FILE_HEADER = {"mid","tid","batch_no","invoice","stan","rrn","auth_code","amount","to_char","additional_amount","status"};

    public static final String[] ATF_FILE_HEADER1 = {"transactionId"};

    public static final String[] MISSING_TXN_HEADER = {"RRN"};

    public static final String[] ATF_RULE_COUNT = {"Count"};
    public static final String Sale = "Sale";
    public static final String UPI = "UPI";

    public static final String Void = "Void";

    public static final String Reversal = "Reversal";


    public static final String[] ATF_FILE_HEADER2 = {"transactionId","responseDateCheck","voidAndSaleTxnIdCheckWithDate","reversalAndSaleTxnIdCheckWithDate","voidTxnResponseCodeCheck","reversalAndAckStatus","settledTxnWrongStatus","notSettledTxnWrongStatus","voidTxnOtherThanHostStatus","saleUpiNullValueStatus","voidReversalNullValueStatus","upiAndReversalTxnIdEqualStatus","saleTxnOnlyInitStatus"};

    public static final String[] SENT_TO = {"muthupandi@bijlipay.co.in","txn.support@bijlipay.co.in","rameshkumarm@bijlipay.co.in","madhusuthanan@bijlipay.co.in","sarvesh@bijlipay.co.in","ramalingom.sundaram@bijlipay.co.in"};

    public static final String[] SENT_ATF_MAIL = {"muthupandi@bijlipay.co.in","txn.support@bijlipay.co.in","rameshkumarm@bijlipay.co.in","monikandan@bijlipay.co.in"};


//    public static final String[] SENT_TO = {"muthupandi@bijlipay.co.in"};

//    public static final String[] SENT_TO = {"muthupandi@bijlipay.co.in","txn.support@bijlipay.co.in","rameshkumarm@bijlipay.co.in","madhusuthanan@bijlipay.co.in","sarvesh@bijlipay.co.in","ramalingom.sundaram@bijlipay.co.in","monikandan@bijlipay.co.in","mohammedazaruddin.ba@bijlipay.co.in"};

    public static final String[] SENT_VALIDATED_ATF = {"muthupandi@bijlipay.co.in","txn.support@bijlipay.co.in","rameshkumarm@bijlipay.co.in","madhusuthanan@bijlipay.co.in","monikandan@bijlipay.co.in","mohammedazaruddin.ba@bijlipay.co.in"};

    public static final String[] SENT_TO_PHONEPE_REPORTS = {"muthupandi@bijlipay.co.in"};

    public static final String[] SENT_TOTAL_MAIL = {"muthupandi@bijlipay.co.in","txn.support@bijlipay.co.in","selvamuthukumar@bijlipay.co.in"};

//    public static final String[] SENT_TOTAL_MAIL = {"muthupandi@bijlipay.co.in"};


    public static final int SESSION_TIMEOUT = 15000;
    public static final int CHANNEL_TIMEOUT = 15000;

    public static final String SFTP_ATF_HOST = "192.168.4.200";
    public static final String SFTP_ATF_USERNAME = "uat1";
    public static final String SFTP_ATF_PASSWORD = "Uat@1234";
    public static final int SFTP_ATF_PORT = 22;


    public static final String Axis_Public_Key = "/var/www/html/axisKeys/axisPubKey/rgw.jwejws.uat.axisb.com-sscert_28.txt";
    public static final String Axis_Private_Key = "/var/www/html/axisKeys/axisPriKey/uatapp_bijlipay_co_in.key";
    public static final String Axis_Key_Store_File = "/var/www/html/axisKeys/axisStoreKey/BIJLIPAY.p12";

//    public static final String SFTP_Bijlipay_HOST = "118.185.235.22";
//    public static final String SFTP_Bijlipay_USERNAME = "bijlipay_internal";
//    public static final String SFTP_Bijlipay_PASSWORD = "i0gmj2LRqjLmkZwH";


    public static final String SFTP_Bijlipay_HOST = "192.168.4.200";
    public static final String SFTP_Bijlipay_USERNAME = "uat1";
    public static final String SFTP_Bijlipay_PASSWORD = "Uat@1234";
    public static final int SFTP_Bijlipay_PORT = 22;

    public static final String PATHSEPARATOR = "/";



}
