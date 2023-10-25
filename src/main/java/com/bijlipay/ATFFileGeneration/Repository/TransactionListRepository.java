package com.bijlipay.ATFFileGeneration.Repository;

import com.bijlipay.ATFFileGeneration.Model.TransactionList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TransactionListRepository extends JpaRepository<TransactionList,Long> {

    @Query(value = "select t.mti,t.txn_type,t.terminal_id,t.merchant_id,t.txn_date,t.txn_time,t.txn_amount,t.txn_response_code,t.response_received_time,t.rrn,t.stan,t.invoice_number,t.batch_number,t.urn,t.auth_response_code,t.txn_additional_amount,t.institution_id from txn_list_main t where t.rrn not in (select rrn from atf_file_report_main)",nativeQuery = true)
    List<Object[]> findByMissingAllTxnData();

    @Query(value = "select t.mti,t.txn_type,t.terminal_id,t.merchant_id,t.txn_date,t.txn_time,t.txn_amount,t.txn_response_code,t.response_received_time,t.rrn,t.stan,t.invoice_number,t.batch_number,t.urn,t.auth_response_code,t.txn_additional_amount,t.institution_id from txn_list_main t where t.rrn not in (select rrn from settlement_file_main)",nativeQuery = true)
    List<Object[]> findByMissingSettlementData();

}
