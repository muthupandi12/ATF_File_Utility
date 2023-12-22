package com.bijlipay.ATFFileGeneration.Repository;

import com.bijlipay.ATFFileGeneration.Model.SettlementFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface SettlementFileRepository extends JpaRepository<SettlementFile,Long> {


    @Transactional
    @Modifying
    @Query(value = "Truncate table settlement_file_main", nativeQuery = true)
    void removeSettlementData();

}
