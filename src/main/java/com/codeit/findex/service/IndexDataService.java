package com.codeit.findex.service;

import com.codeit.findex.dto.data.IndexChartDto;
import com.codeit.findex.dto.data.IndexPerformanceDto;
import com.codeit.findex.dto.data.RankedIndexPerformanceDto;
import com.codeit.findex.entity.PeriodType;
import java.util.List;
import java.util.UUID;

public interface IndexDataService {

  IndexChartDto getChartData(UUID id, PeriodType periodType);

  List<RankedIndexPerformanceDto> getPerformanceRanking(UUID indexInfoId, PeriodType periodType, int limit);

  List<IndexPerformanceDto> getPerformanceFavorite(PeriodType periodType);
}