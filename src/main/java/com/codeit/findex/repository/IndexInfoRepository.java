package com.codeit.findex.repository;

import com.codeit.findex.entity.IndexInfo;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndexInfoRepository extends JpaRepository<IndexInfo, UUID> {

  List<IndexInfo> findByIndexClassificationInAndIndexNameIn(
      Collection<String> indexClassifications,
      Collection<String> indexNames
  );
}
