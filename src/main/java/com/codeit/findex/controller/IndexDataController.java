package com.codeit.findex.controller;

import com.codeit.findex.dto.data.IndexChartDto;
import com.codeit.findex.dto.data.IndexPerformanceDto;
import com.codeit.findex.dto.data.RankedIndexPerformanceDto;
import com.codeit.findex.entity.PeriodType;
import com.codeit.findex.service.IndexDataService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/index-data")
public class IndexDataController {
  private final IndexDataService indexDataService;

  // 지수 차트 조회
  @GetMapping("{id}/chart")
  public ResponseEntity<IndexChartDto> getIndexChart(
      @PathVariable UUID id,
      @RequestParam(defaultValue = "DAILY") PeriodType periodType
  ) {
    IndexChartDto response = indexDataService.getChartData(id, periodType);
    return ResponseEntity.ok(response);
  }

  // 지수 성과 랭킹 조회
  @GetMapping("performance/rank")
  public ResponseEntity<List<RankedIndexPerformanceDto>> getPerformanceRank(
      @RequestParam UUID indexInfoId,
      @RequestParam(defaultValue = "DAILY") PeriodType periodType,
      @RequestParam(defaultValue = "10") Integer limit
  ) {
    List<RankedIndexPerformanceDto> response =
        indexDataService.getPerformanceRanking(indexInfoId, periodType, limit);
    return ResponseEntity.ok(response);
  }

  // 관심 지수 성과 조회
  @GetMapping("performance/favorite")
  public ResponseEntity<List<IndexPerformanceDto>> getPerformanceFavorite(
      @RequestParam(defaultValue = "DAILY") PeriodType periodType
  ) {
    List<IndexPerformanceDto> response =
        indexDataService.getPerformanceFavorite(periodType);
    return ResponseEntity.ok(response);
  }


}
