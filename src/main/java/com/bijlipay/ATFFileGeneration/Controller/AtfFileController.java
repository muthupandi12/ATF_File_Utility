package com.bijlipay.ATFFileGeneration.Controller;

import com.bijlipay.ATFFileGeneration.Config.ApiResponse;
import com.bijlipay.ATFFileGeneration.Model.AtfFileReport;
import com.bijlipay.ATFFileGeneration.Service.AtfFileService;
import com.bijlipay.ATFFileGeneration.Util.DateUtil;
import com.bijlipay.ATFFileGeneration.Util.MailHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Map;
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

    @GetMapping("/upload-atf-file")
    public void uploadAtfFile() throws Exception {
        String allTxnDate = DateUtil.allTxnDate();
        String currentDate = DateUtil.currentDate1();
        boolean allTxnFileUpdated = false;
        boolean txnListBefore = false;
        boolean txnListAfter = false;
//        String atfFile = atfFileReportPath + "All_Txn_File-" + allTxnDate + ".csv";

        String atfFile = atfFileReportPath + "All_Txn_File.csv";


//        String atfFile = atfFileReportPath + "All_Txn_File-2023-10-31.csv";
//        String atfFile = atfFileReportPath + "ATFSettledTxn.csv";


//        String missingTxnBefore = atfFileReportPath + "TransactionList_"+ allTxnDate + ".csv";
//        String missingTxnAfter = atfFileReportPath + "TransactionList_"+ currentDate + ".csv";
//
        String missingTxnBefore = atfFileReportPath + "TransactionList-1.csv";
        String missingTxnAfter = atfFileReportPath + "TransactionList1.csv";

        try {
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

            List<AtfFileReport> atfFileReports =atfFileService.updateDataBasedOnTransId();
            logger.info("Atf File All Rules Updated Successfully");

            atfFileService.generateAtfFileReport();
            atfFileService.generateMissingATFFileTxn();
            mailHandler.sendMail();
            logger.info("Mail Send Successfully....");
//            String destinationPath = "/home/uat1/ATFFiles/";
//            boolean upload = atfFileService.uploadFilesToSFTP(updatedAtfFilePath,destinationPath);
//            if(upload){
//                logger.info("File Moved SuccessFully---");
//            }
            Boolean remove =atfFileService.removeDataInDB();
            if(remove){
                logger.info("Remove Data in DB Successfully");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping(value = {"/getAftFileData", "/getAftFileData/{searchTerm}"})
    public ResponseEntity<?> getAtfData(@PathVariable("searchTerm") Optional<String> searchTerm) {
//        pageable = webConfig.resolvePageable(requestParams, pageable);
        List<AtfFileReport> atfFileReport = atfFileService.getAtfFileData(searchTerm);
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "Success", atfFileReport), HttpStatus.OK);
    }

    @PutMapping("updateAtfData/{transId}")
    public ResponseEntity<?> getAtfData(@PathVariable("transId") String transactionId, @RequestBody AtfFileReport atfRequest) {
        AtfFileReport update = atfFileService.updateAtfDataBasedOnTransId(transactionId, atfRequest);
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "Success", update), HttpStatus.OK);
    }

    @GetMapping("/upload-switch-txn-files")
    public void uploadSwitchTxnFiles() throws Exception {
        String settlementDate = DateUtil.currentDate2();
//        String settlementFile = atfFileReportPath + "Response_BIGILIPAY_AXIS_H2H_SETTLEMENT_" + settlementDate + ".csv";
//        String txnListFile = atfFileReportPath + "TransactionList_" + DateUtil.currentDate1() + ".csv";

        String settlementFile = atfFileReportPath + "Response_BIGILIPAY_AXIS_H2H_SETTLEMENT.csv";
        String txnListFile = atfFileReportPath + "TransactionList1.csv";
        boolean settlementFileUpdated = false;
        boolean txnListFileUpdated = false;
        String atfFile = atfFileReportPath + "All_Txn_File.csv";
        boolean allTxnFileUpdated = false;
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
            Boolean remove =atfFileService.removeDataInDB1();
            if(remove){
                logger.info("Remove Data in DB Successfully");
            }
//            mailHandler.sendTotalFileMail();
//            logger.info("Send All Switch Txn Files Successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
