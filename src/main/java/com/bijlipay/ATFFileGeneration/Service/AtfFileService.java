package com.bijlipay.ATFFileGeneration.Service;

import com.bijlipay.ATFFileGeneration.Model.AtfFileReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public interface AtfFileService {

    boolean updateDataIntoDb(String atfFile);

    Boolean removeDataInDB();

    void generateAtfFileReport() throws IOException;

    List<AtfFileReport> getAtfFileData(Optional<String> searchTerm);

    AtfFileReport updateAtfDataBasedOnTransId(String transactionId,AtfFileReport atfRequest);

    List<AtfFileReport> updateDataBasedOnTransId();

    boolean updatesettlementFileData(String settlementFile);

    boolean updateTxnListData(String txnListFile);


    void generateAllTxnFileMissingDataFile();

    void generateSettlementFileMissingDataFile();

    Boolean removeDataInDB1();

    void generateTxnAndSettlementMissingFile();

}


