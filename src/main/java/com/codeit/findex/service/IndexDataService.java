package com.codeit.findex.service;

import com.codeit.findex.dto.indexdata.IndexChartDto;
import com.codeit.findex.dto.indexdata.IndexDataCreateRequest;
import com.codeit.findex.dto.indexdata.IndexDataResponse;
import com.codeit.findex.dto.indexdata.IndexDataSearchRequest;
import com.codeit.findex.dto.indexdata.IndexDataUpdateRequest;
import com.codeit.findex.dto.indexdata.IndexPerformanceDto;
import com.codeit.findex.dto.indexdata.RankedIndexPerformanceDto;
import com.codeit.findex.entity.IndexData;
import com.codeit.findex.entity.PeriodType;
import java.util.List;
import org.springframework.data.domain.Page;
import java.util.UUID;
import org.springframework.data.domain.Slice;



public interface IndexDataService {

  IndexDataResponse create(IndexDataCreateRequest request);
  IndexDataResponse update(UUID id, IndexDataUpdateRequest request);
  void delete(UUID id);
  List<IndexDataResponse> search(IndexDataSearchRequest request);
  List<IndexData> findAllForExport(IndexDataSearchRequest request);

  IndexChartDto getChartData(UUID id, PeriodType periodType);

  List<RankedIndexPerformanceDto> getPerformanceRanking(UUID indexInfoId, PeriodType periodType,
      int limit);

  List<IndexPerformanceDto> getPerformanceFavorite(PeriodType periodType);

}
