package com.codeit.findex.repository;

import com.codeit.findex.entity.Result;
import com.codeit.findex.entity.SyncJob;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SyncJobRepository extends JpaRepository<SyncJob, UUID> {
  long countByResultAndJobTimeAfter (
      Result result,
      Instant jobTime
  );

  Optional<SyncJob> findTopByOrderByJobTimeDesc();

  @Query("""
      select max(s.targetDate)
      from SyncJob s
      where s.indexInfo.id = :indexInfoId
        and s.jobType = 'INDEX_DATA'
        and s.result = 'SUCCESS'
      """)
  Optional<LocalDate> findLastSuccessfulAutoSyncDate(
      @Param("indexInfoId") UUID indexInfoId
  );

}
