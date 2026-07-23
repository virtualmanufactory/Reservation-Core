package com.reservation.sap.repository;

import com.reservation.sap.model.BranchStaging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BranchStagingRepository extends JpaRepository<BranchStaging, Integer> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from BranchStaging s")
    int deleteAllRows();
}
