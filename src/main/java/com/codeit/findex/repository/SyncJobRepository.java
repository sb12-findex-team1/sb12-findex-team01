package com.codeit.findex.repository;

import com.codeit.findex.entity.JobType;
import com.codeit.findex.entity.Result;
import com.codeit.findex.entity.SyncJob;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SyncJobRepository extends JpaRepository<SyncJob, UUID> {

  @Query("""
      select s
      from SyncJob s
      left join fetch s.indexInfo i
      """)
  List<SyncJob> findSyncJobList(Pageable pageable);

  @Query("""
      select count(s)
      from SyncJob s
      """)
  int countSyncJobList();
}
