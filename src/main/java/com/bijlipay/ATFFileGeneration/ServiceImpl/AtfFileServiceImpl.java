package com.bijlipay.ATFFileGeneration.ServiceImpl;

import com.bijlipay.ATFFileGeneration.Model.*;
import com.bijlipay.ATFFileGeneration.Model.Dto.AxisDto;
import com.bijlipay.ATFFileGeneration.Model.Dto.DateDto;
import com.bijlipay.ATFFileGeneration.Repository.*;
import com.bijlipay.ATFFileGeneration.Service.AtfFileService;
import com.bijlipay.ATFFileGeneration.Util.*;
import com.bijlipay.ATFFileGeneration.Util.DateUtil;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.nimbusds.jose.JOSEException;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
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
    private NotificationDataRepository notificationDataRepository;

    @Autowired
    private NotificationFieldsRepository notificationFieldsRepository;

    @Autowired
    private SwitchRequestRepository switchRequestRepository;

    @Autowired
    private SwitchResponseRepository switchResponseRepository;

    @Autowired
    private TxnListMainTotalRepository txnListMainTotalRepository;

    @Autowired
    private PhonePeSettlementDataRepository phonePeSettlementDataRepository;

    @Value("${atf.file.updated.path}")
    private String updatedAtfFilePath;

//    @Value("${switch.datasource.url}")
//    private String switchUrl;
//    @Value("${switch.datasource.username}")
//    private String username;
//    @Value("${switch.datasource.password}")
//    private String password;

    @Value("${atf.datasource.url}")
    private String atfUrl;
    @Value("${atf.datasource.username}")
    private String atfUserName;
    @Value("${atf.datasource.password}")
    private String atfPassword;

    @Value("${atf.check.url}")
    private String atfCheckUrl;
    @Value("${atf.check.username}")
    private String atfCheckUserName;
    @Value("${atf.check.password}")
    private String atfCheckPassword;

    @Value("${atf.sftp.source.path}")
    private String atfSftpSourcePath;
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
        for (int i = 0; i < 19; i++) {
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
                addressesArr[0] = "Rule 6- SALE Reversal txns OriginalTransactionID  & Txn ID should not match with a SALE Txn ID and same file with different date";
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
                addressesArr[0] = "Rule 17- Rules Not Verified because Multiple data for the same transactionId and Data alignment issues";
                atf = atfFileRepository.findByAtfFileDataRule17();
            } else if (i == 17) {
                addressesArr[0] = "Rule 18- Zero Transaction Amount";
                atf = atfFileRepository.findByAtfFileDataRule19();
            } else if (i == 18) {
                addressesArr[0] = "Rule 19- Sale and UPI Multiple Records like HOST OR ACK OR INIT";
                atf = atfFileRepository.findByAtfFileDataRule20();
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
        logger.info("Void Or Reversal Transaction Id List Size --{}", voidOrReversalCase.size());
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
                if (update.get(0).getTransactionType().equals("Sale") && update.get(1).getTransactionType().equals("Sale")) {
                    update.get(0).setSaleUpiMultipleRecord(true);
                    update.get(1).setSaleUpiMultipleRecord(true);
                }
                if (update.get(0).getTransactionType().equals("UPI") && update.get(1).getTransactionType().equals("UPI")) {
                    update.get(0).setSaleUpiMultipleRecord(true);
                    update.get(1).setSaleUpiMultipleRecord(true);
                }
                if (update.get(0).getAmount().equals("000000000000") || update.get(1).getAmount().equals("000000000000")) {
                    update.get(0).setZeroTransactionAmount(true);
                    update.get(1).setZeroTransactionAmount(true);
                }
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
                                if ((DateUtil.parseSimpleDateForRules(update.get(0).getTransactionDate()).equals(DateUtil.parseSimpleDateForRules(update.get(1).getTransactionDate())))) {
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
                                    if ((DateUtil.parseSimpleDateForRules(update.get(0).getTransactionDate()).equals(DateUtil.parseSimpleDateForRules(update.get(1).getTransactionDate())))) {
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
                                if (((update.get(0).getMaskedCardNumber() == null) || (update.get(0).getRrn() == null) || (update.get(0).getTransactionAuthCode() == null) || (update.get(0).getResponseCode() == null) || (update.get(0).getTransactionDate() == null) || (update.get(0).getResponseDate() == null) || (update.get(0).getStan() == null) || (update.get(0).getMerchantId() == null) || (update.get(0).getTerminalId() == null) || (update.get(0).getInvoiceNumber() == null) || (update.get(0).getBatchNumber() == null) || (update.get(0).getAmount() == null) || (update.get(0).getTransactionType() == null) || (update.get(0).getTransactionId() == null) || (update.get(0).getOrgTransactionId() == null)) ||
                                        ((update.get(0).getMaskedCardNumber().equals("null")) || (update.get(0).getRrn().equals("null")) || (update.get(0).getTransactionAuthCode().equals("null")) || (update.get(0).getResponseCode().equals("null")) || (update.get(0).getTransactionDate().equals("null")) || (update.get(0).getResponseDate().equals("null")) || (update.get(0).getStan().equals("null")) || (update.get(0).getMerchantId().equals("null")) || (update.get(0).getTerminalId().equals("null")) || (update.get(0).getInvoiceNumber().equals("null")) || (update.get(0).getBatchNumber().equals("null")) || (update.get(0).getAmount().equals("null")) || (update.get(0).getTransactionType().equals("null")) || (update.get(0).getTransactionId().equals("null")) || (update.get(0).getOrgTransactionId().equals("null"))) ||
                                        ((update.get(0).getMaskedCardNumber().equals("")) || (update.get(0).getRrn().equals("")) || (update.get(0).getTransactionAuthCode().equals("")) || (update.get(0).getResponseCode().equals("")) || (update.get(0).getTransactionDate().equals("")) || (update.get(0).getResponseDate().equals("")) || (update.get(0).getStan().equals("")) || (update.get(0).getMerchantId().equals("")) || (update.get(0).getTerminalId().equals("")) || (update.get(0).getInvoiceNumber().equals("")) || (update.get(0).getBatchNumber().equals("")) || (update.get(0).getAmount().equals("")) || (update.get(0).getTransactionType().equals("")) || (update.get(0).getTransactionId().equals("")) || (update.get(0).getOrgTransactionId().equals("")))) {
                                    logger.info(DOUBLE_VALUE + " Checking Void and Reversal Null Values ---{}", update.get(0).getTransactionId());
                                    update.get(0).setVoidReversalNullValueStatus(true);
                                    update.get(1).setVoidReversalNullValueStatus(true);
                                }
                                if ((update.get(1).getStatus().equals("HOST")) && (!update.get(1).getResponseCode().equals("00"))) {
                                    update.get(0).setHostFailureWithReversal(true);
                                    update.get(1).setHostFailureWithReversal(true);
                                }
                            } else if (update.get(1).getTransactionType().equals("UPI")) {
                                if (((update.get(0).getRrn() == null) || (update.get(0).getResponseCode() == null) || (update.get(0).getTransactionDate() == null) || (update.get(0).getResponseDate() == null) || (update.get(0).getStan() == null) || (update.get(0).getMerchantId() == null) || (update.get(0).getTerminalId() == null) || (update.get(0).getInvoiceNumber() == null) || (update.get(0).getBatchNumber() == null) || (update.get(0).getAmount() == null) || (update.get(0).getTransactionType() == null) || (update.get(0).getTransactionId() == null) || (update.get(0).getOrgTransactionId() == null)) ||
                                        ((update.get(0).getRrn().equals("null")) || (update.get(0).getTransactionDate().equals("null")) || (update.get(0).getResponseDate().equals("null")) || (update.get(0).getStan().equals("null")) || (update.get(0).getMerchantId().equals("null")) || (update.get(0).getTerminalId().equals("null")) || (update.get(0).getInvoiceNumber().equals("null")) || (update.get(0).getBatchNumber().equals("null")) || (update.get(0).getAmount().equals("null")) || (update.get(0).getTransactionType().equals("null")) || (update.get(0).getTransactionId().equals("null")) || (update.get(0).getOrgTransactionId().equals("null"))) ||
                                        ((update.get(0).getRrn().equals("")) || (update.get(0).getResponseCode().equals("")) || (update.get(0).getTransactionDate().equals("")) || (update.get(0).getResponseDate().equals("")) || (update.get(0).getStan().equals("")) || (update.get(0).getMerchantId().equals("")) || (update.get(0).getTerminalId().equals("")) || (update.get(0).getInvoiceNumber().equals("")) || (update.get(0).getBatchNumber().equals("")) || (update.get(0).getAmount().equals("")) || (update.get(0).getTransactionType().equals("")) || (update.get(0).getTransactionId().equals("")) || (update.get(0).getOrgTransactionId().equals("")))) {
                                    logger.info(DOUBLE_VALUE + " Checking Void and Reversal Null Values ---{}", update.get(0).getTransactionId());
                                    update.get(0).setVoidReversalNullValueStatus(true);
                                    update.get(1).setVoidReversalNullValueStatus(true);
                                }
                                if ((update.get(1).getStatus().equals("HOST")) && (!update.get(1).getResponseCode().equals("00"))) {
                                    update.get(0).setHostFailureWithReversal(true);
                                    update.get(1).setHostFailureWithReversal(true);
                                }
                            }
                        }
                    } else {
                        if (update.get(1).getTransactionType().equals("Reversal")) {
                            if ((update.get(1).getStatus().equals("INIT"))) {
                                if (update.get(0).getTransactionType().equals("Sale")) {
                                    if (((update.get(1).getMaskedCardNumber() == null) || (update.get(1).getRrn() == null) || (update.get(1).getTransactionAuthCode() == null) || (update.get(1).getResponseCode() == null) || (update.get(1).getTransactionDate() == null) || (update.get(1).getResponseDate() == null) || (update.get(1).getStan() == null) || (update.get(1).getMerchantId() == null) || (update.get(1).getTerminalId() == null) || (update.get(1).getInvoiceNumber() == null) || (update.get(1).getBatchNumber() == null) || (update.get(1).getAmount() == null) || (update.get(1).getTransactionType() == null) || (update.get(1).getTransactionId() == null) || (update.get(1).getOrgTransactionId() == null)) ||
                                            ((update.get(1).getMaskedCardNumber().equals("null")) || (update.get(1).getRrn().equals("null")) || (update.get(1).getTransactionAuthCode().equals("null")) || (update.get(1).getResponseCode().equals("null")) || (update.get(1).getTransactionDate().equals("null")) || (update.get(1).getResponseDate().equals("null")) || (update.get(1).getStan().equals("null")) || (update.get(1).getMerchantId().equals("null")) || (update.get(1).getTerminalId().equals("null")) || (update.get(1).getInvoiceNumber().equals("null")) || (update.get(1).getBatchNumber().equals("null")) || (update.get(1).getAmount().equals("null")) || (update.get(1).getTransactionType().equals("null")) || (update.get(1).getTransactionId().equals("null")) || (update.get(1).getOrgTransactionId().equals("null"))) ||
                                            ((update.get(1).getMaskedCardNumber().equals("")) || (update.get(1).getRrn().equals("")) || (update.get(1).getTransactionAuthCode().equals("")) || (update.get(1).getResponseCode().equals("")) || (update.get(1).getTransactionDate().equals("")) || (update.get(1).getResponseDate().equals("")) || (update.get(1).getStan().equals("")) || (update.get(1).getMerchantId().equals("")) || (update.get(1).getTerminalId().equals("")) || (update.get(1).getInvoiceNumber().equals("")) || (update.get(1).getBatchNumber().equals("")) || (update.get(1).getAmount().equals("")) || (update.get(1).getTransactionType().equals("")) || (update.get(1).getTransactionId().equals("")) || (update.get(1).getOrgTransactionId().equals("")))) {
                                        logger.info(DOUBLE_VALUE + " Checking Void and Reversal Null Values ---{}", update.get(0).getTransactionId());
                                        update.get(0).setVoidReversalNullValueStatus(true);
                                        update.get(1).setVoidReversalNullValueStatus(true);
                                    }
                                    if ((update.get(0).getStatus().equals("HOST")) && (!update.get(0).getResponseCode().equals("00"))) {
                                        update.get(0).setHostFailureWithReversal(true);
                                        update.get(1).setHostFailureWithReversal(true);
                                    }
                                } else if (update.get(0).getTransactionType().equals("UPI")) {
                                    if (((update.get(1).getRrn() == null) || (update.get(1).getResponseCode() == null) || (update.get(1).getTransactionDate() == null) || (update.get(1).getResponseDate() == null) || (update.get(1).getStan() == null) || (update.get(1).getMerchantId() == null) || (update.get(1).getTerminalId() == null) || (update.get(1).getInvoiceNumber() == null) || (update.get(1).getBatchNumber() == null) || (update.get(1).getAmount() == null) || (update.get(1).getTransactionType() == null) || (update.get(1).getTransactionId() == null) || (update.get(1).getOrgTransactionId() == null)) ||
                                            ((update.get(1).getRrn().equals("null")) || (update.get(1).getTransactionDate().equals("null")) || (update.get(1).getResponseDate().equals("null")) || (update.get(1).getStan().equals("null")) || (update.get(1).getMerchantId().equals("null")) || (update.get(1).getTerminalId().equals("null")) || (update.get(1).getInvoiceNumber().equals("null")) || (update.get(1).getBatchNumber().equals("null")) || (update.get(1).getAmount().equals("null")) || (update.get(1).getTransactionType().equals("null")) || (update.get(1).getTransactionId().equals("null")) || (update.get(1).getOrgTransactionId().equals("null"))) ||
                                            ((update.get(1).getRrn().equals("")) || (update.get(1).getResponseCode().equals("")) || (update.get(1).getTransactionDate().equals("")) || (update.get(1).getResponseDate().equals("")) || (update.get(1).getStan().equals("")) || (update.get(1).getMerchantId().equals("")) || (update.get(1).getTerminalId().equals("")) || (update.get(1).getInvoiceNumber().equals("")) || (update.get(1).getBatchNumber().equals("")) || (update.get(1).getAmount().equals("")) || (update.get(1).getTransactionType().equals("")) || (update.get(1).getTransactionId().equals("")) || (update.get(1).getOrgTransactionId().equals("")))) {
                                        logger.info(DOUBLE_VALUE + " Checking Void and Reversal Null Values ---{}", update.get(0).getTransactionId());
                                        update.get(0).setVoidReversalNullValueStatus(true);
                                        update.get(1).setVoidReversalNullValueStatus(true);
                                    }
                                    if ((update.get(0).getStatus().equals("HOST")) && (!update.get(0).getResponseCode().equals("00"))) {
                                        update.get(0).setHostFailureWithReversal(true);
                                        update.get(1).setHostFailureWithReversal(true);
                                    }
                                }
                            }
                        }
                    }
                    if (update.get(0).getTransactionType().equals("Void")) {
                        if (!(update.get(0).getStatus().equals("INIT"))) {
                            if (((update.get(0).getMaskedCardNumber() == null) || (update.get(0).getRrn() == null) || (update.get(0).getTransactionAuthCode() == null) || (update.get(0).getResponseCode() == null) || (update.get(0).getTransactionDate() == null) || (update.get(0).getResponseDate() == null) || (update.get(0).getStan() == null) || (update.get(0).getMerchantId() == null) || (update.get(0).getTerminalId() == null) || (update.get(0).getInvoiceNumber() == null) || (update.get(0).getBatchNumber() == null) || (update.get(0).getAmount() == null) || (update.get(0).getTransactionType() == null) || (update.get(0).getTransactionId() == null) || (update.get(0).getOrgTransactionId() == null)) ||
                                    ((update.get(0).getMaskedCardNumber().equals("null")) || (update.get(0).getRrn().equals("null")) || (update.get(0).getTransactionAuthCode().equals("null")) || (update.get(0).getResponseCode().equals("null")) || (update.get(0).getTransactionDate().equals("null")) || (update.get(0).getResponseDate().equals("null")) || (update.get(0).getStan().equals("null")) || (update.get(0).getMerchantId().equals("null")) || (update.get(0).getTerminalId().equals("null")) || (update.get(0).getInvoiceNumber().equals("null")) || (update.get(0).getBatchNumber().equals("null")) || (update.get(0).getAmount().equals("null")) || (update.get(0).getTransactionType().equals("null")) || (update.get(0).getTransactionId().equals("null")) || (update.get(0).getOrgTransactionId().equals("null"))) ||
                                    ((update.get(0).getMaskedCardNumber().equals("")) || (update.get(0).getRrn().equals("")) || (update.get(0).getTransactionAuthCode().equals("")) || (update.get(0).getResponseCode().equals("")) || (update.get(0).getTransactionDate().equals("")) || (update.get(0).getResponseDate().equals("")) || (update.get(0).getStan().equals("")) || (update.get(0).getMerchantId().equals("")) || (update.get(0).getTerminalId().equals("")) || (update.get(0).getInvoiceNumber().equals("")) || (update.get(0).getBatchNumber().equals("")) || (update.get(0).getAmount().equals("")) || (update.get(0).getTransactionType().equals("")) || (update.get(0).getTransactionId().equals("")) || (update.get(0).getOrgTransactionId().equals("")))) {
                                logger.info(DOUBLE_VALUE + " Checking Void and Reversal Null Values ---{}", update.get(0).getTransactionId());
                                update.get(0).setVoidReversalNullValueStatus(true);
                                update.get(1).setVoidReversalNullValueStatus(true);
                            }
                        }
                    } else {
                        if (update.get(1).getTransactionType().equals("Void")) {
                            if (!(update.get(1).getStatus().equals("INIT"))) {
                                if (((update.get(1).getMaskedCardNumber() == null) || (update.get(1).getRrn() == null) || (update.get(1).getTransactionAuthCode() == null) || (update.get(1).getResponseCode() == null) || (update.get(0).getTransactionDate() == null) || (update.get(1).getResponseDate() == null) || (update.get(1).getStan() == null) || (update.get(1).getMerchantId() == null) || (update.get(1).getTerminalId() == null) || (update.get(1).getInvoiceNumber() == null) || (update.get(1).getBatchNumber() == null) || (update.get(1).getAmount() == null) || (update.get(1).getTransactionType() == null) || (update.get(1).getTransactionId() == null) || (update.get(1).getOrgTransactionId() == null)) ||
                                        ((update.get(1).getMaskedCardNumber().equals("null")) || (update.get(1).getRrn().equals("null")) || (update.get(1).getTransactionAuthCode().equals("null")) || (update.get(1).getResponseCode().equals("null")) || (update.get(1).getTransactionDate().equals("null")) || (update.get(1).getResponseDate().equals("null")) || (update.get(1).getStan().equals("null")) || (update.get(1).getMerchantId().equals("null")) || (update.get(1).getTerminalId().equals("null")) || (update.get(1).getInvoiceNumber().equals("null")) || (update.get(1).getBatchNumber().equals("null")) || (update.get(1).getAmount().equals("null")) || (update.get(1).getTransactionType().equals("null")) || (update.get(1).getTransactionId().equals("null")) || (update.get(1).getOrgTransactionId().equals("null"))) ||
                                        ((update.get(1).getMaskedCardNumber().equals("")) || (update.get(1).getRrn().equals("")) || (update.get(1).getTransactionAuthCode().equals("")) || (update.get(1).getResponseCode().equals("")) || (update.get(1).getTransactionDate().equals("")) || (update.get(1).getResponseDate().equals("")) || (update.get(1).getStan().equals("")) || (update.get(1).getMerchantId().equals("")) || (update.get(1).getTerminalId().equals("")) || (update.get(1).getInvoiceNumber().equals("")) || (update.get(1).getBatchNumber().equals("")) || (update.get(1).getAmount().equals("")) || (update.get(1).getTransactionType().equals("")) || (update.get(1).getTransactionId().equals("")) || (update.get(1).getOrgTransactionId().equals("")))) {
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
//                                    out = index[0] + "-" + index[1];
                                    out = index[0];

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
//                                        out = index[0] + "-" + index[1];
                                        out = index[0];
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
//                                    out = index[0] + "-" + index[1];
                                    out = index[0];
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
//                                        out = index[0] + "-" + index[1];
                                        out = index[0] ;
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
//                            out = index[0] + "-" + index[1];
                            out = index[0];
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
                            if ((fileReport.getRrn() == null || fileReport.getTransactionAuthCode() == null || fileReport.getResponseCode() == null || fileReport.getTransactionDate() == null || fileReport.getResponseDate() == null || fileReport.getStan() == null || fileReport.getTerminalId() == null || fileReport.getMerchantId() == null || fileReport.getInvoiceNumber() == null || fileReport.getBatchNumber() == null || fileReport.getAmount() == null || fileReport.getTransactionType() == null || fileReport.getTransactionId() == null) ||
                                    (fileReport.getRrn().equals("null") || fileReport.getTransactionAuthCode().equals("null") || fileReport.getResponseCode().equals("null") || fileReport.getTransactionDate().equals("null") || fileReport.getResponseDate().equals("null") || fileReport.getStan().equals("null") || fileReport.getTerminalId().equals("null") || fileReport.getMerchantId().equals("null") || fileReport.getInvoiceNumber().equals("null") || fileReport.getBatchNumber().equals("null") || fileReport.getAmount().equals("null") || fileReport.getTransactionType().equals("null") || fileReport.getTransactionId().equals("null")) ||
                                    (fileReport.getRrn().equals("") || fileReport.getTransactionAuthCode().equals("") || fileReport.getResponseCode().equals("") || fileReport.getTransactionDate().equals("") || fileReport.getResponseDate().equals("") || fileReport.getStan().equals("") || fileReport.getTerminalId().equals("") || fileReport.getMerchantId().equals("") || fileReport.getInvoiceNumber().equals("") || fileReport.getBatchNumber().equals("") || fileReport.getAmount().equals("") || fileReport.getTransactionType().equals("") || fileReport.getTransactionId().equals(""))) {
                                logger.info(SINGLE_VALUE + " checking Sale&UPI Required Fields is Null or Not---{}", fileReport.getTransactionId());
                                fileReport.setSaleUpiNullValueStatus(true);
                            }
                        } else {
                            if (!(fileReport.getResponseCode().equals("00"))) {
                                if ((fileReport.getResponseCode() == null || fileReport.getTransactionDate() == null || fileReport.getResponseDate() == null || fileReport.getStan() == null || fileReport.getTerminalId() == null || fileReport.getMerchantId() == null || fileReport.getInvoiceNumber() == null || fileReport.getBatchNumber() == null || fileReport.getAmount() == null || fileReport.getTransactionType() == null || fileReport.getTransactionId() == null) ||
                                        (fileReport.getResponseCode().equals("null") || fileReport.getTransactionDate().equals("null") || fileReport.getResponseDate().equals("null") || fileReport.getStan().equals("null") || fileReport.getTerminalId().equals("null") || fileReport.getMerchantId().equals("null") || fileReport.getInvoiceNumber().equals("null") || fileReport.getBatchNumber().equals("null") || fileReport.getAmount().equals("null") || fileReport.getTransactionType().equals("null") || fileReport.getTransactionId().equals("null")) ||
                                        (fileReport.getResponseCode().equals("") || fileReport.getTransactionDate().equals("") || fileReport.getResponseDate().equals("") || fileReport.getStan().equals("") || fileReport.getTerminalId().equals("") || fileReport.getMerchantId().equals("") || fileReport.getInvoiceNumber().equals("") || fileReport.getBatchNumber().equals("") || fileReport.getAmount().equals("") || fileReport.getTransactionType().equals("") || fileReport.getTransactionId().equals(""))) {
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
                            if ((fileReport.getRrn() == null || fileReport.getResponseCode() == null || fileReport.getTransactionDate() == null || fileReport.getResponseDate() == null || fileReport.getStan() == null || fileReport.getTerminalId() == null || fileReport.getMerchantId() == null || fileReport.getInvoiceNumber() == null || fileReport.getBatchNumber() == null || fileReport.getAmount() == null || fileReport.getTransactionType() == null || fileReport.getTransactionId() == null) ||
                                    (fileReport.getRrn().equals("null") || fileReport.getTransactionDate().equals("null") || fileReport.getResponseDate().equals("null") || fileReport.getStan().equals("null") || fileReport.getTerminalId().equals("null") || fileReport.getMerchantId().equals("null") || fileReport.getInvoiceNumber().equals("null") || fileReport.getBatchNumber().equals("null") || fileReport.getAmount().equals("null") || fileReport.getTransactionType().equals("null") || fileReport.getTransactionId().equals("null")) ||
                                    (fileReport.getRrn().equals("") || fileReport.getResponseCode().equals("") || fileReport.getTransactionDate().equals("") || fileReport.getResponseDate().equals("") || fileReport.getStan().equals("") || fileReport.getTerminalId().equals("") || fileReport.getMerchantId().equals("") || fileReport.getInvoiceNumber().equals("") || fileReport.getBatchNumber().equals("") || fileReport.getAmount().equals("") || fileReport.getTransactionType().equals("") || fileReport.getTransactionId().equals(""))) {
                                logger.info(SINGLE_VALUE + " checking Sale&UPI Required Fields is Null or Not---{}", fileReport.getTransactionId());
                                fileReport.setSaleUpiNullValueStatus(true);
                            }
                        } else {
                            if (!(fileReport.getResponseCode().equals("00"))) {
                                if ((fileReport.getResponseCode() == null || fileReport.getTransactionDate() == null || fileReport.getResponseDate() == null || fileReport.getStan() == null || fileReport.getTerminalId() == null || fileReport.getMerchantId() == null || fileReport.getInvoiceNumber() == null || fileReport.getBatchNumber() == null || fileReport.getAmount() == null || fileReport.getTransactionType() == null || fileReport.getTransactionId() == null) ||
                                        (fileReport.getTransactionDate().equals("null") || fileReport.getResponseDate().equals("null") || fileReport.getStan().equals("null") || fileReport.getTerminalId().equals("null") || fileReport.getMerchantId().equals("null") || fileReport.getInvoiceNumber().equals("null") || fileReport.getBatchNumber().equals("null") || fileReport.getAmount().equals("null") || fileReport.getTransactionType().equals("null") || fileReport.getTransactionId().equals("null")) ||
                                        (fileReport.getResponseCode().equals("") || fileReport.getTransactionDate().equals("") || fileReport.getResponseDate().equals("") || fileReport.getStan().equals("") || fileReport.getTerminalId().equals("") || fileReport.getMerchantId().equals("") || fileReport.getInvoiceNumber().equals("") || fileReport.getBatchNumber().equals("") || fileReport.getAmount().equals("") || fileReport.getTransactionType().equals("") || fileReport.getTransactionId().equals(""))) {
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
                if (fileReport.getAmount().equals("000000000000")) {
                    fileReport.setZeroTransactionAmount(true);
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

        addressesArr[0] = "Rule 20 - ATF File Missing Transactions - RRN List";
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
                e.printStackTrace();
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



    public Connection getCheckATFConnection() throws SQLException {
        return DriverManager.getConnection(atfCheckUrl, atfCheckUserName, atfCheckPassword);
    }



//    @Override
//    public void executeQueryUpdation() {
//        atfFileRepository.removeReversalEntries();
//        atfFileRepository.removeVoidEntries();
//        atfFileRepository.removeSaleWithInitEntries();
//        atfFileRepository.removeUPIWithInitEntries();
//        atfFileRepository.removeSaleAndHostNotInResponseCodeSuccess();
//        atfFileRepository.removeUPIAndHostNotInResponseCodeSuccess();
//        logger.info("All Queries are updated successfully---");
//    }


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
        List<Object[]> settlementDataOut = atfFileRepository.findByFinalSettlementData(date1,date2);

        if (!atfMissingDataFile.exists()) {
            logger.info("ATF File Missing data size --{}", atfMissingDataOut.size());
            ReportUtil.generateAllTxnAndSettlementMissingDataCSVFile(atfMissingDataOut, Constants.TXN_SETTLEMENT_FILE_HEADER, atfMissingDataFile, atfFileSheet);
        }
        if (!settlementMissingDataFile.exists()) {
            logger.info("Settlement File Missing data size --{}", settlementMissingDataOut.size());
            ReportUtil.generateSettlementMissingDataCSVFile(settlementMissingDataOut, Constants.TXN_SETTLEMENT_MISSING_FILE_HEADER, settlementMissingDataFile, settlementMissingDataFileSheet);
        }

        int deleteQuery = this.removeATFMissingRecord(date1, date2);
        logger.info("delete Query Out Final --{}", deleteQuery);
        if (deleteQuery > 1) {
//            atfFileRepository.removeMissingATFDataBasedOnSettlementData(date1, date2);
            if (!settlementDataFile.exists()) {
                ReportUtil.generateAllTxnAndSettlementMissingDataCSVFile(settlementDataOut, Constants.TXN_SETTLEMENT_FILE_HEADER, settlementDataFile, settlementDataFileSheet);
            }
        }
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
        String sourcePath = atfSftpSourcePath + currentDate;
        String sourcePath1 = atfSftpSourcePath + previousDate;
        String destinationPath = updatedAtfFilePath;


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
                } else {
                    return status;
                }
                if (exists(mChannelSftp, sourcePath1)) {
                    mChannelSftp.cd(sourcePath1);
                    logger.info("=====recursive folder download started=====Source -{} ----Destination -{}", sourcePath, destinationPath);
                    recursiveFolderDownload(sourcePath1,
                            destinationPath);
                    logger.info("downloadStatus Success Previous Date ! ");
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
    public void validatedAtfFileReport(String date) throws IOException, ParseException {
        Date currentDate = DateUtil.currentDate();
        String currentDateTime = DateUtil.dateToStringForATF(currentDate);
        String updatedAtfFile = updatedAtfFilePath + "Validated_ATF_Report"+date+".txt";
        String atfFileSheet = "Validated_ATF_Report-" + currentDateTime + ".txt";
        List<Object[]> atf = null;
        for (int i = 0; i < 20; i++) {
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
            } else if (i == 18) {
                addressesArr[0] = "Rule 19- Zero Transaction Amount";
                atf = atfFileRepository.findByAtfFileDataRule19();
            } else if (i == 19) {
                addressesArr[0] = "Rule 20- Sale and UPI Multiple Records like HOST OR ACK OR INIT";
                atf = atfFileRepository.findByAtfFileDataRule20();
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
                    logger.info("file name in SFTP ----" + item.getFilename());

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


    @Override
    public boolean updateNotificationDataIntoDb(String notificationFile) throws IOException, ParseException {
        boolean updatedNotificationData = false;
        boolean updatedNotificationFields = false;
        DataFormatter formatter = new DataFormatter();

        List<NotificationData> notificationDataList = new ArrayList<>();
        List<NotificationFields> notificationFieldsList = new ArrayList<>();
        String fileExtension = "";
        int index = notificationFile.lastIndexOf(".");
        if (index > 0) {
            fileExtension = notificationFile.substring(index + 1);
            if (fileExtension.equals("xlsx")) {
                Workbook workbook = null;
                try {
                    FileInputStream excelFile = new FileInputStream(notificationFile);


                    workbook = new XSSFWorkbook(excelFile);

                    //Insertion of Notification Data

                    Sheet notificationDataSheet = workbook.getSheetAt(0);
                    Iterator<Row> rowIteratorForNotificationData = notificationDataSheet.rowIterator();

                    while (rowIteratorForNotificationData.hasNext()) {
                        Row row = rowIteratorForNotificationData.next();
                        // skip first row, as it contains column names
                        if (row.getRowNum() == 0) {
                            continue;
                        }

                        NotificationData notificationData = new NotificationData();
                        notificationData.setId((row.getCell(0) != null && !row.getCell(0).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? Long.parseLong(row.getCell(0).getStringCellValue().replaceAll("\u00a0", "").trim()) : null);
                        notificationData.setTxnCorrelationId((row.getCell(1) != null && !row.getCell(1).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(1).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationData.setTransactionId((row.getCell(2) != null && !row.getCell(2).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(2).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationData.setOrgTransactionId((row.getCell(3) != null && !row.getCell(3).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(3).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationData.setNotificationType((row.getCell(4) != null && !row.getCell(4).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(4).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationData.setTerminalId((row.getCell(5) != null && !row.getCell(5).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(5).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationData.setMTI((row.getCell(6) != null && !row.getCell(6).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(6).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationData.setTransactionType((row.getCell(7) != null && !row.getCell(7).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(7).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationData.setProcessingCode((row.getCell(8) != null && !row.getCell(8).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(8).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationData.setDescription((row.getCell(9) != null && !row.getCell(9).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(9).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationData.setNotificationStatus((row.getCell(10) != null && !row.getCell(10).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(10).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationData.setDateTime((row.getCell(11) != null && !row.getCell(11).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? DateUtil.stringToDate(row.getCell(11).getStringCellValue().replaceAll("\u00a0", "").trim()) : null);
                        notificationData.setRrn((row.getCell(12) != null && !row.getCell(12).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(12).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationData.setMerchantId((row.getCell(13) != null && !row.getCell(13).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(13).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationData.setTransactionDateTime((row.getCell(14) != null && !row.getCell(14).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(14).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationData.setNotificationFieldsId((row.getCell(15) != null && !row.getCell(15).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? Long.parseLong(row.getCell(15).getStringCellValue().replaceAll("\u00a0", "").trim()) : null);
                        notificationData.setTransactionAuthCode((row.getCell(16) != null && !row.getCell(16).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(16).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationData.setResponseDateTime((row.getCell(17) != null && !row.getCell(17).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(17).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationData.setHostResponseDateTime((row.getCell(18) != null && !row.getCell(18).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(18).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationData.setResponseCode((row.getCell(19) != null && !row.getCell(19).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(19).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationData.setStan((row.getCell(20) != null && !row.getCell(20).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(20).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationData.setSettlementStatus((row.getCell(21) != null && !row.getCell(21).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(21).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationData.setInvoiceNumber((row.getCell(22) != null && !row.getCell(22).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(22).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationData.setBatchNumber((row.getCell(23) != null && !row.getCell(23).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(23).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationData.setNotificationRecipient((row.getCell(24) != null && !row.getCell(24).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(24).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationDataList.add(notificationData);
                        logger.info("Notification Data - Transaction Id - {}", notificationData.getTransactionId());

                    }

                    notificationDataRepository.saveAll(notificationDataList);
                    updatedNotificationData = true;

                    logger.info("Notification Data File Inserted Successfully----!!!");

                    //Insertion of Notification Fields

                    Sheet notificationFieldsSheet = workbook.getSheetAt(1);
                    Iterator<Row> rowIteratorForNotificationFields = notificationFieldsSheet.rowIterator();

                    while (rowIteratorForNotificationFields.hasNext()) {
                        Row row = rowIteratorForNotificationFields.next();
                        // skip first row, as it contains column names
                        if (row.getRowNum() == 0) {
                            continue;
                        }

                        NotificationFields notificationFields = new NotificationFields();

                        notificationFields.setId((row.getCell(0) != null && !row.getCell(0).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? Long.parseLong(formatter.formatCellValue(row.getCell(0)).replaceAll("\u00a0", "").trim()) : null);
                        notificationFields.setRrn((row.getCell(1) != null && !row.getCell(1).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(1).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationFields.setBatchNumber((row.getCell(2) != null && !row.getCell(2).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(2).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationFields.setCardHolderName((row.getCell(3) != null && !row.getCell(3).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(3).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationFields.setMerchantId((row.getCell(4) != null && !row.getCell(4).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(4).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationFields.setAmount((row.getCell(5) != null && !row.getCell(5).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(5).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationFields.setMaskedCardNumber((row.getCell(6) != null && !row.getCell(6).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(6).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationFields.setPosDeviceId((row.getCell(7) != null && !row.getCell(7).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(7).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationFields.setTransactionMode((row.getCell(8) != null && !row.getCell(8).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(8).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationFields.setTransactionType((row.getCell(9) != null && !row.getCell(9).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(9).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationFields.setCardNetwork((row.getCell(10) != null && !row.getCell(10).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(10).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationFields.setInvoiceNumber((row.getCell(11) != null && !row.getCell(11).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(11).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationFields.setAcquirerBank((row.getCell(12) != null && !row.getCell(12).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(12).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationFields.setCardIssuerCountryCode((row.getCell(13) != null && !row.getCell(13).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(13).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationFields.setCardType((row.getCell(14) != null && !row.getCell(14).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(14).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationFields.setTransactionDateTime((row.getCell(15) != null && !row.getCell(15).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(15).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationFields.setSettlementMode((row.getCell(16) != null && !row.getCell(16).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(16).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationFields.setTerminalId((row.getCell(17) != null && !row.getCell(17).toString().replaceAll("\u00a0", "").trim().equals("NULL")) ? row.getCell(17).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        notificationFieldsList.add(notificationFields);
                        logger.info("Notification Field - RRN -  {}", notificationFields.getRrn());

                    }

                    notificationFieldsRepository.saveAll(notificationFieldsList);
                    updatedNotificationFields = true;

                    workbook.close();
                    excelFile.close();

                    logger.info("Notification Fields Inserted Successfully----!!!");
                } catch (FileNotFoundException fe) {
                    logger.info("Notification File not available for " + DateUtil.allTxnDate());
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            } else {
                logger.info("Invalid File format");
            }
        }

        return updatedNotificationData && updatedNotificationFields;
    }

    @Override
    public boolean updatedSwitchReqResDataIntoDb(String switchReqResFile) {
        boolean updatedSwitchRequest = false;
        boolean updatedSwitchResponse = false;

        List<SwitchRequest> switchRequestList = new ArrayList<>();
        List<SwitchResponse> switchResponseList = new ArrayList<>();
        String fileExtension = "";
        int index = switchReqResFile.lastIndexOf(".");
        if (index > 0) {
            fileExtension = switchReqResFile.substring(index + 1);
            if (fileExtension.equals("xlsx")) {
                try {
                    FileInputStream excelFile = new FileInputStream(switchReqResFile);

                    Workbook workbook = new XSSFWorkbook(excelFile);

                    //Insertion of Switch Request

                    Sheet switchRequestSheet = workbook.getSheetAt(0);
                    Iterator<Row> rowIteratorForSwitchRequest = switchRequestSheet.rowIterator();

                    while (rowIteratorForSwitchRequest.hasNext()) {
                        Row row = rowIteratorForSwitchRequest.next();
                        // skip first row, as it contains column names
                        if (row.getRowNum() == 0) {
                            continue;
                        }

                        SwitchRequest switchRequest = new SwitchRequest();
                        switchRequest.setTxnCorrelationId((row.getCell(0) != null) && !row.getCell(0).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(0).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setCardholderPan((row.getCell(1) != null) && !row.getCell(1).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(1).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setMerchantPan((row.getCell(2) != null) && !row.getCell(2).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(2).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setBeneficiaryPan((row.getCell(3) != null) && !row.getCell(3).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(3).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setCardholderAccNo((row.getCell(4) != null) && !row.getCell(4).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(4).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setBeneficiaryAccNo((row.getCell(5) != null) && !row.getCell(5).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(5).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setMTI((row.getCell(6) != null) && !row.getCell(6).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(6).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTxnType((row.getCell(7) != null) && !row.getCell(7).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(7).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setCardholderAccType((row.getCell(8) != null) && !row.getCell(8).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(8).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setBeneficiaryAccType((row.getCell(9) != null) && !row.getCell(9).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(9).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setStan((row.getCell(10) != null) && !row.getCell(10).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(10).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setEMV((row.getCell(11) != null) && !row.getCell(11).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(11).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setExpiryDate((row.getCell(12) != null && !row.getCell(12).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? Long.valueOf(row.getCell(12).getStringCellValue().replaceAll("\u00a0", "").trim()) : null));
                        switchRequest.setServiceCode((row.getCell(13) != null) && !row.getCell(13).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(13).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setAadhaarNumber((row.getCell(14) != null) && !row.getCell(14).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(14).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setSuperInstId((row.getCell(15) != null) && !row.getCell(15).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(15).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setInstitutionId((row.getCell(16) != null) && !row.getCell(16).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(16).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setSponsorBankId((row.getCell(17) != null) && !row.getCell(17).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(17).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setSuperMerchantId((row.getCell(18) != null) && !row.getCell(18).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(18).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setGroupMerchantId((row.getCell(19) != null) && !row.getCell(19).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(19).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setMerchantId((row.getCell(20) != null) && !row.getCell(20).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(20).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setSubMerchantId((row.getCell(21) != null) && !row.getCell(21).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(21).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTerminalId((row.getCell(22) != null) && !row.getCell(22).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(22).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setCardHolderCountryCode((row.getCell(23) != null) && !row.getCell(23).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(23).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTerminalAccNo((row.getCell(24) != null) && !row.getCell(24).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(24).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTerminalCardReadCapability((row.getCell(25) != null) && !row.getCell(25).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(25).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTerminalAuthReadCapability((row.getCell(26) != null) && !row.getCell(26).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(26).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTerminalCardInputMode((row.getCell(27) != null) && !row.getCell(27).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(27).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTerminalOpMode((row.getCell(28) != null) && !row.getCell(28).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(28).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTerminalOpEnv((row.getCell(29) != null) && !row.getCell(29).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(29).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTerminalCardCaptureCapability((row.getCell(30) != null) && !row.getCell(30).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(30).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTerminalCardPresentStatus((row.getCell(31) != null) && !row.getCell(31).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(31).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTerminalCardHolderPresentStatus((row.getCell(32) != null) && !row.getCell(32).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(32).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTerminalOuptutCapability((row.getCell(33) != null) && !row.getCell(33).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(33).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTerminalPINCaptureCapability((row.getCell(34) != null) && !row.getCell(34).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(34).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTerminalAddress((row.getCell(35) != null) && !row.getCell(35).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(35).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTerminalCity((row.getCell(36) != null) && !row.getCell(36).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(36).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTerminalStateCode((row.getCell(37) != null) && !row.getCell(37).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(37).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTerminalCountryCode((row.getCell(38) != null) && !row.getCell(38).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(38).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTerminalType((row.getCell(39) != null) && !row.getCell(39).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(39).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setPANEntryMode((row.getCell(40) != null) && !row.getCell(40).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(40).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setPINEntryCapability((row.getCell(41) != null) && !row.getCell(41).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(41).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setPOSConditionCode((row.getCell(42) != null) && !row.getCell(42).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(42).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setMerchantName((row.getCell(43) != null) && !row.getCell(43).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(43).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setMerchantCity((row.getCell(44) != null) && !row.getCell(44).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(44).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setMerchantStateCode((row.getCell(45) != null) && !row.getCell(45).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(45).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setMerchantCountryCode((row.getCell(46) != null) && !row.getCell(46).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(46).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setMerchantCategoryCode((row.getCell(47) != null) && !row.getCell(47).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(47).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setMerchantType((row.getCell(48) != null) && !row.getCell(48).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(48).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setMerchantFraudScore((row.getCell(49) != null) && !row.getCell(49).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(49).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setBillOrInvoiceNumber((row.getCell(50) != null) && !row.getCell(50).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(50).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTxnDateTime((row.getCell(51) != null) && !row.getCell(51).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(51).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTxnDate((row.getCell(52) != null) && !row.getCell(52).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(52).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTxnTime((row.getCell(53) != null) && !row.getCell(53).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(53).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setRRNumber((row.getCell(54) != null) && !row.getCell(54).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(54).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setBatchNumber((row.getCell(55) != null) && !row.getCell(55).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(55).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setInvoiceNumber((row.getCell(56) != null) && !row.getCell(56).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(56).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTxnCurrencyCode((row.getCell(57) != null) && !row.getCell(57).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(57).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTxnAmount((row.getCell(58) != null) && !row.getCell(58).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(58).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTxnBillingAmount((row.getCell(59) != null) && !row.getCell(59).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(59).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTxnFeeAmount((row.getCell(60) != null) && !row.getCell(60).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(60).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTxnFeeCurrencyCode((row.getCell(61) != null) && !row.getCell(61).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(61).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTxnFeeAmountType((row.getCell(62) != null) && !row.getCell(62).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(62).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setSettlementAmount((row.getCell(63) != null) && !row.getCell(63).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(63).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setSettlementCurrencyCode((row.getCell(64) != null) && !row.getCell(64).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(64).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setSettlementDate((row.getCell(65) != null) && !row.getCell(65).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(65).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTxnAdditionalAmount((row.getCell(66) != null) && !row.getCell(66).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(66).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTxnAdditionalAmountType((row.getCell(67) != null) && !row.getCell(67).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(67).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTxnReversalAmount((row.getCell(68) != null) && !row.getCell(68).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(68).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setAdviseReasonCode((row.getCell(69) != null) && !row.getCell(69).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(69).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setAcqInstitutionId((row.getCell(70) != null) && !row.getCell(70).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(70).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setFwdInstitutionId((row.getCell(71) != null) && !row.getCell(71).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(71).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setNetworkSourceId((row.getCell(72) != null) && !row.getCell(72).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(72).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setNetworkDestId((row.getCell(73) != null) && !row.getCell(73).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(73).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTxnIdentifier((row.getCell(74) != null) && !row.getCell(74).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(74).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setAuthResponseCode((row.getCell(75) != null) && !row.getCell(75).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(75).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setSrcChannelType((row.getCell(76) != null) && !row.getCell(76).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(76).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setSrcRoutingId((row.getCell(77) != null) && !row.getCell(77).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(77).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setDestRoutingId((row.getCell(78) != null) && !row.getCell(78).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(78).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setMandatoryCheckStatus((row.getCell(79) != null) && !row.getCell(79).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(79).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setMerchantValidationStatus((row.getCell(80) != null) && !row.getCell(80).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(80).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setPVVVerficationStatus((row.getCell(81) != null) && !row.getCell(81).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(81).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setCVVVerficationStatus((row.getCell(82) != null) && !row.getCell(82).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(82).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setCVV2VerficationStatus((row.getCell(83) != null) && !row.getCell(83).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(83).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setEMVValidationStatus((row.getCell(84) != null) && !row.getCell(84).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(84).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setSwitchMode((row.getCell(85) != null) && !row.getCell(85).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(85).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setBinId((row.getCell(86) != null) && !row.getCell(86).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(86).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setBIN((row.getCell(87) != null) && !row.getCell(87).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(87).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setOriginalMTI((row.getCell(88) != null) && !row.getCell(88).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(88).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setOriginalDateTime((row.getCell(89) != null) && !row.getCell(89).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(89).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setOriginalStan((row.getCell(90) != null) && !row.getCell(90).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(90).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setOriginalAcqInstId((row.getCell(91) != null) && !row.getCell(91).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(91).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setOriginalFwdInstId((row.getCell(92) != null) && !row.getCell(92).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(92).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setAuthIndicator((row.getCell(93) != null) && !row.getCell(93).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(93).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setECIndicator((row.getCell(94) != null) && !row.getCell(94).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(94).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setPaySecureIssuerId((row.getCell(95) != null) && !row.getCell(95).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(95).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setNetworkId((row.getCell(96) != null && !row.getCell(96).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? Long.valueOf(row.getCell(96).getStringCellValue().replaceAll("\u00a0", "").trim()) : null));
                        switchRequest.setPreAuthTimeLimit((row.getCell(97) != null) && !row.getCell(97).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(97).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTerminalIssueId((row.getCell(98) != null) && !row.getCell(98).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(98).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setTerminalComment((row.getCell(99) != null) && !row.getCell(99).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(99).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setMerchantMobileNo((row.getCell(100) != null) && !row.getCell(100).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(100).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setCardInfo((row.getCell(101) != null) && !row.getCell(101).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(101).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setMatrixData((row.getCell(102) != null) && !row.getCell(102).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(102).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setIntRefNo((row.getCell(103) != null) && !row.getCell(103).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(103).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setUrn((row.getCell(104) != null) && !row.getCell(104).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(104).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setRefundId((row.getCell(105) != null) && !row.getCell(105).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(105).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setDeviceInvoiceNumber((row.getCell(106) != null) && !row.getCell(106).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(106).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchRequest.setRequestRouteTime(row.getCell(107) != null && !row.getCell(107).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? DateUtil.stringToDate(row.getCell(107).getStringCellValue().replaceAll("\u00a0", "").trim()) : null);

                        switchRequestList.add(switchRequest);
                        logger.info("Switch Request - Correlation id  - {}", switchRequest.getTxnCorrelationId());

                    }

                    switchRequestRepository.saveAll(switchRequestList);
                    updatedSwitchRequest = true;

                    logger.info("Switch Request File Inserted Successfully----!!!");

                    //Insertion of Switch Response

                    Sheet switchResponseSheet = workbook.getSheetAt(1);
                    Iterator<Row> rowIteratorForSwitchResponse = switchResponseSheet.rowIterator();

                    while (rowIteratorForSwitchResponse.hasNext()) {
                        Row row = rowIteratorForSwitchResponse.next();
                        // skip first row, as it contains column names
                        if (row.getRowNum() == 0) {
                            continue;
                        }

                        SwitchResponse switchResponse = new SwitchResponse();
                        switchResponse.setTxnCorrelationId((row.getCell(0) != null) && !row.getCell(0).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(0).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchResponse.setMTI((row.getCell(1) != null) && !row.getCell(1).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(1).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchResponse.setStan((row.getCell(2) != null) && !row.getCell(2).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(2).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchResponse.setTxnDateTimeGMT((row.getCell(3) != null) && !row.getCell(3).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(3).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchResponse.setRRNumber((row.getCell(4) != null) && !row.getCell(4).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(4).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchResponse.setTxnResponseCode((row.getCell(5) != null) && !row.getCell(5).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(5).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchResponse.setAuthResponseCode((row.getCell(6) != null) && !row.getCell(6).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(6).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchResponse.setAdditionalResponseData((row.getCell(7) != null) && !row.getCell(7).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(7).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchResponse.setTxnIdentifier((row.getCell(8) != null) && !row.getCell(8).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(8).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchResponse.setTxnResponseDescription((row.getCell(9) != null) && !row.getCell(9).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(9).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchResponse.setTxnFailureCode((row.getCell(10) != null) && !row.getCell(10).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(10).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchResponse.setTxnProcessedBy((row.getCell(11) != null) && !row.getCell(11).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(11).getStringCellValue().replaceAll("\u00a0", "").trim() : "");
                        switchResponse.setResponseReceivedTime(DateUtil.stringToDate((row.getCell(12) != null) && !row.getCell(12).toString().replaceAll("\u00a0", "").trim().equals("NULL") ? row.getCell(12).getStringCellValue().replaceAll("\u00a0", "").trim() : ""));

                        switchResponseList.add(switchResponse);
                        logger.info("Switch Response - Correlation id  - {}", switchResponse.getTxnCorrelationId());

                    }

                    switchResponseRepository.saveAll(switchResponseList);
                    updatedSwitchResponse = true;

                    logger.info("Switch Response Inserted Successfully----!!!");
                } catch (FileNotFoundException fe) {
                    logger.info("Switch File not available for " + DateUtil.allTxnDate());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                logger.info("Invalid File format");
            }
        }
        return updatedSwitchRequest && updatedSwitchResponse;
    }

    @Override
    public boolean generateATFfile(MultipartFile file, String atfFile) throws IOException, SQLException {

        byte[] bytes = file.getBytes();
        final String filepath = file.getOriginalFilename();
        Path path = Paths.get(file.getOriginalFilename());
        Files.write(path, bytes);

        File existingAtfFile = new File(atfFile);

        if (existingAtfFile.delete()) {
            logger.info("File Deleted");
        } else {
            logger.info("File Deletion Failed");
        }

        CSVWriter csvWriter = new CSVWriter(new FileWriter(atfFile, true), ',', CSVWriter.NO_QUOTE_CHARACTER);

        List<String[]> finalOut = new ArrayList<>();
        String[] out = null;

        String[] header = {"terminalId", "merchantId", "posDeviceId", "batchNumber", "cardHolderName", "maskedCardNumber", "transactionMode", "invoiceNumber", "acquirerBank", "cardType", "cardNetwork", "cardIssuerCountryCode", "amount", "responseCode", "rrn", "transactionAuthCode",
                "transactionDate", "responseDate", "transactionId", "orgTransactionID", "transactionType", "status", "Stan", "settlementMode", "settlementStatus"};
        csvWriter.writeNext(header);


        Connection con = null;
        Statement stmt = null;


        con = getCheckATFConnection();
        stmt = con.createStatement();

        String fileExtension = "";
        int index = file.getOriginalFilename().lastIndexOf(".");
        if (index > 0) {
            fileExtension = file.getOriginalFilename().substring(index + 1);
        }
        if (fileExtension.equals("xlsx")) {
        logger.info("inside excel file");
            try {
                FileInputStream excelFile = new FileInputStream(new File(filepath));
                Workbook workbook = new XSSFWorkbook(excelFile);
                Sheet sheet = workbook.getSheetAt(0);
                Iterator<Row> rowIterator = sheet.rowIterator();
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    if (row.getRowNum() == 0) {
                        continue;// skip first row, as it contains column name
                    }
                    Iterator<Cell> cellIterator = row.cellIterator();
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        if (cell.getColumnIndex() == 0 && cell.getCellTypeEnum() != CellType.BLANK) {
                            DataFormatter formatter = new DataFormatter();
                            String rrnOrtxn = formatter.formatCellValue(sheet.getRow(row.getRowNum()).getCell(cell.getColumnIndex())).replaceAll("\u00a0", "").trim();
                            logger.info("RRN or Transaction Id  ---{}",rrnOrtxn);

                           /* Query to fetch ATF from notification Data and Notification Field
                            When notification data table has notification field id as 0 (happens in rare case), to map to nofication field table,
                            we use terminal id and nf. transaction date time with timeframe match with nd response date time with 15 second frame to match the records
                            */

                            String notificationQuery = "select nd.TerminalId,nd.merchant_Id,nf.pos_Device_Id,nd.batch_number,nf.card_holder_name,nf.masked_card_number,\n" +
                                    "                                    nf.transaction_mode,nd.invoice_number,nf.acquirer_bank,nf.card_type,nf.card_network,nf.card_issuer_country_code,nf.amount,\n" +
                                    "                                    nd.response_code,nd.rrn,nd.transaction_auth_code,nd.transaction_date_time,nd.DateTime,nd.TransactionId,\n" +
                                    "                                    CASE WHEN nd.OrgTransactionId = '' THEN 'null'\n" +
                                    "                                    ELSE nd.OrgTransactionId END AS OrgTransactionId,\n" +
                                    "                                    CASE WHEN nd.OrgTransactionId != 'null' and nd.TransactionType = 'UPI' THEN 'Reversal'\n" +
                                    "                                    ELSE nd.TransactionType END AS TransactionType ,nd.NotificationType as status,nd.Stan,'Auto' as settlement_mode,nd.settlement_status\n" +
                                    "                                    FROM notification_data nd\n" +
                                    "                                    left join notification_fields nf\n" +
                                    "                                    on  nd.notification_fields_id = nf.Id\n" +
                                    "                                          where nd.rrn ='"+rrnOrtxn+"' or nd.TransactionId ='"+rrnOrtxn+"';";

                            logger.info("Notification Query ----{}",notificationQuery);
                            ResultSet notificationRS = stmt.executeQuery(notificationQuery);

                            List<AtfFileDTO> atfFileDTOFromNotificationList = new ArrayList<>();


                            //Load notification result set

                            while (notificationRS.next()) {
                                AtfFileDTO atfFileNotificationDTO = new AtfFileDTO();
                                atfFileNotificationDTO.setTerminalId(notificationRS.getString("TerminalId"));
                                atfFileNotificationDTO.setMerchantId(notificationRS.getString("merchant_Id"));
                                atfFileNotificationDTO.setPosDeviceId(notificationRS.getString("pos_Device_Id"));
                                atfFileNotificationDTO.setBatchNumber(notificationRS.getString("batch_number"));
                                atfFileNotificationDTO.setCardHolderName(notificationRS.getString("card_holder_name"));
                                atfFileNotificationDTO.setMaskedCardNumber(notificationRS.getString("masked_card_number"));
                                atfFileNotificationDTO.setTransactionMode(notificationRS.getString("transaction_mode"));
                                atfFileNotificationDTO.setInvoiceNumber(notificationRS.getString("invoice_number"));
                                atfFileNotificationDTO.setAcquireBank(notificationRS.getString("acquirer_bank"));
                                atfFileNotificationDTO.setCardType(notificationRS.getString("card_type"));
                                atfFileNotificationDTO.setCardNetwork(notificationRS.getString("card_network"));
                                atfFileNotificationDTO.setCardIssuerCountryCode(notificationRS.getString("card_issuer_country_code"));
                                atfFileNotificationDTO.setAmount(notificationRS.getString("amount"));
                                atfFileNotificationDTO.setResponseCode(notificationRS.getString("response_code"));
                                atfFileNotificationDTO.setRrn(notificationRS.getString("rrn"));
                                atfFileNotificationDTO.setTransactionAuthCode(notificationRS.getString("transaction_auth_code"));
                                String txnDateTime = notificationRS.getString("transaction_date_time");
                                if (!txnDateTime.isEmpty() && !txnDateTime.trim().equalsIgnoreCase("NULL")) {
                                    Date txnDate = DateUtil.stringToDate(txnDateTime.trim());
                                    atfFileNotificationDTO.setTransactionDate(txnDate);
                                } else {
                                    atfFileNotificationDTO.setTransactionDate(null);
                                }

                                String responseDateTime = notificationRS.getString("DateTime");
                                //if(!responseDateTime.isEmpty()){
                                    Date resDate = DateUtil.stringToDate(responseDateTime);
                                atfFileNotificationDTO.setResponseDate(resDate);
                                //}
                                //else {
                                //    atfFileNotificationDTO.setResponseDate(null);
                                //}

                                atfFileNotificationDTO.setTransactionId(notificationRS.getString("TransactionId"));
                                atfFileNotificationDTO.setOrgTransactionId(notificationRS.getString("OrgTransactionId"));
                                atfFileNotificationDTO.setTransactionType(notificationRS.getString("TransactionType"));
                                atfFileNotificationDTO.setStatus(notificationRS.getString("status"));
                                atfFileNotificationDTO.setStan(notificationRS.getString("Stan"));
                                atfFileNotificationDTO.setSettlementMode(notificationRS.getString("settlement_mode"));
                                atfFileNotificationDTO.setSettlementStatus(notificationRS.getString("settlement_status"));

                                atfFileDTOFromNotificationList.add(atfFileNotificationDTO);

                            }

                            if (atfFileDTOFromNotificationList.size() > 0) {
                                logger.info("IF Part Executing -----");

                                List<AtfFileDTO> saleAckList = atfFileDTOFromNotificationList.stream().filter(r -> ((r.getTransactionType().equals("Sale") || r.getTransactionType().equals("UPI")) && r.getStatus().equals("ACK"))).collect(Collectors.toList());
                                List<AtfFileDTO> saleHostList = atfFileDTOFromNotificationList.stream().filter(r -> ((r.getTransactionType().equals("Sale") || r.getTransactionType().equals("UPI")) && r.getStatus().equals("HOST"))).collect(Collectors.toList());
                                List<AtfFileDTO> saleInitList = atfFileDTOFromNotificationList.stream().filter(r -> ((r.getTransactionType().equals("Sale") || r.getTransactionType().equals("UPI")) && r.getStatus().equals("INIT"))).collect(Collectors.toList());
                                List<AtfFileDTO> voidHostList = atfFileDTOFromNotificationList.stream().filter(r -> (r.getTransactionType().equals("Void") && r.getStatus().equals("HOST"))).collect(Collectors.toList());
                                List<AtfFileDTO> voidInitList = atfFileDTOFromNotificationList.stream().filter(r -> (r.getTransactionType().equals("Void") && r.getStatus().equals("INIT"))).collect(Collectors.toList());
                                List<AtfFileDTO> reversalInitList = atfFileDTOFromNotificationList.stream().filter(r -> (r.getTransactionType().equals("Reversal") && r.getStatus().equals("INIT"))).collect(Collectors.toList());

                                // Set Merchant id, pos device id , batch number, card holder name, masked card number,
                                // transaction mode, invoice number, acquirer bank, card type, card network,
                                // card issuer country code to ACk entry from Host entry since records having notification field id as 0 would not have ack entry with the mentioned data.

                                if (saleHostList.size() > 0 && saleAckList.size() > 0) {
                                    saleAckList.get(0).setMerchantId(saleHostList.get(0).getMerchantId());
                                    saleAckList.get(0).setPosDeviceId(saleHostList.get(0).getPosDeviceId());
                                    saleAckList.get(0).setBatchNumber(saleHostList.get(0).getBatchNumber());
                                    saleAckList.get(0).setCardHolderName(saleHostList.get(0).getCardHolderName());
                                    saleAckList.get(0).setMaskedCardNumber(saleHostList.get(0).getMaskedCardNumber());
                                    saleAckList.get(0).setTransactionMode(saleHostList.get(0).getTransactionMode());
                                    saleAckList.get(0).setInvoiceNumber(saleHostList.get(0).getInvoiceNumber());
                                    saleAckList.get(0).setAcquireBank(saleHostList.get(0).getAcquireBank());
                                    saleAckList.get(0).setCardType(saleHostList.get(0).getCardType());
                                    saleAckList.get(0).setCardNetwork(saleHostList.get(0).getCardNetwork());
                                    saleAckList.get(0).setCardIssuerCountryCode(saleHostList.get(0).getCardIssuerCountryCode());
                                    saleAckList.get(0).setAmount(saleHostList.get(0).getAmount());
                                }

                                // Set Merchant id, pos device id , batch number, card holder name, masked card number,
                                // transaction mode, invoice number, acquirer bank, card type, card network,
                                // card issuer country code to HOST entry from INIT entry since records having notification field id as 0 would not have INIT entry with the mentioned data.

                                if (saleInitList.size() > 0 && saleHostList.size() > 0 && saleAckList.size() == 0) {
                                    saleHostList.get(0).setMerchantId(saleInitList.get(0).getMerchantId());
                                    saleHostList.get(0).setPosDeviceId(saleInitList.get(0).getPosDeviceId());
                                    saleHostList.get(0).setBatchNumber(saleInitList.get(0).getBatchNumber());
                                    saleHostList.get(0).setCardHolderName(saleInitList.get(0).getCardHolderName());
                                    saleHostList.get(0).setMaskedCardNumber(saleInitList.get(0).getMaskedCardNumber());
                                    saleHostList.get(0).setTransactionMode(saleInitList.get(0).getTransactionMode());
                                    saleHostList.get(0).setInvoiceNumber(saleInitList.get(0).getInvoiceNumber());
                                    saleHostList.get(0).setAcquireBank(saleInitList.get(0).getAcquireBank());
                                    saleHostList.get(0).setCardType(saleInitList.get(0).getCardType());
                                    saleHostList.get(0).setCardNetwork(saleInitList.get(0).getCardNetwork());
                                    saleHostList.get(0).setCardIssuerCountryCode(saleInitList.get(0).getCardIssuerCountryCode());
                                    saleHostList.get(0).setAmount(saleInitList.get(0).getAmount());
                                    saleHostList.get(0).setStan(saleInitList.get(0).getStan());
                                }

                                //set transaction date from INIT Entry

                                if (saleInitList.size() > 0 && saleHostList.size() > 0) {
                                    if (saleHostList.get(0).getTransactionDate() == null) {
                                        saleHostList.get(0).setTransactionDate(saleInitList.get(0).getTransactionDate());
                                    }
                                }
                                if (saleInitList.size() > 0 && saleAckList.size() > 0) {
                                    if (saleAckList.get(0).getTransactionDate() == null) {
                                        saleAckList.get(0).setTransactionDate(saleInitList.get(0).getTransactionDate());
                                    }
                                }

                                Date saleTransactionDate = null;
                                Date voidTransactionDate = null;
                                Date reversalTransactionDate = null;


                                String[] saleAckData = null;
                                String[] saleHostData = null;
                                String[] saleInitData = null;
                                String[] voidHostData = null;
                                String[] voidInitData = null;
                                String[] reversalInitData = null;


                                //Sale txns
                                if (saleAckList.size() > 0) {

                                    saleTransactionDate = saleAckList.get(0).getTransactionDate() != null ? saleAckList.get(0).getTransactionDate() : null;

                                    String txnDateStr = saleAckList.get(0).getTransactionDate() != null ? DateUtil.getFormatedDate(saleAckList.get(0).getTransactionDate()) : "";
                                    String resDateStr = saleAckList.get(0).getResponseDate() != null ? DateUtil.getFormatedDate(saleAckList.get(0).getResponseDate()) : "";
                                    saleAckData = new String[]{saleAckList.get(0).getTerminalId(), saleAckList.get(0).getMerchantId(), saleAckList.get(0).getPosDeviceId(),
                                            saleAckList.get(0).getBatchNumber(), saleAckList.get(0).getCardHolderName(), saleAckList.get(0).getMaskedCardNumber(),
                                            saleAckList.get(0).getTransactionMode(), saleAckList.get(0).getInvoiceNumber(), saleAckList.get(0).getAcquireBank(),
                                            saleAckList.get(0).getCardType(), saleAckList.get(0).getCardNetwork(), saleAckList.get(0).getCardIssuerCountryCode(),
                                            saleAckList.get(0).getAmount(), saleAckList.get(0).getResponseCode(), saleAckList.get(0).getRrn(),
                                            saleAckList.get(0).getTransactionAuthCode(), txnDateStr, resDateStr,
                                            saleAckList.get(0).getTransactionId(), saleAckList.get(0).getOrgTransactionId(), saleAckList.get(0).getTransactionType(),
                                            saleAckList.get(0).getStatus(), saleAckList.get(0).getStan(), saleAckList.get(0).getSettlementMode(), saleAckList.get(0).getSettlementStatus()};


                                }

                                if (saleHostList.size() > 0) {

                                    saleTransactionDate = saleHostList.get(0).getTransactionDate() != null ? saleHostList.get(0).getTransactionDate() : null;

                                    String txnDateStr = saleHostList.get(0).getTransactionDate() != null ? DateUtil.getFormatedDate(saleHostList.get(0).getTransactionDate()) : "";
                                    String resDateStr = saleHostList.get(0).getResponseDate() != null ? DateUtil.getFormatedDate(saleHostList.get(0).getResponseDate()) : "";

                                    saleHostData = new String[]{saleHostList.get(0).getTerminalId(), saleHostList.get(0).getMerchantId(), saleHostList.get(0).getPosDeviceId(),
                                            saleHostList.get(0).getBatchNumber(), saleHostList.get(0).getCardHolderName(), saleHostList.get(0).getMaskedCardNumber(),
                                            saleHostList.get(0).getTransactionMode(), saleHostList.get(0).getInvoiceNumber(), saleHostList.get(0).getAcquireBank(),
                                            saleHostList.get(0).getCardType(), saleHostList.get(0).getCardNetwork(), saleHostList.get(0).getCardIssuerCountryCode(),
                                            saleHostList.get(0).getAmount(), saleHostList.get(0).getResponseCode(), saleHostList.get(0).getRrn(),
                                            saleHostList.get(0).getTransactionAuthCode(), txnDateStr, resDateStr,
                                            saleHostList.get(0).getTransactionId(), saleHostList.get(0).getOrgTransactionId(), saleHostList.get(0).getTransactionType(),
                                            saleHostList.get(0).getStatus(), saleHostList.get(0).getStan(), saleHostList.get(0).getSettlementMode(), saleHostList.get(0).getSettlementStatus()};

                                }

                                if (saleInitList.size() > 0) {

                                    saleTransactionDate = saleInitList.get(0).getTransactionDate() != null ? saleInitList.get(0).getTransactionDate() : null;

                                    String txnDateStr = saleInitList.get(0).getTransactionDate() != null ? DateUtil.getFormatedDate(saleInitList.get(0).getTransactionDate()) : "";
                                    String resDateStr = saleInitList.get(0).getResponseDate() != null ? DateUtil.getFormatedDate(saleInitList.get(0).getResponseDate()) : "";

                                    saleInitData = new String[]{saleInitList.get(0).getTerminalId(), saleInitList.get(0).getMerchantId(), saleInitList.get(0).getPosDeviceId(),
                                            saleInitList.get(0).getBatchNumber(), saleInitList.get(0).getCardHolderName(), saleInitList.get(0).getMaskedCardNumber(),
                                            saleInitList.get(0).getTransactionMode(), saleInitList.get(0).getInvoiceNumber(), saleInitList.get(0).getAcquireBank(),
                                            saleInitList.get(0).getCardType(), saleInitList.get(0).getCardNetwork(), saleInitList.get(0).getCardIssuerCountryCode(),
                                            saleInitList.get(0).getAmount(), saleInitList.get(0).getResponseCode(), saleInitList.get(0).getRrn(),
                                            saleInitList.get(0).getTransactionAuthCode(), txnDateStr, resDateStr,
                                            saleInitList.get(0).getTransactionId(), saleInitList.get(0).getOrgTransactionId(), saleInitList.get(0).getTransactionType(),
                                            saleInitList.get(0).getStatus(), saleInitList.get(0).getStan(), saleInitList.get(0).getSettlementMode(), saleInitList.get(0).getSettlementStatus()};

                                }

                                //void txns
                                if (voidHostList.size() > 0) {

                                    voidTransactionDate = voidHostList.get(0).getTransactionDate() != null ? voidHostList.get(0).getTransactionDate() : null;

                                    String txnDateStr = voidHostList.get(0).getTransactionDate() != null ? DateUtil.getFormatedDate(voidHostList.get(0).getTransactionDate()) : "";
                                    String resDateStr = voidHostList.get(0).getResponseDate() != null ? DateUtil.getFormatedDate(voidHostList.get(0).getResponseDate()) : "";

                                    voidHostData = new String[]{voidHostList.get(0).getTerminalId(), voidHostList.get(0).getMerchantId(), voidHostList.get(0).getPosDeviceId(),
                                            voidHostList.get(0).getBatchNumber(), voidHostList.get(0).getCardHolderName(), voidHostList.get(0).getMaskedCardNumber(),
                                            voidHostList.get(0).getTransactionMode(), voidHostList.get(0).getInvoiceNumber(), voidHostList.get(0).getAcquireBank(),
                                            voidHostList.get(0).getCardType(), voidHostList.get(0).getCardNetwork(), voidHostList.get(0).getCardIssuerCountryCode(),
                                            voidHostList.get(0).getAmount(), voidHostList.get(0).getResponseCode(), voidHostList.get(0).getRrn(),
                                            voidHostList.get(0).getTransactionAuthCode(), txnDateStr, resDateStr,
                                            voidHostList.get(0).getTransactionId(), voidHostList.get(0).getOrgTransactionId(), voidHostList.get(0).getTransactionType(),
                                            voidHostList.get(0).getStatus(), voidHostList.get(0).getStan(), voidHostList.get(0).getSettlementMode(), voidHostList.get(0).getSettlementStatus()};

                                }

                                if (voidInitList.size() > 0) {

                                    voidTransactionDate = voidInitList.get(0).getTransactionDate() != null ? voidInitList.get(0).getTransactionDate() : null;

                                    String txnDateStr = voidInitList.get(0).getTransactionDate() != null ? DateUtil.getFormatedDate(voidInitList.get(0).getTransactionDate()) : "";
                                    String resDateStr = voidInitList.get(0).getResponseDate() != null ? DateUtil.getFormatedDate(voidInitList.get(0).getResponseDate()) : "";

                                    voidInitData = new String[]{voidInitList.get(0).getTerminalId(), voidInitList.get(0).getMerchantId(), voidInitList.get(0).getPosDeviceId(),
                                            voidInitList.get(0).getBatchNumber(), voidInitList.get(0).getCardHolderName(), voidInitList.get(0).getMaskedCardNumber(),
                                            voidInitList.get(0).getTransactionMode(), voidInitList.get(0).getInvoiceNumber(), voidInitList.get(0).getAcquireBank(),
                                            voidInitList.get(0).getCardType(), voidInitList.get(0).getCardNetwork(), voidInitList.get(0).getCardIssuerCountryCode(),
                                            voidInitList.get(0).getAmount(), voidInitList.get(0).getResponseCode(), voidInitList.get(0).getRrn(),
                                            voidInitList.get(0).getTransactionAuthCode(), txnDateStr, resDateStr,
                                            voidInitList.get(0).getTransactionId(), voidInitList.get(0).getOrgTransactionId(), voidInitList.get(0).getTransactionType(),
                                            voidInitList.get(0).getStatus(), voidInitList.get(0).getStan(), voidInitList.get(0).getSettlementMode(), voidInitList.get(0).getSettlementStatus()};

                                }

                                //Init Reversals
                                if (reversalInitList.size() > 0) {

                                    reversalTransactionDate = reversalInitList.get(0).getTransactionDate() != null ? reversalInitList.get(0).getTransactionDate() : null;

                                    String txnDateStr = reversalInitList.get(0).getTransactionDate() != null ? DateUtil.getFormatedDate(reversalInitList.get(0).getTransactionDate()) : "";
                                    String resDateStr = reversalInitList.get(0).getResponseDate() != null ? DateUtil.getFormatedDate(reversalInitList.get(0).getResponseDate()) : "";

                                    reversalInitData = new String[]{reversalInitList.get(0).getTerminalId(), reversalInitList.get(0).getMerchantId(), reversalInitList.get(0).getPosDeviceId(),
                                            reversalInitList.get(0).getBatchNumber(), reversalInitList.get(0).getCardHolderName(), reversalInitList.get(0).getMaskedCardNumber(),
                                            reversalInitList.get(0).getTransactionMode(), reversalInitList.get(0).getInvoiceNumber(), reversalInitList.get(0).getAcquireBank(),
                                            reversalInitList.get(0).getCardType(), reversalInitList.get(0).getCardNetwork(), reversalInitList.get(0).getCardIssuerCountryCode(),
                                            reversalInitList.get(0).getAmount(), reversalInitList.get(0).getResponseCode(), reversalInitList.get(0).getRrn(),
                                            reversalInitList.get(0).getTransactionAuthCode(), txnDateStr, resDateStr,
                                            reversalInitList.get(0).getTransactionId(), reversalInitList.get(0).getOrgTransactionId(), reversalInitList.get(0).getTransactionType(),
                                            reversalInitList.get(0).getStatus(), reversalInitList.get(0).getStan(), reversalInitList.get(0).getSettlementMode(), reversalInitList.get(0).getSettlementStatus()};

                                }


                                Date saleTxnCutOffDate = saleTransactionDate != null ? DateUtil.stringToDate(DateUtil.parseDateFromDateTime(saleTransactionDate).concat(" 23:00:00")) : null;

                                String updatedSettlementStatus = "";
                                if (saleTransactionDate != null && saleTxnCutOffDate != null && saleTransactionDate.before(saleTxnCutOffDate) && (voidTransactionDate != null && voidTransactionDate.before(saleTxnCutOffDate) || (reversalTransactionDate != null && reversalTransactionDate.before(saleTxnCutOffDate)))) {
                                    updatedSettlementStatus = "NotSettled";
                                } else if (saleInitData != null && saleHostData == null && saleAckData == null) {               // Means only sale/UPI  INIT available
                                    updatedSettlementStatus = "NotSettled";
                                } else if (saleHostData != null && saleHostData[13] != null && !saleHostData[13].equals("00")) {  // Means txn having host with response code as not 00
                                    updatedSettlementStatus = "NotSettled";
                                } else {
                                    updatedSettlementStatus = "Settled";
                                }


                                //Below snippet for setting the Settlement Status and write into ATF File

                                if (saleAckData != null) {
                                    saleAckData[24] = updatedSettlementStatus;
                                    csvWriter.writeNext(saleAckData);
                                    logger.info("Notification : Generated for txn -- {}", saleAckData[18]);
                                } else if (saleHostData != null) {
                                    saleHostData[24] = updatedSettlementStatus;
                                    csvWriter.writeNext(saleHostData);
                                    logger.info("Notification : Generated for txn -- {}", saleHostData[18]);
                                } else if (saleInitData != null) {
                                    saleInitData[24] = updatedSettlementStatus;
                                    csvWriter.writeNext(saleInitData);
                                    logger.info("Notification : Generated for txn -- {}", saleInitData[18]);
                                }

                                if (voidHostData != null) {
                                    voidHostData[24] = updatedSettlementStatus;
                                    csvWriter.writeNext(voidHostData);
                                    logger.info("Notification : Generated for txn -- {}", voidHostData[18]);
                                } else if (voidInitData != null) {
                                    voidInitData[24] = updatedSettlementStatus;
                                    csvWriter.writeNext(voidInitData);
                                    logger.info("Notification : Generated for txn -- {}", voidInitData[18]);
                                }

                                if (reversalInitData != null) {
                                    reversalInitData[24] = updatedSettlementStatus;
                                    csvWriter.writeNext(reversalInitData);
                                    logger.info("Notification : Generated for txn -- {}", reversalInitData[18]);
                                }

                            } else {
                                logger.info("Else Part Executing -----");

                                //if not present in notification data and notification fields imported table ,
                                //fetch from switch request and response imported table.

                                String switchReqResQuery = " select p.TerminalId,p.MerchantId AS merchant_Id, null AS pos_Device_id, p.BatchNumber AS batch_number, null AS card_holder_name,\n" +
                                        "CASE WHEN p.MTI = '0100' THEN ''\n" +
                                        "ELSE CONCAT(p.BIN,'******',p.CardInfo) END AS masked_card_number,\n" +
                                        "CASE WHEN p.PANEntryMode = '07' THEN 'NFC'\n" +
                                        "WHEN p.PANEntryMode = '05' THEN 'EMV' \n" +
                                        "WHEN (p.PANEntryMode = '90' OR p.PANEntryMode = '80') THEN 'Magstripe' WHEN p.MTI = '0100' THEN 'UPI' END AS transaction_mode, \n" +
                                        "CASE WHEN p.DeviceInvoiceNumber != ''  THEN p.DeviceInvoiceNumber \n" +
                                        "else p.InvoiceNumber end As invoice_number ,'AXIS BANK' AS acquirer_bank, null AS card_type, \n" +
                                        "null AS card_network, '0356' AS card_issuer_country_code, \n" +
                                        "CASE WHEN p.MTI = '0200' AND p.TXNTYPE = '02' THEN p.TxnAdditionalAmount \n" +
                                        "ELSE p.TxnAmount END AS amount,\n" +
                                        "p.TxnResponseCode AS response_code,\n" +
                                        "p.RRNumber AS rrn, p.AuthResponseCode AS transaction_auth_code , p.RequestRouteTime AS transaction_date_time, p.ResponseReceivedTime AS response_date_time,\n" +
                                        "CASE WHEN p.MTI = '0100' THEN p.InvoiceNumber\n" +
                                        "WHEN p.MTI = '0200' THEN REPLACE(REPLACE(REPLACE(replace( CONCAT (p.TerminalId ,CONVERT (varchar,p.ResponseReceivedTime,23)),'-',''),':',''),'.',''),' ','') END  AS TransactionId, null as OrgTransactionId,\n" +
                                        "CASE WHEN p.MTI = '0100' AND (p.TXNTYPE = '36' OR p.TXNTYPE = '37' OR p.TXNTYPE = '39') THEN 'UPI'\n" +
                                        "WHEN p.MTI = '0200' AND p.TXNTYPE = '00' THEN 'Sale'\n" +
                                        "WHEN p.MTI = '0200' AND p.TXNTYPE = '02' THEN 'Void'\n" +
                                        "WHEN ((p.MTI = '0400' AND p.TXNTYPE = '00') OR (p.MTI = '0100' AND p.TXNTYPE = '38')) THEN 'Reversal' END AS TransactionType,\n" +
                                        "CASE WHEN p.MTI = '0200' THEN 'HOST'\n" +
                                        "WHEN p.MTI = '0400' THEN 'INIT'\n" +
                                        "WHEN p.MTI = '0100' AND (p.TXNTYPE = '36' OR p.TXNTYPE = '38') THEN 'INIT'\n" +
                                        "WHEN p.MTI = '0100' AND (p.TXNTYPE = '37' OR p.TXNTYPE = '39') THEN 'HOST'\n" +
                                        "WHEN p.MTI = '0300' AND p.TXNTYPE = '51' THEN 'ACK'\n" +
                                        "WHEN p.MTI = '0200' AND p.TXNTYPE = '00' THEN 'INIT' END AS status,\n" +
                                        "p.Stan, 'Auto' as settlement_mode,null AS settlement_status\n" +
                                        "FROM phonepe_transaction p where p.RRNumber ='" + rrnOrtxn + "' or REPLACE(REPLACE(REPLACE(replace( CONCAT (p.TerminalId ,CONVERT (varchar,p.ResponseReceivedTime,23)),'-',''),':',''),'.',''),' ','') = '" + rrnOrtxn + "' " +
                                        "or p.InvoiceNumber = '" + rrnOrtxn + "';";

                                logger.info("Switch Query ---{}",switchReqResQuery);
                                ResultSet switchReqResRS = stmt.executeQuery(switchReqResQuery);

                                List<AtfFileDTO> atfFileDTOFromSwitchList = new ArrayList<>();


                                //Load notification result set

                                while (switchReqResRS.next()) {

                                    AtfFileDTO atfFileFromSwitchDTO = new AtfFileDTO();
                                    atfFileFromSwitchDTO.setTerminalId(switchReqResRS.getString("TerminalId"));
                                    atfFileFromSwitchDTO.setMerchantId(switchReqResRS.getString("merchant_Id"));
                                    atfFileFromSwitchDTO.setPosDeviceId(switchReqResRS.getString("pos_Device_Id"));
                                    atfFileFromSwitchDTO.setBatchNumber(switchReqResRS.getString("batch_number"));
                                    atfFileFromSwitchDTO.setCardHolderName(switchReqResRS.getString("card_holder_name"));
                                    atfFileFromSwitchDTO.setMaskedCardNumber(switchReqResRS.getString("masked_card_number"));
                                    atfFileFromSwitchDTO.setTransactionMode(switchReqResRS.getString("transaction_mode"));
                                    atfFileFromSwitchDTO.setInvoiceNumber(switchReqResRS.getString("invoice_number"));
                                    atfFileFromSwitchDTO.setAcquireBank(switchReqResRS.getString("acquirer_bank"));
                                    atfFileFromSwitchDTO.setCardType(switchReqResRS.getString("card_type"));
                                    atfFileFromSwitchDTO.setCardNetwork(switchReqResRS.getString("card_network"));
                                    atfFileFromSwitchDTO.setCardIssuerCountryCode(switchReqResRS.getString("card_issuer_country_code"));
                                    atfFileFromSwitchDTO.setAmount(switchReqResRS.getString("amount"));
                                    atfFileFromSwitchDTO.setResponseCode(switchReqResRS.getString("response_code"));
                                    atfFileFromSwitchDTO.setRrn(switchReqResRS.getString("rrn"));
                                    atfFileFromSwitchDTO.setTransactionAuthCode(switchReqResRS.getString("transaction_auth_code"));
                                    String txnDateTime = switchReqResRS.getString("transaction_date_time");
                                    if (!txnDateTime.isEmpty()) {
                                        Date txnDate = DateUtil.stringToDate(txnDateTime);
                                        atfFileFromSwitchDTO.setTransactionDate(txnDate);
                                    } else {
                                        atfFileFromSwitchDTO.setTransactionDate(null);
                                    }

                                    String responseDateTime = switchReqResRS.getString("response_date_time");
                                    if (!responseDateTime.isEmpty()) {
                                        Date resDate = DateUtil.stringToDate(responseDateTime);
                                        atfFileFromSwitchDTO.setResponseDate(resDate);
                                    } else {
                                        atfFileFromSwitchDTO.setResponseDate(null);
                                    }

                                    atfFileFromSwitchDTO.setTransactionId(switchReqResRS.getString("TransactionId"));
                                    atfFileFromSwitchDTO.setOrgTransactionId(switchReqResRS.getString("OrgTransactionId"));
                                    atfFileFromSwitchDTO.setTransactionType(switchReqResRS.getString("TransactionType"));
                                    atfFileFromSwitchDTO.setStatus(switchReqResRS.getString("status"));
                                    atfFileFromSwitchDTO.setStan(switchReqResRS.getString("Stan"));
                                    atfFileFromSwitchDTO.setSettlementMode(switchReqResRS.getString("settlement_mode"));
                                    atfFileFromSwitchDTO.setSettlementStatus(switchReqResRS.getString("settlement_status"));

                                    atfFileDTOFromSwitchList.add(atfFileFromSwitchDTO);

                                }
                                logger.info("Switch Txn size ---{}",atfFileDTOFromSwitchList.size());

                                if (atfFileDTOFromSwitchList.size() > 0) {

                                    List<AtfFileDTO> saleAckList = atfFileDTOFromSwitchList.stream().filter(r -> ("ACK".equals(r.getStatus()))).collect(Collectors.toList());
                                    List<AtfFileDTO> saleHostList = atfFileDTOFromSwitchList.stream().filter(r -> r.getTransactionType() != null && ((r.getTransactionType().equalsIgnoreCase("Sale") || r.getTransactionType().equalsIgnoreCase("UPI")) && "HOST".equals(r.getStatus()))).collect(Collectors.toList());
                                    List<AtfFileDTO> saleInitList = atfFileDTOFromSwitchList.stream().filter(r -> r.getTransactionType() != null && ((r.getTransactionType().equalsIgnoreCase("Sale") || r.getTransactionType().equalsIgnoreCase("UPI")) && "INIT".equals(r.getStatus()))).collect(Collectors.toList());
                                    List<AtfFileDTO> voidHostList = atfFileDTOFromSwitchList.stream().filter(r -> r.getTransactionType() != null && (r.getTransactionType().equalsIgnoreCase("Void") && "HOST".equals(r.getStatus()))).collect(Collectors.toList());
                                    List<AtfFileDTO> voidInitList = atfFileDTOFromSwitchList.stream().filter(r -> r.getTransactionType() != null && (r.getTransactionType().equalsIgnoreCase("Void") && "INIT".equals(r.getStatus()))).collect(Collectors.toList());
                                    List<AtfFileDTO> reversalInitList = atfFileDTOFromSwitchList.stream().filter(r -> r.getTransactionType() != null && (r.getTransactionType().equalsIgnoreCase("Reversal") && "INIT".equals(r.getStatus()))).collect(Collectors.toList());


                                    //for ACK record, we sets the Sales or UPI datas for batch number, masked card number,
                                    // Transaction Mode, Invoice Number, Amount, Transaction Auth Code, TransactionId  and  Transaction Type

                                    if (saleAckList.size() > 0) {
                                        if (saleHostList.size() > 0) {
                                            saleAckList.get(0).setBatchNumber(saleHostList.get(0).getBatchNumber());
                                            saleAckList.get(0).setMaskedCardNumber(saleHostList.get(0).getMaskedCardNumber());
                                            saleAckList.get(0).setTransactionMode(saleHostList.get(0).getTransactionMode());
                                            saleAckList.get(0).setInvoiceNumber(saleHostList.get(0).getInvoiceNumber());
                                            saleAckList.get(0).setAmount(saleHostList.get(0).getAmount());
                                            saleAckList.get(0).setTransactionAuthCode(saleHostList.get(0).getTransactionAuthCode());
                                            saleAckList.get(0).setTransactionId(saleHostList.get(0).getTransactionId());
                                            saleAckList.get(0).setTransactionType(saleHostList.get(0).getTransactionType());
                                        } else if (saleInitList.size() > 0) {
                                            saleAckList.get(0).setBatchNumber(saleInitList.get(0).getBatchNumber());
                                            saleAckList.get(0).setMaskedCardNumber(saleInitList.get(0).getMaskedCardNumber());
                                            saleAckList.get(0).setTransactionMode(saleInitList.get(0).getTransactionMode());
                                            saleAckList.get(0).setInvoiceNumber(saleInitList.get(0).getInvoiceNumber());
                                            saleAckList.get(0).setAmount(saleInitList.get(0).getAmount());
                                            saleAckList.get(0).setTransactionAuthCode(saleInitList.get(0).getTransactionAuthCode());
                                            saleAckList.get(0).setTransactionType(saleInitList.get(0).getTransactionType());
                                        }
                                    }

                                    //Set the Original Transaction Id for Reversal Entry
                                    //For Sale : gets the previous transaction id and set as Original transaction id and transaction id
                                    //For UPI : gets the previous transaction id and set as Original transaction id

                                    if (reversalInitList.size() > 0) {

                                        if (saleAckList.size() > 0) {
                                            if (saleAckList.get(0).getTransactionType() != null && saleAckList.get(0).getTransactionType().equalsIgnoreCase("Sale")) {
                                                reversalInitList.get(0).setOrgTransactionId(saleAckList.get(0).getTransactionId());
                                                reversalInitList.get(0).setTransactionId(saleAckList.get(0).getTransactionId());
                                            } else if (saleAckList.get(0).getTransactionType() != null && saleAckList.get(0).getTransactionType().equalsIgnoreCase("UPI")) {
                                                reversalInitList.get(0).setOrgTransactionId(saleAckList.get(0).getTransactionId());
                                            }

                                        } else if (saleHostList.size() > 0) {
                                            if (saleHostList.get(0).getTransactionType() != null && saleHostList.get(0).getTransactionType().equalsIgnoreCase("Sale")) {
                                                reversalInitList.get(0).setOrgTransactionId(saleHostList.get(0).getTransactionId());
                                                reversalInitList.get(0).setTransactionId(saleHostList.get(0).getTransactionId());
                                            } else if (saleHostList.get(0).getTransactionType() != null && saleHostList.get(0).getTransactionType().equalsIgnoreCase("UPI")) {
                                                reversalInitList.get(0).setOrgTransactionId(saleHostList.get(0).getTransactionId());
                                            }

                                        } else if (saleInitList.size() > 0) {
                                            if (saleInitList.get(0).getTransactionType() != null && saleInitList.get(0).getTransactionType().equalsIgnoreCase("Sale")) {
                                                reversalInitList.get(0).setOrgTransactionId(saleInitList.get(0).getTransactionId());
                                                reversalInitList.get(0).setTransactionId(saleInitList.get(0).getTransactionId());
                                            } else if (saleInitList.get(0).getTransactionType() != null && saleInitList.get(0).getTransactionType().equalsIgnoreCase("UPI")) {
                                                reversalInitList.get(0).setOrgTransactionId(saleInitList.get(0).getTransactionId());
                                            }
                                        }
                                    }

                                    //Set the Original Transaction Id for Void Entry
                                    //For Sale :  for Reversal we will get the previous transaction id and set as Original transaction id and transaction id
                                    if (voidHostList.size() > 0) {

                                        if (saleAckList.size() > 0) {
                                            voidHostList.get(0).setOrgTransactionId(saleAckList.get(0).getTransactionId());
                                        } else if (saleHostList.size() > 0) {
                                            voidHostList.get(0).setOrgTransactionId(saleAckList.get(0).getTransactionId());
                                        } else if (saleInitList.size() > 0) {
                                            voidHostList.get(0).setOrgTransactionId(saleAckList.get(0).getTransactionId());
                                        }

                                    } else if (voidInitList.size() > 0) {
                                        if (saleAckList.size() > 0) {
                                            voidInitList.get(0).setOrgTransactionId(voidInitList.get(0).getTransactionId());
                                        } else if (saleHostList.size() > 0) {
                                            voidInitList.get(0).setOrgTransactionId(voidInitList.get(0).getTransactionId());
                                        } else if (saleInitList.size() > 0) {
                                            voidInitList.get(0).setOrgTransactionId(voidInitList.get(0).getTransactionId());
                                        }
                                    }


                                    Date saleTransactionDate = null;
                                    Date voidTransactionDate = null;
                                    Date reversalTransactionDate = null;


                                    String[] saleAckData = null;
                                    String[] saleHostData = null;
                                    String[] saleInitData = null;
                                    String[] voidHostData = null;
                                    String[] voidInitData = null;
                                    String[] reversalInitData = null;


                                    //Sale txns
                                    if (saleAckList.size() > 0) {

                                        saleTransactionDate = saleAckList.get(0).getTransactionDate() != null ? saleAckList.get(0).getTransactionDate() : null;

                                        String txnDateStr = saleAckList.get(0).getTransactionDate() != null ? DateUtil.getFormatedDate(saleAckList.get(0).getTransactionDate()) : "";
                                        String resDateStr = saleAckList.get(0).getResponseDate() != null ? DateUtil.getFormatedDate(saleAckList.get(0).getResponseDate()) : "";

                                        saleAckData = new String[]{saleAckList.get(0).getTerminalId(), saleAckList.get(0).getMerchantId(), saleAckList.get(0).getPosDeviceId(),
                                                saleAckList.get(0).getBatchNumber(), saleAckList.get(0).getCardHolderName(), saleAckList.get(0).getMaskedCardNumber(),
                                                saleAckList.get(0).getTransactionMode(), saleAckList.get(0).getInvoiceNumber(), saleAckList.get(0).getAcquireBank(),
                                                saleAckList.get(0).getCardType(), saleAckList.get(0).getCardNetwork(), saleAckList.get(0).getCardIssuerCountryCode(),
                                                saleAckList.get(0).getAmount(), saleAckList.get(0).getResponseCode(), saleAckList.get(0).getRrn(),
                                                saleAckList.get(0).getTransactionAuthCode(), txnDateStr, resDateStr,
                                                saleAckList.get(0).getTransactionId(), saleAckList.get(0).getOrgTransactionId(), saleAckList.get(0).getTransactionType(),
                                                saleAckList.get(0).getStatus(), saleAckList.get(0).getStan(), saleAckList.get(0).getSettlementMode(), saleAckList.get(0).getSettlementStatus()};

                                    }

                                    if (saleHostList.size() > 0) {

                                        saleTransactionDate = saleHostList.get(0).getTransactionDate() != null ? saleHostList.get(0).getTransactionDate() : null;

                                        String txnDateStr = saleHostList.get(0).getTransactionDate() != null ? DateUtil.getFormatedDate(saleHostList.get(0).getTransactionDate()) : "";
                                        String resDateStr = saleHostList.get(0).getResponseDate() != null ? DateUtil.getFormatedDate(saleHostList.get(0).getResponseDate()) : "";

                                        saleHostData = new String[]{saleHostList.get(0).getTerminalId(), saleHostList.get(0).getMerchantId(), saleHostList.get(0).getPosDeviceId(),
                                                saleHostList.get(0).getBatchNumber(), saleHostList.get(0).getCardHolderName(), saleHostList.get(0).getMaskedCardNumber(),
                                                saleHostList.get(0).getTransactionMode(), saleHostList.get(0).getInvoiceNumber(), saleHostList.get(0).getAcquireBank(),
                                                saleHostList.get(0).getCardType(), saleHostList.get(0).getCardNetwork(), saleHostList.get(0).getCardIssuerCountryCode(),
                                                saleHostList.get(0).getAmount(), saleHostList.get(0).getResponseCode(), saleHostList.get(0).getRrn(),
                                                saleHostList.get(0).getTransactionAuthCode(), txnDateStr, resDateStr,
                                                saleHostList.get(0).getTransactionId(), saleHostList.get(0).getOrgTransactionId(), saleHostList.get(0).getTransactionType(),
                                                saleHostList.get(0).getStatus(), saleHostList.get(0).getStan(), saleHostList.get(0).getSettlementMode(), saleHostList.get(0).getSettlementStatus()};
                                    }

                                    if (saleInitList.size() > 0) {

                                        saleTransactionDate = saleInitList.get(0).getTransactionDate() != null ? saleInitList.get(0).getTransactionDate() : null;

                                        String txnDateStr = saleInitList.get(0).getTransactionDate() != null ? DateUtil.getFormatedDate(saleInitList.get(0).getTransactionDate()) : "";
                                        String resDateStr = saleInitList.get(0).getResponseDate() != null ? DateUtil.getFormatedDate(saleInitList.get(0).getResponseDate()) : "";

                                        saleInitData = new String[]{saleInitList.get(0).getTerminalId(), saleInitList.get(0).getMerchantId(), saleInitList.get(0).getPosDeviceId(),
                                                saleInitList.get(0).getBatchNumber(), saleInitList.get(0).getCardHolderName(), saleInitList.get(0).getMaskedCardNumber(),
                                                saleInitList.get(0).getTransactionMode(), saleInitList.get(0).getInvoiceNumber(), saleInitList.get(0).getAcquireBank(),
                                                saleInitList.get(0).getCardType(), saleInitList.get(0).getCardNetwork(), saleInitList.get(0).getCardIssuerCountryCode(),
                                                saleInitList.get(0).getAmount(), saleInitList.get(0).getResponseCode(), saleInitList.get(0).getRrn(),
                                                saleInitList.get(0).getTransactionAuthCode(), txnDateStr, resDateStr,
                                                saleInitList.get(0).getTransactionId(), saleInitList.get(0).getOrgTransactionId(), saleInitList.get(0).getTransactionType(),
                                                saleInitList.get(0).getStatus(), saleInitList.get(0).getStan(), saleInitList.get(0).getSettlementMode(), saleInitList.get(0).getSettlementStatus()};

                                    }

                                    //void txns
                                    if (voidHostList.size() > 0) {

                                        voidTransactionDate = voidHostList.get(0).getTransactionDate() != null ? voidHostList.get(0).getTransactionDate() : null;

                                        String txnDateStr = voidHostList.get(0).getTransactionDate() != null ? DateUtil.getFormatedDate(voidHostList.get(0).getTransactionDate()) : "";
                                        String resDateStr = voidHostList.get(0).getResponseDate() != null ? DateUtil.getFormatedDate(voidHostList.get(0).getResponseDate()) : "";

                                        voidHostData = new String[]{voidHostList.get(0).getTerminalId(), voidHostList.get(0).getMerchantId(), voidHostList.get(0).getPosDeviceId(),
                                                voidHostList.get(0).getBatchNumber(), voidHostList.get(0).getCardHolderName(), voidHostList.get(0).getMaskedCardNumber(),
                                                voidHostList.get(0).getTransactionMode(), voidHostList.get(0).getInvoiceNumber(), voidHostList.get(0).getAcquireBank(),
                                                voidHostList.get(0).getCardType(), voidHostList.get(0).getCardNetwork(), voidHostList.get(0).getCardIssuerCountryCode(),
                                                voidHostList.get(0).getAmount(), voidHostList.get(0).getResponseCode(), voidHostList.get(0).getRrn(),
                                                voidHostList.get(0).getTransactionAuthCode(), txnDateStr, resDateStr,
                                                voidHostList.get(0).getTransactionId(), voidHostList.get(0).getOrgTransactionId(), voidHostList.get(0).getTransactionType(),
                                                voidHostList.get(0).getStatus(), voidHostList.get(0).getStan(), voidHostList.get(0).getSettlementMode(), voidHostList.get(0).getSettlementStatus()};

                                    }

                                    if (voidInitList.size() > 0) {

                                        voidTransactionDate = voidInitList.get(0).getTransactionDate() != null ? voidInitList.get(0).getTransactionDate() : null;

                                        String txnDateStr = voidInitList.get(0).getTransactionDate() != null ? DateUtil.getFormatedDate(voidInitList.get(0).getTransactionDate()) : "";
                                        String resDateStr = voidInitList.get(0).getResponseDate() != null ? DateUtil.getFormatedDate(voidInitList.get(0).getResponseDate()) : "";

                                        voidInitData = new String[]{voidInitList.get(0).getTerminalId(), voidInitList.get(0).getMerchantId(), voidInitList.get(0).getPosDeviceId(),
                                                voidInitList.get(0).getBatchNumber(), voidInitList.get(0).getCardHolderName(), voidInitList.get(0).getMaskedCardNumber(),
                                                voidInitList.get(0).getTransactionMode(), voidInitList.get(0).getInvoiceNumber(), voidInitList.get(0).getAcquireBank(),
                                                voidInitList.get(0).getCardType(), voidInitList.get(0).getCardNetwork(), voidInitList.get(0).getCardIssuerCountryCode(),
                                                voidInitList.get(0).getAmount(), voidInitList.get(0).getResponseCode(), voidInitList.get(0).getRrn(),
                                                voidInitList.get(0).getTransactionAuthCode(), txnDateStr, resDateStr,
                                                voidInitList.get(0).getTransactionId(), voidInitList.get(0).getOrgTransactionId(), voidInitList.get(0).getTransactionType(),
                                                voidInitList.get(0).getStatus(), voidInitList.get(0).getStan(), voidInitList.get(0).getSettlementMode(), voidInitList.get(0).getSettlementStatus()};

                                    }

                                    //Init Reversals
                                    if (reversalInitList.size() > 0) {

                                        reversalTransactionDate = reversalInitList.get(0).getTransactionDate() != null ? reversalInitList.get(0).getTransactionDate() : null;

                                        String txnDateStr = reversalInitList.get(0).getTransactionDate() != null ? DateUtil.getFormatedDate(reversalInitList.get(0).getTransactionDate()) : "";
                                        String resDateStr = reversalInitList.get(0).getResponseDate() != null ? DateUtil.getFormatedDate(reversalInitList.get(0).getResponseDate()) : "";

                                        reversalInitData = new String[]{reversalInitList.get(0).getTerminalId(), reversalInitList.get(0).getMerchantId(), reversalInitList.get(0).getPosDeviceId(),
                                                reversalInitList.get(0).getBatchNumber(), reversalInitList.get(0).getCardHolderName(), reversalInitList.get(0).getMaskedCardNumber(),
                                                reversalInitList.get(0).getTransactionMode(), reversalInitList.get(0).getInvoiceNumber(), reversalInitList.get(0).getAcquireBank(),
                                                reversalInitList.get(0).getCardType(), reversalInitList.get(0).getCardNetwork(), reversalInitList.get(0).getCardIssuerCountryCode(),
                                                reversalInitList.get(0).getAmount(), reversalInitList.get(0).getResponseCode(), reversalInitList.get(0).getRrn(),
                                                reversalInitList.get(0).getTransactionAuthCode(), txnDateStr, resDateStr,
                                                reversalInitList.get(0).getTransactionId(), reversalInitList.get(0).getOrgTransactionId(), reversalInitList.get(0).getTransactionType(),
                                                reversalInitList.get(0).getStatus(), reversalInitList.get(0).getStan(), reversalInitList.get(0).getSettlementMode(), reversalInitList.get(0).getSettlementStatus()};

                                    }

                                    Date saleTxnCutOffDate = saleTransactionDate != null ? DateUtil.stringToDate(DateUtil.parseDateFromDateTime(saleTransactionDate).concat(" 23:00:00")) : null;

                                    String updatedSettlementStatus = "";
                                    if (saleTransactionDate != null && saleTxnCutOffDate != null && saleTransactionDate.before(saleTxnCutOffDate) && (voidTransactionDate != null && voidTransactionDate.before(saleTxnCutOffDate) || (reversalTransactionDate != null && reversalTransactionDate.before(saleTxnCutOffDate)))) {
                                        updatedSettlementStatus = "NotSettled";
                                    } else if (saleInitData != null && saleHostData == null && saleAckData == null) {                 // Means only sale/UPI  INIT available
                                        updatedSettlementStatus = "NotSettled";
                                    } else if (saleHostData != null && saleHostData[13] != null && !saleHostData[13].equals("00")) {  // Means txn having host with not response code as not 00
                                        updatedSettlementStatus = "NotSettled";
                                    } else {
                                        updatedSettlementStatus = "Settled";
                                    }


                                    //Below snippet for setting the Settlement Status and write into ATF File

                                    if (saleAckData != null) {
                                        saleAckData[24] = updatedSettlementStatus;
                                        csvWriter.writeNext(saleAckData);
                                        logger.info("Switch : Generated for txn -- {}", saleAckData[18]);
                                    } else if (saleHostData != null) {
                                        saleHostData[24] = updatedSettlementStatus;
                                        csvWriter.writeNext(saleHostData);
                                        logger.info("Switch : Generated for txn -- {}", saleHostData[18]);
                                    } else if (saleInitData != null) {
                                        saleInitData[24] = updatedSettlementStatus;
                                        csvWriter.writeNext(saleInitData);
                                        logger.info("Switch : Generated for txn -- {}", saleInitData[18]);
                                    }

                                    if (voidHostData != null) {
                                        voidHostData[24] = updatedSettlementStatus;
                                        csvWriter.writeNext(voidHostData);
                                        logger.info("Switch : Generated for txn -- {}", voidHostData[18]);
                                    } else if (voidInitData != null) {
                                        voidInitData[24] = updatedSettlementStatus;
                                        csvWriter.writeNext(voidInitData);
                                        logger.info("Switch : Generated for txn -- {}", voidInitData[18]);
                                    }

                                    if (reversalInitData != null) {
                                        reversalInitData[24] = updatedSettlementStatus;
                                        csvWriter.writeNext(reversalInitData);
                                        logger.info("Switch : Generated for txn -- {}", reversalInitData[18]);
                                    }
                                }
                            }
                        }
                    }
                }

                excelFile.close();

                logger.info("CSV File Generated Successfully----!!!");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            } finally {
                stmt.close();
                con.close();
                csvWriter.close();
            }
        }
        return true;
    }

    @Override
    public boolean generateAtfCSVResult(List<AtfFileDTO> atfFIleOut, String updatedAtfFile) {
        logger.info("Creating CSV File ");
        try {
            //creating csv file
            CSVWriter csvWriter = new CSVWriter(new FileWriter(updatedAtfFile, true), ',', CSVWriter.NO_QUOTE_CHARACTER);

            List<String[]> finalOut = new ArrayList<>();
            String[] out = null;

            String[] header = {"terminalId", "merchantId", "posDeviceId", "batchNumber", "cardHolderName", "maskedCardNumber", "transactionMode", "invoiceNumber", "acquirerBank", "cardType", "cardNetwork", "cardIssuerCountryCode", "amount", "responseCode", "rrn", "transactionAuthCode",
                    "transactionDate", "responseDate", "transactionId", "orgTransactionID", "transactionType", "status", "Stan", "settlementMode", "settlementStatus"};
            csvWriter.writeNext(header);

            for (AtfFileDTO obj : atfFIleOut) {       //writing data to sheet
                String terminalId = String.valueOf(obj.getTerminalId());
                String merchantId = String.valueOf(obj.getMerchantId());
                String posDeviceId = String.valueOf(obj.getPosDeviceId());
                String batchNumber = String.valueOf(obj.getBatchNumber());
                String cardHolderName = String.valueOf(obj.getCardHolderName());
                String maskedCardNumber = String.valueOf(obj.getMaskedCardNumber());
                String transactionMode = String.valueOf(obj.getTransactionMode());
                String invoiceNumber = String.valueOf(obj.getInvoiceNumber());
                String acquirerBank = String.valueOf(obj.getAcquireBank());
                String cardType = String.valueOf(obj.getCardType());
                String cardNetwork = String.valueOf(obj.getCardNetwork());
                String cardIssuerCountryCode = String.valueOf(obj.getCardIssuerCountryCode());
                String amount = String.valueOf(obj.getAmount());
                String responseCode = String.valueOf(obj.getResponseCode());
                String rrn = String.valueOf(obj.getRrn());
                String transactionAuthCode = String.valueOf(obj.getTransactionAuthCode());
                String transactionDate = DateUtil.getFormatedDate(obj.getTransactionDate());
                String responseDate = DateUtil.getFormatedDate(obj.getResponseDate());
                String transactionId = String.valueOf(obj.getTransactionId());
                String orgTransactionID = String.valueOf(obj.getOrgTransactionId());
                String transactionType = String.valueOf(obj.getTransactionType());
                String status = String.valueOf(obj.getStatus());
                String Stan = String.valueOf(obj.getStan());
                String settlementMode = String.valueOf(obj.getSettlementMode());
                String settlementStatus = String.valueOf(obj.getSettlementStatus());


                String[] data = {terminalId, merchantId, posDeviceId, batchNumber, cardHolderName, maskedCardNumber, transactionMode, invoiceNumber, acquirerBank, cardType, cardNetwork, cardIssuerCountryCode, amount, responseCode, rrn, transactionAuthCode, transactionDate, responseDate, transactionId, orgTransactionID,
                        transactionType, status, Stan, settlementMode, settlementStatus};

                csvWriter.writeNext(data);
            }

            csvWriter.close();
            logger.info("CSV File Generated Successfully----!!!");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean downloadLargeFile(String currentDate) {
        boolean connectionStatus = false;
        boolean status = false;
        String previousDate = DateUtil.previousDateATF();
        String sourcePath = "/home/uat1/uploads/" + currentDate + "";

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
                logger.info("connection status --- {}", connectionStatus);
            }
            if (connectionStatus) {
                if (exists(mChannelSftp, sourcePath)) {
                    mChannelSftp.cd(sourcePath);

                    logger.info("downloadStatus Success Current Date ! ");
                } else {
                    return status;
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

//    @Override
//    public void processMulipleData() {
//        List<String> multipleData = atfFileRepository.findByMultipleData();
//        multipleData.forEach(l->{
//            List<AtfFileReport> data = atfFileRepository.findByData();
//
//        });
//
//    }

    public static File largestFile(File f) {
        if (f.isFile()) {
            return f;
        } else {
            File largestFile = null;

            for (File file : f.listFiles()) {
                // only recurse largestFile once
                File possiblyLargeFile = largestFile(file);
                if (possiblyLargeFile != null) {
                    if (largestFile == null || possiblyLargeFile.length() > largestFile.length()) {
                        largestFile = possiblyLargeFile;
                    }
                }
            }
            return largestFile;
        }
    }

}
