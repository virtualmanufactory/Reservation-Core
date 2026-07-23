package com.reservation.sap.repository;

import com.reservation.sap.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface BranchRepository extends JpaRepository<Branch, Integer> {

    List<Branch> findByIdIn(Collection<Integer> ids);

    @Query("select b from Branch b where b.pageStatus is null or upper(b.pageStatus) <> upper(:obsoleteStatus)")
    List<Branch> findAllNotObsolete(@Param("obsoleteStatus") String obsoleteStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Branch b set b.pageStatus = :obsoleteStatus where b.id not in :ids "
            + "and (b.pageStatus is null or upper(b.pageStatus) <> upper(:obsoleteStatus))")
    int markObsoleteMissing(@Param("ids") Collection<Integer> ids, @Param("obsoleteStatus") String obsoleteStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Branch b where b.id not in :ids")
    int deleteMissing(@Param("ids") Collection<Integer> ids);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Branch b set b.pageStatus = :obsoleteStatus "
            + "where b.pageStatus is null or upper(b.pageStatus) <> upper(:obsoleteStatus)")
    int markAllObsolete(@Param("obsoleteStatus") String obsoleteStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Branch b")
    int deleteAllRows();
}
