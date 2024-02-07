package com.bijlipay.ATFFileGeneration.Controller;

import com.bijlipay.ATFFileGeneration.Config.ApiResponse;
import com.bijlipay.ATFFileGeneration.Model.AtfFileReport;
import com.bijlipay.ATFFileGeneration.Model.Dto.AxisDto;
import com.bijlipay.ATFFileGeneration.Service.AtfFileService;
import com.bijlipay.ATFFileGeneration.Util.DateUtil;
import com.bijlipay.ATFFileGeneration.Util.JWEMainClient;
import com.bijlipay.ATFFileGeneration.Util.MailHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AtfFileController {

    private static final Logger logger = LoggerFactory.getLogger(AtfFileController.class);

    @Value("${atf.file.report.path}")
    private String atfFileReportPath;

    @Value("${atf.file.updated.path}")
    private String updatedAtfFilePath;
    @Autowired
    private MailHandler mailHandler;

    @Autowired
    private AtfFileService atfFileService;

    @GetMapping("/upload-atf-file/{date}")
    public ResponseEntity<?> uploadAtfFile(@PathVariable("date") String date) throws Exception {
        boolean allTxnFileUpdated = false;
        boolean txnListBefore = false;
        boolean txnListAfter = false;
        String atfFile = atfFileReportPath + "All_Txn_File-" + date + ".csv";

        String afterDate = DateUtil.addOneDay(date);
        String missingTxnBefore = atfFileReportPath + "TransactionList_" + date + ".csv";
        String missingTxnAfter = atfFileReportPath + "TransactionList_" + afterDate + ".csv";

        logger.info("Files -- ATF -- Txn Before --- Txn After --{}--{}--{}", atfFile, missingTxnBefore, missingTxnAfter);

        File atf = new File(atfFile);
        File before = new File(missingTxnBefore);
        File after = new File(missingTxnAfter);
        if (atf.exists() && before.exists() && after.exists()) {
            try {
                Boolean beforeCheck = atfFileService.beforeCheck();
                if (beforeCheck) {
                    logger.info("Checking data present in before insert ");
                }
                allTxnFileUpdated = atfFileService.updateDataIntoDb(atfFile);
                txnListBefore = atfFileService.updateTxnListTotalData(missingTxnBefore);
                txnListAfter = atfFileService.updateTxnListTotalData(missingTxnAfter);
                if (allTxnFileUpdated) {
                    logger.info("All Txn File Data inserted in db Successfully----{}", atfFile);
                }
                if (txnListBefore) {
                    logger.info("Txn List File Before Data inserted in db Successfully----{}", missingTxnBefore);
                }
                if (txnListAfter) {
                    logger.info("Txn List File After Data inserted in db Successfully----{}", missingTxnAfter);
                }
                List<AtfFileReport> atfFileReports = atfFileService.updateDataBasedOnTransId(date);
                logger.info("Atf File All Rules Updated Successfully");

                atfFileService.generateAtfFileReport(date);
                atfFileService.generateMissingATFFileTxn(date);
                Boolean remove = atfFileService.removeDataInDB();
                if (remove) {
                    logger.info("Remove Data in DB Successfully");
                }
                mailHandler.sendATFRuleDataMail(date);
                logger.info("ATF Rules Data Mail Send Successfully....");


            } catch (Exception e) {
                e.printStackTrace();
            }
            return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, "please upload the correct files"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = {"/getAftFileData", "/getAftFileData/{searchTerm}"})
    public ResponseEntity<?> getAtfData(@PathVariable("searchTerm") Optional<String> searchTerm) {
        List<AtfFileReport> atfFileReport = atfFileService.getAtfFileData(searchTerm);
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "Success", atfFileReport), HttpStatus.OK);
    }

    @PutMapping("updateAtfData/{transId}")
    public ResponseEntity<?> getAtfData(@PathVariable("transId") String transactionId, @RequestBody AtfFileReport atfRequest) {
        AtfFileReport update = atfFileService.updateAtfDataBasedOnTransId(transactionId, atfRequest);
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "Success", update), HttpStatus.OK);
    }

    @GetMapping("/upload-switch-txn-files")
    public ResponseEntity<?> uploadSwitchTxnFiles() throws Exception {
        String settlementDate = DateUtil.currentDate2();
        String settlementFile = atfFileReportPath + "Response_BIGILIPAY_AXIS_H2H_SETTLEMENT_" + settlementDate + ".csv";
        String txnListFile = atfFileReportPath + "TransactionList_" + DateUtil.currentDate1() + ".csv";

//        String settlementFile = atfFileReportPath + "Response_BIGILIPAY_AXIS_H2H_SETTLEMENT.csv";
//        String txnListFile = atfFileReportPath + "TransactionList1.csv";
        boolean settlementFileUpdated = false;
        boolean txnListFileUpdated = false;
//        String atfFile = atfFileReportPath + "All_Txn_File.csv";

        String atfFile = atfFileReportPath + "All_Txn_File-" + DateUtil.allTxnDate() + ".csv";

        File atf = new File(atfFile);
        File settlement = new File(settlementFile);
        File txnList = new File(txnListFile);


        boolean allTxnFileUpdated = false;
        if (atf.exists() && settlement.exists() && txnList.exists()) {
            try {
                allTxnFileUpdated = atfFileService.updateDataIntoDb(atfFile);
                settlementFileUpdated = atfFileService.updatesettlementFileData(settlementFile);
                txnListFileUpdated = atfFileService.updateTxnListData(txnListFile);
                if (new File(settlementFile).exists() && new File(txnListFile).exists()) {
                    if (settlementFileUpdated) {
                        logger.info("Settlement File Data inserted in db Successfully----{}", settlementFile);
                    }
                    if (txnListFileUpdated) {
                        logger.info("Txn List File Data inserted in db Successfully----{}", txnListFile);
                    }
                    if (allTxnFileUpdated) {
                        logger.info("All Txn File Data inserted in db Successfully----{}", txnListFile);
                    }
                }
                atfFileService.generateAllTxnFileMissingDataFile();
                atfFileService.generateSettlementFileMissingDataFile();
                atfFileService.generateTxnAndSettlementMissingFile();
                Boolean remove = atfFileService.removeDataInDB1();
                if (remove) {
                    logger.info("Remove Data in DB Successfully");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "Success"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, "please upload the correct files"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/process-atf-file/{date}")
    public ResponseEntity<?> processAtfFile(@PathVariable("date") String date) throws Exception {
        String atfFile = atfFileReportPath + "All_Txn_File-" + date + ".csv";
        File atf = new File(atfFile);
        boolean allTxnFileUpdated = false;
        if (atf.exists()) {
            try {
                allTxnFileUpdated = atfFileService.updateDataIntoDb(atfFile);
                if (allTxnFileUpdated) {
                    logger.info("ATF File Data inserted Successfully ----");
                }
                List<AtfFileReport> atfFileReports = atfFileService.updateDataBasedOnTransIdReversalOnly();
                atfFileService.generateAtfFileReportForReversalEntry(date);
                Boolean remove = atfFileService.removeATFFileRecord();
                if (remove) {
                    logger.info("ATF File Data Removed Successfully ---");
                }
                mailHandler.sendReversalMail(date);
                logger.info("Reversal Data Mail send Successfully ---");
                atfFileService.removeFile(date);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "Success"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, "please upload the correct files"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-atf-rules-count/{date}")
    public ResponseEntity<?> getAtfRuleCount(@PathVariable("date") String date) throws Exception {
//        atfFileService.executeQueryUpdation();
        atfFileService.generateUpdatedATFFile(date);
        atfFileService.atfFileRulesCount(date);
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "Success"), HttpStatus.OK);
    }


    @GetMapping("/phonepe-settlement-refund-file/{date}")
    public ResponseEntity<?> generateSettlementFile(@PathVariable("date") String date) throws Exception {
        String atfFile = atfFileReportPath + "All_Txn_File-" + date + ".csv";
        String settlementDate = DateUtil.currentDate2();
        String settlementFile = atfFileReportPath + "Response_BIGILIPAY_AXIS_H2H_SETTLEMENT_" + settlementDate + ".csv";
        boolean allTxnFileUpdated = false;
        boolean settlementFileUpdated = false;
        try {
            allTxnFileUpdated = atfFileService.updateDataIntoDb(atfFile);
            settlementFileUpdated = atfFileService.updatesettlementFileData(settlementFile);
            if (allTxnFileUpdated && settlementFileUpdated) {
                logger.info("ATF and Settlement Response File Data Inserted successfully ---");
            }
            atfFileService.generateRefundFile(date);
            atfFileService.removeSettlementRulesData();
            int splitData = atfFileService.splitPhonePeSettlementData();
            if (splitData > 0) {
                atfFileService.generateFinalSettlementFile(date);
                atfFileService.atfFileRulesCount(date);
            }
            mailHandler.sendPhonePeSettlementReports(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "Success"), HttpStatus.OK);
    }

//    @GetMapping("/phonepe-refund-file/{date}")
//    public ResponseEntity<?> generateRefundFile(@PathVariable("date") String date) throws Exception {
//        atfFileService.generateRefundFile(date);
//        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "Success"), HttpStatus.OK);
//    }

    @GetMapping("/getPhonePeFiles")
    public ResponseEntity<?> getATFResponseFiles() throws Exception {
        boolean download = false;
        String atfFile = atfFileReportPath + "All_Txn_File-" + DateUtil.previousDateATF() + ".csv";
        String settlementDateBefore = DateUtil.currentDate2();
        String settlementDateTwoDayBefore = DateUtil.twoDayBefore();
        String settlementFileBefore = atfFileReportPath + "Response_BIGILIPAY_AXIS_H2H_SETTLEMENT_" + settlementDateBefore + ".csv";
        String settlementFileTwoDayBefore = atfFileReportPath + "Response_BIGILIPAY_AXIS_H2H_SETTLEMENT_" + settlementDateTwoDayBefore + ".csv";

        boolean allTxnFileUpdated = false;
        boolean settlementFileUpdatedBefore = false;
        boolean settlementFileUpdatedTwoDayBefore = false;
        boolean txnListBefore = false;
        boolean txnListAfter = false;
        try {
            String previousDate = DateUtil.previousDateATF();
            String currentDate = DateUtil.currentDateATF();
            String missingTxnBefore = atfFileReportPath + "TransactionList_" + previousDate + ".csv";
            String missingTxnAfter = atfFileReportPath + "TransactionList_" + currentDate + ".csv";

            logger.info("Files -- ATF --SettlementBefore --SettlementAfter--- Txn Before --- Txn After --{}--{}--{}--{} ---{}", atfFile, settlementFileUpdatedBefore, settlementFileUpdatedTwoDayBefore, missingTxnBefore, missingTxnAfter);

//            download = atfFileService.downloadPhonePeFiles(currentDate);
//            if (download) {
            logger.info("File Download Successfully");
//            Boolean beforeCheck = atfFileService.beforeCheck();
//            if (beforeCheck) {
//                logger.info("Checking data present in before insert ");
//            }
            allTxnFileUpdated = atfFileService.updateDataIntoDb(atfFile);
            settlementFileUpdatedBefore = atfFileService.updatesettlementFileData(settlementFileBefore);
            settlementFileUpdatedTwoDayBefore = atfFileService.updatesettlementFileData(settlementFileTwoDayBefore);
//                txnListBefore = atfFileService.updateTxnListTotalData(missingTxnBefore);
//                txnListAfter = atfFileService.updateTxnListTotalData(missingTxnAfter);
//                if (txnListAfter && txnListBefore) {
//                    logger.info("Transaction List File Data inserted successfully");
//                }
            if (allTxnFileUpdated && settlementFileUpdatedBefore && settlementFileUpdatedTwoDayBefore) {
                logger.info("ATF and Settlement Response File Data Inserted successfully ---");
            }
//            }
            atfFileService.generateMissingRRNFromATF(previousDate);
            atfFileService.generateRefundFile(previousDate);
            int remove = atfFileService.removeSettlementRulesData();
            if (remove > 1) {
                int splitData = atfFileService.splitPhonePeSettlementData();
                if (splitData > 1) {
                    logger.info("enter to generate files ---");
                    atfFileService.generateFinalSettlementFile(previousDate);
                    atfFileService.atfFileRulesCount(previousDate);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "Success"), HttpStatus.OK);
    }


    @PostMapping("/axisEncryptData")
    public ResponseEntity<?> generateAxisData(@RequestBody AxisDto axisDto) throws Exception {
        String encryptString = JWEMainClient.encryptData(axisDto);
        logger.info("Encrypt String ---{}", encryptString);
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "Success"), HttpStatus.OK);
    }

    @PostMapping("/axisDecryptData")
    public ResponseEntity<?> decryptData(@RequestBody String encryptString) throws Exception {
        String decryptString = JWEMainClient.decryptData(encryptString);
        logger.info("Decrypt String ---{}", decryptString);
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "Success"), HttpStatus.OK);
    }

    @PostMapping("/axisGeoTag")
    public ResponseEntity<?> axisGeoTag(@RequestBody AxisDto axisDto) throws Exception {
        String response = atfFileService.callAxisApiForGeotag(axisDto);
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "Success", response), HttpStatus.OK);

    }


    @GetMapping("/validate-atf-file/{date}")
    public ResponseEntity<?> validateAtfFile(@PathVariable("date") String date) throws Exception {
        boolean allTxnFileUpdated = false;
        String atfFile = atfFileReportPath + "All_Txn_File-" + date + ".csv";
        logger.info("Files -- ATF --{}", atfFile);
        try {
            allTxnFileUpdated = atfFileService.updateDataIntoDb(atfFile);
            if (allTxnFileUpdated) {
                logger.info("All Txn File Data inserted in db Successfully----{}", atfFile);
            }
            List<AtfFileReport> atfFileReports = atfFileService.updateDataBasedOnTransId(date);
            logger.info("Atf File All Rules Updated Successfully");

            atfFileService.validatedAtfFileReport(date);
            Boolean remove = atfFileService.removeATFFileRecord();
            if (remove) {
                logger.info("Remove Data in DB Successfully");
            }
            mailHandler.sendValidatedATFDataMail(date);
            logger.info("ATF Rules Data Mail Send Successfully....");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success"), HttpStatus.OK);
    }

}