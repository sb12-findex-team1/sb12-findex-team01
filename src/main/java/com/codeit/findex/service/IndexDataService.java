package com.codeit.findex.service;

import com.codeit.findex.dto.indexdata.IndexDataCreateRequest;
import com.codeit.findex.dto.indexdata.IndexDataResponse;
import com.codeit.findex.dto.indexdata.IndexDataSearchRequest;
import com.codeit.findex.dto.indexdata.IndexDataUpdateRequest;
import com.codeit.findex.dto.indexdata.IndexPerformanceDto;
import com.codeit.findex.entity.IndexData;
import com.codeit.findex.entity.PeriodType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;


public interface IndexDataService {

  IndexDataResponse create(IndexDataCreateRequest request);
  IndexDataResponse update(UUID id, IndexDataUpdateRequest request);
  void delete(UUID id);
  Page<IndexDataResponse> search(IndexDataSearchRequest request);
  List<IndexData> findAllForExport(IndexDataSearchRequest request);
  List<IndexPerformanceDto> getPerformanceFavorite(PeriodType periodType);
}
