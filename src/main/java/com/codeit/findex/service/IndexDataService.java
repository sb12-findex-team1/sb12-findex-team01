package com.codeit.findex.service;


import com.codeit.findex.dto.data.IndexChartDto;
import com.codeit.findex.dto.data.IndexPerformanceDto;
import com.codeit.findex.dto.data.RankedIndexPerformanceDto;
import com.codeit.findex.entity.PeriodType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IndexDataService {
  public IndexChartDto getChartData(UUID id, PeriodType periodType) {
    return null;
  }

  public List<RankedIndexPerformanceDto> getPerformanceRanking(UUID indexInfoId, PeriodType periodType, int limit) {
    return new ArrayList<>();
  }

  public List<IndexPerformanceDto> getPerformanceFavorite(PeriodType periodType) {
    return new ArrayList<>();
  }
}
