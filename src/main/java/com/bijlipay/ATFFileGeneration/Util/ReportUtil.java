package com.bijlipay.ATFFileGeneration.Util;

import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ReportUtil {
    private static final Logger logger = LoggerFactory.getLogger(ReportUtil.class);
    @Value("${atf.file.updated.path}")
    private static String updatedAtfFilePath;

    public static void generateAtfFileData(List<Object[]> atfFIleOut, String[] atfFileHeader, File updatedAtfFile, String atfFileSheet) {
        logger.info("Creating CSV File -----{}", atfFileSheet);
        try {
            //creating csv file

            FileWriter fileWriter = new FileWriter(updatedAtfFile);
//            log.info("File Writer ---{}", fileWriter);
            CSVWriter csvWriter = new CSVWriter(fileWriter);
            csvWriter.writeNext(atfFileHeader);
//            log.info("Switch TXN CSV File Header Details ----{}", txnFileHeader);
            List<String[]> finalOut = new ArrayList<>();
            String[] out = null;
            for (Object[] obj : atfFIleOut) {       //writing data to sheet
//                String terminalId = String.valueOf(obj[0]);
//                String merchantId = String.valueOf(obj[1]);
//                String posDeviceId = String.valueOf(obj[2]);
//                String batchNumber = String.valueOf(obj[3]);
//                String cardHolderName = String.valueOf(obj[4]);
//                String maskedCardNumber = String.valueOf(obj[5]);
//                String transactionMode = String.valueOf(obj[6]);
//                String invoiceNumber = String.valueOf(obj[7]);
//                String acquirerBank = String.valueOf(obj[8]);
//                String cardType = String.valueOf(obj[9]);
//                String cardNetwork = String.valueOf(obj[10]);
//                String cardIssuerCountryCode = String.valueOf(obj[11]);
//                String amount = String.valueOf(obj[12]);
//                String responseCode = String.valueOf(obj[13]);
//                String rrn = String.valueOf(obj[14]);
//                String transactionAuthCode = String.valueOf(obj[15]);
//                logger.info("Transaction Date ---{}",obj[16]);
//                Date transDate = (Date) obj[16];
//                String transactionDate =null;
//                if(transDate !=null) {
//                     transactionDate = DateUtil.parseSimpleDate(transDate);
//                }else{
//                     transactionDate = "null";
//                }
//                logger.info("Response Date ---{}",obj[17]);
//                Date responseDate1 = (Date)obj[17];
//                String responseDate = null;
//                if(responseDate1 !=null){
//                     responseDate = DateUtil.parseSimpleDate(responseDate1);
//                }else{
//                     responseDate = "null";
//                }
                String transactionId = String.valueOf(obj[0]);
//                String orgTransactionID = String.valueOf(obj[19]);
//                String transactionType = String.valueOf(obj[20]);
//                String status = String.valueOf(obj[21]);
//                String Stan = String.valueOf(obj[22]);
//                String settlementMode = String.valueOf(obj[23]);
//                String settlementStatus = String.valueOf(obj[24]);
//                String ackStatus = String.valueOf(obj[25]);
//                String initStatus = String.valueOf(obj[26]);
//                String hostStatus = String.valueOf(obj[27]);
//                String reversalStatus = String.valueOf(obj[28]);
//                String voidStatus = String.valueOf(obj[29]);
                String responseDateCheck = String.valueOf(obj[1]);
                String voidAndSaleTxnIdCheckWithDate = String.valueOf(obj[2]);
                String reversalAndSaleTxnIdCheckWithDate = String.valueOf(obj[3]);
                String voidTxnResponseCodeCheck = String.valueOf(obj[4]);
                String reversalAndAckStatus = String.valueOf(obj[5]);
                String settledTxnWrongStatus = String.valueOf(obj[6]);
                String notSettledTxnWrongStatus = String.valueOf(obj[7]);
                String voidTxnOtherThanHostStatus = String.valueOf(obj[8]);
                String saleUpiNullValueStatus = String.valueOf(obj[9]);
                String voidReversalNullValueStatus = String.valueOf(obj[10]);
                String upiAndReversalTxnIdEqualStatus = String.valueOf(obj[11]);
                String saleTxnOnlyInitStatus = String.valueOf(obj[12]);


//                out = Arrays.asList(terminalId, merchantId, posDeviceId, batchNumber, cardHolderName, maskedCardNumber, transactionMode, invoiceNumber, acquirerBank, cardType, cardNetwork, cardIssuerCountryCode, amount, responseCode, rrn, transactionAuthCode, transactionDate, responseDate, transactionId, orgTransactionID,
//                        transactionType, status, Stan, settlementMode, settlementStatus,ackStatus,initStatus,hostStatus,reversalStatus,voidStatus,responseDateCheck,
//                        voidAndSaleTxnIdCheckWithDate,reversalAndSaleTxnIdCheckWithDate,upiAndSaleTxnIdCheck,voidTxnResponseCodeCheck,reversalAndAckStatus,
//                        settledTxnWrongStatus,notSettledTxnWrongStatus,voidTxnOtherThanHostStatus,saleUpiNullValueStatus,voidReversalNullValueStatus,upiTxnIdEqualStatus,saleTxnOnlyInitStatus).toArray(new String[0]);

                out = Arrays.asList( transactionId,responseDateCheck,
                        voidAndSaleTxnIdCheckWithDate,reversalAndSaleTxnIdCheckWithDate,voidTxnResponseCodeCheck,reversalAndAckStatus,
                        settledTxnWrongStatus,notSettledTxnWrongStatus,voidTxnOtherThanHostStatus,saleUpiNullValueStatus,voidReversalNullValueStatus,upiAndReversalTxnIdEqualStatus,saleTxnOnlyInitStatus).toArray(new String[0]);
                finalOut.add(out);
            }
            csvWriter.writeAll(finalOut);
            csvWriter.close();
            fileWriter.close();
            logger.info("CSV File Generated Successfully----!!!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void generateAtfFileDataDummy(List<Object[]> atfFIleOut, String[] atfFileHeader, String updatedAtfFile, String atfFileSheet,String[] ruleHeader) {
        logger.info("Creating CSV File -----{}", atfFileSheet);
        try {
            //creating csv file
            CSVWriter csvWriter = new CSVWriter(new FileWriter(updatedAtfFile,true));
            csvWriter.writeNext(ruleHeader);
            csvWriter.writeNext(atfFileHeader);
            List<String[]> finalOut = new ArrayList<>();
            String[] out = null;
            for (Object[] obj : atfFIleOut) {       //writing data to sheet
                String transactionId = String.valueOf(obj[0]);
//                String responseDateCheck = String.valueOf(obj[1]);
//                String voidAndSaleTxnIdCheckWithDate = String.valueOf(obj[2]);
//                String reversalAndSaleTxnIdCheckWithDate = String.valueOf(obj[3]);
//                String voidTxnResponseCodeCheck = String.valueOf(obj[4]);
//                String reversalAndAckStatus = String.valueOf(obj[5]);
//                String settledTxnWrongStatus = String.valueOf(obj[6]);
//                String notSettledTxnWrongStatus = String.valueOf(obj[7]);
//                String voidTxnOtherThanHostStatus = String.valueOf(obj[8]);
//                String saleUpiNullValueStatus = String.valueOf(obj[9]);
//                String voidReversalNullValueStatus = String.valueOf(obj[10]);
//                String upiAndReversalTxnIdEqualStatus = String.valueOf(obj[11]);
//                String saleTxnOnlyInitStatus = String.valueOf(obj[12]);


//                out = Arrays.asList(terminalId, merchantId, posDeviceId, batchNumber, cardHolderName, maskedCardNumber, transactionMode, invoiceNumber, acquirerBank, cardType, cardNetwork, cardIssuerCountryCode, amount, responseCode, rrn, transactionAuthCode, transactionDate, responseDate, transactionId, orgTransactionID,
//                        transactionType, status, Stan, settlementMode, settlementStatus,ackStatus,initStatus,hostStatus,reversalStatus,voidStatus,responseDateCheck,
//                        voidAndSaleTxnIdCheckWithDate,reversalAndSaleTxnIdCheckWithDate,upiAndSaleTxnIdCheck,voidTxnResponseCodeCheck,reversalAndAckStatus,
//                        settledTxnWrongStatus,notSettledTxnWrongStatus,voidTxnOtherThanHostStatus,saleUpiNullValueStatus,voidReversalNullValueStatus,upiTxnIdEqualStatus,saleTxnOnlyInitStatus).toArray(new String[0]);

                out = Arrays.asList( transactionId).toArray(new String[0]);
                finalOut.add(out);
            }
            csvWriter.writeAll(finalOut);
            csvWriter.close();
            logger.info("CSV File Generated Successfully----!!!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateAtfFileReportInTextFile(List<Object[]> atfFileOut, String[] atfFileHeader, String updatedAtfFile, String atfFileSheet, String[] ruleHeader) throws IOException {
        File file = new File(updatedAtfFile);
        FileWriter fw = new FileWriter(file, true);
        BufferedWriter bw = new BufferedWriter(fw);
        for(String ruleHeaders : ruleHeader){
            bw.write(ruleHeaders);
        }
        bw.newLine();
        for(String atfHeaders : atfFileHeader){
            bw.write(atfHeaders);
        }
        bw.newLine();
        for(Object[] ob :atfFileOut){
            bw.write(ob[0].toString());
            bw.newLine();
        }
        bw.newLine();
        bw.close();
        logger.info("Text File Generated Successfully");
    }




    public static void generateMissingDataCSVFile(List<Object[]> FileOut, String[] txnFileHeader, File file, String fileSheet) {
        logger.info("Creating CSV File -----{}", fileSheet);
        try {
            //creating csv file

            FileWriter fileWriter = new FileWriter(file);
//            log.info("File Writer ---{}", fileWriter);
            CSVWriter csvWriter = new CSVWriter(fileWriter);
            csvWriter.writeNext(txnFileHeader);
//            log.info("Switch TXN CSV File Header Details ----{}", txnFileHeader);
            List<String[]> finalOut = new ArrayList<>();
            String[] out = null;
            for (Object[] obj : FileOut) {       //writing data to sheet
                String MTI = String.valueOf(obj[0]);
                String TxnType = String.valueOf(obj[1]);
                String TerminalId = String.valueOf(obj[2]);
                String MerchantId = String.valueOf(obj[3]);
                String TxnDate = String.valueOf(obj[4]);
                String TxnTime = String.valueOf(obj[5]);
                String TxnAmount = String.valueOf(obj[6]);
                String TxnResponseCode = String.valueOf(obj[7]);
                String ResponseReceivedTime = String.valueOf(obj[8]);
                String RRNumber = String.valueOf(obj[9]);
                String Stan = String.valueOf(obj[10]);
                String InvoiceNumber = String.valueOf(obj[11]);
                String BatchNumber = String.valueOf(obj[12]);
                String Urn = String.valueOf(obj[13]);
                String AuthResponseCode = String.valueOf(obj[14]);
                String TxnAdditionalAmount =String.valueOf(obj[15]);
                String InstitutionId =String.valueOf(obj[16]);
                out = Arrays.asList(MTI, TxnType, TerminalId, MerchantId, TxnDate, TxnTime, TxnAmount, TxnResponseCode, ResponseReceivedTime, RRNumber, Stan, InvoiceNumber, BatchNumber, Urn, AuthResponseCode,TxnAdditionalAmount,InstitutionId).toArray(new String[0]);
                finalOut.add(out);
//                log.info("Final Out Data ----{}", finalOut);
            }
            csvWriter.writeAll(finalOut);
            csvWriter.close();
            fileWriter.close();
            logger.info("CSV File Generated Successfully----!!!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void generateAllTxnAndSettlementMissingDataCSVFile(List<Object[]> FileOut, String[] txnFileHeader, File file, String fileSheet) {
        logger.info("Creating CSV File -----{}", fileSheet);
        try {
            //creating csv file

            FileWriter fileWriter = new FileWriter(file);
//            log.info("File Writer ---{}", fileWriter);
            CSVWriter csvWriter = new CSVWriter(fileWriter);
            csvWriter.writeNext(txnFileHeader);
//            log.info("Switch TXN CSV File Header Details ----{}", txnFileHeader);
            List<String[]> finalOut = new ArrayList<>();
            String[] out = null;
            for (Object[] obj : FileOut) {       //writing data to sheet
                String terminalId = String.valueOf(obj[0]);
                String merchantId = String.valueOf(obj[1]);
                String posDeviceId = String.valueOf(obj[2]);
                String batchNumber = String.valueOf(obj[3]);
                String cardHolderName = String.valueOf(obj[4]);
                String maskedCardNumber = String.valueOf(obj[5]);
                String transactionMode = String.valueOf(obj[6]);
                String invoiceNumber = String.valueOf(obj[7]);
                String acquirerBank = String.valueOf(obj[8]);
                String cardType = String.valueOf(obj[9]);
                String cardNetwork = String.valueOf(obj[10]);
                String cardIssuerCountryCode = String.valueOf(obj[11]);
                String amount = String.valueOf(obj[12]);
                String responseCode = String.valueOf(obj[13]);
                String rrn = String.valueOf(obj[14]);
                String transactionAuthCode =String.valueOf(obj[15]);
                String transactionDate =String.valueOf(obj[16]);
                String responseDate =String.valueOf(obj[17]);
                String transactionId =String.valueOf(obj[18]);
                String orgTransactionID =String.valueOf(obj[19]);
                String transactionType =String.valueOf(obj[20]);
                String status =String.valueOf(obj[21]);
                String Stan =String.valueOf(obj[22]);
                String settlementMode =String.valueOf(obj[23]);
                String settlementStatus =String.valueOf(obj[24]);


                out = Arrays.asList(terminalId, merchantId, posDeviceId, batchNumber, cardHolderName, maskedCardNumber, transactionMode, invoiceNumber, acquirerBank, cardType, cardNetwork, cardIssuerCountryCode, amount, responseCode, rrn,transactionAuthCode,transactionDate,responseDate,transactionId,orgTransactionID,transactionType,status,Stan,settlementMode,settlementStatus).toArray(new String[0]);
                finalOut.add(out);
//                log.info("Final Out Data ----{}", finalOut);
            }
            csvWriter.writeAll(finalOut);
            csvWriter.close();
            fileWriter.close();
            logger.info("AllAndSettlement Missing Data CSV File Generated Successfully----!!!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
