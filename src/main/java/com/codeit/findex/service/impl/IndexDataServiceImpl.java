package com.codeit.findex.service.impl;

import com.codeit.findex.dto.indexdata.ChartDataPoint;
import com.codeit.findex.dto.indexdata.IndexChartDto;
import com.codeit.findex.dto.indexdata.IndexDataCreateRequest;
import com.codeit.findex.dto.indexdata.IndexDataResponse;
import com.codeit.findex.dto.indexdata.IndexDataSearchRequest;
import com.codeit.findex.dto.indexdata.IndexDataUpdateRequest;
import com.codeit.findex.dto.indexdata.IndexPerformanceDto;
import com.codeit.findex.dto.indexdata.RankedIndexPerformanceDto;
import com.codeit.findex.entity.IndexData;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.entity.PeriodType;
import com.codeit.findex.repository.IndexDataRepository;
import com.codeit.findex.repository.IndexInfoRepository;
import com.codeit.findex.service.IndexDataService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.codeit.findex.exception.DuplicateException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndexDataServiceImpl implements IndexDataService {

  private final IndexDataRepository indexDataRepository;
  private final IndexInfoRepository indexInfoRepository;

  private record PerformanceContext(
      List<IndexData> currentDataList,
      Map<UUID, IndexData> beforeMap
  ) {}

  @Override
  @Transactional
  public IndexDataResponse create(IndexDataCreateRequest request) {
    IndexInfo indexInfo = indexInfoRepository.findById(request.indexInfoId())
        .orElseThrow(() -> new EntityNotFoundException("IndexInfo not found"));

    if (indexDataRepository.existsByIndexInfoIdAndBaseDate(
        request.indexInfoId(), request.baseDate())) {
      throw new DuplicateException("이미 등록된 날짜의 데이터입니다.");
    }

    IndexData indexData = IndexData.builder()
        .indexInfo(indexInfo)
        .baseDate(request.baseDate())
        .marketPrice(request.marketPrice())
        .closingPrice(request.closingPrice())
        .highPrice(request.highPrice())
        .lowPrice(request.lowPrice())
        .versus(request.versus())
        .fluctuationRate(request.fluctuationRate())
        .tradingQuantity(request.tradingQuantity())
        .tradingPrice(request.tradingPrice())
        .marketTotalAmount(request.marketTotalAmount())
        .build();

    return IndexDataResponse.from(indexDataRepository.save(indexData));
  }

  @Override
  @Transactional
  public IndexDataResponse update(UUID id, IndexDataUpdateRequest request) {
    IndexData indexData = indexDataRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("IndexData not found"));

    indexData.update(
        request.marketPrice(),
        request.closingPrice(),
        request.highPrice(),
        request.lowPrice(),
        request.versus(),
        request.fluctuationRate(),
        request.tradingQuantity(),
        request.tradingPrice(),
        request.marketTotalAmount()
    );

    return IndexDataResponse.from(indexData);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    IndexData indexData = indexDataRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("IndexData not found"));
    indexDataRepository.delete(indexData);
  }

  @Override
  public Page<IndexDataResponse> search(IndexDataSearchRequest request) {
    Pageable pageable = PageRequest.of(request.page(), request.size());
    return indexDataRepository.search(request, pageable)
        .map(IndexDataResponse::from);
  }

  @Override
  public List<IndexData> findAllForExport(IndexDataSearchRequest request) {
//    return indexDataRepository.findAllForExport(request);
    return new ArrayList<>();
  }

  @Override
  public IndexChartDto getChartData(UUID id, PeriodType periodType) {
    IndexInfo indexInfo = indexInfoRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("지수 정보가 존재하지 않습니다."));

    LocalDate endDate = LocalDate.now();
    LocalDate targetStartDate = resolveTargetDate(endDate, periodType);

    int fetchDays = (int) indexDataRepository.countByPeriod(id, targetStartDate, endDate);
    if (fetchDays == 0) {
      fetchDays = 22;
    }

    List<IndexData> rawData = fetchRawData(id, fetchDays, endDate);

    List<ChartDataPoint> dataPoints = new ArrayList<>();
    List<ChartDataPoint> ma5DataPoints = new ArrayList<>();
    List<ChartDataPoint> ma20DataPoints = new ArrayList<>();

    int totalSize = rawData.size();

    int startIndexToCollect = Math.max(0, totalSize - fetchDays);

    BigDecimal sum5 = BigDecimal.ZERO;
    BigDecimal sum20 = BigDecimal.ZERO;

    BigDecimal period5 = BigDecimal.valueOf(5);
    BigDecimal period20 = BigDecimal.valueOf(20);

    for (int i = 0; i < totalSize; i++) {
      IndexData current = rawData.get(i);
      BigDecimal currentPrice = current.getClosingPrice() != null ? current.getClosingPrice() : BigDecimal.ZERO;

      sum5 = sum5.add(currentPrice);
      if (i >= 5) {
        BigDecimal out5Price = rawData.get(i - 5).getClosingPrice() != null ? rawData.get(i - 5).getClosingPrice() : BigDecimal.ZERO;
        sum5 = sum5.subtract(out5Price);
      }

      sum20 = sum20.add(currentPrice);
      if (i >= 20) {
        BigDecimal out20Price = rawData.get(i - 20).getClosingPrice() != null ? rawData.get(i - 20).getClosingPrice() : BigDecimal.ZERO;
        sum20 = sum20.subtract(out20Price);
      }

      if (i >= startIndexToCollect) {
        String dateStr = current.getBaseDate().toString();

        if (current.getClosingPrice() != null) {
          dataPoints.add(new ChartDataPoint(dateStr, currentPrice.doubleValue()));
        }

        if (i >= 4) {
          BigDecimal avg5 = sum5.divide(period5, 2, RoundingMode.HALF_UP);
          ma5DataPoints.add(new ChartDataPoint(dateStr, avg5.doubleValue()));
        }

        if (i >= 19) {
          BigDecimal avg20 = sum20.divide(period20, 2, RoundingMode.HALF_UP);
          ma20DataPoints.add(new ChartDataPoint(dateStr, avg20.doubleValue()));
        }
      }
    }

    Collections.reverse(dataPoints);
    Collections.reverse(ma5DataPoints);
    Collections.reverse(ma20DataPoints);

    return new IndexChartDto(
        indexInfo.getId(), indexInfo.getIndexClassification(), indexInfo.getIndexName(),
        periodType.name(), dataPoints, ma5DataPoints, ma20DataPoints
    );
  }

  @Override
  public List<RankedIndexPerformanceDto> getPerformanceRanking(UUID indexInfoId, PeriodType periodType, int limit) {
    IndexDataServiceImpl.PerformanceContext context = preparePerformanceContext(periodType);

    List<IndexPerformanceDto> performances = new ArrayList<>();

    for (IndexData current : context.currentDataList()) {
      if (indexInfoId != null && !current.getIndexInfo().getId().equals(indexInfoId)) {
        continue;
      }

      IndexData before = context.beforeMap().get(current.getIndexInfo().getId());

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
    PerformanceContext context = preparePerformanceContext(periodType);

    List<IndexData> curDataList = indexDataRepository.findByFavoriteWithLatestDataSafe();

    return curDataList.stream()
        .map(current -> {
          IndexData before = context.beforeMap().get(current.getIndexInfo().getId());
          return convertToPerformanceDto(current, before);
        })
        .sorted(Comparator.comparing(
            IndexPerformanceDto::fluctuationRate,
            Comparator.nullsLast(Comparator.reverseOrder())
        ))
        .toList();
  }

  private PerformanceContext preparePerformanceContext(PeriodType periodType) {
    LocalDate realToday = LocalDate.now();

    LocalDate actualToday = indexDataRepository.findLatestAvailableDate(realToday)
        .orElseThrow(() -> new IllegalArgumentException("현재 기준 조회 가능한 데이터가 존재하지 않습니다."));

    LocalDate targetDate = resolveTargetDate(actualToday, periodType);

    LocalDate actualBeforeDate = indexDataRepository.findOldestAvailableDate(targetDate)
        .orElseThrow(() -> new IllegalArgumentException("비교할 과거 데이터가 존재하지 않습니다."));

    List<IndexData> currentDataList = indexDataRepository.findByBaseDateWithIndexInfo(actualToday);
    List<IndexData> beforeDataList = indexDataRepository.findByBaseDateWithIndexInfo(actualBeforeDate);

    Map<UUID, IndexData> beforeMap = beforeDataList.stream()
        .collect(Collectors.toMap(d -> d.getIndexInfo().getId(), d -> d));

    return new PerformanceContext(currentDataList, beforeMap);
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
      case QUARTERLY -> today.minusMonths(3);
      case YEARLY -> today.minusYears(1);
    };
  }

  private List<IndexData> fetchRawData(UUID id, int fetchDays, LocalDate endDate) {
    LocalDate startDate = endDate.minusDays(fetchDays + 70);
    return indexDataRepository.findChartRawData(id, startDate, endDate);
  }
}