package com.bijlipay.ATFFileGeneration.Repository;

import com.bijlipay.ATFFileGeneration.Model.AtfFileReport;
import com.bijlipay.ATFFileGeneration.Util.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtfFileRepository extends JpaRepository<AtfFileReport,Long> {

    @Query(value = "select t.transaction_id from atf_file_report_main t where t.response_date_check =1 and t.transaction_type in ('Sale','UPI') ",nativeQuery = true)
    List<Object[]> findByAtfFileDataRule1();
    @Query(value = "select t.transaction_id from atf_file_report_main t where t.sale_txn_only_init_status =1 ",nativeQuery = true)
    List<Object[]> findByAtfFileDataRule2();
    @Query(value = "select t.transaction_id from atf_file_report_main t where t.sale_upi_null_value_status =1 ",nativeQuery = true)
    List<Object[]> findByAtfFileDataRule3();
    @Query(value = "select t.transaction_id  from atf_file_report_main t where t.void_reversal_null_value_status =1 and t.transaction_type in ('Sale','UPI') ",nativeQuery = true)
    List<Object[]> findByAtfFileDataRule4();
    @Query(value = "select t.transaction_id from atf_file_report_main t where t.void_and_sale_txn_id_check =1 and t.transaction_type in ('Sale','UPI') ",nativeQuery = true)
    List<Object[]> findByAtfFileDataRule5();
    @Query(value = "select t.transaction_id from atf_file_report_main t where t.reversal_and_sale_txn_id_check =1 and t.transaction_type in ('Sale','UPI') ",nativeQuery = true)
    List<Object[]> findByAtfFileDataRule6();
    @Query(value = "select t.transaction_id from atf_file_report_main t where t.upi_and_reversal_txn_id_equal_status =1 and t.transaction_type in ('Sale','UPI') ",nativeQuery = true)
    List<Object[]> findByAtfFileDataRule7();
    @Query(value = "select t.transaction_id from atf_file_report_main t where t.reversal_and_ack_status =1 and t.transaction_type in ('Sale','UPI') ",nativeQuery = true)
    List<Object[]> findByAtfFileDataRule8();
    @Query(value = "select t.transaction_id from atf_file_report_main t where t.void_txn_response_code_check =1 and t.transaction_type in ('Sale','UPI') ",nativeQuery = true)
    List<Object[]> findByAtfFileDataRule9();
    @Query(value = "select t.transaction_id from atf_file_report_main t where t.void_txn_other_than_host_status =1 and t.transaction_type in ('Sale','UPI') ",nativeQuery = true)
    List<Object[]> findByAtfFileDataRule10();
    @Query(value = "select t.transaction_id from atf_file_report_main t where t.settled_txn_wrong_status =1 ",nativeQuery = true)
    List<Object[]> findByAtfFileDataRule11();
    @Query(value = "select t.transaction_id from atf_file_report_main t where t.not_settled_txn_wrong_status =1 ",nativeQuery = true)
    List<Object[]> findByAtfFileDataRule12();

    @Query(value = "select t.transaction_id from atf_file_report_main t where t.not_settled_txn_wrong_corresponding_void_or_reversal =1 and t.transaction_type in ('Sale','UPI') ",nativeQuery = true)
    List<Object[]> findByAtfFileDataRule13();

    @Query(value = "select t.transaction_id from atf_file_report_main t where t.settled_txn_wrong_corresponding_void_or_reversal =1 and t.transaction_type in ('Sale','UPI') ",nativeQuery = true)
    List<Object[]> findByAtfFileDataRule16();

    @Query(value = "select t.org_transaction_id from atf_file_report_main t where t.only_void_reversal_without_sale_or_upi =1 ",nativeQuery = true)
    List<Object[]> findByAtfFileDataRule14();
    @Query(value = "select t.transaction_id from atf_file_report_main t where t.response_date_mismatch =1 and t.transaction_type in ('Sale','UPI') ",nativeQuery = true)
    List<Object[]> findByAtfFileDataRule15();


    @Query(value = "select t.org_transaction_id from atf_file_report_main t where t.rules_verified_status =0 ",nativeQuery = true)
    List<Object[]> findByAtfFileDataRule17();
    @Query(value = "select a from AtfFileReport a where a.transactionId =?1")
    List<AtfFileReport> getDataWithSearchTerm(String searchTerm);

    @Query(value = "select a from AtfFileReport a ")
    List<AtfFileReport> getAtfDataWithoutSearchTerm();

    @Query(value = "select a from AtfFileReport a where a.transactionId =?1")
    AtfFileReport findByTransId(String transactionId);
    @Query(value = "select distinct(a.transaction_id) from atf_file_report_main a where a.transaction_type in ('"+ Constants.Sale +"'"+",'"+Constants.UPI+"' ) and a.rules_verified_status =0 ",nativeQuery = true)
    List<String> findAllTransId();

    @Query(value = "select distinct(a.org_transaction_id) from atf_file_report_main a where a.transaction_type in ('"+ Constants.Void +"'"+",'"+Constants.Reversal+"' ) and a.rules_verified_status =0 ",nativeQuery = true)
    List<String> findAllTransIdForVoidOrReversal();

    @Query(value ="select a from AtfFileReport a where (a.transactionId IN (?1) OR a.orgTransactionId IN (?1)) ")
    List<AtfFileReport> findByTransIdTotalList(List<String> transId);


    @Query(value = "select t.terminal_id,t.merchant_id,t.pos_device_id,t.batch_number,t.card_holder_name,t.masked_card_number,t.transaction_mode,t.invoice_number,t.acquire_bank,t.card_type,t.card_network,t.card_issuer_country_code,t.amount,t.response_code,t.rrn,t.transaction_auth_code,t.transaction_date,t.response_date,t.transaction_id,t.org_transaction_id,t.transaction_type,t.status,t.stan,t.settlement_mode,t.settlement_status from atf_file_report_main t where t.rrn not in (select rrn from settlement_file_main)",nativeQuery = true)
    List<Object[]> findByMissingAllTxnAndSettlementData();

    @Query(value = "select count(*) from atf_file_report_main ",nativeQuery = true)
    int findTotalInsertedData();

//    @Query("select rrn from atf_file_report_main where response_code ='00'")
//    List<String> findByDataBasedOnRRN();
}
