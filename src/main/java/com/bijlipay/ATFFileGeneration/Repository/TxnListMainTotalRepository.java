package com.bijlipay.ATFFileGeneration.Repository;

import com.bijlipay.ATFFileGeneration.Model.TxnListMainTotal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TxnListMainTotalRepository extends JpaRepository<TxnListMainTotal,Long> {

//    @Query(value = "select rrn from txn_list_main_before where response_received_time >'?1 23:00:00'",nativeQuery = true)
//    List<String> findByDataBasedOnRRN(String allTxnDate);

//    @Query(value = "select * from txn_list_main_before where  ",nativeQuery = true)
//    List<Object[]> findByMissingData();



    @Query(value = "select t.rrn from txn_list_main_total t where t.response_received_time between ?1 and ?2 and t.rrn not in(select m.rrn from atf_file_report_main m where m.response_code ='00')",nativeQuery = true)
    List<Object[]> findByATFFileMissingData(String previousDate1, String previousDate2);
}
