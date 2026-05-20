package com.codeit.findex.repository;

import com.codeit.findex.entity.IndexInfo;
import java.util.Collection;
import java.util.List;
import com.codeit.findex.repository.querydsl.IndexInfoQueryRepository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IndexInfoRepository extends JpaRepository<IndexInfo, UUID>,
    IndexInfoQueryRepository {

  List<IndexInfo> findByIndexClassificationInAndIndexNameIn(
      Collection<String> indexClassifications,
      Collection<String> indexNames
  );
}
  boolean existsByIndexClassificationAndIndexName(String indexClassification, String indexName);

  @Query("""
      select count(i)
      from IndexInfo i
      where (:indexClassification is null or i.indexClassification like %:indexClassification%)
        and (:indexName is null or i.indexName like %:indexName%)
        and (:favorite is null or i.favorite = :favorite)
      """)
  int countIndexInfoList(
      @Param("indexClassification") String indexClassification,
      @Param("indexName") String indexName,
      @Param("favorite") Boolean favorite
  );
}
