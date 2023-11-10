package com.bijlipay.ATFFileGeneration.ServiceImpl;

import com.bijlipay.ATFFileGeneration.Model.AtfFileReport;
import com.bijlipay.ATFFileGeneration.Model.TxnListMainTotal;
import com.bijlipay.ATFFileGeneration.Model.SettlementFile;
import com.bijlipay.ATFFileGeneration.Model.TransactionList;
import com.bijlipay.ATFFileGeneration.Repository.AtfFileRepository;
import com.bijlipay.ATFFileGeneration.Repository.SettlementFileRepository;
import com.bijlipay.ATFFileGeneration.Repository.TransactionListRepository;
import com.bijlipay.ATFFileGeneration.Repository.TxnListMainTotalRepository;
import com.bijlipay.ATFFileGeneration.Service.AtfFileService;
import com.bijlipay.ATFFileGeneration.Util.Constants;
import com.bijlipay.ATFFileGeneration.Util.DateUtil;
import com.bijlipay.ATFFileGeneration.Util.ReportUtil;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.bijlipay.ATFFileGeneration.Util.Constants.*;

@Service
public class AtfFileServiceImpl implements AtfFileService {

    public static final String DOUBLE_VALUE = "DOUBLE ENTRY";
    public static final String SINGLE_VALUE = "SINGLE ENTRY";

    private JSch mJschSession = null;
    private Session mSSHSession = null;
    private ChannelSftp mChannelSftp = null;


    @Autowired
    private AtfFileRepository atfFileRepository;

    @Autowired
    private SettlementFileRepository settlementFileRepository;

    @Autowired
    private TransactionListRepository transactionListRepository;

    @Autowired
    private TxnListMainTotalRepository txnListMainTotalRepository;

    @Value("${atf.file.updated.path}")
    private String updatedAtfFilePath;

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
                BufferedReader fileReader = null;
                try {
                    String line = "";
                    fileReader = new BufferedReader(new FileReader(atfFile));
                    Files.write(Paths.get(atfFile), new String(Files.readAllBytes(Paths.get(atfFile))).replace("\"", "").getBytes());

                    //read csv header
                    fileReader.readLine();
                    // Read customer data line by line
                    while ((line = fileReader.readLine()) != null) {
                        String[] element = line.split(",");
                        if (element.length > 0) {
                            logger.info("Enter to insert All Txn Data--");
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
                    fileReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    atfFileRepository.saveAll(allTxnFiles);
                    updated = true;
                    logger.info("AllTxn File Data Inserted Successfully----!!!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return updated;
    }

    @Override
    public Boolean removeDataInDB() {
        atfFileRepository.deleteAll();
        txnListMainTotalRepository.deleteAll();
        return true;
    }

    @Override
    public void generateAtfFileReport() throws IOException, ParseException {
//        String updatedAtfFile = updatedAtfFilePath + "/All_Txn_File_Updated-" + DateUtil.allTxnDate() + ".txt";
        String updatedAtfFile = updatedAtfFilePath + "/All_Txn_File_Updated-"+ DateUtil.dateToStringForMail(DateUtil.currentDate()) + ".txt";

        String atfFileSheet = "All_Txn_File_Updated-" + DateUtil.allTxnDate() + ".txt";
        List<Object[]> atf = null;
        for (int i = 0; i < 17; i++) {
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
                addressesArr[0] = "Rule 7- UPI txns Txn ID should match with a Reversal OriginalTransactionID in same file";
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
            }else if (i == 16) {
                addressesArr[0] = "Rule 17- Rules Not Verified because Data alignment issues Data";
                atf = atfFileRepository.findByAtfFileDataRule17();
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
    public List<AtfFileReport> updateDataBasedOnTransId() {
        List<String> transactionId = atfFileRepository.findAllTransId();
        List<String> voidOrReversalCase = atfFileRepository.findAllTransIdForVoidOrReversal();
        logger.info("Transaction Id List Size --{}", transactionId.size());
        logger.info("Void Or Reversal Transaction Id List Size --{}", transactionId.size());
        int count = 0;
        for (int i = 0; i < transactionId.size(); i += 500) {
            count++;
            List<String> sub = transactionId.subList(i, Math.min(transactionId.size(), i + 500));
            logger.info("Loop Count For Sale AND UPI with Last TransactionId--- {}---- {}", count, sub.get(sub.size() - 1));
            threadExecution(sub);
        }
        for (int i = 0; i < voidOrReversalCase.size(); i += 500) {
            count++;
            List<String> sub = voidOrReversalCase.subList(i, Math.min(voidOrReversalCase.size(), i + 500));
            logger.info("Loop Count For Void AND Reversal with Last TransactionId--- {}---- {}", count, sub.get(sub.size() - 1));
            threadExecution1(sub);
        }
        return null;
    }


    public void threadExecution(List<String> transId) throws RuntimeException {
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
                                logger.info("Date Original --{}",DateUtil.oneHourBeforeDate(update.get(1).getTransactionDate()));
                                logger.info("Date After compare ---{}",(DateUtil.parseSimpleDateForRules(update.get(0).getTransactionDate()).equals(DateUtil.parseSimpleDateForRules(update.get(1).getTransactionDate()))));
                                logger.info("Date Compare --{}",((update.get(1).getTransactionDate().before(DateUtil.oneHourBeforeDate(update.get(1).getTransactionDate())))));
                                if ((DateUtil.parseSimpleDateForRules(update.get(0).getTransactionDate()).equals(DateUtil.parseSimpleDateForRules(update.get(1).getTransactionDate()))) && (update.get(1).getTransactionDate().before(DateUtil.oneHourBeforeDate(update.get(1).getTransactionDate())))) {
                                    if (update.get(1).getTransactionType().equals("Sale") || update.get(1).getTransactionType().equals("UPI")) {
                                        if (((update.get(1).getStatus().equals("ACK") || update.get(1).getStatus().equals("HOST")) && update.get(1).getResponseCode().equals("00") && update.get(1).getSettlementStatus().equals("Settled")) || update.get(0).getSettlementStatus().equals("Settled")) {
                                            update.get(0).setNotSettledTxnWrongCorrespondingVoidOrReversal(true);
                                            update.get(1).setNotSettledTxnWrongCorrespondingVoidOrReversal(true);
                                        }
                                    }
                                } else {
                                    String oneHourDate = DateUtil.dateToString(update.get(1).getTransactionDate());
                                    logger.info("One Hour Date --{}", oneHourDate);
                                    logger.info("After One --{}",DateUtil.oneHourBeforeDate(update.get(1).getTransactionDate()));
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
                                        logger.info("One Hour Date --{}",oneHourDate);
                                        logger.info("After One --{}",(update.get(0).getTransactionDate().before(DateUtil.oneHourBeforeDate(update.get(0).getTransactionDate()))));
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
                                if (((update.get(0).getMaskedCardNumber() == null) || (update.get(0).getCardHolderName() == null) || (update.get(0).getPosDeviceId() == null) || (update.get(0).getRrn() == null) || (update.get(0).getTransactionAuthCode() == null) || (update.get(0).getResponseCode() == null) || (update.get(0).getTransactionDate() == null) || (update.get(0).getResponseDate() == null) || (update.get(0).getStan() == null) || (update.get(0).getMerchantId() == null) || (update.get(0).getTerminalId() == null) || (update.get(0).getInvoiceNumber() == null) || (update.get(0).getBatchNumber() == null) || (update.get(0).getAmount() == null) || (update.get(0).getTransactionType() == null) || (update.get(0).getTransactionId() == null) || (update.get(0).getOrgTransactionId() == null)) ||
                                        ((update.get(0).getMaskedCardNumber().equals("null")) || (update.get(0).getCardHolderName().equals("null")) || (update.get(0).getPosDeviceId().equals("null")) || (update.get(0).getRrn().equals("null")) || (update.get(0).getTransactionAuthCode().equals("null")) || (update.get(0).getResponseCode().equals("null")) || (update.get(0).getTransactionDate().equals("null")) || (update.get(0).getResponseDate().equals("null")) || (update.get(0).getStan().equals("null")) || (update.get(0).getMerchantId().equals("null")) || (update.get(0).getTerminalId().equals("null")) || (update.get(0).getInvoiceNumber().equals("null")) || (update.get(0).getBatchNumber().equals("null")) || (update.get(0).getAmount().equals("null")) || (update.get(0).getTransactionType().equals("null")) || (update.get(0).getTransactionId().equals("null")) || (update.get(0).getOrgTransactionId().equals("null"))) ||
                                        ((update.get(0).getMaskedCardNumber().equals("")) || (update.get(0).getCardHolderName().equals("")) || (update.get(0).getRrn().equals("")) || (update.get(0).getPosDeviceId().equals("")) || (update.get(0).getTransactionAuthCode().equals("")) || (update.get(0).getResponseCode().equals("")) || (update.get(0).getTransactionDate().equals("")) || (update.get(0).getResponseDate().equals("")) || (update.get(0).getStan().equals("")) || (update.get(0).getMerchantId().equals("")) || (update.get(0).getTerminalId().equals("")) || (update.get(0).getInvoiceNumber().equals("")) || (update.get(0).getBatchNumber().equals("")) || (update.get(0).getAmount().equals("")) || (update.get(0).getTransactionType().equals("")) || (update.get(0).getTransactionId().equals("")) || (update.get(0).getOrgTransactionId().equals("")))) {
                                    logger.info(DOUBLE_VALUE + " Checking Void and Reversal Null Values ---{}", update.get(0).getTransactionId());
                                    update.get(0).setVoidReversalNullValueStatus(true);
                                    update.get(1).setVoidReversalNullValueStatus(true);
                                }
                            } else if (update.get(1).getTransactionType().equals("UPI")) {
                                if (((update.get(0).getRrn() == null) || (update.get(0).getResponseCode() == null) || (update.get(0).getTransactionDate() == null) || (update.get(0).getResponseDate() == null) || (update.get(0).getStan() == null) || (update.get(0).getPosDeviceId() == null) || (update.get(0).getMerchantId() == null) || (update.get(0).getTerminalId() == null) || (update.get(0).getInvoiceNumber() == null) || (update.get(0).getBatchNumber() == null) || (update.get(0).getAmount() == null) || (update.get(0).getTransactionType() == null) || (update.get(0).getTransactionId() == null) || (update.get(0).getOrgTransactionId() == null)) ||
                                        ((update.get(0).getRrn().equals("null")) || (update.get(0).getTransactionDate().equals("null")) || (update.get(0).getResponseDate().equals("null")) || (update.get(0).getStan().equals("null")) || (update.get(0).getPosDeviceId().equals("null")) || (update.get(0).getMerchantId().equals("null")) || (update.get(0).getTerminalId().equals("null")) || (update.get(0).getInvoiceNumber().equals("null")) || (update.get(0).getBatchNumber().equals("null")) || (update.get(0).getAmount().equals("null")) || (update.get(0).getTransactionType().equals("null")) || (update.get(0).getTransactionId().equals("null")) || (update.get(0).getOrgTransactionId().equals("null"))) ||
                                        ((update.get(0).getRrn().equals("")) || (update.get(0).getResponseCode().equals("")) || (update.get(0).getTransactionDate().equals("")) || (update.get(0).getResponseDate().equals("")) || (update.get(0).getStan().equals("")) || (update.get(0).getPosDeviceId().equals("")) || (update.get(0).getMerchantId().equals("")) || (update.get(0).getTerminalId().equals("")) || (update.get(0).getInvoiceNumber().equals("")) || (update.get(0).getBatchNumber().equals("")) || (update.get(0).getAmount().equals("")) || (update.get(0).getTransactionType().equals("")) || (update.get(0).getTransactionId().equals("")) || (update.get(0).getOrgTransactionId().equals("")))) {
                                    logger.info(DOUBLE_VALUE + " Checking Void and Reversal Null Values ---{}", update.get(0).getTransactionId());
                                    update.get(0).setVoidReversalNullValueStatus(true);
                                    update.get(1).setVoidReversalNullValueStatus(true);
                                }
                            }
                        }
                    } else {
                        if (update.get(1).getTransactionType().equals("Reversal")) {
                            if ((update.get(1).getStatus().equals("INIT"))) {
                                if (update.get(0).getTransactionType().equals("Sale")) {
                                    if (((update.get(1).getMaskedCardNumber() == null) || (update.get(1).getCardHolderName() == null) || (update.get(1).getRrn() == null) || (update.get(1).getPosDeviceId() == null) || (update.get(1).getTransactionAuthCode() == null) || (update.get(1).getResponseCode() == null) || (update.get(1).getTransactionDate() == null) || (update.get(1).getResponseDate() == null) || (update.get(1).getStan() == null) || (update.get(1).getMerchantId() == null) || (update.get(1).getTerminalId() == null) || (update.get(1).getInvoiceNumber() == null) || (update.get(1).getBatchNumber() == null) || (update.get(1).getAmount() == null) || (update.get(1).getTransactionType() == null) || (update.get(1).getTransactionId() == null) || (update.get(1).getOrgTransactionId() == null)) ||
                                            ((update.get(1).getMaskedCardNumber().equals("null")) || (update.get(1).getCardHolderName().equals("null")) || (update.get(1).getRrn().equals("null")) || (update.get(1).getPosDeviceId().equals("null")) || (update.get(1).getTransactionAuthCode().equals("null")) || (update.get(1).getResponseCode().equals("null")) || (update.get(1).getTransactionDate().equals("null")) || (update.get(1).getResponseDate().equals("null")) || (update.get(1).getStan().equals("null")) || (update.get(1).getMerchantId().equals("null")) || (update.get(1).getTerminalId().equals("null")) || (update.get(1).getInvoiceNumber().equals("null")) || (update.get(1).getBatchNumber().equals("null")) || (update.get(1).getAmount().equals("null")) || (update.get(1).getTransactionType().equals("null")) || (update.get(1).getTransactionId().equals("null")) || (update.get(1).getOrgTransactionId().equals("null"))) ||
                                            ((update.get(1).getMaskedCardNumber().equals("")) || (update.get(1).getCardHolderName().equals("")) || (update.get(1).getRrn().equals("")) || (update.get(1).getPosDeviceId().equals("")) || (update.get(1).getTransactionAuthCode().equals("")) || (update.get(1).getResponseCode().equals("")) || (update.get(1).getTransactionDate().equals("")) || (update.get(1).getResponseDate().equals("")) || (update.get(1).getStan().equals("")) || (update.get(1).getMerchantId().equals("")) || (update.get(1).getTerminalId().equals("")) || (update.get(1).getInvoiceNumber().equals("")) || (update.get(1).getBatchNumber().equals("")) || (update.get(1).getAmount().equals("")) || (update.get(1).getTransactionType().equals("")) || (update.get(1).getTransactionId().equals("")) || (update.get(1).getOrgTransactionId().equals("")))) {
                                        logger.info(DOUBLE_VALUE + " Checking Void and Reversal Null Values ---{}", update.get(0).getTransactionId());
                                        update.get(0).setVoidReversalNullValueStatus(true);
                                        update.get(1).setVoidReversalNullValueStatus(true);
                                    }
                                } else if (update.get(0).getTransactionType().equals("UPI")) {
                                    if (((update.get(1).getRrn() == null) || (update.get(1).getResponseCode() == null) || (update.get(1).getTransactionDate() == null) || (update.get(1).getResponseDate() == null) || (update.get(1).getStan() == null) || (update.get(1).getPosDeviceId() == null) || (update.get(1).getMerchantId() == null) || (update.get(1).getTerminalId() == null) || (update.get(1).getInvoiceNumber() == null) || (update.get(1).getBatchNumber() == null) || (update.get(1).getAmount() == null) || (update.get(1).getTransactionType() == null) || (update.get(1).getTransactionId() == null) || (update.get(1).getOrgTransactionId() == null)) ||
                                            ((update.get(1).getRrn().equals("null")) || (update.get(1).getTransactionDate().equals("null")) || (update.get(1).getResponseDate().equals("null")) || (update.get(1).getStan().equals("null")) || (update.get(1).getPosDeviceId().equals("null")) || (update.get(1).getMerchantId().equals("null")) || (update.get(1).getTerminalId().equals("null")) || (update.get(1).getInvoiceNumber().equals("null")) || (update.get(1).getBatchNumber().equals("null")) || (update.get(1).getAmount().equals("null")) || (update.get(1).getTransactionType().equals("null")) || (update.get(1).getTransactionId().equals("null")) || (update.get(1).getOrgTransactionId().equals("null"))) ||
                                            ((update.get(1).getRrn().equals("")) || (update.get(1).getResponseCode().equals("")) || (update.get(1).getTransactionDate().equals("")) || (update.get(1).getResponseDate().equals("")) || (update.get(1).getStan().equals("")) || (update.get(1).getPosDeviceId().equals("")) || (update.get(1).getMerchantId().equals("")) || (update.get(1).getTerminalId().equals("")) || (update.get(1).getInvoiceNumber().equals("")) || (update.get(1).getBatchNumber().equals("")) || (update.get(1).getAmount().equals("")) || (update.get(1).getTransactionType().equals("")) || (update.get(1).getTransactionId().equals("")) || (update.get(1).getOrgTransactionId().equals("")))) {
                                        logger.info(DOUBLE_VALUE + " Checking Void and Reversal Null Values ---{}", update.get(0).getTransactionId());
                                        update.get(0).setVoidReversalNullValueStatus(true);
                                        update.get(1).setVoidReversalNullValueStatus(true);
                                    }
                                }
                            }
                        }
                    }
                    if (update.get(0).getTransactionType().equals("Void")) {
                        if (!(update.get(0).getStatus().equals("INIT"))) {
                            if (((update.get(0).getMaskedCardNumber() == null) || (update.get(0).getCardHolderName() == null) || (update.get(0).getRrn() == null) || (update.get(0).getPosDeviceId() == null) || (update.get(0).getTransactionAuthCode() == null) || (update.get(0).getResponseCode() == null) || (update.get(0).getTransactionDate() == null) || (update.get(0).getResponseDate() == null) || (update.get(0).getStan() == null) || (update.get(0).getMerchantId() == null) || (update.get(0).getTerminalId() == null) || (update.get(0).getInvoiceNumber() == null) || (update.get(0).getBatchNumber() == null) || (update.get(0).getAmount() == null) || (update.get(0).getTransactionType() == null) || (update.get(0).getTransactionId() == null) || (update.get(0).getOrgTransactionId() == null)) ||
                                    ((update.get(0).getMaskedCardNumber().equals("null")) || (update.get(0).getCardHolderName().equals("null")) || (update.get(0).getRrn().equals("null")) || (update.get(0).getPosDeviceId().equals("null")) || (update.get(0).getTransactionAuthCode().equals("null")) || (update.get(0).getResponseCode().equals("null")) || (update.get(0).getTransactionDate().equals("null")) || (update.get(0).getResponseDate().equals("null")) || (update.get(0).getStan().equals("null")) || (update.get(0).getMerchantId().equals("null")) || (update.get(0).getTerminalId().equals("null")) || (update.get(0).getInvoiceNumber().equals("null")) || (update.get(0).getBatchNumber().equals("null")) || (update.get(0).getAmount().equals("null")) || (update.get(0).getTransactionType().equals("null")) || (update.get(0).getTransactionId().equals("null")) || (update.get(0).getOrgTransactionId().equals("null"))) ||
                                    ((update.get(0).getMaskedCardNumber().equals("")) || (update.get(0).getCardHolderName().equals("")) || (update.get(0).getRrn().equals("")) || (update.get(0).getPosDeviceId().equals("")) || (update.get(0).getTransactionAuthCode().equals("")) || (update.get(0).getResponseCode().equals("")) || (update.get(0).getTransactionDate().equals("")) || (update.get(0).getResponseDate().equals("")) || (update.get(0).getStan().equals("")) || (update.get(0).getMerchantId().equals("")) || (update.get(0).getTerminalId().equals("")) || (update.get(0).getInvoiceNumber().equals("")) || (update.get(0).getBatchNumber().equals("")) || (update.get(0).getAmount().equals("")) || (update.get(0).getTransactionType().equals("")) || (update.get(0).getTransactionId().equals("")) || (update.get(0).getOrgTransactionId().equals("")))) {
                                logger.info(DOUBLE_VALUE + " Checking Void and Reversal Null Values ---{}", update.get(0).getTransactionId());
                                update.get(0).setVoidReversalNullValueStatus(true);
                                update.get(1).setVoidReversalNullValueStatus(true);
                            }
                        }
                    } else {
                        if (update.get(1).getTransactionType().equals("Void")) {
                            if (!(update.get(1).getStatus().equals("INIT"))) {
                                if (((update.get(1).getMaskedCardNumber() == null) || (update.get(1).getCardHolderName() == null) || (update.get(1).getRrn() == null) || (update.get(1).getPosDeviceId() == null) || (update.get(1).getTransactionAuthCode() == null) || (update.get(1).getResponseCode() == null) || (update.get(0).getTransactionDate() == null) || (update.get(1).getResponseDate() == null) || (update.get(1).getStan() == null) || (update.get(1).getMerchantId() == null) || (update.get(1).getTerminalId() == null) || (update.get(1).getInvoiceNumber() == null) || (update.get(1).getBatchNumber() == null) || (update.get(1).getAmount() == null) || (update.get(1).getTransactionType() == null) || (update.get(1).getTransactionId() == null) || (update.get(1).getOrgTransactionId() == null)) ||
                                        ((update.get(1).getMaskedCardNumber().equals("null")) || (update.get(1).getCardHolderName().equals("null")) || (update.get(1).getRrn().equals("null")) || (update.get(1).getPosDeviceId().equals("null")) || (update.get(1).getTransactionAuthCode().equals("null")) || (update.get(1).getResponseCode().equals("null")) || (update.get(1).getTransactionDate().equals("null")) || (update.get(1).getResponseDate().equals("null")) || (update.get(1).getStan().equals("null")) || (update.get(1).getMerchantId().equals("null")) || (update.get(1).getTerminalId().equals("null")) || (update.get(1).getInvoiceNumber().equals("null")) || (update.get(1).getBatchNumber().equals("null")) || (update.get(1).getAmount().equals("null")) || (update.get(1).getTransactionType().equals("null")) || (update.get(1).getTransactionId().equals("null")) || (update.get(1).getOrgTransactionId().equals("null"))) ||
                                        ((update.get(1).getMaskedCardNumber().equals("")) || (update.get(1).getCardHolderName().equals("")) || (update.get(1).getRrn().equals("")) || (update.get(1).getPosDeviceId().equals("")) || (update.get(1).getTransactionAuthCode().equals("")) || (update.get(1).getResponseCode().equals("")) || (update.get(1).getTransactionDate().equals("")) || (update.get(1).getResponseDate().equals("")) || (update.get(1).getStan().equals("")) || (update.get(1).getMerchantId().equals("")) || (update.get(1).getTerminalId().equals("")) || (update.get(1).getInvoiceNumber().equals("")) || (update.get(1).getBatchNumber().equals("")) || (update.get(1).getAmount().equals("")) || (update.get(1).getTransactionType().equals("")) || (update.get(1).getTransactionId().equals("")) || (update.get(1).getOrgTransactionId().equals("")))) {
                                    logger.info(DOUBLE_VALUE + " Checking Void and Reversal Null Values ---{}", update.get(1).getTransactionId());
                                    update.get(0).setVoidReversalNullValueStatus(true);
                                    update.get(1).setVoidReversalNullValueStatus(true);
                                }
                            }
                        }
                    }

                    if (update.get(0).getTransactionType().equals("Sale") || update.get(0).getTransactionType().equals("UPI") || update.get(0).getTransactionType().equals("Void")) {
                        if (!(update.get(1).getStatus().equals("INIT"))) {
                            if ((update.get(1).getResponseDate() == null) || (update.get(1).getTransactionDate() == null)) {
                                update.get(0).setResponseDateCheck(true);
                                update.get(1).setResponseDateCheck(true);
                            } else if ((update.get(1).getResponseDate().before(update.get(1).getTransactionDate())) || (update.get(0).getResponseDate().before(update.get(0).getTransactionDate()))) {
                                update.get(0).setResponseDateCheck(true);
                                update.get(1).setResponseDateCheck(true);
                            }
                            if(update.get(0).getTransactionDate() !=null && update.get(1).getTransactionDate() !=null) {
                                String date = null;
                                try {
                                    date = DateUtil.dateComparison(DateUtil.currentDate());
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                                try {
                                    if ((!date.equals(DateUtil.dateComparison(update.get(0).getTransactionDate()))) || (!date.equals(DateUtil.dateComparison(update.get(0).getResponseDate()))) || (!date.equals(DateUtil.dateComparison(update.get(1).getTransactionDate()))) || (!date.equals(DateUtil.dateComparison(update.get(1).getResponseDate())))) {
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
                                if(update.get(0).getTransactionDate() !=null && update.get(1).getTransactionDate() !=null) {
                                    String date = null;
                                    try {
                                        date = DateUtil.dateComparison(DateUtil.currentDate());
                                    } catch (ParseException e) {
                                        throw new RuntimeException(e);
                                    }
                                    try {
                                        if ((!date.equals(DateUtil.dateComparison(update.get(0).getTransactionDate()))) || (!date.equals(DateUtil.dateComparison(update.get(0).getResponseDate()))) || (!date.equals(DateUtil.dateComparison(update.get(1).getTransactionDate()))) || (!date.equals(DateUtil.dateComparison(update.get(1).getResponseDate())))) {
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
                            if(update.get(0).getTransactionDate() !=null && update.get(1).getTransactionDate() !=null) {
                                String date = null;
                                try {
                                    date = DateUtil.dateComparison(DateUtil.currentDate());
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                                try {
                                    if ((!date.equals(DateUtil.dateComparison(update.get(0).getTransactionDate()))) || (!date.equals(DateUtil.dateComparison(update.get(0).getResponseDate()))) || (!date.equals(DateUtil.dateComparison(update.get(1).getTransactionDate()))) || (!date.equals(DateUtil.dateComparison(update.get(1).getResponseDate())))) {
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
                                if(update.get(0).getTransactionDate() !=null && update.get(1).getTransactionDate() !=null) {
                                    String date = null;
                                    try {
                                        date = DateUtil.dateComparison(DateUtil.currentDate());
                                    } catch (ParseException e) {
                                        throw new RuntimeException(e);
                                    }
                                    try {
                                        if ((!date.equals(DateUtil.dateComparison(update.get(0).getTransactionDate()))) || (!date.equals(DateUtil.dateComparison(update.get(0).getResponseDate()))) || (!date.equals(DateUtil.dateComparison(update.get(1).getTransactionDate()))) || (!date.equals(DateUtil.dateComparison(update.get(1).getResponseDate())))) {
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
                        String date =null;
                        try {
                           date = DateUtil.dateComparison(DateUtil.currentDate());
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
                        if(fileReport.getTransactionDate() !=null) {
                            try {
                                String date1 = DateUtil.dateComparison(fileReport.getTransactionDate());
//                                logger.info("Comparison Date --{}", date1);
                                if ((!date.equals(DateUtil.dateComparison(fileReport.getTransactionDate()))) || (!date.equals(DateUtil.dateComparison(fileReport.getResponseDate())))) {
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
                if (fileReport.getTransactionType().equals("Void") || fileReport.getTransactionType().equals("Reversal") ) {
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
        transId.forEach(l->{
            List<AtfFileReport> update = totalList.stream().filter(t -> t.getTransactionId().equals(l) || t.getOrgTransactionId().equals(l)).collect(Collectors.toList());
            if(update.size() ==1){
                logger.info("Single Transaction Id List--{}", update.size());
                AtfFileReport fileReport = totalList.stream().filter(p -> p.getOrgTransactionId().equals(l)).findAny().orElse(null);
                fileReport.setRulesVerifiedStatus(true);
                if (fileReport.getTransactionType().equals("Void") || fileReport.getTransactionType().equals("Reversal") ) {
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
                BufferedReader fileReader = null;
                try {
                    String line = "";
                    fileReader = new BufferedReader(new FileReader(settlementFile));
                    Files.write(Paths.get(settlementFile), new String(Files.readAllBytes(Paths.get(settlementFile))).replace("\"", "").getBytes());

                    //read csv header
                    fileReader.readLine();
                    // Read customer data line by line
                    while ((line = fileReader.readLine()) != null) {
                        String[] element = line.split(",");
                        if (element.length > 0) {
//                            if(element[8].contains(DateUtil.previousDateForSettlement())){
                            logger.info("Enter to insert Settlement Data--");
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
//                            settlementFile1.setDate(DateUtil.stringToDate(element[12]));

                            settlementFile1.setDate(element[11]);
                            settlementFile1.setFilename(element[12]);
                            settlementFiles.add(settlementFile1);
//                            }
                        }
                    }
                    fileReader.close();
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
                BufferedReader fileReader = null;
                try {
                    String line = "";
                    fileReader = new BufferedReader(new FileReader(txnListFile));
                    Files.write(Paths.get(txnListFile), new String(Files.readAllBytes(Paths.get(txnListFile))).replace("\"", "").getBytes());

                    //read csv header
                    fileReader.readLine();
                    // Read customer data line by line
                    while ((line = fileReader.readLine()) != null) {
                        String[] element = line.split(",");
                        if (element.length > 0) {
//                            if(element[8].contains(DateUtil.previousDate())){
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
                    }
                    fileReader.close();
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
        atfFileRepository.deleteAll();
        settlementFileRepository.deleteAll();
        transactionListRepository.deleteAll();
//        txnListMainTotalRepository.deleteAll();
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
                BufferedReader fileReader = null;
                try {
                    String line = "";
                    fileReader = new BufferedReader(new FileReader(missingTxnBefore));
                    Files.write(Paths.get(missingTxnBefore), new String(Files.readAllBytes(Paths.get(missingTxnBefore))).replace("\"", "").getBytes());

                    //read csv header
                    fileReader.readLine();
                    // Read customer data line by line
                    while ((line = fileReader.readLine()) != null) {
                        String[] element = line.split(",");
                        if (element.length > 0) {
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
                    }
                    fileReader.close();
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
    public void generateMissingATFFileTxn() throws IOException, ParseException {
//        String updatedAtfFile = updatedAtfFilePath + "/All_Txn_File_Updated-" + DateUtil.allTxnDate() + ".txt";
//        String updatedAtfFile = updatedAtfFilePath + "/All_Txn_File_Updated.txt";

        String updatedAtfFile = updatedAtfFilePath + "/All_Txn_File_Updated-"+ DateUtil.dateToStringForMail(DateUtil.currentDate()) + ".txt";


        String atfFileSheet = "All_Txn_File_Updated-" + DateUtil.allTxnDate() + ".txt";
        String[] addressesArr = new String[1];

        String date1 = DateUtil.allTxnDate().concat(" 23:00:00");
        String date2 = DateUtil.allTxnDate2().concat(" 23:00:00");

//        String date1 = "2023-10-13".concat(" 23:00:00");
//        String date2 = "2023-10-14".concat(" 23:00:00");

        addressesArr[0] = "Rule 18 - ATF File Missing Transactions - RRN List";
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

            logger.info("Connected Success! for the path {}",sourcePath);
            this.mChannelSftp = (ChannelSftp) this.mSSHSession.openChannel("sftp");

            this.mChannelSftp.connect();

            if (this.mChannelSftp != null) {
                connectionStatus = true;
            }
            if (connectionStatus) {
                mChannelSftp.cd(destination);
                File paths = new File(sourcePath);
                String[] files = paths.list();
                for (String fileName:files){
                    logger.info("upload file Name {} ",fileName);
                    mChannelSftp.put(sourcePath+"/"+fileName,destination);
                    logger.info("uploaded");
                }
                status = true;
//                logger.info("upload Success! ");
                if (status) {
                    logger.info("Moved Success! destination- {}-{}",sourcePath,status);
                    return status;
                } else {
                    logger.info("Not Moved! {}",status);
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


    public boolean uploadMicro(String sourcePath,String destination) {

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

            logger.info("Connected Success! for the path {}",sourcePath);
            this.mChannelSftp = (ChannelSftp) this.mSSHSession.openChannel("sftp");

            this.mChannelSftp.connect();

            if (this.mChannelSftp != null) {
                connectionStatus = true;
            }
            if (connectionStatus) {
                mChannelSftp.cd(destination);
                File paths = new File(sourcePath);
                String[] files = paths.list();
                for (String fileName:files){
                    logger.info("upload file Name {} ",fileName);
                    mChannelSftp.put(sourcePath+"/"+fileName,destination);
                    logger.info("uploaded");
                }
                status = true;
                logger.info("upload Success! ");
                if (status) {
                    logger.info("Moved Success! destination- {}-{}",sourcePath,status);
                    return status;
                } else {
                    logger.info("Not Moved! {}",status);
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





}
