package com.codeit.findex.repository.querydsl;

import com.codeit.findex.entity.IndexInfo;
import java.util.List;
import java.util.UUID;

public interface IndexInfoQueryRepository {

  List<IndexInfo> findIndexInfoList(
      String indexClassification,
      String indexName,
      Boolean favorite,
      String sortField,
      String sortDirection,
      String sortValue,
      UUID idAfter,
      int limit
  );
}