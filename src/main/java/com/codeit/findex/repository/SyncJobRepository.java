package com.codeit.findex.repository;

import com.codeit.findex.entity.SyncJob;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncJobRepository extends JpaRepository<SyncJob, UUID> {

}
