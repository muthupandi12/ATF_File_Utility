package com.bijlipay.ATFFileGeneration.Repository;

import com.bijlipay.ATFFileGeneration.Model.SwitchRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SwitchRequestRepository extends JpaRepository<SwitchRequest,String> {

    @Transactional
    @Modifying
    @Query(value = "Truncate table switch_request", nativeQuery = true)
    void truncate();
}
