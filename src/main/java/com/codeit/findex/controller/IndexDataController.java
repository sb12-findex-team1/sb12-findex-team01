package com.codeit.findex.controller;

import com.codeit.findex.csv.IndexDataCsvExporter;
import com.codeit.findex.dto.indexdata.IndexChartDto;
import com.codeit.findex.dto.indexdata.IndexDataCreateRequest;
import com.codeit.findex.dto.indexdata.IndexDataResponse;
import com.codeit.findex.dto.indexdata.IndexDataSearchRequest;
import com.codeit.findex.dto.indexdata.IndexDataUpdateRequest;
import com.codeit.findex.dto.indexdata.IndexPerformanceDto;
import com.codeit.findex.dto.indexdata.RankedIndexPerformanceDto;
import com.codeit.findex.entity.PeriodType;
import com.codeit.findex.service.IndexDataService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/index-data")
public class IndexDataController {
  private final IndexDataService indexDataService;
  private final IndexDataCsvExporter indexDataCsvExporter;

  @PostMapping
  @Operation(summary = "지수 데이터 등록")
  public ResponseEntity<IndexDataResponse> create(
      @RequestBody @Valid IndexDataCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(indexDataService.create(request));
  }
  @PatchMapping("/{id}")
  @Operation(summary = "지수 데이터 수정")
  public ResponseEntity<IndexDataResponse> update(
      @PathVariable UUID id,
      @RequestBody @Valid IndexDataUpdateRequest request) {
    return ResponseEntity.ok(indexDataService.update(id, request));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "지수 데이터 삭제")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    indexDataService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  @Operation(summary = "지수 데이터 목록 조회")
  public ResponseEntity<Page<IndexDataResponse>> search(
      @ModelAttribute IndexDataSearchRequest request) {
    return ResponseEntity.ok(indexDataService.search(request));
  }
  // 지수 차트 조회
  @GetMapping("/{id}/chart")
  public ResponseEntity<IndexChartDto> getIndexChart(
      @PathVariable UUID id,
      @RequestParam(defaultValue = "DAILY") PeriodType periodType
  ) {
    IndexChartDto response = indexDataService.getChartData(id, periodType);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/export/csv")
  @Operation(summary = "지수 데이터 CSV 다운로드")
  public void exportCsv(
      @ModelAttribute IndexDataSearchRequest request,
      HttpServletResponse response) throws IOException {
    indexDataCsvExporter.export(request, response);
  }

  // 지수 성과 랭킹 조회
  @GetMapping("/performance/rank")
  public ResponseEntity<List<RankedIndexPerformanceDto>> getPerformanceRank(
      @RequestParam UUID indexInfoId,
      @RequestParam(defaultValue = "DAILY") PeriodType periodType,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) Integer limit
  ) {
    List<RankedIndexPerformanceDto> response =
        indexDataService.getPerformanceRanking(indexInfoId, periodType, limit);
    return ResponseEntity.ok(response);
  }

  // 관심 지수 성과 조회
  @GetMapping("/performance/favorite")
  public ResponseEntity<List<IndexPerformanceDto>> getPerformanceFavorite(
      @RequestParam(defaultValue = "DAILY") PeriodType periodType) {
    List<IndexPerformanceDto> response =
        indexDataService.getPerformanceFavorite(periodType);
    return ResponseEntity.ok(response);
  }


}
