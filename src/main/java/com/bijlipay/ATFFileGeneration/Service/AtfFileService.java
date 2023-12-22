package com.bijlipay.ATFFileGeneration.Service;

import com.bijlipay.ATFFileGeneration.Model.AtfFileReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
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

    void processQueryExecution(String filepath) throws FileNotFoundException, SQLException;
}


