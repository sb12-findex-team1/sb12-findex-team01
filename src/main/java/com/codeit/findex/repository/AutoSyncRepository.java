package com.codeit.findex.repository;

import com.codeit.findex.entity.AutoSync;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AutoSyncRepository extends JpaRepository<AutoSync, UUID> {

  @Query("""
      select a
      from AutoSync a
      join fetch a.indexInfo i
      where (:indexInfoId is null or i.id = :indexInfoId)
        and (:enabled is null or a.enabled = :enabled)
        and (:idAfter is null or a.id > :idAfter)
      """)
  List<AutoSync> findAutoSyncList(
      @Param("indexInfoId") UUID indexInfoId,
      @Param("enabled") Boolean enabled,
      @Param("idAfter") UUID idAfter,
      Pageable pageable
  );

  @Query("""
      select count(a)
      from AutoSync a
      join a.indexInfo i
      where (:indexInfoId is null or i.id = :indexInfoId)
        and (:enabled is null or a.enabled = :enabled)
      """)
  int countAutoSyncList(
      @Param("indexInfoId") UUID indexInfoId,
      @Param("enabled") Boolean enabled
  );

}
