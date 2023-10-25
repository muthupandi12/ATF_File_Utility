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
    @Autowired
    private MailHandler mailHandler;

    @Autowired
    private AtfFileService atfFileService;

    @GetMapping("/upload-atf-file")
    public void uploadAtfFile() throws Exception {
        String allTxnDate = DateUtil.allTxnDate();
        boolean allTxnFileUpdated = false;
        String atfFile = atfFileReportPath + "All_Txn_File-" + allTxnDate + ".csv";

//        String atfFile = atfFileReportPath + "All_Txn_File-Testing.csv";


//        String atfFile = atfFileReportPath + "ATF_Null_Final_Testing.csv";

        try {
            allTxnFileUpdated = atfFileService.updateDataIntoDb(atfFile);
            if (allTxnFileUpdated) {
                logger.info("All Txn File Data inserted in db Successfully----{}", atfFile);
            }


            List<AtfFileReport> atfFileReports =atfFileService.updateDataBasedOnTransId();
            logger.info("Atf File All Rules Updated Successfully");

            atfFileService.generateAtfFileReport();
            mailHandler.sendMail();
            logger.info("Mail Send Successfully....");
//            Boolean remove =atfFileService.removeDataInDB();
//            if(remove){
//                logger.info("Remove Data in DB Successfully");
//            }
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
        String settlementFile = atfFileReportPath + "Response_BIGILIPAY_AXIS_H2H_SETTLEMENT_" + settlementDate + ".csv";
        String txnListFile = atfFileReportPath + "TransactionList_" + DateUtil.currentDate1() + ".csv";
        boolean settlementFileUpdated = false;
        boolean txnListFileUpdated = false;
        try {
            settlementFileUpdated = atfFileService.updatesettlementFileData(settlementFile);
            txnListFileUpdated = atfFileService.updateTxnListData(txnListFile);
            if (new File(settlementFile).exists() && new File(txnListFile).exists()) {
                if (settlementFileUpdated) {
                    logger.info("Settlement File Data inserted in db Successfully----{}", settlementFile);
                }
                if (txnListFileUpdated) {
                    logger.info("Txn List File Data inserted in db Successfully----{}", txnListFile);
                }
            }
            atfFileService.generateAllTxnFileMissingDataFile();
            atfFileService.generateSettlementFileMissingDataFile();
            atfFileService.generateTxnAndSettlementMissingFile();
            Boolean remove =atfFileService.removeDataInDB1();
            if(remove){
                logger.info("Remove Data in DB Successfully");
            }
            mailHandler.sendTotalFileMail();
            logger.info("Send All Switch Txn Files Successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
