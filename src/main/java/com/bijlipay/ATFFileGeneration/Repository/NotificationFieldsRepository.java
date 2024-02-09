package com.bijlipay.ATFFileGeneration.Repository;

import com.bijlipay.ATFFileGeneration.Model.NotificationFields;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface NotificationFieldsRepository extends JpaRepository<NotificationFields,Long> {

    @Transactional
    @Modifying
    @Query(value = "Truncate table notification_fields", nativeQuery = true)
    void truncate();
}
