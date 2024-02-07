package com.bijlipay.ATFFileGeneration.Repository;

import com.bijlipay.ATFFileGeneration.Model.PhonePeSettlementData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface PhonePeSettlementDataRepository extends JpaRepository<PhonePeSettlementData,Long> {

    @Query(value = "select mid,tid,batch_number,invoice_number,stan,rrn,auth_code,amount,to_char,additional_amount,status from phonepe_settlement_data where status ='00' and rrn not in (select rrn from atf_file_report_main where response_code in ('00') and transaction_type ='Sale' and transaction_date between ?1 and ?2) ",nativeQuery = true)
    List<Object[]> findByMissingSettlementDataBasedOnATFData(String minDate,String maxDate);


    @Transactional
    @Modifying
    @Query(value = "insert into phonepe_settlement_data (mid,tid,batch_number,invoice_number,stan,rrn,auth_code,amount,to_char,additional_amount,status)\n" +
            "select s.mid ,s.tid,s.batch_number,s.invoice_number ,s.stan,s.rrn ,s.auth_code,s.amount,s.to_char,s.additional_amount,s.status  from settlement_file_main s where  s.tid in \n" +
            "(select a.terminal_id from atf_file_report_main a where a.response_code ='00' and a.transaction_type ='Sale')",nativeQuery = true)
    void insertPhonePeData();
}
