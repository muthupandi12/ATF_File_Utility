package com.bijlipay.ATFFileGeneration.Repository;

import com.bijlipay.ATFFileGeneration.Model.AtfFileReport;
import com.bijlipay.ATFFileGeneration.Model.Dto.DateDto;
import com.bijlipay.ATFFileGeneration.Util.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface AtfFileRepository extends JpaRepository<AtfFileReport, Long> {

    @Query(value = "select t.transaction_id from atf_file_report_main t where t.response_date_check =1 and t.transaction_type in ('Sale','UPI') ", nativeQuery = true)
    List<Object[]> findByAtfFileDataRule1();

    @Query(value = "select t.transaction_id from atf_file_report_main t where t.sale_txn_only_init_status =1 ", nativeQuery = true)
    List<Object[]> findByAtfFileDataRule2();

    @Query(value = "select t.transaction_id from atf_file_report_main t where t.sale_upi_null_value_status =1 ", nativeQuery = true)
    List<Object[]> findByAtfFileDataRule3();

    @Query(value = "select t.transaction_id  from atf_file_report_main t where t.void_reversal_null_value_status =1 and t.transaction_type in ('Sale','UPI') ", nativeQuery = true)
    List<Object[]> findByAtfFileDataRule4();

    @Query(value = "select t.transaction_id from atf_file_report_main t where t.void_and_sale_txn_id_check =1 and t.transaction_type in ('Sale','UPI') ", nativeQuery = true)
    List<Object[]> findByAtfFileDataRule5();

    @Query(value = "select t.transaction_id from atf_file_report_main t where t.reversal_and_sale_txn_id_check =1 and t.transaction_type in ('Sale','UPI') ", nativeQuery = true)
    List<Object[]> findByAtfFileDataRule6();

    @Query(value = "select t.transaction_id from atf_file_report_main t where t.upi_and_reversal_txn_id_equal_status =1 and t.transaction_type in ('Sale','UPI') ", nativeQuery = true)
    List<Object[]> findByAtfFileDataRule7();

    @Query(value = "select t.transaction_id from atf_file_report_main t where t.reversal_and_ack_status =1 and t.transaction_type in ('Sale','UPI') ", nativeQuery = true)
    List<Object[]> findByAtfFileDataRule8();

    @Query(value = "select t.transaction_id from atf_file_report_main t where t.void_txn_response_code_check =1 and t.transaction_type in ('Sale','UPI') ", nativeQuery = true)
    List<Object[]> findByAtfFileDataRule9();

    @Query(value = "select t.transaction_id from atf_file_report_main t where t.void_txn_other_than_host_status =1 and t.transaction_type in ('Sale','UPI') ", nativeQuery = true)
    List<Object[]> findByAtfFileDataRule10();

    @Query(value = "select t.transaction_id from atf_file_report_main t where t.settled_txn_wrong_status =1 ", nativeQuery = true)
    List<Object[]> findByAtfFileDataRule11();

    @Query(value = "select t.transaction_id from atf_file_report_main t where t.not_settled_txn_wrong_status =1 ", nativeQuery = true)
    List<Object[]> findByAtfFileDataRule12();

    @Query(value = "select t.transaction_id from atf_file_report_main t where t.not_settled_txn_wrong_corresponding_void_or_reversal =1 and t.transaction_type in ('Sale','UPI') ", nativeQuery = true)
    List<Object[]> findByAtfFileDataRule13();

    @Query(value = "select t.transaction_id from atf_file_report_main t where t.settled_txn_wrong_corresponding_void_or_reversal =1 and t.transaction_type in ('Sale','UPI') ", nativeQuery = true)
    List<Object[]> findByAtfFileDataRule16();

    @Query(value = "select t.org_transaction_id from atf_file_report_main t where t.only_void_reversal_without_sale_or_upi =1 ", nativeQuery = true)
    List<Object[]> findByAtfFileDataRule14();

    @Query(value = "select t.transaction_id from atf_file_report_main t where t.response_date_mismatch =1 and t.transaction_type in ('Sale','UPI') ", nativeQuery = true)
    List<Object[]> findByAtfFileDataRule15();


    @Query(value = "select t.transaction_id from atf_file_report_main t where t.rules_verified_status =0 ", nativeQuery = true)
    List<Object[]> findByAtfFileDataRule17();

    @Query(value = "select t.transaction_id from atf_file_report_main t where t.host_failure_with_reversal =1 and t.transaction_type in ('Sale','UPI') ", nativeQuery = true)
    List<Object[]> findByAtfFileDataRule18();

    @Query(value = "select t.transaction_id from atf_file_report_main t where t.zero_transaction_amount =1 and t.transaction_type in ('Sale','UPI') ", nativeQuery = true)
    List<Object[]> findByAtfFileDataRule19();

    @Query(value = "select distinct(t.transaction_id) from atf_file_report_main t where t.sale_upi_multiple_record =1 and t.transaction_type in ('Sale','UPI') ", nativeQuery = true)
    List<Object[]> findByAtfFileDataRule20();


    @Query(value = "select a from AtfFileReport a where a.transactionId =?1")
    List<AtfFileReport> getDataWithSearchTerm(String searchTerm);

    @Query(value = "select a from AtfFileReport a ")
    List<AtfFileReport> getAtfDataWithoutSearchTerm();

    @Query(value = "select a from AtfFileReport a where a.transactionId =?1")
    AtfFileReport findByTransId(String transactionId);

    @Query(value = "select distinct(a.transaction_id) from atf_file_report_main a where a.transaction_type in ('" + Constants.Sale + "'" + ",'" + Constants.UPI + "' ) and a.rules_verified_status =0 ", nativeQuery = true)
    List<String> findAllTransId();

    @Query(value = "select distinct(a.org_transaction_id) from atf_file_report_main a where a.transaction_type in ('" + Constants.Void + "'" + ",'" + Constants.Reversal + "' ) and a.rules_verified_status =0 ", nativeQuery = true)
    List<String> findAllTransIdForVoidOrReversal();

    @Query(value = "select a from AtfFileReport a where (a.transactionId IN (?1) OR a.orgTransactionId IN (?1)) ")
    List<AtfFileReport> findByTransIdTotalList(List<String> transId);


    @Query(value = "select t.terminal_id,t.merchant_id,t.pos_device_id,t.batch_number,t.card_holder_name,t.masked_card_number,t.transaction_mode,t.invoice_number,t.acquire_bank,t.card_type,t.card_network,t.card_issuer_country_code,t.amount,t.response_code,t.rrn,t.transaction_auth_code,t.transaction_date,t.response_date,t.transaction_id,t.org_transaction_id,t.transaction_type,t.status,t.stan,t.settlement_mode,t.settlement_status from atf_file_report_main t where t.rrn not in (select rrn from settlement_file_main)", nativeQuery = true)
    List<Object[]> findByMissingAllTxnAndSettlementData();

    @Query(value = "select count(*) from atf_file_report_main ", nativeQuery = true)
    int findTotalInsertedData();

    @Query(value = "select t.transaction_id from atf_file_report_main t where t.reversal_and_ack_status =1 and t.transaction_type in ('Reversal') ", nativeQuery = true)
    List<String> findReversalEntry();

    @Query(value = "select terminal_id,merchant_id,pos_device_id,batch_number,card_holder_name,masked_card_number,transaction_mode,invoice_number,acquire_bank,card_type,card_network,card_issuer_country_code,amount,response_code,rrn,transaction_auth_code,transaction_date,response_date,transaction_id,org_transaction_id,transaction_type,status,stan,settlement_mode,settlement_status from atf_file_report_main where transaction_id not in(?1)", nativeQuery = true)
    List<Object[]> findByWithoutReversalEntry(List<String> reversalEntry);

    @Transactional
    @Modifying
    @Query(value = "Truncate table atf_file_report_main", nativeQuery = true)
    void removeATFFileData();

    @Transactional
    @Modifying
    @Query(value = "delete from atf_file_report_main where transaction_type ='Reversal'", nativeQuery = true)
    void removeReversalEntries();

    @Transactional
    @Modifying
    @Query(value = "delete from atf_file_report_main where transaction_type ='Void' ", nativeQuery = true)
    void removeVoidEntries();

    @Transactional
    @Modifying
    @Query(value = "delete from atf_file_report_main where sale_txn_only_init_status =1", nativeQuery = true)
    void removeSaleWithInitEntries();

    @Transactional
    @Modifying
    @Query(value = "delete from atf_file_report_main where transaction_type ='UPI' and status ='INIT'", nativeQuery = true)
    void removeUPIWithInitEntries();

    @Transactional
    @Modifying
    @Query(value = "delete from atf_file_report_main where transaction_type ='Sale' and status ='HOST' and response_code not in ('00')", nativeQuery = true)
    void removeSaleAndHostNotInResponseCodeSuccess();

    @Transactional
    @Modifying
    @Query(value = "delete from atf_file_report_main where transaction_type ='UPI' and status ='HOST' and response_code not in ('00')", nativeQuery = true)
    void removeUPIAndHostNotInResponseCodeSuccess();

    @Query(value = "select count(*) from atf_file_report_main where settlement_status ='NotSettled'",nativeQuery = true)
    List<Object[]> findNotSettledCount();

    @Query(value = "select count(*) from atf_file_report_main where transaction_type ='Sale' and status ='INIT'",nativeQuery = true)
    List<Object[]> findSaleWithINITCount();
    @Query(value = "select count(*) from atf_file_report_main where transaction_type ='Sale' and status ='HOST' and response_code ='00' and settlement_status ='NotSettled'",nativeQuery = true)
    List<Object[]> findSaleHostNotSettledCount();
    @Query(value = "select count(*) from atf_file_report_main where transaction_type ='Sale' and status ='ACK' and response_code ='00' and settlement_status ='NotSettled'",nativeQuery = true)
    List<Object[]> findSaleACKNotSettledCount();

    @Query(value = "select count(*) from atf_file_report_main where transaction_type ='UPI' and status ='INIT'",nativeQuery = true)
    List<Object[]> findUPIWithINITCount();

    @Query(value = "select count(*) from atf_file_report_main where transaction_type ='UPI' and status ='HOST' and response_code ='00' and settlement_status ='NotSettled'",nativeQuery = true)
    List<Object[]> findUPIHostNotSettledCount();
    @Query(value = "select count(*) from atf_file_report_main where transaction_type ='UPI' and status ='ACK' and response_code ='00' and settlement_status ='NotSettled'",nativeQuery = true)
    List<Object[]> findUPIACKNotSettledCount();
    @Query(value = "select count(*) from atf_file_report_main where transaction_type ='Void'",nativeQuery = true)
    List<Object[]> findVoidCount();
    @Query(value = "select count(*) from atf_file_report_main where transaction_type ='Reversal'",nativeQuery = true)
    List<Object[]> findReversalCount();
    @Query(value = "select count(*) from atf_file_report_main where response_date_check =1",nativeQuery = true)
    List<Object[]> findDateCheckCount();


    @Query(value = "select terminal_id,merchant_id,pos_device_id,batch_number,card_holder_name,masked_card_number,transaction_mode,invoice_number,acquire_bank,card_type,card_network,card_issuer_country_code,amount,response_code,rrn,transaction_auth_code,transaction_date,response_date,transaction_id,org_transaction_id,transaction_type,status,stan,settlement_mode,settlement_status from atf_file_report_main ", nativeQuery = true)
    List<Object[]> findByRuleUpdatedData();

    @Query(value = "select terminal_id,merchant_id,pos_device_id,batch_number,card_holder_name,masked_card_number,transaction_mode,invoice_number,acquire_bank,card_type,card_network,card_issuer_country_code,amount,response_code,rrn,transaction_auth_code,transaction_date,response_date,transaction_id,org_transaction_id,transaction_type,status,stan,settlement_mode,settlement_status from atf_file_report_main where rrn not in (select rrn from settlement_file_main where status='00')",nativeQuery = true)
    List<Object[]> findBySettlementDataForPhonePe();

    @Transactional
    @Modifying
    @Query(value = "delete from atf_file_report_main  where transaction_id in (?1) OR org_transaction_id in(?1)",nativeQuery = true)
    void removeVoidOrReversalEntry(List<String> voidOrReversal);

    @Transactional
    @Modifying
    @Query(value = "delete from atf_file_report_main  where transaction_type ='Sale' and status ='INIT'",nativeQuery = true)
    void removeSaleWithInitStatus();

    @Transactional
    @Modifying
    @Query(value = "delete from atf_file_report_main  where transaction_type ='UPI' and status ='INIT'",nativeQuery = true)
    void removeUPIWithInitStatus();

    @Query(value = "select terminal_id,merchant_id,pos_device_id,batch_number,card_holder_name,masked_card_number,transaction_mode,invoice_number,acquire_bank,card_type,card_network,card_issuer_country_code,amount,response_code,rrn,transaction_auth_code,transaction_date,response_date,transaction_id,org_transaction_id,transaction_type,status,stan,settlement_mode,settlement_status from atf_file_report_main where transaction_date between ?1 and ?2 and response_code in ('00') and transaction_type ='Sale' and rrn not in (select p.rrn from phonepe_settlement_data p where p.status='00')",nativeQuery = true)
    List<Object[]> findByMissingATFDataBasedOnSettlementData(String minDate,String maxDate);

    @Query(value = "select terminal_id,merchant_id,pos_device_id,batch_number,card_holder_name,masked_card_number,transaction_mode,invoice_number,acquire_bank,card_type,card_network,card_issuer_country_code,amount,response_code,rrn,transaction_auth_code,transaction_date,response_date,transaction_id,org_transaction_id,transaction_type,status,stan,settlement_mode,settlement_status from atf_file_report_main where transaction_date between ?1 and ?2 and response_code in ('00') and transaction_type ='Sale'",nativeQuery = true)
    List<Object[]> findByFinalSettlementData(String minDate,String maxDate);

    @Transactional
    @Modifying
    @Query(value = "delete from atf_file_report_main where transaction_date between ?1 and ?2 and response_code in ('00') and transaction_type ='Sale' and rrn not in (select rrn from phonepe_settlement_data where status='00')",nativeQuery = true)
    void removeMissingATFDataBasedOnSettlementData(String date1,String date2);

    @Query(value = "select terminal_id,merchant_id,pos_device_id,batch_number,card_holder_name,masked_card_number,transaction_mode,invoice_number,acquire_bank,card_type,card_network,card_issuer_country_code,amount,response_code,rrn,transaction_auth_code,transaction_date,response_date,transaction_id,org_transaction_id,transaction_type,status,stan,settlement_mode,settlement_status from atf_file_report_main " +
            "where transaction_type ='Reversal' and response_code ='00' \n" +
            "and settlement_status ='Settled' and transaction_date between ?1 and ?2",nativeQuery = true)
    List<Object[]> findByRefundData(String date1, String date2);
    @Query(value = "select org_transaction_id from atf_file_report_main " +
            "where transaction_type ='Reversal' and response_code ='00' \n" +
            "and settlement_status ='Settled' ",nativeQuery = true)
    List<String> findByRefundDataOnly();

    @Query(value = "select distinct(a.transaction_id) from atf_file_report_main a where a.sale_upi_multiple_record =1",nativeQuery = true)
    List<String> findByMultipleData();

//    List<AtfFileReport> findByData();

//    @Query(value = "select min(t.transaction_date), max(t.transaction_date) from atf_file_report_main t",nativeQuery = true)
//    DateDto findByDatesFromATF();

//    @Query(value = "select new com.bijlipay.ATFFileGeneration.Model.Dto.DateDto(min(t.transactionDate) as minDate,max(t.transactionDate) as maxDate) from AtfFileReport t")
//    DateDto findByDatesFromATF();


//    @Query("select rrn from atf_file_report_main where response_code ='00'")
//    List<String> findByDataBasedOnRRN();


//    @Query(value = "select count(*) from atf_file_report_main t where t.transaction_type in ('UPI') and t.transaction_date like '?1%'", nativeQuery = true)
//    List<Object[]> findByUPITotalData(String date);
//    @Query(value = "select count(*) from atf_file_report_main t where t.transaction_type in ('UPI') and t.transaction_date like '?1%'", nativeQuery = true)
//    List<Object[]> findByUPISettledData(String date1,String date2);
//    @Query(value = "select count(*) from atf_file_report_main t where t.transaction_type in ('UPI') and t.transaction_date like '?1%'", nativeQuery = true)
//    List<Object[]> findByUPINotSettledData(String date1,String date2);


    @Query(value = "select count(*) from atf_file_report_main t where t.transaction_type in ('UPI') and t.transaction_date like '?1%'", nativeQuery = true)
    String findByUPITotalData(String date);
    @Query(value = "select count(*) from atf_file_report_main t where t.transaction_type in ('UPI') and t.transaction_date between ?1 and ?2 and t.settlement_status ='Settled'", nativeQuery = true)
    String findByUPISettledData(String date1,String date2);
    @Query(value = "select count(*) from atf_file_report_main t where t.transaction_type in ('UPI') and t.transaction_date between ?1 and ?2 and t.settlement_status ='NotSettled'", nativeQuery = true)
    String findByUPINotSettledData(String date1,String date2);


}
