package com.codeit.findex.service.impl;

import com.codeit.findex.dto.indexdata.*;
import com.codeit.findex.entity.IndexData;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.entity.PeriodType;
import com.codeit.findex.repository.IndexDataRepository;
import com.codeit.findex.repository.IndexInfoRepository;
import com.codeit.findex.service.IndexDataService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
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

    // 날짜 계산 로직 생각중입니다 일단은 하드코딩
    int fetchDays = switch (periodType) {
      case DAILY -> 1;
      case WEEKLY -> 7;
      case MONTHLY -> 22;
    };

    LocalDate endDate = LocalDate.now();
    LocalDate startDate = endDate.minusDays(fetchDays + 30);

    List<IndexData> rawData = indexDataRepository.findChartRawData(id, startDate, endDate);

    List<ChartDataPoint> dataPoints = new ArrayList<>();
    List<ChartDataPoint> ma5DataPoints = new ArrayList<>();
    List<ChartDataPoint> ma20DataPoints = new ArrayList<>();

    // 데이터가 요청한 fetchDays보다 적을 수 있으므로 안전장치 마련
    int limit = Math.min(fetchDays, rawData.size());

    // 과거(limit - 1)부터 최신(0)으로 거꾸로 루프를 돕니다
    // 결과 리스트(dataPoints)에는 차트용 ASC(과거->최신) 순서로 쌓입니다
    for (int i = limit - 1; i >= 0; i--) {
      IndexData current = rawData.get(i);
      String dateStr = current.getBaseDate().toString();

      if (current.getClosingPrice() != null) {
        dataPoints.add(new ChartDataPoint(dateStr, current.getClosingPrice().doubleValue()));
      }

      if (i + 5 <= rawData.size()) {
        double avg5 = rawData.subList(i, i + 5).stream()
            .map(IndexData::getClosingPrice)
            .filter(Objects::nonNull)
            .mapToDouble(BigDecimal::doubleValue)
            .summaryStatistics().getAverage();
        ma5DataPoints.add(new ChartDataPoint(dateStr, avg5));
      }

      if (i + 20 <= rawData.size()) {
        double avg20 = rawData.subList(i, i + 20).stream()
            .map(IndexData::getClosingPrice)
            .filter(Objects::nonNull)
            .mapToDouble(BigDecimal::doubleValue)
            .summaryStatistics().getAverage();
        ma20DataPoints.add(new ChartDataPoint(dateStr, avg20));
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
      if (before != null && current.getClosingPrice() != null && before.getClosingPrice() != null) {

        BigDecimal currentPrice = current.getClosingPrice();
        BigDecimal beforePrice = before.getClosingPrice();

        BigDecimal versus = currentPrice.subtract(beforePrice);

        BigDecimal fluctuationRate = BigDecimal.ZERO;
        if (beforePrice.compareTo(BigDecimal.ZERO) != 0) {
          fluctuationRate = versus.divide(beforePrice, 4, RoundingMode.HALF_UP)
              .multiply(BigDecimal.valueOf(100));
        }

        performances.add(new IndexPerformanceDto(
            current.getIndexInfo().getId(),
            current.getIndexInfo().getIndexClassification(),
            current.getIndexInfo().getIndexName(),
            versus.doubleValue(),
            fluctuationRate.doubleValue(),
            currentPrice.doubleValue(),
            beforePrice.doubleValue()
        ));
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
        }).toList();
  }

  private LocalDate resolveTargetDate(LocalDate today, PeriodType periodType) {
    return switch (periodType) {
      case DAILY -> today.minusDays(1);
      case WEEKLY -> today.minusWeeks(1);
      case MONTHLY -> today.minusMonths(1);
      default -> today.minusDays(1);
    };
  }
}