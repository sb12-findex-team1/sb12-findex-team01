package com.codeit.findex.repository;

import com.codeit.findex.entity.AutoSync;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutoSyncRepository extends JpaRepository<AutoSync, UUID> {

}
