package com.codeit.findex.service.impl;

import com.codeit.findex.dto.indexdata.IndexChartDto;
import com.codeit.findex.dto.indexdata.IndexPerformanceDto;
import com.codeit.findex.dto.indexdata.RankedIndexPerformanceDto;
import com.codeit.findex.entity.PeriodType;
import com.codeit.findex.repository.IndexDataRepository;
import com.codeit.findex.service.IndexDataService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IndexDataServiceImpl implements IndexDataService { // ⭐️ 인터페이스 구현 선언

  private final IndexDataRepository indexDataRepository;

  @Override
  public IndexChartDto getChartData(UUID id, PeriodType periodType) {
    return null;
  }

  @Override
  public List<RankedIndexPerformanceDto> getPerformanceRanking(UUID indexInfoId, PeriodType periodType, int limit) {
    return new ArrayList<>();
  }

  @Override
  public List<IndexPerformanceDto> getPerformanceFavorite(PeriodType periodType) {
    return new ArrayList<>();
  }
}