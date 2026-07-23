package com.reservation.sap.repository;

import com.reservation.sap.model.OddzialStaging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface OddzialStagingRepository extends JpaRepository<OddzialStaging, Integer> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from OddzialStaging s")
    int deleteAllRows();
}
