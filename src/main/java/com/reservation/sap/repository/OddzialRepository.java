package com.reservation.sap.repository;

import com.reservation.sap.model.Oddzial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface OddzialRepository extends JpaRepository<Oddzial, Integer> {

    List<Oddzial> findByIdIn(Collection<Integer> ids);

    @Query("select o from Oddzial o where o.statusNaStronie is null or upper(o.statusNaStronie) <> upper(:obsoleteStatus)")
    List<Oddzial> findAllNotObsolete(@Param("obsoleteStatus") String obsoleteStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Oddzial o set o.statusNaStronie = :obsoleteStatus where o.id not in :ids "
            + "and (o.statusNaStronie is null or upper(o.statusNaStronie) <> upper(:obsoleteStatus))")
    int markObsoleteMissing(@Param("ids") Collection<Integer> ids, @Param("obsoleteStatus") String obsoleteStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Oddzial o where o.id not in :ids")
    int deleteMissing(@Param("ids") Collection<Integer> ids);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Oddzial o set o.statusNaStronie = :obsoleteStatus "
            + "where o.statusNaStronie is null or upper(o.statusNaStronie) <> upper(:obsoleteStatus)")
    int markAllObsolete(@Param("obsoleteStatus") String obsoleteStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Oddzial o")
    int deleteAllRows();
}
