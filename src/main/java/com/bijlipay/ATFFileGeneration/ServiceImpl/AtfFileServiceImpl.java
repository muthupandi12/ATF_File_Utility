package com.bijlipay.ATFFileGeneration.ServiceImpl;

import com.bijlipay.ATFFileGeneration.Model.*;
import com.bijlipay.ATFFileGeneration.Model.Dto.AxisDto;
import com.bijlipay.ATFFileGeneration.Model.Dto.DateDto;
import com.bijlipay.ATFFileGeneration.Repository.*;
import com.bijlipay.ATFFileGeneration.Service.AtfFileService;
import com.bijlipay.ATFFileGeneration.Util.*;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.nimbusds.jose.JOSEException;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.bijlipay.ATFFileGeneration.Util.Constants.*;

@Service
public class AtfFileServiceImpl implements AtfFileService {

    public static final String DOUBLE_VALUE = "DOUBLE ENTRY";
    public static final String SINGLE_VALUE = "SINGLE ENTRY";

    private JSch mJschSession = null;
    private Session mSSHSession = null;
    private ChannelSftp mChannelSftp = null;

    @Autowired
    RestTemplate restTemplate;


    @Autowired
    private AtfFileRepository atfFileRepository;

    @Autowired
    private SettlementFileRepository settlementFileRepository;

    @Autowired
    private TransactionListRepository transactionListRepository;

    @Autowired
    private TxnListMainTotalRepository txnListMainTotalRepository;

    @Autowired
    private PhonePeSettlementDataRepository phonePeSettlementDataRepository;

    @Value("${atf.file.updated.path}")
    private String updatedAtfFilePath;

    @Value("${switch.datasource.url}")
    private String switchUrl;
    @Value("${switch.datasource.username}")
    private String username;
    @Value("${switch.datasource.password}")
    private String password;

    @Value("${atf.datasource.url}")
    private String atfUrl;
    @Value("${atf.datasource.username}")
    private String atfUserName;
    @Value("${atf.datasource.password}")
    private String atfPassword;

    private static final Logger logger = LoggerFactory.getLogger(AtfFileServiceImpl.class);

    @Override
    public boolean updateDataIntoDb(String atfFile) {
        boolean updated = false;
        // get the csv file
        List<AtfFileReport> allTxnFiles = new ArrayList<>();
        String fileExtension = "";
        int index = atfFile.lastIndexOf(".");
        if (index > 0) {
            fileExtension = atfFile.substring(index + 1);
            if (fileExtension.equals("csv")) {
                try {
                    CSVReader csvReader = new CSVReaderBuilder(new FileReader(atfFile)).withSkipLines(1).build();
                    List<String[]> csvData = csvReader.readAll();
                    int dataCount = atfFileRepository.findTotalInsertedData();
                    logger.info("Inserted Data Count--{}", dataCount);
                    if (dataCount < csvData.size()) {
                        for (int i = dataCount; i < csvData.size(); i++) {
                            String[] element = csvData.get(i);
                            logger.info("Enter to insert All Txn Data--{}", element[18]);
                            AtfFileReport allTxnFile1 = new AtfFileReport();
                            allTxnFile1.setTerminalId(element[0] != null ? element[0] : "");
                            allTxnFile1.setMerchantId(element[1] != null ? element[1] : "");
                            allTxnFile1.setPosDeviceId(element[2] != null ? element[2] : "");
                            allTxnFile1.setBatchNumber(element[3] != null ? element[3] : "");
                            allTxnFile1.setCardHolderName(element[4] != null ? element[4] : "");
                            allTxnFile1.setMaskedCardNumber(element[5] != null ? element[5] : "");
                            allTxnFile1.setTransactionMode(element[6] != null ? element[6] : "");
                            allTxnFile1.setInvoiceNumber(element[7] != null ? element[7] : "");
                            allTxnFile1.setAcquireBank(element[8] != null ? element[8] : "");
                            allTxnFile1.setCardType(element[9] != null ? element[9] : "");
                            allTxnFile1.setCardNetwork(element[10] != null ? element[10] : "");
                            allTxnFile1.setCardIssuerCountryCode(element[11] != null ? element[11] : "");
                            allTxnFile1.setAmount(element[12] != null ? element[12] : "");
                            allTxnFile1.setResponseCode(element[13] != null ? element[13] : "");
                            allTxnFile1.setRrn(element[14] != null ? element[14] : "");
                            allTxnFile1.setTransactionAuthCode(element[15] != null ? element[15] : "");
                            if (element[16].equals("") || element[16].length() < 12) {
                                allTxnFile1.setTransactionDate(null);
                            } else {
                                allTxnFile1.setTransactionDate(DateUtil.stringToDate(element[16]));
                            }
                            if (element[17].equals("") || element[17].length() < 12) {
                                allTxnFile1.setResponseDate(null);
                            } else {
                                allTxnFile1.setResponseDate(DateUtil.stringToDate(element[17]));
                            }
                            allTxnFile1.setTransactionId(element[18] != null ? element[18] : "");
                            allTxnFile1.setOrgTransactionId(element[19] != null ? element[19] : "");
                            allTxnFile1.setTransactionType(element[20] != null ? element[20] : "");
                            allTxnFile1.setStatus(element[21] != null ? element[21] : "");
                            allTxnFile1.setStan(element[22] != null ? element[22] : "");
                            allTxnFile1.setSettlementMode(element[23] != null ? element[23] : "");
                            allTxnFile1.setSettlementStatus(element[24] != null ? element[24] : "");
                            if (element[21].equals("ACK")) {
                                allTxnFile1.setAckStatus(true);
                            }
                            if (element[21].equals("HOST")) {
                                allTxnFile1.setHostStatus(true);
                            }
                            if (element[21].equals("INIT")) {
                                allTxnFile1.setInitStatus(true);
                            }
                            allTxnFiles.add(allTxnFile1);
                        }
                    }
                    csvReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    atfFileRepository.saveAll(allTxnFiles);
                    updated = true;
//                    logger.info("AllTxn File Data Inserted Successfully----!!!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return updated;
    }

    @Override
    public Boolean removeDataInDB() {
        atfFileRepository.removeATFFileData();
        txnListMainTotalRepository.removeTxnListData();
        return true;
    }

    @Override
    public void generateAtfFileReport(String date) throws IOException, ParseException {
        String updatedAtfFile = updatedAtfFilePath + "/ATF_Rules_Executed_Report-" + date + ".txt";
        String atfFileSheet = "ATF_Rules_Executed_Report-" + date + ".txt";
        List<Object[]> atf = null;
        for (int i = 0; i < 18; i++) {
            String[] addressesArr = new String[1];
            if (i == 0) {
                addressesArr[0] = "Rule 1- SALE & UPI & VOID & Reversal ResponseDate lesser than transactionDate";
                atf = atfFileRepository.findByAtfFileDataRule1();
            } else if (i == 1) {
                addressesArr[0] = "Rule 2- Sale transaction with only INIT and no corresponding Reversal";
                atf = atfFileRepository.findByAtfFileDataRule2();

            } else if (i == 2) {
                addressesArr[0] = "Rule 3- SALE & UPI in Host and Ack status should have the following fields  - RRN, AuthCode, responseCode, transactionDate, responseDate, Stan, MID, TID, PosDeviceID, Invoice No, batch No, amount, txntype, TransactionID";
                atf = atfFileRepository.findByAtfFileDataRule3();

            } else if (i == 3) {
                addressesArr[0] = "Rule 4- VOID and Reversal should have the following fields  - Masked card number, Card holder name ,RRN, AuthCode, responseCode, transactionDate, responseDate, Stan, MID, TID, PosDeviceID, Invoice No, batch No, amount, txntype, TransactionID, OrginalTransactionID";
                atf = atfFileRepository.findByAtfFileDataRule4();
            } else if (i == 4) {
                addressesArr[0] = "Rule 5- VOID txns OriginalTransactionID should not match with a SALE Txn ID in same file with different date";
                atf = atfFileRepository.findByAtfFileDataRule5();
            } else if (i == 5) {
                addressesArr[0] = "Rule 6- SALE Reversal txns OriginalTransactionID  & Txn ID should not match with a SALE Txn ID in same file with different date";
                atf = atfFileRepository.findByAtfFileDataRule6();

            } else if (i == 6) {
                addressesArr[0] = "Rule 7- UPI txns Txn ID matched with a Reversal OriginalTransactionID in same file";
                atf = atfFileRepository.findByAtfFileDataRule7();

            } else if (i == 7) {
                addressesArr[0] = "Rule 8- Reversals corresponding SALE/ UPI transaction in ACK status";
                atf = atfFileRepository.findByAtfFileDataRule8();
            } else if (i == 8) {
                addressesArr[0] = "Rule 9- VOID transactions original txn should not be in ACK or HOST with responsecode 00";
                atf = atfFileRepository.findByAtfFileDataRule9();
            } else if (i == 9) {
                addressesArr[0] = "Rule 10- VOID transaction status other than HOST";
                atf = atfFileRepository.findByAtfFileDataRule10();
            } else if (i == 10) {
                addressesArr[0] = "Rule 11- SALE and UPI transactions marked as Not Settled corresponding ACK or HOST with responsecode 00";
                atf = atfFileRepository.findByAtfFileDataRule11();
            } else if (i == 11) {
                addressesArr[0] = "Rule 12- SALE and UPI transactions with ACK or HOST with responsecode 00 but not marked as settled with no corresponding VOID or Reversal";
                atf = atfFileRepository.findByAtfFileDataRule12();
            } else if (i == 12) {
                addressesArr[0] = "Rule 13- SALE and UPI transactions with ACK or HOST with responsecode 00 but marked as settled with corresponding VOID or Reversal";
                atf = atfFileRepository.findByAtfFileDataRule13();
            } else if (i == 13) {
                addressesArr[0] = "Rule 14- VOID or Reversal entry only available without corresponding SALE and UPI transactions";
                atf = atfFileRepository.findByAtfFileDataRule14();
            } else if (i == 14) {
                addressesArr[0] = "Rule 15- Response Date and Transaction Date Year MisMatch Data";
                atf = atfFileRepository.findByAtfFileDataRule15();
            } else if (i == 15) {
                addressesArr[0] = "Rule 16- SALE and UPI transactions with ACK or HOST with responsecode 00 but marked as not settled with corresponding VOID or Reversal in transactionDate before 23:00:00";
                atf = atfFileRepository.findByAtfFileDataRule16();
            } else if (i == 16) {
                addressesArr[0] = "Rule 17- Rules Not Verified because Data alignment issues Data";
                atf = atfFileRepository.findByAtfFileDataRule17();
            } else if (i == 17) {
                addressesArr[0] = "Rule 18- Host Failure Response with Reversal";
                atf = atfFileRepository.findByAtfFileDataRule18();
            }
//            ReportUtil.generateAtfFileDataDummy(atf, Constants.ATF_FILE_HEADER1, updatedAtfFile, atfFileSheet, addressesArr);
            ReportUtil.generateAtfFileReportInTextFile(atf, Constants.ATF_FILE_HEADER1, updatedAtfFile, atfFileSheet, addressesArr);
        }
    }

    @Override
    public List<AtfFileReport> getAtfFileData(Optional<String> searchTerm) {
        if (searchTerm.isPresent()) {
            return atfFileRepository.getDataWithSearchTerm(searchTerm.get());
        } else {
            return atfFileRepository.getAtfDataWithoutSearchTerm();
        }
    }

    @Override
    public AtfFileReport updateAtfDataBasedOnTransId(String transactionId, AtfFileReport atfRequest) {
        AtfFileReport atfFileReport = atfFileRepository.findByTransId(transactionId);
        atfFileReport.setStatus(atfFileReport.getStatus());
        atfFileReport.setTransactionType(atfRequest.getTransactionType());
        atfFileReport.setSettlementMode(atfFileReport.getSettlementMode());
        atfFileReport.setSettlementStatus(atfFileReport.getSettlementStatus());
        return atfFileRepository.save(atfFileReport);
    }

    @Override
    public List<AtfFileReport> updateDataBasedOnTransId(String requestDate) {
        List<String> transactionId = atfFileRepository.findAllTransId();
        List<String> voidOrReversalCase = atfFileRepository.findAllTransIdForVoidOrReversal();
        logger.info("Transaction Id List Size --{}", transactionId.size());
        logger.info("Void Or Reversal Transaction Id List Size --{}", transactionId.size());
        int count = 0;
        for (int i = 0; i < transactionId.size(); i += 500) {
            count++;
            List<String> sub = transactionId.subList(i, Math.min(transactionId.size(), i + 500));
            logger.info("Loop Count For Sale AND UPI with Last TransactionId--- {}---- {}", count, sub.get(sub.size() - 1));
            threadExecution(sub, requestDate);
        }
        for (int i = 0; i < voidOrReversalCase.size(); i += 500) {
            count++;
            List<String> sub = voidOrReversalCase.subList(i, Math.min(voidOrReversalCase.size(), i + 500));
            logger.info("Loop Count For Void AND Reversal with Last TransactionId--- {}---- {}", count, sub.get(sub.size() - 1));
            threadExecution1(sub);
        }
        return null;
    }


    public void threadExecution(List<String> transId, String requestDate) throws RuntimeException {
        List<AtfFileReport> totalList = atfFileRepository.findByTransIdTotalList(transId);
        logger.info("After Split up data Size --{}", totalList.size());
        List<AtfFileReport> singleEntry = new ArrayList<>();
        List<AtfFileReport> doubleEntry = new ArrayList<>();
        transId.forEach(l -> {
            List<AtfFileReport> update = totalList.stream().filter(t -> t.getTransactionId().equals(l) || t.getOrgTransactionId().equals(l)).collect(Collectors.toList());
            if (update.size() > 1) {
                logger.info("Double Transaction Id List --{}", update.size());
                update.get(0).setRulesVerifiedStatus(true);
                update.get(1).setRulesVerifiedStatus(true);
                if ((update.get(0).getTransactionType().equals("Void") || update.get(0).getTransactionType().equals("Reversal")) || (update.get(1).getTransactionType().equals("Void") || update.get(1).getTransactionType().equals("Reversal"))) {
                    if (update.get(0).getTransactionType().equals("Void") || update.get(1).getTransactionType().equals("Void")) {
                        if (update.get(0).getTransactionType().equals("Void")) {
                            if (update.get(1).getTransactionType().equals("Sale")) {
                                try {
                                    if ((update.get(0).getOrgTransactionId().equals(update.get(0).getTransactionId())) || (!(DateUtil.parseSimpleDateForRules(update.get(0).getTransactionDate()).equals(DateUtil.parseSimpleDateForRules(update.get(1).getTransactionDate()))))) {
                                        logger.info(DOUBLE_VALUE + " Checking Void And Sale Original & Txn Id ---{}", update.get(0).getTransactionId());
                                        update.get(0).setVoidAndSaleTxnIdCheckWithDate(true);
                                        update.get(1).setVoidAndSaleTxnIdCheckWithDate(true);
                                    }
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        } else {
                            if (update.get(1).getTransactionType().equals("Void")) {
                                if (update.get(0).getTransactionType().equals("Sale")) {
                                    try {
                                        if ((update.get(1).getOrgTransactionId().equals(update.get(1).getTransactionId())) || (!(DateUtil.parseSimpleDateForRules(update.get(0).getTransactionDate()).equals(DateUtil.parseSimpleDateForRules(update.get(1).getTransactionDate()))))) {
                                            logger.info(DOUBLE_VALUE + " Checking Void And Sale Original & Txn Id ---{}", update.get(0).getTransactionId());
                                            update.get(0).setVoidAndSaleTxnIdCheckWithDate(true);
                                            update.get(1).setVoidAndSaleTxnIdCheckWithDate(true);
                                        }
                                    } catch (ParseException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        }
                        if (update.get(0).getTransactionType().equals("Void")) {
                            if (((update.get(1).getStatus().equals("ACK") || update.get(1).getStatus().equals("HOST")) && !(update.get(1).getResponseCode().equals("00")))) {
                                logger.info(DOUBLE_VALUE + " Checking Void Corresponding Ack Or Host With ResponseCode ---{}", update.get(0).getTransactionId());
                                update.get(0).setVoidTxnResponseCodeCheck(true);
                                update.get(1).setVoidTxnResponseCodeCheck(true);
                            }
                        } else {
                            if (update.get(1).getTransactionType().equals("Void")) {
                                if (((update.get(0).getStatus().equals("ACK") || update.get(0).getStatus().equals("HOST")) && !(update.get(0).getResponseCode().equals("00")))) {
                                    logger.info(DOUBLE_VALUE + " Checking Void Corresponding Ack Or Host With ResponseCode ---{}", update.get(1).getTransactionId());
                                    update.get(0).setVoidTxnResponseCodeCheck(true);
                                    update.get(1).setVoidTxnResponseCodeCheck(true);
                                }
                            }
                        }
                        if (update.get(0).getTransactionType().equals("Void")) {
                            if (!(update.get(0).getStatus().equals("HOST"))) {
                                logger.info(DOUBLE_VALUE + " Checking Void Other than HOST Status ----{}", update.get(0).getTransactionId());
                                update.get(0).setVoidTxnOtherThanHostStatus(true);
                                update.get(1).setVoidTxnOtherThanHostStatus(true);
                            }
                        } else {
                            if (update.get(1).getTransactionType().equals("Void")) {
                                if (!(update.get(1).getStatus().equals("HOST"))) {
                                    logger.info(DOUBLE_VALUE + " Checking Void Other than HOST Status ----{}", update.get(0).getTransactionId());
                                    update.get(0).setVoidTxnOtherThanHostStatus(true);
                                    update.get(1).setVoidTxnOtherThanHostStatus(true);
                                }
                            }
                        }

                        if (update.get(0).getTransactionType().equals("Void")) {
                            if (update.get(1).getTransactionType().equals("Sale") || update.get(1).getTransactionType().equals("UPI")) {
                                if ((update.get(1).getStatus().equals("ACK") || update.get(1).getStatus().equals("HOST")) && (update.get(1).getResponseCode().equals("00") && update.get(1).getSettlementStatus().equals("Settled"))) {
                                    update.get(0).setNotSettledTxnWrongCorrespondingVoidOrReversal(true);
                                    update.get(1).setNotSettledTxnWrongCorrespondingVoidOrReversal(true);
                                }
                            }
                        } else {
                            if (update.get(1).getTransactionType().equals("Void")) {
                                if (update.get(0).getTransactionType().equals("Sale") || update.get(0).getTransactionType().equals("UPI")) {
                                    if ((update.get(0).getStatus().equals("ACK") || update.get(0).getStatus().equals("HOST")) && (update.get(1).getResponseCode().equals("00") && update.get(1).getSettlementStatus().equals("Settled"))) {
                                        update.get(0).setNotSettledTxnWrongCorrespondingVoidOrReversal(true);
                                        update.get(1).setNotSettledTxnWrongCorrespondingVoidOrReversal(true);
                                    }
                                }
                            }
                        }
                        update.get(0).setVoidStatus(true);
                        update.get(1).setVoidStatus(true);

                    }
                    if ((update.get(0).getTransactionType().equals("Reversal")) || (update.get(1).getTransactionType().equals("Reversal"))) {
                        update.get(0).setReversalStatus(true);
                        update.get(1).setReversalStatus(true);
                        if (update.get(0).getTransactionType().equals("Reversal")) {
                            if (update.get(1).getTransactionType().equals("Sale")) {
                                try {
                                    if ((!(update.get(0).getOrgTransactionId().equals(update.get(0).getTransactionId()))) || (!(DateUtil.parseSimpleDateForRules(update.get(0).getTransactionDate()).equals(DateUtil.parseSimpleDateForRules(update.get(1).getTransactionDate()))))) {
                                        logger.info(DOUBLE_VALUE + " Checking Reversal And Sale Original Txn Id ---{}", update.get(0).getTransactionId());
                                        update.get(0).setReversalAndSaleTxnIdCheckWithDate(true);
                                        update.get(1).setReversalAndSaleTxnIdCheckWithDate(true);
                                    }
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        } else {
                            if (update.get(1).getTransactionType().equals("Reversal")) {
                                if (update.get(0).getTransactionType().equals("Sale")) {
                                    try {
                                        if ((!(update.get(1).getOrgTransactionId().equals(update.get(1).getTransactionId()))) || (!(DateUtil.parseSimpleDateForRules(update.get(0).getTransactionDate()).equals(DateUtil.parseSimpleDateForRules(update.get(1).getTransactionDate()))))) {
                                            logger.info(DOUBLE_VALUE + " Checking Reversal And Sale Original Txn Id ---{}", update.get(0).getTransactionId());
                                            update.get(0).setReversalAndSaleTxnIdCheckWithDate(true);
                                            update.get(1).setReversalAndSaleTxnIdCheckWithDate(true);
                                        }
                                    } catch (ParseException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        }
                        if (update.get(0).getTransactionType().equals("Sale") || update.get(0).getTransactionType().equals("UPI")) {
                            if ((update.get(0).getStatus().equals("ACK"))) {
                                logger.info(DOUBLE_VALUE + " Checking Reversal Corresponding Ack Status Wrong---{}", update.get(0).getTransactionId());
                                update.get(0).setReversalAndAckStatus(true);
                                update.get(1).setReversalAndAckStatus(true);
                            }
                        } else {
                            if (update.get(1).getTransactionType().equals("Sale") || update.get(1).getTransactionType().equals("UPI")) {
                                if (update.get(1).getStatus().equals("ACK")) {
                                    logger.info(DOUBLE_VALUE + " Checking Reversal Corresponding Ack Status Wrong---{}", update.get(1).getTransactionId());
                                    update.get(0).setReversalAndAckStatus(true);
                                    update.get(1).setReversalAndAckStatus(true);
                                }
                            }
                        }
                        if (update.get(0).getTransactionType().equals("UPI")) {
                            if ((update.get(1).getTransactionId().equals(update.get(1).getOrgTransactionId()))) {
                                logger.info(DOUBLE_VALUE + " UPI And Reversal Txn Id Equal Checking----{}", update.get(0).getTransactionId());
                                update.get(0).setUpiAndReversalTxnIdEqualStatus(true);
                                update.get(1).setUpiAndReversalTxnIdEqualStatus(true);
                            }
                        } else {
                            if (update.get(1).getTransactionType().equals("UPI")) {
                                if ((update.get(0).getTransactionId().equals(update.get(0).getOrgTransactionId()))) {
                                    logger.info(DOUBLE_VALUE + " UPI And Reversal Txn Id Equal Checking----{}", update.get(0).getTransactionId());
                                    update.get(0).setUpiAndReversalTxnIdEqualStatus(true);
                                    update.get(1).setUpiAndReversalTxnIdEqualStatus(true);
                                }
                            }
                        }
                        if (update.get(0).getTransactionType().equals("Reversal") || update.get(0).getTransactionType().equals("Void")) {
                            try {
//                                logger.info("Date Original --{}",DateUtil.oneHourBeforeDate(update.get(1).getTransactionDate()));
//                                logger.info("Date After compare ---{}",(DateUtil.parseSimpleDateForRules(update.get(0).getTransactionDate()).equals(DateUtil.parseSimpleDateForRules(update.get(1).getTransactionDate()))));
//                                logger.info("Date Compare --{}",((update.get(1).getTransactionDate().before(DateUtil.oneHourBeforeDate(update.get(1).getTransactionDate())))));
                                if ((DateUtil.parseSimpleDateForRules(update.get(0).getTransactionDate()).equals(DateUtil.parseSimpleDateForRules(update.get(1).getTransactionDate()))) && (update.get(1).getTransactionDate().before(DateUtil.oneHourBeforeDate(update.get(1).getTransactionDate())))) {
                                    if (update.get(1).getTransactionType().equals("Sale") || update.get(1).getTransactionType().equals("UPI")) {
                                        if (((update.get(1).getStatus().equals("ACK") || update.get(1).getStatus().equals("HOST")) && update.get(1).getResponseCode().equals("00") && update.get(1).getSettlementStatus().equals("Settled")) || update.get(0).getSettlementStatus().equals("Settled")) {
                                            update.get(0).setNotSettledTxnWrongCorrespondingVoidOrReversal(true);
                                            update.get(1).setNotSettledTxnWrongCorrespondingVoidOrReversal(true);
                                        }
                                    }
                                } else {
                                    String oneHourDate = DateUtil.dateToString(update.get(1).getTransactionDate());
//                                    logger.info("One Hour Date --{}", oneHourDate);
//                                    logger.info("After One --{}",DateUtil.oneHourBeforeDate(update.get(1).getTransactionDate()));
                                    if (update.get(1).getTransactionDate().before(DateUtil.oneHourBeforeDate(update.get(1).getTransactionDate()))) {
                                        if (update.get(1).getTransactionType().equals("Sale") || update.get(1).getTransactionType().equals("UPI")) {
                                            if (((update.get(1).getStatus().equals("ACK") || update.get(1).getStatus().equals("HOST")) && update.get(1).getResponseCode().equals("00") && update.get(1).getSettlementStatus().equals("NotSettled")) || update.get(0).getSettlementStatus().equals("NotSettled")) {
                                                update.get(0).setSettledTxnWrongCorrespondingVoidOrReversal(true);
                                                update.get(1).setSettledTxnWrongCorrespondingVoidOrReversal(true);
                                            }
                                        }
                                    }
                                }
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            if (update.get(1).getTransactionType().equals("Reversal") || update.get(1).getTransactionType().equals("Void")) {
                                try {
                                    if ((DateUtil.parseSimpleDateForRules(update.get(0).getTransactionDate()).equals(DateUtil.parseSimpleDateForRules(update.get(1).getTransactionDate()))) && (update.get(0).getTransactionDate().before(DateUtil.oneHourBeforeDate(update.get(0).getTransactionDate())))) {
                                        if (update.get(0).getTransactionType().equals("Sale") || update.get(0).getTransactionType().equals("UPI")) {
                                            if (((update.get(0).getStatus().equals("ACK") || update.get(0).getStatus().equals("HOST")) && update.get(0).getResponseCode().equals("00") && update.get(0).getSettlementStatus().equals("Settled")) || update.get(1).getSettlementStatus().equals("Settled")) {
                                                update.get(0).setNotSettledTxnWrongCorrespondingVoidOrReversal(true);
                                                update.get(1).setNotSettledTxnWrongCorrespondingVoidOrReversal(true);
                                            }
                                        }
                                    } else {
                                        String oneHourDate = DateUtil.dateToString(update.get(0).getTransactionDate());
//                                        logger.info("One Hour Date --{}",oneHourDate);
//                                        logger.info("After One --{}",(update.get(0).getTransactionDate().before(DateUtil.oneHourBeforeDate(update.get(0).getTransactionDate()))));
                                        if (update.get(0).getTransactionDate().before(DateUtil.oneHourBeforeDate(update.get(0).getTransactionDate()))) {
                                            if (update.get(0).getTransactionType().equals("Sale") || update.get(0).getTransactionType().equals("UPI")) {
                                                if (((update.get(0).getStatus().equals("ACK") || update.get(0).getStatus().equals("HOST")) && update.get(0).getResponseCode().equals("00") && update.get(0).getSettlementStatus().equals("NotSettled")) || update.get(1).getSettlementStatus().equals("NotSettled")) {
                                                    update.get(0).setSettledTxnWrongCorrespondingVoidOrReversal(true);
                                                    update.get(1).setSettledTxnWrongCorrespondingVoidOrReversal(true);
                                                }
                                            }
                                        }
                                    }
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                    if (update.get(0).getTransactionType().equals("Reversal")) {
                        if ((update.get(0).getStatus().equals("INIT"))) {
                            if (update.get(1).getTransactionType().equals("Sale")) {
                                if (((update.get(0).getMaskedCardNumber() == null) || (update.get(0).getPosDeviceId() == null) || (update.get(0).getRrn() == null) || (update.get(0).getTransactionAuthCode() == null) || (update.get(0).getResponseCode() == null) || (update.get(0).getTransactionDate() == null) || (update.get(0).getResponseDate() == null) || (update.get(0).getStan() == null) || (update.get(0).getMerchantId() == null) || (update.get(0).getTerminalId() == null) || (update.get(0).getInvoiceNumber() == null) || (update.get(0).getBatchNumber() == null) || (update.get(0).getAmount() == null) || (update.get(0).getTransactionType() == null) || (update.get(0).getTransactionId() == null) || (update.get(0).getOrgTransactionId() == null)) ||
                                        ((update.get(0).getMaskedCardNumber().equals("null")) || (update.get(0).getPosDeviceId().equals("null")) || (update.get(0).getRrn().equals("null")) || (update.get(0).getTransactionAuthCode().equals("null")) || (update.get(0).getResponseCode().equals("null")) || (update.get(0).getTransactionDate().equals("null")) || (update.get(0).getResponseDate().equals("null")) || (update.get(0).getStan().equals("null")) || (update.get(0).getMerchantId().equals("null")) || (update.get(0).getTerminalId().equals("null")) || (update.get(0).getInvoiceNumber().equals("null")) || (update.get(0).getBatchNumber().equals("null")) || (update.get(0).getAmount().equals("null")) || (update.get(0).getTransactionType().equals("null")) || (update.get(0).getTransactionId().equals("null")) || (update.get(0).getOrgTransactionId().equals("null"))) ||
                                        ((update.get(0).getMaskedCardNumber().equals("")) || (update.get(0).getRrn().equals("")) || (update.get(0).getPosDeviceId().equals("")) || (update.get(0).getTransactionAuthCode().equals("")) || (update.get(0).getResponseCode().equals("")) || (update.get(0).getTransactionDate().equals("")) || (update.get(0).getResponseDate().equals("")) || (update.get(0).getStan().equals("")) || (update.get(0).getMerchantId().equals("")) || (update.get(0).getTerminalId().equals("")) || (update.get(0).getInvoiceNumber().equals("")) || (update.get(0).getBatchNumber().equals("")) || (update.get(0).getAmount().equals("")) || (update.get(0).getTransactionType().equals("")) || (update.get(0).getTransactionId().equals("")) || (update.get(0).getOrgTransactionId().equals("")))) {
                                    logger.info(DOUBLE_VALUE + " Checking Void and Reversal Null Values ---{}", update.get(0).getTransactionId());
                                    update.get(0).setVoidReversalNullValueStatus(true);
                                    update.get(1).setVoidReversalNullValueStatus(true);
                                }
                                if((update.get(1).getStatus().equals("HOST")) && (!update.get(1).getResponseCode().equals("00"))){
                                    update.get(0).setHostFailureWithReversal(true);
                                    update.get(1).setHostFailureWithReversal(true);
                                }
                            } else if (update.get(1).getTransactionType().equals("UPI")) {
                                if (((update.get(0).getRrn() == null) || (update.get(0).getResponseCode() == null) || (update.get(0).getTransactionDate() == null) || (update.get(0).getResponseDate() == null) || (update.get(0).getStan() == null) || (update.get(0).getPosDeviceId() == null) || (update.get(0).getMerchantId() == null) || (update.get(0).getTerminalId() == null) || (update.get(0).getInvoiceNumber() == null) || (update.get(0).getBatchNumber() == null) || (update.get(0).getAmount() == null) || (update.get(0).getTransactionType() == null) || (update.get(0).getTransactionId() == null) || (update.get(0).getOrgTransactionId() == null)) ||
                                        ((update.get(0).getRrn().equals("null")) || (update.get(0).getTransactionDate().equals("null")) || (update.get(0).getResponseDate().equals("null")) || (update.get(0).getStan().equals("null")) || (update.get(0).getPosDeviceId().equals("null")) || (update.get(0).getMerchantId().equals("null")) || (update.get(0).getTerminalId().equals("null")) || (update.get(0).getInvoiceNumber().equals("null")) || (update.get(0).getBatchNumber().equals("null")) || (update.get(0).getAmount().equals("null")) || (update.get(0).getTransactionType().equals("null")) || (update.get(0).getTransactionId().equals("null")) || (update.get(0).getOrgTransactionId().equals("null"))) ||
                                        ((update.get(0).getRrn().equals("")) || (update.get(0).getResponseCode().equals("")) || (update.get(0).getTransactionDate().equals("")) || (update.get(0).getResponseDate().equals("")) || (update.get(0).getStan().equals("")) || (update.get(0).getPosDeviceId().equals("")) || (update.get(0).getMerchantId().equals("")) || (update.get(0).getTerminalId().equals("")) || (update.get(0).getInvoiceNumber().equals("")) || (update.get(0).getBatchNumber().equals("")) || (update.get(0).getAmount().equals("")) || (update.get(0).getTransactionType().equals("")) || (update.get(0).getTransactionId().equals("")) || (update.get(0).getOrgTransactionId().equals("")))) {
                                    logger.info(DOUBLE_VALUE + " Checking Void and Reversal Null Values ---{}", update.get(0).getTransactionId());
                                    update.get(0).setVoidReversalNullValueStatus(true);
                                    update.get(1).setVoidReversalNullValueStatus(true);
                                }
                                if((update.get(1).getStatus().equals("HOST")) && (!update.get(1).getResponseCode().equals("00"))){
                                    update.get(0).setHostFailureWithReversal(true);
                                    update.get(1).setHostFailureWithReversal(true);
                                }
                            }
                        }
                    } else {
                        if (update.get(1).getTransactionType().equals("Reversal")) {
                            if ((update.get(1).getStatus().equals("INIT"))) {
                                if (update.get(0).getTransactionType().equals("Sale")) {
                                    if (((update.get(1).getMaskedCardNumber() == null) || (update.get(1).getRrn() == null) || (update.get(1).getPosDeviceId() == null) || (update.get(1).getTransactionAuthCode() == null) || (update.get(1).getResponseCode() == null) || (update.get(1).getTransactionDate() == null) || (update.get(1).getResponseDate() == null) || (update.get(1).getStan() == null) || (update.get(1).getMerchantId() == null) || (update.get(1).getTerminalId() == null) || (update.get(1).getInvoiceNumber() == null) || (update.get(1).getBatchNumber() == null) || (update.get(1).getAmount() == null) || (update.get(1).getTransactionType() == null) || (update.get(1).getTransactionId() == null) || (update.get(1).getOrgTransactionId() == null)) ||
                                            ((update.get(1).getMaskedCardNumber().equals("null")) || (update.get(1).getRrn().equals("null")) || (update.get(1).getPosDeviceId().equals("null")) || (update.get(1).getTransactionAuthCode().equals("null")) || (update.get(1).getResponseCode().equals("null")) || (update.get(1).getTransactionDate().equals("null")) || (update.get(1).getResponseDate().equals("null")) || (update.get(1).getStan().equals("null")) || (update.get(1).getMerchantId().equals("null")) || (update.get(1).getTerminalId().equals("null")) || (update.get(1).getInvoiceNumber().equals("null")) || (update.get(1).getBatchNumber().equals("null")) || (update.get(1).getAmount().equals("null")) || (update.get(1).getTransactionType().equals("null")) || (update.get(1).getTransactionId().equals("null")) || (update.get(1).getOrgTransactionId().equals("null"))) ||
                                            ((update.get(1).getMaskedCardNumber().equals("")) || (update.get(1).getRrn().equals("")) || (update.get(1).getPosDeviceId().equals("")) || (update.get(1).getTransactionAuthCode().equals("")) || (update.get(1).getResponseCode().equals("")) || (update.get(1).getTransactionDate().equals("")) || (update.get(1).getResponseDate().equals("")) || (update.get(1).getStan().equals("")) || (update.get(1).getMerchantId().equals("")) || (update.get(1).getTerminalId().equals("")) || (update.get(1).getInvoiceNumber().equals("")) || (update.get(1).getBatchNumber().equals("")) || (update.get(1).getAmount().equals("")) || (update.get(1).getTransactionType().equals("")) || (update.get(1).getTransactionId().equals("")) || (update.get(1).getOrgTransactionId().equals("")))) {
                                        logger.info(DOUBLE_VALUE + " Checking Void and Reversal Null Values ---{}", update.get(0).getTransactionId());
                                        update.get(0).setVoidReversalNullValueStatus(true);
                                        update.get(1).setVoidReversalNullValueStatus(true);
                                    }
                                    if((update.get(0).getStatus().equals("HOST")) && (!update.get(0).getResponseCode().equals("00"))){
                                        update.get(0).setHostFailureWithReversal(true);
                                        update.get(1).setHostFailureWithReversal(true);
                                    }
                                } else if (update.get(0).getTransactionType().equals("UPI")) {
                                    if (((update.get(1).getRrn() == null) || (update.get(1).getResponseCode() == null) || (update.get(1).getTransactionDate() == null) || (update.get(1).getResponseDate() == null) || (update.get(1).getStan() == null) || (update.get(1).getPosDeviceId() == null) || (update.get(1).getMerchantId() == null) || (update.get(1).getTerminalId() == null) || (update.get(1).getInvoiceNumber() == null) || (update.get(1).getBatchNumber() == null) || (update.get(1).getAmount() == null) || (update.get(1).getTransactionType() == null) || (update.get(1).getTransactionId() == null) || (update.get(1).getOrgTransactionId() == null)) ||
                                            ((update.get(1).getRrn().equals("null")) || (update.get(1).getTransactionDate().equals("null")) || (update.get(1).getResponseDate().equals("null")) || (update.get(1).getStan().equals("null")) || (update.get(1).getPosDeviceId().equals("null")) || (update.get(1).getMerchantId().equals("null")) || (update.get(1).getTerminalId().equals("null")) || (update.get(1).getInvoiceNumber().equals("null")) || (update.get(1).getBatchNumber().equals("null")) || (update.get(1).getAmount().equals("null")) || (update.get(1).getTransactionType().equals("null")) || (update.get(1).getTransactionId().equals("null")) || (update.get(1).getOrgTransactionId().equals("null"))) ||
                                            ((update.get(1).getRrn().equals("")) || (update.get(1).getResponseCode().equals("")) || (update.get(1).getTransactionDate().equals("")) || (update.get(1).getResponseDate().equals("")) || (update.get(1).getStan().equals("")) || (update.get(1).getPosDeviceId().equals("")) || (update.get(1).getMerchantId().equals("")) || (update.get(1).getTerminalId().equals("")) || (update.get(1).getInvoiceNumber().equals("")) || (update.get(1).getBatchNumber().equals("")) || (update.get(1).getAmount().equals("")) || (update.get(1).getTransactionType().equals("")) || (update.get(1).getTransactionId().equals("")) || (update.get(1).getOrgTransactionId().equals("")))) {
                                        logger.info(DOUBLE_VALUE + " Checking Void and Reversal Null Values ---{}", update.get(0).getTransactionId());
                                        update.get(0).setVoidReversalNullValueStatus(true);
                                        update.get(1).setVoidReversalNullValueStatus(true);
                                    }
                                    if((update.get(0).getStatus().equals("HOST")) && (!update.get(0).getResponseCode().equals("00"))){
                                        update.get(0).setHostFailureWithReversal(true);
                                        update.get(1).setHostFailureWithReversal(true);
                                    }
                                }
                            }
                        }
                    }
                    if (update.get(0).getTransactionType().equals("Void")) {
                        if (!(update.get(0).getStatus().equals("INIT"))) {
                            if (((update.get(0).getMaskedCardNumber() == null) || (update.get(0).getRrn() == null) || (update.get(0).getPosDeviceId() == null) || (update.get(0).getTransactionAuthCode() == null) || (update.get(0).getResponseCode() == null) || (update.get(0).getTransactionDate() == null) || (update.get(0).getResponseDate() == null) || (update.get(0).getStan() == null) || (update.get(0).getMerchantId() == null) || (update.get(0).getTerminalId() == null) || (update.get(0).getInvoiceNumber() == null) || (update.get(0).getBatchNumber() == null) || (update.get(0).getAmount() == null) || (update.get(0).getTransactionType() == null) || (update.get(0).getTransactionId() == null) || (update.get(0).getOrgTransactionId() == null)) ||
                                    ((update.get(0).getMaskedCardNumber().equals("null")) || (update.get(0).getRrn().equals("null")) || (update.get(0).getPosDeviceId().equals("null")) || (update.get(0).getTransactionAuthCode().equals("null")) || (update.get(0).getResponseCode().equals("null")) || (update.get(0).getTransactionDate().equals("null")) || (update.get(0).getResponseDate().equals("null")) || (update.get(0).getStan().equals("null")) || (update.get(0).getMerchantId().equals("null")) || (update.get(0).getTerminalId().equals("null")) || (update.get(0).getInvoiceNumber().equals("null")) || (update.get(0).getBatchNumber().equals("null")) || (update.get(0).getAmount().equals("null")) || (update.get(0).getTransactionType().equals("null")) || (update.get(0).getTransactionId().equals("null")) || (update.get(0).getOrgTransactionId().equals("null"))) ||
                                    ((update.get(0).getMaskedCardNumber().equals("")) || (update.get(0).getRrn().equals("")) || (update.get(0).getPosDeviceId().equals("")) || (update.get(0).getTransactionAuthCode().equals("")) || (update.get(0).getResponseCode().equals("")) || (update.get(0).getTransactionDate().equals("")) || (update.get(0).getResponseDate().equals("")) || (update.get(0).getStan().equals("")) || (update.get(0).getMerchantId().equals("")) || (update.get(0).getTerminalId().equals("")) || (update.get(0).getInvoiceNumber().equals("")) || (update.get(0).getBatchNumber().equals("")) || (update.get(0).getAmount().equals("")) || (update.get(0).getTransactionType().equals("")) || (update.get(0).getTransactionId().equals("")) || (update.get(0).getOrgTransactionId().equals("")))) {
                                logger.info(DOUBLE_VALUE + " Checking Void and Reversal Null Values ---{}", update.get(0).getTransactionId());
                                update.get(0).setVoidReversalNullValueStatus(true);
                                update.get(1).setVoidReversalNullValueStatus(true);
                            }
                        }
                    } else {
                        if (update.get(1).getTransactionType().equals("Void")) {
                            if (!(update.get(1).getStatus().equals("INIT"))) {
                                if (((update.get(1).getMaskedCardNumber() == null) || (update.get(1).getRrn() == null) || (update.get(1).getPosDeviceId() == null) || (update.get(1).getTransactionAuthCode() == null) || (update.get(1).getResponseCode() == null) || (update.get(0).getTransactionDate() == null) || (update.get(1).getResponseDate() == null) || (update.get(1).getStan() == null) || (update.get(1).getMerchantId() == null) || (update.get(1).getTerminalId() == null) || (update.get(1).getInvoiceNumber() == null) || (update.get(1).getBatchNumber() == null) || (update.get(1).getAmount() == null) || (update.get(1).getTransactionType() == null) || (update.get(1).getTransactionId() == null) || (update.get(1).getOrgTransactionId() == null)) ||
                                        ((update.get(1).getMaskedCardNumber().equals("null")) || (update.get(1).getRrn().equals("null")) || (update.get(1).getPosDeviceId().equals("null")) || (update.get(1).getTransactionAuthCode().equals("null")) || (update.get(1).getResponseCode().equals("null")) || (update.get(1).getTransactionDate().equals("null")) || (update.get(1).getResponseDate().equals("null")) || (update.get(1).getStan().equals("null")) || (update.get(1).getMerchantId().equals("null")) || (update.get(1).getTerminalId().equals("null")) || (update.get(1).getInvoiceNumber().equals("null")) || (update.get(1).getBatchNumber().equals("null")) || (update.get(1).getAmount().equals("null")) || (update.get(1).getTransactionType().equals("null")) || (update.get(1).getTransactionId().equals("null")) || (update.get(1).getOrgTransactionId().equals("null"))) ||
                                        ((update.get(1).getMaskedCardNumber().equals("")) || (update.get(1).getRrn().equals("")) || (update.get(1).getPosDeviceId().equals("")) || (update.get(1).getTransactionAuthCode().equals("")) || (update.get(1).getResponseCode().equals("")) || (update.get(1).getTransactionDate().equals("")) || (update.get(1).getResponseDate().equals("")) || (update.get(1).getStan().equals("")) || (update.get(1).getMerchantId().equals("")) || (update.get(1).getTerminalId().equals("")) || (update.get(1).getInvoiceNumber().equals("")) || (update.get(1).getBatchNumber().equals("")) || (update.get(1).getAmount().equals("")) || (update.get(1).getTransactionType().equals("")) || (update.get(1).getTransactionId().equals("")) || (update.get(1).getOrgTransactionId().equals("")))) {
                                    logger.info(DOUBLE_VALUE + " Checking Void and Reversal Null Values ---{}", update.get(1).getTransactionId());
                                    update.get(0).setVoidReversalNullValueStatus(true);
                                    update.get(1).setVoidReversalNullValueStatus(true);
                                }
                            }
                        }
                    }

                    if (update.get(0).getTransactionType().equals("Sale") || update.get(0).getTransactionType().equals("UPI") || update.get(0).getTransactionType().equals("Void")) {
                        if (!(update.get(1).getStatus().equals("INIT"))) {
                            if ((update.get(1).getResponseDate() == null) || (update.get(1).getTransactionDate() == null) || (update.get(0).getResponseDate() == null) || (update.get(0).getTransactionDate() == null)) {
                                update.get(0).setResponseDateCheck(true);
                                update.get(1).setResponseDateCheck(true);
                            } else if ((update.get(1).getResponseDate().before(update.get(1).getTransactionDate())) || (update.get(0).getResponseDate().before(update.get(0).getTransactionDate()))) {
                                update.get(0).setResponseDateCheck(true);
                                update.get(1).setResponseDateCheck(true);
                            }
                            if (update.get(0).getTransactionDate() != null && update.get(1).getTransactionDate() != null && update.get(0).getResponseDate() != null && update.get(1).getResponseDate() != null) {
                                String date = null;
                                String out = null;
                                try {
                                    date = DateUtil.dateComparison(DateUtil.currentDate());
                                    String index[] = requestDate.split("-");
//                                    logger.info("index1 {}----index2 {}",index[0],index[1]);
                                    out = index[0] + "-" + index[1];
//                                    logger.info("finalDate --{}",out);
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                                try {
                                    if ((!out.equals(DateUtil.dateComparison(update.get(0).getTransactionDate()))) || (!out.equals(DateUtil.dateComparison(update.get(0).getResponseDate()))) || (!out.equals(DateUtil.dateComparison(update.get(1).getTransactionDate()))) || (!out.equals(DateUtil.dateComparison(update.get(1).getResponseDate())))) {
                                        update.get(0).setResponseDateMismatch(true);
                                        update.get(1).setResponseDateMismatch(true);
                                    }
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                        }
                    } else {
                        if (update.get(1).getTransactionType().equals("Sale") || update.get(1).getTransactionType().equals("UPI") || update.get(1).getTransactionType().equals("Void")) {
                            if (!(update.get(0).getStatus().equals("INIT"))) {
                                if ((update.get(1).getResponseDate() == null) || (update.get(1).getTransactionDate() == null) || (update.get(0).getResponseDate() == null) || (update.get(0).getTransactionDate() == null)) {
                                    update.get(0).setResponseDateCheck(true);
                                    update.get(1).setResponseDateCheck(true);
                                } else if ((update.get(0).getResponseDate().before(update.get(0).getTransactionDate())) || (update.get(1).getResponseDate().before(update.get(1).getTransactionDate()))) {
                                    update.get(0).setResponseDateCheck(true);
                                    update.get(1).setResponseDateCheck(true);
                                }
                                if (update.get(0).getTransactionDate() != null && update.get(1).getTransactionDate() != null && update.get(0).getResponseDate() != null && update.get(1).getResponseDate() != null) {
                                    String date = null;
                                    String out = null;
                                    try {
                                        date = DateUtil.dateComparison(DateUtil.currentDate());
                                        String index[] = requestDate.split("-");
//                                        logger.info("index1 {}----index2 {}",index[0],index[1]);
                                        out = index[0] + "-" + index[1];
//                                        logger.info("finalDate --{}",out);
                                    } catch (ParseException e) {
                                        throw new RuntimeException(e);
                                    }
                                    try {
                                        if ((!out.equals(DateUtil.dateComparison(update.get(0).getTransactionDate()))) || (!out.equals(DateUtil.dateComparison(update.get(0).getResponseDate()))) || (!out.equals(DateUtil.dateComparison(update.get(1).getTransactionDate()))) || (!out.equals(DateUtil.dateComparison(update.get(1).getResponseDate())))) {
                                            update.get(0).setResponseDateMismatch(true);
                                            update.get(1).setResponseDateMismatch(true);
                                        }
                                    } catch (ParseException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        }
                    }
                    if (update.get(0).getTransactionType().equals("Reversal")) {
                        if (!(update.get(1).getStatus().equals("INIT")) && !(update.get(1).getTransactionType().equals("UPI"))) {
                            if ((update.get(1).getResponseDate() == null) || (update.get(1).getTransactionDate() == null) || (update.get(0).getResponseDate() == null) || (update.get(0).getTransactionDate() == null)) {
                                update.get(0).setResponseDateCheck(true);
                                update.get(1).setResponseDateCheck(true);
                            } else if (update.get(1).getResponseDate().before(update.get(1).getTransactionDate())) {
                                logger.info(DOUBLE_VALUE + " Compare Void ResponseDate and Transaction Date ---{}", update.get(0).getTransactionId());
                                update.get(0).setResponseDateCheck(true);
                                update.get(1).setResponseDateCheck(true);
                            }
                            if (update.get(0).getTransactionDate() != null && update.get(1).getTransactionDate() != null && update.get(0).getResponseDate() != null && update.get(1).getResponseDate() != null) {
                                String date = null;
                                String out = null;
                                try {
                                    date = DateUtil.dateComparison(DateUtil.currentDate());
                                    String index[] = requestDate.split("-");
//                                    logger.info("index1 {}----index2 {}",index[0],index[1]);
                                    out = index[0] + "-" + index[1];
//                                    logger.info("finalDate --{}",out);
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                                try {
                                    if ((!out.equals(DateUtil.dateComparison(update.get(0).getTransactionDate()))) || (!out.equals(DateUtil.dateComparison(update.get(0).getResponseDate()))) || (!out.equals(DateUtil.dateComparison(update.get(1).getTransactionDate()))) || (!out.equals(DateUtil.dateComparison(update.get(1).getResponseDate())))) {
                                        update.get(0).setResponseDateMismatch(true);
                                        update.get(1).setResponseDateMismatch(true);
                                    }
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    } else {
                        if (update.get(1).getTransactionType().equals("Reversal")) {
                            if (!(update.get(0).getStatus().equals("INIT")) && !(update.get(0).getTransactionType().equals("UPI"))) {
                                if ((update.get(0).getResponseDate() == null) || (update.get(0).getTransactionDate() == null) || (update.get(1).getResponseDate() == null) || (update.get(1).getTransactionDate() == null)) {
                                    update.get(0).setResponseDateCheck(true);
                                    update.get(1).setResponseDateCheck(true);
                                } else if ((update.get(0).getResponseDate().before(update.get(0).getTransactionDate()))) {
                                    logger.info(DOUBLE_VALUE + " Compare Void ResponseDate and Transaction Date ---{}", update.get(0).getTransactionId());
                                    update.get(0).setResponseDateCheck(true);
                                    update.get(1).setResponseDateCheck(true);
                                }
                                if (update.get(0).getTransactionDate() != null && update.get(1).getTransactionDate() != null && update.get(0).getResponseDate() != null && update.get(1).getResponseDate() != null) {
                                    String date = null;
                                    String out = null;
                                    try {
                                        date = DateUtil.dateComparison(DateUtil.currentDate());
                                        String index[] = requestDate.split("-");
//                                        logger.info("index1 {}----index2 {}",index[0],index[1]);
                                        out = index[0] + "-" + index[1];
//                                        logger.info("finalDate --{}",out);
                                    } catch (ParseException e) {
                                        throw new RuntimeException(e);
                                    }
                                    try {
                                        if ((!out.equals(DateUtil.dateComparison(update.get(0).getTransactionDate()))) || (!out.equals(DateUtil.dateComparison(update.get(0).getResponseDate()))) || (!out.equals(DateUtil.dateComparison(update.get(1).getTransactionDate()))) || (!out.equals(DateUtil.dateComparison(update.get(1).getResponseDate())))) {
                                            update.get(0).setResponseDateMismatch(true);
                                            update.get(1).setResponseDateMismatch(true);
                                        }
                                    } catch (ParseException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        }
                    }
                    doubleEntry.addAll(update);
                }

            } else {
                logger.info("Single Transaction Id List--{}", update.size());
                AtfFileReport fileReport = totalList.stream().filter(p -> p.getTransactionId().equals(l)).findAny().orElse(null);
                fileReport.setRulesVerifiedStatus(true);
                if (fileReport.getTransactionType().equals("Sale") || fileReport.getTransactionType().equals("UPI") || fileReport.getTransactionType().equals("Void")) {
                    if (!(fileReport.getStatus().equals("INIT"))) {
                        String date = null;
                        String out = null;
                        try {
                            date = DateUtil.dateComparison(DateUtil.currentDate());
                            String index[] = requestDate.split("-");
//                            logger.info("index1 {}----index2 {}",index[0],index[1]);
                            out = index[0] + "-" + index[1];
//                            logger.info("finalDate --{}",out);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                        if ((fileReport.getResponseDate() == null) || (fileReport.getTransactionDate() == null)) {
                            logger.info(SINGLE_VALUE + " Checking ResponseDate --{}", fileReport.getTransactionId());
                            fileReport.setResponseDateCheck(true);
                        } else if (fileReport.getResponseDate().before(fileReport.getTransactionDate())) {
                            logger.info(SINGLE_VALUE + " Checking ResponseDate --{}", fileReport.getTransactionId());
                            fileReport.setResponseDateCheck(true);
                        }
                        if (fileReport.getTransactionDate() != null && fileReport.getResponseDate() != null) {
                            try {
                                String date1 = DateUtil.dateComparison(fileReport.getTransactionDate());
//                                logger.info("Comparison Date --{}", date1);
                                if ((!out.equals(DateUtil.dateComparison(fileReport.getTransactionDate()))) || (!out.equals(DateUtil.dateComparison(fileReport.getResponseDate())))) {
                                    fileReport.setResponseDateMismatch(true);
                                }
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
                if (fileReport.getTransactionType().equals("Sale") || fileReport.getTransactionType().equals("UPI")) {
                    if (fileReport.getStatus().equals("ACK") || fileReport.getStatus().equals("HOST")) {
                        if (fileReport.getResponseCode().equals("00") && fileReport.getSettlementStatus().equals("NotSettled")) {
                            logger.info(SINGLE_VALUE + " Checking Settled Wrong Status -----{}", fileReport.getTransactionId());
                            fileReport.setSettledTxnWrongStatus(true);
                        } else if ((!fileReport.getResponseCode().equals("00")) && fileReport.getSettlementStatus().equals("Settled")) {
                            logger.info(SINGLE_VALUE + " Checking Not Settled Wrong Status for Sale&UPI-----");
                            fileReport.setNotSettledTxnWrongStatus(true);
                        }
                    }
                }
                if (fileReport.getTransactionType().equals("Sale")) {
                    if (!(fileReport.getStatus().equals("INIT"))) {
                        if (fileReport.getResponseCode().equals("00")) {
                            if ((fileReport.getRrn() == null || fileReport.getTransactionAuthCode() == null || fileReport.getResponseCode() == null || fileReport.getTransactionDate() == null || fileReport.getResponseDate() == null || fileReport.getStan() == null || fileReport.getPosDeviceId() == null || fileReport.getTerminalId() == null || fileReport.getMerchantId() == null || fileReport.getInvoiceNumber() == null || fileReport.getBatchNumber() == null || fileReport.getAmount() == null || fileReport.getTransactionType() == null || fileReport.getTransactionId() == null) ||
                                    (fileReport.getRrn().equals("null") || fileReport.getTransactionAuthCode().equals("null") || fileReport.getResponseCode().equals("null") || fileReport.getTransactionDate().equals("null") || fileReport.getResponseDate().equals("null") || fileReport.getStan().equals("null") || fileReport.getPosDeviceId().equals("null") || fileReport.getTerminalId().equals("null") || fileReport.getMerchantId().equals("null") || fileReport.getInvoiceNumber().equals("null") || fileReport.getBatchNumber().equals("null") || fileReport.getAmount().equals("null") || fileReport.getTransactionType().equals("null") || fileReport.getTransactionId().equals("null")) ||
                                    (fileReport.getRrn().equals("") || fileReport.getTransactionAuthCode().equals("") || fileReport.getResponseCode().equals("") || fileReport.getTransactionDate().equals("") || fileReport.getResponseDate().equals("") || fileReport.getStan().equals("") || fileReport.getPosDeviceId().equals("") || fileReport.getTerminalId().equals("") || fileReport.getMerchantId().equals("") || fileReport.getInvoiceNumber().equals("") || fileReport.getBatchNumber().equals("") || fileReport.getAmount().equals("") || fileReport.getTransactionType().equals("") || fileReport.getTransactionId().equals(""))) {
                                logger.info(SINGLE_VALUE + " checking Sale&UPI Required Fields is Null or Not---{}", fileReport.getTransactionId());
                                fileReport.setSaleUpiNullValueStatus(true);
                            }
                        } else {
                            if (!(fileReport.getResponseCode().equals("00"))) {
                                if ((fileReport.getResponseCode() == null || fileReport.getTransactionDate() == null || fileReport.getResponseDate() == null || fileReport.getStan() == null || fileReport.getPosDeviceId() == null || fileReport.getTerminalId() == null || fileReport.getMerchantId() == null || fileReport.getInvoiceNumber() == null || fileReport.getBatchNumber() == null || fileReport.getAmount() == null || fileReport.getTransactionType() == null || fileReport.getTransactionId() == null) ||
                                        (fileReport.getResponseCode().equals("null") || fileReport.getTransactionDate().equals("null") || fileReport.getResponseDate().equals("null") || fileReport.getStan().equals("null") || fileReport.getPosDeviceId().equals("null") || fileReport.getTerminalId().equals("null") || fileReport.getMerchantId().equals("null") || fileReport.getInvoiceNumber().equals("null") || fileReport.getBatchNumber().equals("null") || fileReport.getAmount().equals("null") || fileReport.getTransactionType().equals("null") || fileReport.getTransactionId().equals("null")) ||
                                        (fileReport.getResponseCode().equals("") || fileReport.getTransactionDate().equals("") || fileReport.getResponseDate().equals("") || fileReport.getStan().equals("") || fileReport.getPosDeviceId().equals("") || fileReport.getTerminalId().equals("") || fileReport.getMerchantId().equals("") || fileReport.getInvoiceNumber().equals("") || fileReport.getBatchNumber().equals("") || fileReport.getAmount().equals("") || fileReport.getTransactionType().equals("") || fileReport.getTransactionId().equals(""))) {
                                    logger.info(SINGLE_VALUE + " checking Sale&UPI Required Fields is Null or Not---{}", fileReport.getTransactionId());
                                    fileReport.setSaleUpiNullValueStatus(true);
                                }
                            }
                        }
                    }
                }

                if (fileReport.getTransactionType().equals("UPI")) {
                    if (!(fileReport.getStatus().equals("INIT"))) {
                        if (fileReport.getResponseCode().equals("00")) {
                            if ((fileReport.getRrn() == null || fileReport.getResponseCode() == null || fileReport.getTransactionDate() == null || fileReport.getResponseDate() == null || fileReport.getStan() == null || fileReport.getPosDeviceId() == null || fileReport.getTerminalId() == null || fileReport.getMerchantId() == null || fileReport.getInvoiceNumber() == null || fileReport.getBatchNumber() == null || fileReport.getAmount() == null || fileReport.getTransactionType() == null || fileReport.getTransactionId() == null) ||
                                    (fileReport.getRrn().equals("null") || fileReport.getTransactionDate().equals("null") || fileReport.getResponseDate().equals("null") || fileReport.getStan().equals("null") || fileReport.getPosDeviceId().equals("null") || fileReport.getTerminalId().equals("null") || fileReport.getMerchantId().equals("null") || fileReport.getInvoiceNumber().equals("null") || fileReport.getBatchNumber().equals("null") || fileReport.getAmount().equals("null") || fileReport.getTransactionType().equals("null") || fileReport.getTransactionId().equals("null")) ||
                                    (fileReport.getRrn().equals("") || fileReport.getResponseCode().equals("") || fileReport.getTransactionDate().equals("") || fileReport.getResponseDate().equals("") || fileReport.getStan().equals("") || fileReport.getPosDeviceId().equals("") || fileReport.getTerminalId().equals("") || fileReport.getMerchantId().equals("") || fileReport.getInvoiceNumber().equals("") || fileReport.getBatchNumber().equals("") || fileReport.getAmount().equals("") || fileReport.getTransactionType().equals("") || fileReport.getTransactionId().equals(""))) {
                                logger.info(SINGLE_VALUE + " checking Sale&UPI Required Fields is Null or Not---{}", fileReport.getTransactionId());
                                fileReport.setSaleUpiNullValueStatus(true);
                            }
                        } else {
                            if (!(fileReport.getResponseCode().equals("00"))) {
                                if ((fileReport.getResponseCode() == null || fileReport.getTransactionDate() == null || fileReport.getResponseDate() == null || fileReport.getStan() == null || fileReport.getPosDeviceId() == null || fileReport.getTerminalId() == null || fileReport.getMerchantId() == null || fileReport.getInvoiceNumber() == null || fileReport.getBatchNumber() == null || fileReport.getAmount() == null || fileReport.getTransactionType() == null || fileReport.getTransactionId() == null) ||
                                        (fileReport.getTransactionDate().equals("null") || fileReport.getResponseDate().equals("null") || fileReport.getStan().equals("null") || fileReport.getPosDeviceId().equals("null") || fileReport.getTerminalId().equals("null") || fileReport.getMerchantId().equals("null") || fileReport.getInvoiceNumber().equals("null") || fileReport.getBatchNumber().equals("null") || fileReport.getAmount().equals("null") || fileReport.getTransactionType().equals("null") || fileReport.getTransactionId().equals("null")) ||
                                        (fileReport.getResponseCode().equals("") || fileReport.getTransactionDate().equals("") || fileReport.getResponseDate().equals("") || fileReport.getStan().equals("") || fileReport.getPosDeviceId().equals("") || fileReport.getTerminalId().equals("") || fileReport.getMerchantId().equals("") || fileReport.getInvoiceNumber().equals("") || fileReport.getBatchNumber().equals("") || fileReport.getAmount().equals("") || fileReport.getTransactionType().equals("") || fileReport.getTransactionId().equals(""))) {
                                    logger.info(SINGLE_VALUE + " checking Sale&UPI Required Fields is Null or Not---{}", fileReport.getTransactionId());
                                    fileReport.setSaleUpiNullValueStatus(true);
                                }
                            }
                        }
                    }
                }
                if (fileReport.getTransactionType().equals("Void") || fileReport.getTransactionType().equals("Reversal")) {
                    fileReport.setOnlyVoidReversalWithoutSaleOrUPI(true);
                }
                if (fileReport.getTransactionType().equals("Sale")) {
                    if (fileReport.getStatus().equals("INIT")) {
                        logger.info(SINGLE_VALUE + " Sale Txn Only INIT Status Check ----");
                        fileReport.setSaleTxnOnlyInitStatus(true);
                    }
                }
                singleEntry.add(fileReport);
            }
        });

        atfFileRepository.saveAll(singleEntry);
        atfFileRepository.saveAll(doubleEntry);
        logger.info("Rules Updated Successfully---");
    }


    public void threadExecution1(List<String> transId) throws RuntimeException {
        List<AtfFileReport> totalList = atfFileRepository.findByTransIdTotalList(transId);
        logger.info("After Split up data Size --{}", totalList.size());
        List<AtfFileReport> singleEntry = new ArrayList<>();
        transId.forEach(l -> {
            List<AtfFileReport> update = totalList.stream().filter(t -> t.getTransactionId().equals(l) || t.getOrgTransactionId().equals(l)).collect(Collectors.toList());
            if (update.size() == 1) {
                logger.info("Single Transaction Id List--{}", update.size());
                AtfFileReport fileReport = totalList.stream().filter(p -> p.getOrgTransactionId().equals(l)).findAny().orElse(null);
                fileReport.setRulesVerifiedStatus(true);
                if (fileReport.getTransactionType().equals("Void") || fileReport.getTransactionType().equals("Reversal")) {
                    fileReport.setOnlyVoidReversalWithoutSaleOrUPI(true);
                }
                singleEntry.add(fileReport);
            }
        });
        atfFileRepository.saveAll(singleEntry);
    }

    @Override
    public boolean updatesettlementFileData(String settlementFile) {
        boolean updated = false;
        // get the csv file
        List<SettlementFile> settlementFiles = new ArrayList<>();
        String fileExtension = "";
        int index = settlementFile.lastIndexOf(".");
        if (index > 0) {
            fileExtension = settlementFile.substring(index + 1);
            if (fileExtension.equals("csv")) {
                try {
                    CSVReader csvReader = new CSVReaderBuilder(new FileReader(settlementFile)).withSkipLines(1).build();
                    List<String[]> csvData = csvReader.readAll();
                    for (int i = 0; i < csvData.size(); i++) {
                        String[] element = csvData.get(i);
                        logger.info("Enter to insert Settlement Data--RRN --{}", element[5]);
                        SettlementFile settlementFile1 = new SettlementFile();
                        settlementFile1.setMid(element[0]);
                        settlementFile1.setTid(element[1]);
                        settlementFile1.setBatchNumber(element[2]);
                        settlementFile1.setInvoiceNumber(element[3]);
                        settlementFile1.setStan(element[4]);
                        settlementFile1.setRrn(element[5]);
                        settlementFile1.setAuthCode(element[6]);
                        settlementFile1.setAmount(element[7]);
                        settlementFile1.setToChar(element[8]);
                        settlementFile1.setAdditionalAmount(element[9]);
                        settlementFile1.setStatus(element[10]);
//                        settlementFile1.setDate(element[11]);
//                        settlementFile1.setFilename(element[12]);
                        settlementFiles.add(settlementFile1);
                    }
                    csvReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    settlementFileRepository.saveAll(settlementFiles);
                    updated = true;
                    logger.info("Settlement File Data Inserted Successfully----!!!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return updated;
    }

    @Override
    public boolean updateTxnListData(String txnListFile) {
        boolean updated = false;
        // get the csv file
        List<TransactionList> transactionLists = new ArrayList<>();
        String fileExtension = "";
        int index = txnListFile.lastIndexOf(".");
        if (index > 0) {
            fileExtension = txnListFile.substring(index + 1);
            if (fileExtension.equals("csv")) {
                try {
                    CSVReader csvReader = new CSVReaderBuilder(new FileReader(txnListFile)).withSkipLines(1).build();
                    List<String[]> csvData = csvReader.readAll();
                    for (int i = 0; i < csvData.size(); i++) {
                        String[] element = csvData.get(i);
                        logger.info("Enter to insert Txn List Data--{}", element[3]);
                        TransactionList transactionList = new TransactionList();
                        transactionList.setMti(element[0]);
                        transactionList.setTxnType(element[1]);
                        transactionList.setTerminalId(element[2]);
                        transactionList.setMerchantId(element[3]);
                        transactionList.setTxnDate(element[4]);
                        transactionList.setTxnTime(element[5]);
                        transactionList.setTxnAmount(element[6]);
                        transactionList.setTxnResponseCode(element[7]);
                        transactionList.setResponseReceivedTime(element[8]);
                        transactionList.setRrn(element[9]);
                        transactionList.setStan(element[10]);
                        transactionList.setInvoiceNumber(element[11]);
                        transactionList.setBatchNumber(element[12]);
                        transactionList.setUrn(element[13]);
                        transactionList.setAuthResponseCode(element[14]);
                        transactionList.setTxnAdditionalAmount(element[15]);
                        transactionList.setInstitutionId(element[16]);
                        transactionLists.add(transactionList);
                    }
                    csvReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    transactionListRepository.saveAll(transactionLists);
                    updated = true;
                    logger.info("Txn List File Data Inserted Successfully----!!!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return updated;
    }


    @Override
    public void generateAllTxnFileMissingDataFile() {
//        File allTxnUpdatedFile = new File(updatedAtfFilePath + "All_Txn_File_Missing_Data-" + DateUtil.previousDate() + ".csv");
        File allTxnUpdatedFile = new File(updatedAtfFilePath + "All_Txn_File_Missing_Data.csv");

        final String allTxnFileSheet = "All_Txn_File_Missing_Data-" + DateUtil.previousDate() + ".csv";
        List<Object[]> allTxnFileOut = transactionListRepository.findByMissingAllTxnData();
        if (!allTxnUpdatedFile.exists()) {
            ReportUtil.generateMissingDataCSVFile(allTxnFileOut, Constants.TXN_FILE_HEADER, allTxnUpdatedFile, allTxnFileSheet);
        }
    }

    @Override
    public void generateSettlementFileMissingDataFile() {
//        File settlementUpdatedFile = new File(updatedAtfFilePath + "Response_BIGILIPAY_AXIS_H2H_SETTLEMENT_Missing_Data_" + DateUtil.currentDate2() + ".csv");
        File settlementUpdatedFile = new File(updatedAtfFilePath + "Response_BIGILIPAY_AXIS_H2H_SETTLEMENT_Missing_Data.csv");

        final String settlementFileSheet = "Response_BIGILIPAY_AXIS_H2H_SETTLEMENT_Missing_Data-" + DateUtil.currentDate2() + ".csv";
        List<Object[]> settlementFileOut = transactionListRepository.findByMissingSettlementData();
        if (!settlementUpdatedFile.exists()) {
            ReportUtil.generateMissingDataCSVFile(settlementFileOut, Constants.TXN_FILE_HEADER, settlementUpdatedFile, settlementFileSheet);
        }
    }

    @Override
    public Boolean removeDataInDB1() {
        atfFileRepository.removeATFFileData();
        settlementFileRepository.removeSettlementData();
        transactionListRepository.removeTxnListDataOnly();
        return true;
    }

    @Override
    public void generateTxnAndSettlementMissingFile() {
//        File settlementUpdatedFile = new File(updatedAtfFilePath + "AllTxnAndSettlementFileMissingData_" + DateUtil.currentDate2() + ".csv");
        File settlementUpdatedFile = new File(updatedAtfFilePath + "AllTxnAndSettlementFileMissingData.csv");

        final String allTxnAndSettlementFileSheet = "AllTxnAndSettlementFileMissingData_-" + DateUtil.currentDate2() + ".csv";
        List<Object[]> allTxnAndSettlementFileOut = atfFileRepository.findByMissingAllTxnAndSettlementData();
        if (!settlementUpdatedFile.exists()) {
            ReportUtil.generateAllTxnAndSettlementMissingDataCSVFile(allTxnAndSettlementFileOut, Constants.TXN_SETTLEMENT_FILE_HEADER, settlementUpdatedFile, allTxnAndSettlementFileSheet);
        }
    }

    @Override
    public boolean updateTxnListTotalData(String missingTxnBefore) {
        boolean updated = false;
        // get the csv file
        List<TxnListMainTotal> missingTxnListDataList = new ArrayList<>();
        String fileExtension = "";
        int index = missingTxnBefore.lastIndexOf(".");
        if (index > 0) {
            fileExtension = missingTxnBefore.substring(index + 1);
            if (fileExtension.equals("csv")) {
                try {
                    CSVReader csvReader = new CSVReaderBuilder(new FileReader(missingTxnBefore)).withSkipLines(1).build();
                    List<String[]> csvData = csvReader.readAll();
                    for (int i = 0; i < csvData.size(); i++) {
                        String[] element = csvData.get(i);
                        logger.info("Enter to insert Txn List File Total Data--{}", element[16]);
                        if ((element[16].equals("WL002") && element[7].equals("00")) && (element[1].equals("00") || element[1].equals("02") || element[1].equals("37") || element[1].equals("39"))) {
                            TxnListMainTotal transactionList = new TxnListMainTotal();
                            transactionList.setMti(element[0]);
                            transactionList.setTxnType(element[1]);
                            transactionList.setTerminalId(element[2]);
                            transactionList.setMerchantId(element[3]);
                            transactionList.setTxnDate(element[4]);
                            transactionList.setTxnTime(element[5]);
                            transactionList.setTxnAmount(element[6]);
                            transactionList.setTxnResponseCode(element[7]);
                            transactionList.setResponseReceivedTime(element[8]);
                            transactionList.setRrn(element[9]);
                            transactionList.setStan(element[10]);
                            transactionList.setInvoiceNumber(element[11]);
                            transactionList.setBatchNumber(element[12]);
                            transactionList.setUrn(element[13]);
                            transactionList.setAuthResponseCode(element[14]);
                            transactionList.setTxnAdditionalAmount(element[15]);
                            transactionList.setInstitutionId(element[16]);
                            missingTxnListDataList.add(transactionList);
                        }
                    }
                    csvReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    txnListMainTotalRepository.saveAll(missingTxnListDataList);
                    updated = true;
                    logger.info("Missing Txn List File Total Data Inserted Successfully----!!!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return updated;
    }

    @Override
    public void generateMissingATFFileTxn(String date) throws IOException, ParseException {
        //        String updatedAtfFile = updatedAtfFilePath + "/All_Txn_File_Updated-"+ DateUtil.dateToStringForMail(DateUtil.currentDate()) + ".txt";

        String updatedAtfFile = updatedAtfFilePath + "/ATF_Rules_Executed_Report-" + date + ".txt";


        String atfFileSheet = "ATF_Rules_Executed_Report-" + date + ".txt";
        String[] addressesArr = new String[1];

        DateDto mainTotal = txnListMainTotalRepository.findByDates();

        String minDate = mainTotal.getMinDate();
        String maxDate = mainTotal.getMaxDate();
        logger.info("Min & Max Dates For Missing RRN  ----{}--{}", minDate, maxDate);
        String date1 = DateUtil.splitDateTime(minDate).concat(" 23:00:00");
        String date2 = DateUtil.minusOneDay(DateUtil.splitDateTime(maxDate)).concat(" 23:00:00");
        logger.info("Date Filter to generate missing RRN --{} ----{}", date1, date2);

//        String date1 = "2023-10-13".concat(" 23:00:00");
//        String date2 = "2023-10-14".concat(" 23:00:00");

        addressesArr[0] = "Rule 19 - ATF File Missing Transactions - RRN List";
        List<Object[]> missingData = txnListMainTotalRepository.findByATFFileMissingData(date1, date2);
        logger.info("Missing Txn List Size --{}", missingData.size());
        ReportUtil.generateAtfFileReportInTextFile(missingData, Constants.MISSING_TXN_HEADER, updatedAtfFile, atfFileSheet, addressesArr);

    }

    @Override
    public boolean uploadFilesToSFTP(String sourcePath, String destination) {
        boolean connectionStatus = false;
        boolean status = false;

        try {
            this.mJschSession = new JSch();

            Properties config = new Properties();

            config.put("StrictHostKeyChecking", "no");

            JSch.setConfig(config);

            this.mSSHSession = mJschSession.getSession(SFTP_ATF_USERNAME, SFTP_ATF_HOST, SFTP_ATF_PORT);

            this.mSSHSession.setPassword(SFTP_ATF_PASSWORD);

            this.mSSHSession.setConfig("PreferredAuthentications",
                    "publickey,keyboard-interactive,password");

            this.mSSHSession.connect();

            logger.info("Connected Success! for the path {}", sourcePath);
            this.mChannelSftp = (ChannelSftp) this.mSSHSession.openChannel("sftp");

            this.mChannelSftp.connect();

            if (this.mChannelSftp != null) {
                connectionStatus = true;
            }
            if (connectionStatus) {
                mChannelSftp.cd(destination);
                File paths = new File(sourcePath);
                String[] files = paths.list();
                for (String fileName : files) {
                    logger.info("upload file Name {} ", fileName);
                    mChannelSftp.put(sourcePath + "/" + fileName, destination);
                    logger.info("uploaded");
                }
                status = true;
//                logger.info("upload Success! ");
                if (status) {
                    logger.info("Moved Success! destination- {}-{}", sourcePath, status);
                    return status;
                } else {
                    logger.info("Not Moved! {}", status);
                    return false;
                }

            } else {
                return status;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                this.mChannelSftp.disconnect();
            } catch (Exception e) {
                System.out.print(e);
            }
            try {
                this.mSSHSession.disconnect();
                status = true;
            } catch (Exception e) {
                System.out.print(e);
            }
        }
        return status;
    }

    @Override
    public Boolean beforeCheck() {
        txnListMainTotalRepository.deleteAll();
        return true;
    }

    @Override
    public List<AtfFileReport> updateDataBasedOnTransIdReversalOnly() {
        List<String> transactionId = atfFileRepository.findAllTransId();
        logger.info("Transaction Id List Size --{}", transactionId.size());
        logger.info("Void Or Reversal Transaction Id List Size --{}", transactionId.size());
        int count = 0;
        for (int i = 0; i < transactionId.size(); i += 500) {
            count++;
            List<String> sub = transactionId.subList(i, Math.min(transactionId.size(), i + 500));
            logger.info("Loop Count For Sale AND UPI with Last TransactionId--- {}---- {}", count, sub.get(sub.size() - 1));
            threadExecutionForReversalEntry(sub);
        }
        return null;
    }

    @Override
    public List<String> processReversalEntry(String date) {
        File updatedAtfFile = new File(updatedAtfFilePath + "/All_Txn_File_Updated-" + date + ".csv");
        String atfFileSheet = "All_Txn_File_Updated-" + date + ".csv";
        List<String> reversalEntry = atfFileRepository.findReversalEntry();
        List<Object[]> result = atfFileRepository.findByWithoutReversalEntry(reversalEntry);

        ReportUtil.generateAtfFileData(result, Constants.ATF_FILE_HEADER, updatedAtfFile, atfFileSheet);

        return null;
    }

    @Override
    public void generateAtfFileReportForReversalEntry(String date) throws IOException {
        String updatedAtfFile = updatedAtfFilePath + "/ATF_ACK_Reversal_Report-" + date + ".txt";
        String atfFileSheet = "ATF_ACK_Reversal_Report-" + date + ".txt";
        List<Object[]> atf = null;
        for (int i = 0; i < 1; i++) {
            String[] addressesArr = new String[1];
            addressesArr[0] = "Reversals corresponding SALE/ UPI transaction in ACK status";
            atf = atfFileRepository.findByAtfFileDataRule8();
            ReportUtil.generateAtfFileReportInTextFile(atf, Constants.ATF_FILE_HEADER1, updatedAtfFile, atfFileSheet, addressesArr);
        }
    }

    @Override
    public Boolean removeATFFileRecord() {
        atfFileRepository.removeATFFileData();
        return true;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(switchUrl, username, password);
    }

    @Override
    public void processQueryExecution(String filepath) throws IOException, SQLException {
        BufferedReader reader = new BufferedReader(new FileReader(filepath));
        reader.readLine().contains("Rule 1- SALE & UPI & VOID & Reversal ResponseDate lesser than transactionDate\n" +
                "transactionId");
        Stream<String> lines = reader.lines().skip(2);
        List<String> transactionId = new ArrayList<>();
        lines.forEachOrdered(line -> {
            transactionId.add(line);
        });
        logger.info("Transaction Id List Size--{}", transactionId.size());
        Connection con = null;
        Statement stmt = null;
        String txnId = transactionId.stream().collect(Collectors.joining("','", "'", "'"));
        try {
            con = getConnection();
            stmt = con.createStatement();
            String insertQuery = "INSERT INTO notification_data_Rev SELECT * FROM notification_data where OrgTransactionId IN (" + txnId + ")";
            String deleteQuery = "delete FROM notification_data where OrgTransactionId IN (" + txnId + ")";
            String updateQuery = "update notification_data set settlement_status='Settled' where transactionId IN(" + txnId + ") and NotificationType='ACK';";
            int insert = stmt.executeUpdate(insertQuery);
            logger.info("Reversal With ACK Data Insert SuccessFully --{}", insert);
            int delete = stmt.executeUpdate(deleteQuery);
            logger.info("Reversal With ACK Data Deleted SuccessFully --{}", delete);
            int update = stmt.executeUpdate(updateQuery);
            logger.info("Reversal With ACK Data Insert SuccessFully --{}", update);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }

    }

    @Override
    public void executeQueryUpdation() {
        atfFileRepository.removeReversalEntries();
        atfFileRepository.removeVoidEntries();
        atfFileRepository.removeSaleWithInitEntries();
        atfFileRepository.removeUPIWithInitEntries();
        atfFileRepository.removeSaleAndHostNotInResponseCodeSuccess();
        atfFileRepository.removeUPIAndHostNotInResponseCodeSuccess();
        logger.info("All Queries are updated successfully---");
    }

    @Override
    public void processQueryExecution1(String filepath) throws IOException {
        List<String> rule1 = new ArrayList();
        List<String> rule2 = new ArrayList();
        List<String> rule3 = new ArrayList();
        List<String> rule4 = new ArrayList();
        List<String> rule5 = new ArrayList();
        List<String> rule6 = new ArrayList();
        List<String> rule7 = new ArrayList();
        List<String> rule8 = new ArrayList();
        List<String> rule9 = new ArrayList();
        List<String> rule10 = new ArrayList();
        List<String> rule11 = new ArrayList();
        List<String> rule12 = new ArrayList();
        List<String> rule13 = new ArrayList();
        List<String> rule14 = new ArrayList();
        List<String> rule15 = new ArrayList();
        List<String> rule16 = new ArrayList();
        BufferedReader reader = new BufferedReader(new FileReader(filepath));
        if (reader.readLine().contains("Rule 1")) {
            Stream<String> lines = reader.lines().skip(2);
            lines.forEachOrdered(line -> {
                rule1.add(line);
            });
        }
//        if(reader.readLine().contains("Rule 2")) {
//            Stream<String> lines = reader.lines().skip(2);
//            lines.forEachOrdered(line -> {
//                rule2.add(line);
//            });
//        }
//        if(reader.readLine().contains("Rule 3")) {
//            Stream<String> lines = reader.lines().skip(2);
//            lines.forEachOrdered(line -> {
//                rule3.add(line);
//            });
//        }
//        if(reader.readLine().contains("Rule 4")) {
//            Stream<String> lines = reader.lines().skip(2);
//            lines.forEachOrdered(line -> {
//                rule4.add(line);
//            });
//        }
//        if(reader.readLine().contains("Rule 5")) {
//            Stream<String> lines = reader.lines().skip(2);
//            lines.forEachOrdered(line -> {
//                rule5.add(line);
//            });
//        }
        logger.info("Rule 1 Size --{}", rule1.size());
        logger.info("Rule 2 Size --{}", rule2.size());
        logger.info("Rule 3 Size --{}", rule3.size());
        logger.info("Rule 4 Size --{}", rule4.size());
        logger.info("Rule 5 Size --{}", rule5.size());

    }

    @Override
    public void atfFileRulesCount(String date) throws IOException {
        String updatedAtfFile = updatedAtfFilePath + "/ATF_Rules_Data_Count-" + date + ".txt";
        String atfFileSheet = "ATF_Rules_Data_Count-" + date + ".txt";
        List<Object[]> atf = null;
        for (int i = 0; i < 10; i++) {
            String[] addressesArr = new String[1];
            if (i == 0) {
                addressesArr[0] = "Rule 1- Count of transactions with SettlementStatus: NotSettled";
                atf = atfFileRepository.findNotSettledCount();
            } else if (i == 1) {
                addressesArr[0] = "Rule 2- Count of transactions with type: SALE and status: INIT";
                atf = atfFileRepository.findSaleWithINITCount();

            } else if (i == 2) {
                addressesArr[0] = "Rule 3- Count of transactions with type: SALE, status: HOST, responseCode: 00 and settlementStatus: NotSettled";
                atf = atfFileRepository.findSaleHostNotSettledCount();

            } else if (i == 3) {
                addressesArr[0] = "Rule 4- Count of transactions with type: SALE, status: ACK and settlementStatus: NotSettled";
                atf = atfFileRepository.findSaleACKNotSettledCount();
            } else if (i == 4) {
                addressesArr[0] = "Rule 5- Count of transactions with type: UPI and status: INIT";
                atf = atfFileRepository.findUPIWithINITCount();
            } else if (i == 5) {
                addressesArr[0] = "Rule 6- Count of transactions with type: UPI , status: HOST, responseCode: 00 and settlementStatus: NotSettled";
                atf = atfFileRepository.findUPIHostNotSettledCount();

            } else if (i == 6) {
                addressesArr[0] = "Rule 7- Count of transactions with type: UPI, status: ACK and settlementStatus: NotSettled";
                atf = atfFileRepository.findUPIACKNotSettledCount();

            } else if (i == 7) {
                addressesArr[0] = "Rule 8- Count of transactions with type: VOID";
                atf = atfFileRepository.findVoidCount();
            } else if (i == 8) {
                addressesArr[0] = "Rule 9- Count of transactions with type: REVERSAL";
                atf = atfFileRepository.findReversalCount();
            } else if (i == 9) {
                addressesArr[0] = "Rule 10- Count of transactions with ResponseDate lesser than transactionDate";
                atf = atfFileRepository.findDateCheckCount();
            }
            ReportUtil.generateAtfFileReportInTextFile(atf, Constants.ATF_RULE_COUNT, updatedAtfFile, atfFileSheet, addressesArr);
        }
    }

    @Override
    public void generateUpdatedATFFile(String date) {
        File allTxnRuleUpdatedFile = new File(updatedAtfFilePath + "All_Txn_After_Rule_Updated.csv");
        final String allTxnFileAfterRuleUpdated = "All_Txn_After_Rule_Updated-" + date + ".csv";
        List<Object[]> allTxnFileAfterRuleOut = atfFileRepository.findByRuleUpdatedData();
        if (!allTxnRuleUpdatedFile.exists()) {
            ReportUtil.generateAllTxnAndSettlementMissingDataCSVFile(allTxnFileAfterRuleOut, Constants.TXN_SETTLEMENT_FILE_HEADER, allTxnRuleUpdatedFile, allTxnFileAfterRuleUpdated);
        }
    }

    @Override
    public void generateSettlementReportForPhonePe(String settlementDate) {
        File settlementFile = new File(updatedAtfFilePath + "Response_BIGILIPAY_SETTLEMENT_Updated_" + settlementDate + ".csv");
        final String settlementFileSheet = "Response_BIGILIPAY_SETTLEMENT_File_Updated_" + settlementDate + ".csv";
        List<Object[]> settlementFileOut = atfFileRepository.findBySettlementDataForPhonePe();
        if (!settlementFile.exists()) {
            ReportUtil.generateAllTxnAndSettlementMissingDataCSVFile(settlementFileOut, Constants.TXN_SETTLEMENT_FILE_HEADER, settlementFile, settlementFileSheet);
        }
    }


    private void threadExecutionForReversalEntry(List<String> transId) {
        List<AtfFileReport> totalList = atfFileRepository.findByTransIdTotalList(transId);
        logger.info("After Split up data Size --{}", totalList.size());
        List<AtfFileReport> doubleEntry = new ArrayList<>();
        transId.forEach(l -> {
            List<AtfFileReport> update = totalList.stream().filter(t -> t.getTransactionId().equals(l) || t.getOrgTransactionId().equals(l)).collect(Collectors.toList());
            if (update.size() > 1) {
                logger.info("Double Transaction Id List --{}", update.size());
                update.get(0).setRulesVerifiedStatus(true);
                update.get(1).setRulesVerifiedStatus(true);
                if ((update.get(0).getTransactionType().equals("Void") || update.get(0).getTransactionType().equals("Reversal")) || (update.get(1).getTransactionType().equals("Void") || update.get(1).getTransactionType().equals("Reversal"))) {
                    if ((update.get(0).getTransactionType().equals("Reversal")) || (update.get(1).getTransactionType().equals("Reversal"))) {
                        if (update.get(0).getTransactionType().equals("Sale") || update.get(0).getTransactionType().equals("UPI")) {
                            if ((update.get(0).getStatus().equals("ACK"))) {
                                logger.info(DOUBLE_VALUE + " Checking Reversal Corresponding Ack Status Wrong---{}", update.get(0).getTransactionId());
                                update.get(0).setReversalAndAckStatus(true);
                                update.get(1).setReversalAndAckStatus(true);
                            }
                        } else {
                            if (update.get(1).getTransactionType().equals("Sale") || update.get(1).getTransactionType().equals("UPI")) {
                                if (update.get(1).getStatus().equals("ACK")) {
                                    logger.info(DOUBLE_VALUE + " Checking Reversal Corresponding Ack Status Wrong---{}", update.get(1).getTransactionId());
                                    update.get(0).setReversalAndAckStatus(true);
                                    update.get(1).setReversalAndAckStatus(true);
                                }
                            }
                        }
                    }
                }
                doubleEntry.addAll(update);
            }
        });
        atfFileRepository.saveAll(doubleEntry);
        logger.info("Rules Updated Successfully---");
    }


    public boolean uploadMicro(String sourcePath, String destination) {

        boolean connectionStatus = false;
        boolean status = false;

        try {
            this.mJschSession = new JSch();

            Properties config = new Properties();

            config.put("StrictHostKeyChecking", "no");

            JSch.setConfig(config);

            this.mSSHSession = mJschSession.getSession(SFTP_ATF_USERNAME, SFTP_ATF_HOST, SFTP_ATF_PORT);

            this.mSSHSession.setPassword(SFTP_ATF_PASSWORD);

            this.mSSHSession.setConfig("PreferredAuthentications",
                    "publickey,keyboard-interactive,password");

            this.mSSHSession.connect();

            logger.info("Connected Success! for the path {}", sourcePath);
            this.mChannelSftp = (ChannelSftp) this.mSSHSession.openChannel("sftp");

            this.mChannelSftp.connect();

            if (this.mChannelSftp != null) {
                connectionStatus = true;
            }
            if (connectionStatus) {
                mChannelSftp.cd(destination);
                File paths = new File(sourcePath);
                String[] files = paths.list();
                for (String fileName : files) {
                    logger.info("upload file Name {} ", fileName);
                    mChannelSftp.put(sourcePath + "/" + fileName, destination);
                    logger.info("uploaded");
                }
                status = true;
                logger.info("upload Success! ");
                if (status) {
                    logger.info("Moved Success! destination- {}-{}", sourcePath, status);
                    return status;
                } else {
                    logger.info("Not Moved! {}", status);
                    return false;
                }

            } else {
                return status;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                this.mChannelSftp.disconnect();
            } catch (Exception e) {
                System.out.print(e);
            }
            try {
                this.mSSHSession.disconnect();
                status = true;
            } catch (Exception e) {
                System.out.print(e);
            }
        }
        return status;
    }

    @Override
    public int removeSettlementRulesData() throws SQLException {
            Connection con = null;
            Statement stmt = null;
            int voidReversalQuery = 0;
            int saleInitQuery = 0;
            int upiInitQuery = 0;
            int saleHostQuery = 0;
            int upiHostQuery = 0;
            try {
                con = getAtfConnection();
                stmt = con.createStatement();
                List<String> voidOrReversal = atfFileRepository.findAllTransIdForVoidOrReversal();
                String list = voidOrReversal.stream().collect(Collectors.joining("','", "'", "'"));
                String voidReversal = "delete from atf_file_report_main  where transaction_id in (" + list + ") OR org_transaction_id in(" + list + ")";
                String saleInit = "delete from atf_file_report_main  where transaction_type ='Sale' and status ='INIT'";
                String upiInit = "delete from atf_file_report_main  where transaction_type ='UPI' and status ='INIT'";
                String saleHost = "delete from atf_file_report_main where transaction_type ='Sale' and status ='HOST' and response_code not in ('00')";
                String upiHost = "delete from atf_file_report_main where transaction_type ='UPI' and status ='HOST' and response_code not in ('00')";
                voidReversalQuery = stmt.executeUpdate(voidReversal);
                saleInitQuery = stmt.executeUpdate(saleInit);
                upiInitQuery = stmt.executeUpdate(upiInit);
                saleHostQuery = stmt.executeUpdate(saleHost);
                upiHostQuery = stmt.executeUpdate(upiHost);
                logger.info("All Query Out -----{} --- {} --{} --{} --{}", voidReversalQuery, saleInitQuery, upiInitQuery, saleHostQuery, upiHostQuery);
                stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (con != null) {
                    con.close();
                }
            }
//            List<String> voidOrReversal = atfFileRepository.findAllTransIdForVoidOrReversal();
//            atfFileRepository.removeVoidOrReversalEntry(voidOrReversal);
//            atfFileRepository.removeSaleWithInitStatus();
//            atfFileRepository.removeUPIWithInitStatus();
//            atfFileRepository.removeSaleAndHostNotInResponseCodeSuccess();
//            atfFileRepository.removeUPIAndHostNotInResponseCodeSuccess();
            logger.info("Data Removed Successfully For Settlement Rules Process");
            return upiHostQuery;
    }

    @Override
    public void generateFinalSettlementFile(String date) throws ParseException, SQLException {

//        DateDto mainTotal = atfFileRepository.findByDatesFromATF();
//
//        String minDate = mainTotal.getMinDate();
//        String maxDate = mainTotal.getMaxDate();
//        logger.info("Min and Max dates --{}--{} ",minDate,maxDate);
        String date1 = DateUtil.minusOneDay(date).concat(" 23:00:00");
        String date2 = date.concat(" 23:00:00");
        logger.info("Min & Max Dates For ATF File ----{}--{}", date1, date2);

        // missing atf file Data compare with WL settlement file
        File atfMissingDataFile = new File(updatedAtfFilePath + "PhonePe_Missing_Response_IN_Settlement_File_" + date + ".csv");
        final String atfFileSheet = "PhonePe_Missing_Response_IN_Settlement_File_" + date + ".csv";
        // missing WL settlement file Data compare with ATF file
        File settlementMissingDataFile = new File(updatedAtfFilePath + "PhonePe_Missing_Response_IN_ATF_File_" + date + ".csv");
        final String settlementMissingDataFileSheet = "PhonePe_Missing_Response_IN_ATF_File_" + date + ".csv";
        // final ATF File After removing data's and rules
        File settlementDataFile = new File(updatedAtfFilePath + "PhonePe_Final_Settlement_File_" + date + ".csv");
        final String settlementDataFileSheet = "PhonePe_Final_Settlement_File_" + date + ".csv";


        List<Object[]> atfMissingDataOut = atfFileRepository.findByMissingATFDataBasedOnSettlementData(date1, date2);
        List<Object[]> settlementMissingDataOut = phonePeSettlementDataRepository.findByMissingSettlementDataBasedOnATFData(date1, date2);
        List<Object[]> settlementDataOut = atfFileRepository.findByFinalSettlementData();

        if (!atfMissingDataFile.exists()) {
            logger.info("ATF File Missing data size --{}", atfMissingDataOut.size());
            ReportUtil.generateAllTxnAndSettlementMissingDataCSVFile(atfMissingDataOut, Constants.TXN_SETTLEMENT_FILE_HEADER, atfMissingDataFile, atfFileSheet);
        }
        if (!settlementMissingDataFile.exists()) {
            logger.info("Settlement File Missing data size --{}", settlementMissingDataOut.size());
            ReportUtil.generateSettlementMissingDataCSVFile(settlementMissingDataOut, Constants.TXN_SETTLEMENT_MISSING_FILE_HEADER, settlementMissingDataFile, settlementMissingDataFileSheet);
        }

//        int deleteQuery = this.removeATFMissingRecord(date1, date2);
//        logger.info("delete Query Out Final --{}", deleteQuery);
//        if (deleteQuery > 1) {
////            atfFileRepository.removeMissingATFDataBasedOnSettlementData(date1, date2);
//            if (!settlementDataFile.exists()) {
//                ReportUtil.generateAllTxnAndSettlementMissingDataCSVFile(settlementDataOut, Constants.TXN_SETTLEMENT_FILE_HEADER, settlementDataFile, settlementDataFileSheet);
//            }
//        }
    }

    private int removeATFMissingRecord(String date1, String date2) throws SQLException {
        Connection con = null;
        Statement stmt = null;
        int delete = 0;
        try {
            con = getAtfConnection();
            stmt = con.createStatement();
            String deleteQuery = "delete from atf_file_report_main where transaction_date between '" + date1 + "' and '" + date2 + "' and response_code in ('00') and transaction_type ='Sale' and rrn not in (select rrn from phonepe_settlement_data where status='00')";
            delete = stmt.executeUpdate(deleteQuery);
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return delete;
    }

    @Override
    public void removeFile(String date) {
        String updatedAtfFile = updatedAtfFilePath + "/All_Txn_File_Updated-" + date + ".txt";
        File file = new File(updatedAtfFile);
        file.delete();
        logger.info("File Deleted Successfully --");
    }

    @Override
    public int splitPhonePeSettlementData() throws SQLException {
//        List<SettlementFile> out = settlementFileRepository.findByPhonePeSettlementData();
//        List<PhonePeSettlementData> result = new ArrayList<>();
//        out.forEach(l -> {
//            PhonePeSettlementData data = new PhonePeSettlementData();
//            data.setMid(l.getMid());
//            data.setTid(l.getTid());
//            data.setBatchNumber(l.getBatchNumber());
//            data.setInvoiceNumber(l.getInvoiceNumber());
//            data.setStan(l.getStan());
//            data.setRrn(l.getRrn());
//            data.setAuthCode(l.getAuthCode());
//            data.setAmount(l.getAmount());
//            data.setToChar(l.getToChar());
//            data.setAdditionalAmount(l.getAdditionalAmount());
//            data.setStatus(l.getStatus());
//            result.add(data);
//        });
//        if (result.size() > 0) {
//            phonePeSettlementDataRepository.saveAll(result);
//            logger.info("PhonePe Settlement Data only inserted");
//            return true;
//        }
        Connection con = null;
        Statement stmt = null;
        int insert = 0;
        try {
            con = getAtfConnection();
            stmt = con.createStatement();
            String insertQuery = "insert into phonepe_settlement_data (mid,tid,batch_number,invoice_number,stan,rrn,auth_code,amount,to_char,additional_amount,status) select s.mid ,s.tid,s.batch_number,s.invoice_number ,s.stan,s.rrn ,s.auth_code,s.amount,s.to_char,s.additional_amount,s.status  from settlement_file_main s " +
                    "where  s.tid in (select a.terminal_id from atf_file_report_main a where a.response_code ='00' " +
                    "and a.transaction_type ='Sale')";
            insert = stmt.executeUpdate(insertQuery);
            logger.info("insert Query out --{}", insert);
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return insert;
//            phonePeSettlementDataRepository.insertPhonePeData();
    }

    @Override
    public void generateRefundFile(String date) throws ParseException {
        String date1 = DateUtil.minusOneDay(date).concat(" 23:00:00");
        String date2 = date.concat(" 23:00:00");
        logger.info("Min & Max Dates For ATF File ----{}--{}", date1, date2);
        File refundDataFile = new File(updatedAtfFilePath + "PhonePe_Refund_File_" + date + ".csv");
        final String refundFileSheet = "PhonePe_Refund_File_" + date + ".csv";
        List<String> transactionIdList = atfFileRepository.findByRefundDataOnly();
        List<Object[]> refundFileOut = atfFileRepository.findByRefundData(date1, date2);
        if (!refundDataFile.exists()) {
            ReportUtil.generateAllTxnAndSettlementMissingDataCSVFile(refundFileOut, Constants.TXN_SETTLEMENT_FILE_HEADER, refundDataFile, refundFileSheet);
        }
    }

    @Override
    public String callAxisApiForGeotag(AxisDto axisDto) throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, KeyStoreException, ParseException, JOSEException, KeyManagementException {
        KeyStore clientStore = KeyStore.getInstance("PKCS12");
        clientStore.load(new FileInputStream(new File(Axis_Key_Store_File)), "Sk!lworth@321".toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(clientStore, "Sk!lworth@321".toCharArray());
        KeyManager[] kms = kmf.getKeyManagers();
        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kms, new TrustManager[]{new DummyTrustManager()}, new SecureRandom());
        SSLContext.setDefault(sslContext);

        HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
        ResponseEntity<String> out = null;
        String url = "https://sakshamuat.axisbank.co.in:443/gateway/api/mos/v1/geotag/processRequest";     //old
//       String url = "https://insightmosuat.axisbank.co.in/GeoTag_MosAggrApi/BIJLIPAY/api/GeoTaggingMosAggr/ProcessRequest";
        long currentTimestamp = System.currentTimeMillis();
        RandomKeyGeneration randomKeyGenerator = new RandomKeyGeneration();// Random  key
        String randomKey = randomKeyGenerator.randomString(22);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text");
        headers.add("x-fapi-channel-id", "BIJLIPAY");
        headers.add("X-IBM-Client-Id", "f0854bee71b5f121c539d5b3e09f60db");
        headers.add("X-IBM-Client-Secret", "fa3e918e10b03dc600b5d111096f34e9");
        headers.add("x-fapi-epoch-millis", String.valueOf(currentTimestamp));
        headers.add("x-fapi-uuid", randomKey);

        String encryptPayload = JWEMainClient.encryptData(axisDto);
        logger.info("Input Headers ----{}", headers);
        logger.info("Axis Url --{}", url);
        logger.info("Request Encrypt Payload --{}", encryptPayload);
        HttpEntity<String> entity = new HttpEntity(encryptPayload, headers);
        out = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        logger.info("Response From Axis  ---{}", out.getBody());
        String decryptedString = JWEMainClient.decryptData(out.getBody());
        logger.info("Response From Axis with Decrypted String ---{}", decryptedString);
        return decryptedString;

    }

    @Override
    public boolean downloadPhonePeFiles(String date) {
        boolean connectionStatus = false;
        boolean status = false;
        String currentDate = DateUtil.currentDateATF();
        String previousDate = DateUtil.previousDateATF();
        String sourcePath = "/home/uat1/uploads/"+currentDate+"";
        String sourcePath1 = "/home/uat1/uploads/"+previousDate+"";
        String destinationPath = "C:\\Users\\muthupandi\\Music";

        try {
            this.mJschSession = new JSch();

            Properties config = new Properties();

            config.put("StrictHostKeyChecking", "no");

            JSch.setConfig(config);

            this.mSSHSession = mJschSession.getSession(SFTP_Bijlipay_USERNAME, SFTP_Bijlipay_HOST, SFTP_Bijlipay_PORT);

            this.mSSHSession.setPassword(SFTP_Bijlipay_PASSWORD);

            this.mSSHSession.setConfig("PreferredAuthentications",
                    "publickey,keyboard-interactive,password");

            this.mSSHSession.connect();

            logger.info("Connected Success! for the path {}", sourcePath);
            this.mChannelSftp = (ChannelSftp) this.mSSHSession.openChannel("sftp");
            this.mChannelSftp.connect();
            if (this.mChannelSftp != null) {
                connectionStatus = true;
                logger.info("====connection status===={}", connectionStatus);
            }
            if (connectionStatus) {
                if (exists(mChannelSftp, sourcePath)) {
                    mChannelSftp.cd(sourcePath);
                    logger.info("=====recursive folder download started=====Source -{} ----Destination -{}", sourcePath, destinationPath);
                    recursiveFolderDownload(sourcePath,
                            destinationPath);
                    logger.info("downloadStatus Success Current Date ! ");
                    mChannelSftp.cd(sourcePath1);

                } else {
                    return status;
                }
                if (exists(mChannelSftp, sourcePath1)) {
                    mChannelSftp.cd(sourcePath1);
                    logger.info("=====recursive folder download started=====Source -{} ----Destination -{}", sourcePath, destinationPath);
                    recursiveFolderDownload(sourcePath1,
                            destinationPath);
                    logger.info("downloadStatus Success Previous Date ! ");
                    mChannelSftp.cd(sourcePath1);

                }
            } else {
                logger.info("SFTP Connection Failed!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                this.mChannelSftp.disconnect();
            } catch (Exception e) {
                System.out.print(e);
            }
            try {
                this.mSSHSession.disconnect();
                status = true;
            } catch (Exception e) {
                System.out.print(e);
            }
        }
        return status;
    }

    @Override
    public void generateMissingRRNFromATF(String previousDate) throws ParseException, IOException {
        String updatedAtfFile = updatedAtfFilePath + "/Missing_RRN_From_ATF_IN_Txn_List_" + previousDate + ".txt";

        String atfFileSheet = "Missing_RRN_From_ATF_IN_Txn_List_-" + previousDate + ".txt";
        String[] addressesArr = new String[1];

        DateDto mainTotal = txnListMainTotalRepository.findByDates();

        String minDate = mainTotal.getMinDate();
        String maxDate = mainTotal.getMaxDate();
        logger.info("Min & Max Dates For Missing RRN  ----{}--{}", minDate, maxDate);
        String date1 = DateUtil.splitDateTime(minDate).concat(" 23:00:00");
        String date2 = DateUtil.minusOneDay(DateUtil.splitDateTime(maxDate)).concat(" 23:00:00");
        logger.info("Date Filter to generate missing RRN --{} ----{}", date1, date2);

//        String date1 = "2023-10-13".concat(" 23:00:00");
//        String date2 = "2023-10-14".concat(" 23:00:00");

        addressesArr[0] = "Rule 1 - ATF File Missing Transactions - RRN List";
        List<Object[]> missingData = txnListMainTotalRepository.findByATFFileMissingData(date1, date2);
        logger.info("Missing Txn List Size --{}", missingData.size());
        ReportUtil.generateAtfFileReportInTextFile(missingData, Constants.MISSING_TXN_HEADER, updatedAtfFile, atfFileSheet, addressesArr);

    }

    @Override
    public void validatedAtfFileReport(String date) throws IOException {
        String updatedAtfFile = updatedAtfFilePath + "/Validated_ATF_Report-" + date + ".txt";
        String atfFileSheet = "Validated_ATF_Report-" + date + ".txt";
        List<Object[]> atf = null;
        for (int i = 0; i < 18; i++) {
            String[] addressesArr = new String[1];
            if (i == 0) {
                addressesArr[0] = "Rule 1- SALE & UPI & VOID & Reversal ResponseDate lesser than transactionDate";
                atf = atfFileRepository.findByAtfFileDataRule1();
            } else if (i == 1) {
                addressesArr[0] = "Rule 2- Sale transaction with only INIT and no corresponding Reversal";
                atf = atfFileRepository.findByAtfFileDataRule2();
            } else if (i == 2) {
                addressesArr[0] = "Rule 3- SALE & UPI in Host and Ack status should have the following fields  - RRN, AuthCode, responseCode, transactionDate, responseDate, Stan, MID, TID, PosDeviceID, Invoice No, batch No, amount, txntype, TransactionID";
                atf = atfFileRepository.findByAtfFileDataRule3();
            } else if (i == 3) {
                addressesArr[0] = "Rule 4- VOID and Reversal should have the following fields  - Masked card number, Card holder name ,RRN, AuthCode, responseCode, transactionDate, responseDate, Stan, MID, TID, PosDeviceID, Invoice No, batch No, amount, txntype, TransactionID, OrginalTransactionID";
                atf = atfFileRepository.findByAtfFileDataRule4();
            } else if (i == 4) {
                addressesArr[0] = "Rule 5- VOID txns OriginalTransactionID should not match with a SALE Txn ID in same file with different date";
                atf = atfFileRepository.findByAtfFileDataRule5();
            } else if (i == 5) {
                addressesArr[0] = "Rule 6- SALE Reversal txns OriginalTransactionID  & Txn ID should not match with a SALE Txn ID in same file with different date";
                atf = atfFileRepository.findByAtfFileDataRule6();
            } else if (i == 6) {
                addressesArr[0] = "Rule 7- UPI txns Txn ID matched with a Reversal OriginalTransactionID in same file";
                atf = atfFileRepository.findByAtfFileDataRule7();
            } else if (i == 7) {
                addressesArr[0] = "Rule 8- Reversals corresponding SALE/ UPI transaction in ACK status";
                atf = atfFileRepository.findByAtfFileDataRule8();
            } else if (i == 8) {
                addressesArr[0] = "Rule 9- VOID transactions original txn should not be in ACK or HOST with responsecode 00";
                atf = atfFileRepository.findByAtfFileDataRule9();
            } else if (i == 9) {
                addressesArr[0] = "Rule 10- VOID transaction status other than HOST";
                atf = atfFileRepository.findByAtfFileDataRule10();
            } else if (i == 10) {
                addressesArr[0] = "Rule 11- SALE and UPI transactions marked as Not Settled corresponding ACK or HOST with responsecode 00";
                atf = atfFileRepository.findByAtfFileDataRule11();
            } else if (i == 11) {
                addressesArr[0] = "Rule 12- SALE and UPI transactions with ACK or HOST with responsecode 00 but not marked as settled with no corresponding VOID or Reversal";
                atf = atfFileRepository.findByAtfFileDataRule12();
            } else if (i == 12) {
                addressesArr[0] = "Rule 13- SALE and UPI transactions with ACK or HOST with responsecode 00 but marked as settled with corresponding VOID or Reversal";
                atf = atfFileRepository.findByAtfFileDataRule13();
            } else if (i == 13) {
                addressesArr[0] = "Rule 14- VOID or Reversal entry only available without corresponding SALE and UPI transactions";
                atf = atfFileRepository.findByAtfFileDataRule14();
            } else if (i == 14) {
                addressesArr[0] = "Rule 15- Response Date and Transaction Date Year MisMatch Data";
                atf = atfFileRepository.findByAtfFileDataRule15();
            } else if (i == 15) {
                addressesArr[0] = "Rule 16- SALE and UPI transactions with ACK or HOST with responsecode 00 but marked as not settled with corresponding VOID or Reversal in transactionDate before 23:00:00";
                atf = atfFileRepository.findByAtfFileDataRule16();
            } else if (i == 16) {
                addressesArr[0] = "Rule 17- Rules Not Verified because Data alignment issues Data";
                atf = atfFileRepository.findByAtfFileDataRule17();
            } else if (i == 17) {
                addressesArr[0] = "Rule 18- Host Failure Response with Reversal";
                atf = atfFileRepository.findByAtfFileDataRule18();
            }
            ReportUtil.generateAtfFileReportInTextFile(atf, Constants.ATF_FILE_HEADER1, updatedAtfFile, atfFileSheet, addressesArr);
        }
    }

    private void recursiveFolderDownload(String sourcePath, String destinationPath) throws SftpException {
        Vector<ChannelSftp.LsEntry> fileAndFolderList = this.mChannelSftp.ls(sourcePath); // Let list of folder content

        //Iterate through list of folder content
        for (ChannelSftp.LsEntry item : fileAndFolderList) {

            if (!item.getAttrs().isDir()) { // Check if it is a file (not a directory).
                if (!(new File(destinationPath + PATHSEPARATOR + item.getFilename())).exists()
                        || (item.getAttrs().getMTime() > Long
                        .valueOf(new File(destinationPath + PATHSEPARATOR + item.getFilename()).lastModified()
                                / (long) 1000)
                        .intValue())) { // Download only if changed later.

                    new File(destinationPath + PATHSEPARATOR + item.getFilename());
                    this.mChannelSftp.get(sourcePath + PATHSEPARATOR + item.getFilename(),
                            destinationPath + PATHSEPARATOR + item.getFilename()); // Download file from source (source filename, destination filename).
                    logger.info("====file name in mchannelSftp====" + item.getFilename());

                }
            } else if (!(".".equals(item.getFilename()) || "..".equals(item.getFilename()))) {
                new File(destinationPath + PATHSEPARATOR + item.getFilename()).mkdirs(); // Empty folder copy.
                recursiveFolderDownload(sourcePath + PATHSEPARATOR + item.getFilename(),
                        destinationPath + PATHSEPARATOR + item.getFilename()); // Enter found folder on server to read its contents and create locally.
            }
        }
    }

    private static boolean exists(ChannelSftp channelSftp, String path) {
        Vector res = null;
        try {
            res = channelSftp.ls(path);
        } catch (SftpException e) {
            logger.error("file not available on sftp: [{}:{}:{}]", e.id, e.getMessage(), path);
        }
        return res != null && !res.isEmpty();
    }

    public Connection getAtfConnection() throws SQLException {
        return DriverManager.getConnection(atfUrl, atfUserName, atfPassword);
    }
}
