package com.codeit.findex.repository;

import com.codeit.findex.dto.indexdata.IndexDataSearchRequest;
import com.codeit.findex.entity.IndexData;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IndexDataRepositoryCustom {
  Page<IndexData> search(IndexDataSearchRequest request, Pageable pageable);
  List<IndexData> findAllForExport(IndexDataSearchRequest request);
}


