package com.codeit.findex.service.impl;

import com.codeit.findex.dto.indexdata.ChartDataPoint;
import com.codeit.findex.dto.indexdata.IndexChartDto;
import com.codeit.findex.dto.indexdata.IndexPerformanceDto;
import com.codeit.findex.dto.indexdata.RankedIndexPerformanceDto;
import com.codeit.findex.entity.IndexData;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.entity.PeriodType;
import com.codeit.findex.repository.IndexDataRepository;
import com.codeit.findex.repository.IndexInfoRepository;
import com.codeit.findex.service.IndexDataService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndexDataServiceImpl implements IndexDataService {

  private final IndexDataRepository indexDataRepository;
  private final IndexInfoRepository indexInfoRepository;

  @Override
  public IndexChartDto getChartData(UUID id, PeriodType periodType) {
    IndexInfo indexInfo = indexInfoRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("지수 정보가 존재하지 않습니다."));

    int fetchDays = resolveFetchDays(periodType);
    List<IndexData> rawData = fetchRawData(id, fetchDays);

    List<ChartDataPoint> dataPoints = new ArrayList<>();
    List<ChartDataPoint> ma5DataPoints = new ArrayList<>();
    List<ChartDataPoint> ma20DataPoints = new ArrayList<>();

    int limit = Math.min(fetchDays, rawData.size());

    for (int i = limit - 1; i >= 0; i--) {
      IndexData current = rawData.get(i);
      String dateStr = current.getBaseDate().toString();

      if (current.getClosingPrice() != null) {
        dataPoints.add(new ChartDataPoint(dateStr, current.getClosingPrice().doubleValue()));
      }

      if (i + 5 <= rawData.size()) {
        ma5DataPoints.add(new ChartDataPoint(dateStr, calculateMovingAverage(rawData, i, 5)));
      }

      if (i + 20 <= rawData.size()) {
        ma20DataPoints.add(new ChartDataPoint(dateStr, calculateMovingAverage(rawData, i, 20)));
      }
    }

    return new IndexChartDto(
        indexInfo.getId(),
        indexInfo.getIndexClassification(),
        indexInfo.getIndexName(),
        periodType.name(),
        dataPoints,
        ma5DataPoints,
        ma20DataPoints
    );
  }

  @Override
  public List<RankedIndexPerformanceDto> getPerformanceRanking(UUID indexInfoId, PeriodType periodType, int limit) {
    LocalDate today = LocalDate.now();
    LocalDate targetDate = resolveTargetDate(today, periodType);

    LocalDate actualBeforeDate = indexDataRepository.findLatestAvailableDate(targetDate)
        .orElseThrow(() -> new IllegalArgumentException("비교할 과거 데이터가 존재하지 않습니다."));

    List<IndexData> currentDataList = indexDataRepository.findByBaseDateWithIndexInfo(today);
    List<IndexData> beforeDataList = indexDataRepository.findByBaseDateWithIndexInfo(actualBeforeDate);

    Map<UUID, IndexData> beforeMap = beforeDataList.stream()
        .collect(Collectors.toMap(d -> d.getIndexInfo().getId(), d -> d));

    List<IndexPerformanceDto> performances = new ArrayList<>();

    for (IndexData current : currentDataList) {
      IndexData before = beforeMap.get(current.getIndexInfo().getId());

      if (before != null && current.getClosingPrice() != null) {
        performances.add(convertToPerformanceDto(current, before));
      }
    }

    performances.sort((p1, p2) -> Double.compare(p2.fluctuationRate(), p1.fluctuationRate()));

    List<RankedIndexPerformanceDto> rankedResults = new ArrayList<>();
    for (int i = 0; i < Math.min(performances.size(), limit); i++) {
      rankedResults.add(new RankedIndexPerformanceDto(performances.get(i), i + 1));
    }

    return rankedResults;
  }

  @Override
  public List<IndexPerformanceDto> getPerformanceFavorite(PeriodType periodType) {
    LocalDate today = LocalDate.now();
    LocalDate targetDate = resolveTargetDate(today, periodType);

    LocalDate actualBeforeDate = indexDataRepository.findLatestAvailableDate(targetDate)
        .orElseThrow(() -> new IllegalArgumentException("비교할 과거 데이터가 존재하지 않습니다."));

    List<IndexData> currentDataList = indexDataRepository.findByBaseDateWithIndexInfo(today);
    List<IndexData> beforeDataList = indexDataRepository.findByBaseDateWithIndexInfo(actualBeforeDate);

    Map<UUID, IndexData> beforeMap = beforeDataList.stream()
        .collect(Collectors.toMap(d -> d.getIndexInfo().getId(), d -> d));

    return currentDataList.stream()
        .filter(d -> d.getIndexInfo() != null && d.getIndexInfo().isFavorite())
        .map(current -> {
          IndexData before = beforeMap.get(current.getIndexInfo().getId());
          return convertToPerformanceDto(current, before);
        })
        .toList();
  }

  private IndexPerformanceDto convertToPerformanceDto(IndexData current, IndexData before) {
    BigDecimal currentPrice = current.getClosingPrice() != null ? current.getClosingPrice() : BigDecimal.ZERO;
    BigDecimal beforePrice = (before != null && before.getClosingPrice() != null) ? before.getClosingPrice() : currentPrice;

    BigDecimal versus = currentPrice.subtract(beforePrice);

    BigDecimal fluctuationRate = BigDecimal.ZERO;
    if (beforePrice.compareTo(BigDecimal.ZERO) != 0) {
      fluctuationRate = versus.divide(beforePrice, 4, RoundingMode.HALF_UP)
          .multiply(BigDecimal.valueOf(100));
    }

    return new IndexPerformanceDto(
        current.getIndexInfo().getId(),
        current.getIndexInfo().getIndexClassification(),
        current.getIndexInfo().getIndexName(),
        versus.doubleValue(),
        fluctuationRate.doubleValue(),
        currentPrice.doubleValue(),
        beforePrice.doubleValue()
    );
  }

  private LocalDate resolveTargetDate(LocalDate today, PeriodType periodType) {
    return switch (periodType) {
      case DAILY -> today.minusDays(1);
      case WEEKLY -> today.minusWeeks(1);
      case MONTHLY -> today.minusMonths(1);
    };
  }

  // 지금은 평균 평일 일수로 계산하였지만 추후 요구사항에 맞추어 수정예정
  private int resolveFetchDays(PeriodType periodType) {
    return switch (periodType) {
      case DAILY -> 1;
      case WEEKLY -> 7;
      case MONTHLY -> 22;
    };
  }

  private List<IndexData> fetchRawData(UUID id, int fetchDays) {
    LocalDate endDate = LocalDate.now();
    LocalDate startDate = endDate.minusDays(fetchDays + 30);
    return indexDataRepository.findChartRawData(id, startDate, endDate);
  }

  private double calculateMovingAverage(List<IndexData> rawData, int startIndex, int period) {
    return rawData.subList(startIndex, startIndex + period).stream()
        .map(IndexData::getClosingPrice)
        .filter(Objects::nonNull)
        .mapToDouble(BigDecimal::doubleValue)
        .summaryStatistics().getAverage();
  }
}