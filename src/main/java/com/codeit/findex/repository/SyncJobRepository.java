package com.codeit.findex.repository;

import com.codeit.findex.entity.Result;
import com.codeit.findex.entity.SyncJob;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncJobRepository extends JpaRepository<SyncJob, UUID> {
  long countByResultAndJobTimeAfter (
      Result result,
      Instant jobTime
  );

  Optional<SyncJob> findTopByOrderByJobTimeDesc();
}
