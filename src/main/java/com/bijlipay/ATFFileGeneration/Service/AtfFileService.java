package com.bijlipay.ATFFileGeneration.Service;

import com.bijlipay.ATFFileGeneration.Model.AtfFileReport;
import com.bijlipay.ATFFileGeneration.Model.Dto.AxisDto;
import com.nimbusds.jose.JOSEException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.bijlipay.ATFFileGeneration.Model.AtfFileDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public interface AtfFileService {

    boolean updateDataIntoDb(String atfFile);

    Boolean removeDataInDB();

    void generateAtfFileReport(String date) throws IOException, ParseException;

    List<AtfFileReport> getAtfFileData(Optional<String> searchTerm);

    AtfFileReport updateAtfDataBasedOnTransId(String transactionId,AtfFileReport atfRequest);

    List<AtfFileReport> updateDataBasedOnTransId(String date);

    boolean updatesettlementFileData(String settlementFile);

    boolean updateTxnListData(String txnListFile);


    void generateAllTxnFileMissingDataFile();

    void generateSettlementFileMissingDataFile();

    Boolean removeDataInDB1();

    void generateTxnAndSettlementMissingFile();

    boolean updateTxnListTotalData(String missingTxnBefore);

    void generateMissingATFFileTxn(String date) throws IOException, ParseException;

    boolean uploadFilesToSFTP(String updatedAtfFilePath, String destinationPath);

    Boolean beforeCheck();

    List<AtfFileReport> updateDataBasedOnTransIdReversalOnly();

    List<String> processReversalEntry(String date);

    void generateAtfFileReportForReversalEntry(String date) throws IOException;

    Boolean removeATFFileRecord();

    void processQueryExecution(String filepath) throws IOException, SQLException;

    void executeQueryUpdation();


    void atfFileRulesCount(String date) throws IOException;

    void generateUpdatedATFFile(String date);

    void generateSettlementReportForPhonePe(String settlementDate);

    int removeSettlementRulesData() throws SQLException;

    void generateFinalSettlementFile(String date) throws ParseException, SQLException;

    void removeFile(String date);

    int splitPhonePeSettlementData() throws SQLException;

    void generateRefundFile(String date) throws ParseException;

    String callAxisApiForGeotag(AxisDto axisDto) throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, KeyStoreException, ParseException, JOSEException, KeyManagementException;

    boolean downloadPhonePeFiles(String date);

    void generateMissingRRNFromATF(String previousDate) throws ParseException, IOException;

    void validatedAtfFileReport(String date) throws IOException, ParseException;


    boolean updateNotificationDataIntoDb(String notificationFile) throws IOException, ParseException;

    boolean updatedSwitchReqResDataIntoDb(String switchReqResFile);

    boolean generateATFfile(MultipartFile file, String atfFile) throws IOException, SQLException;

    boolean generateAtfCSVResult(List<AtfFileDTO> atfFileImports, String updatedAtfFile);

    boolean downloadLargeFile(String currentDate);

//    void processMulipleData();
}


