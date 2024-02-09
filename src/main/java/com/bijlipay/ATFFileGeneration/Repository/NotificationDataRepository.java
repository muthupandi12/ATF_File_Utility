package com.bijlipay.ATFFileGeneration.Repository;

import com.bijlipay.ATFFileGeneration.Model.NotificationData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface NotificationDataRepository extends JpaRepository<NotificationData,Long> {

    @Transactional
    @Modifying
    @Query(value = "Truncate table notification_data", nativeQuery = true)
    void truncate();
}

