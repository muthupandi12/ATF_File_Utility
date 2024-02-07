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

    @Query(value = "select mid,tid,batch_number,invoice_number,stan,rrn,auth_code,amount,to_char,additional_amount,status from settlement_file_main where status ='00' and rrn not in (select rrn from atf_file_report_main where response_code in ('00') and transaction_type ='Sale')",nativeQuery = true)
    List<Object[]> findByMissingSettlementDataBasedOnATFData();

    @Query(value = "select * from settlement_file_main s where  s.tid in (select a.terminal_id from atf_file_report_main a where a.response_code ='00' and a.transaction_type ='Sale')",nativeQuery = true)
    List<SettlementFile> findByPhonePeSettlementData();


}
