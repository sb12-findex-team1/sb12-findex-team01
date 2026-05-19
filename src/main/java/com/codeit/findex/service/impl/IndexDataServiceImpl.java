package com.codeit.findex.service.impl;

import com.codeit.findex.dto.indexdata.*;
import com.codeit.findex.entity.IndexData;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.entity.PeriodType;
import com.codeit.findex.exception.DuplicateException;
import com.codeit.findex.repository.IndexDataRepository;
import com.codeit.findex.repository.IndexInfoRepository;
import com.codeit.findex.service.IndexDataService;
import jakarta.persistence.EntityNotFoundException;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndexDataServiceImpl implements IndexDataService {

  private final IndexDataRepository indexDataRepository;
  private final IndexInfoRepository indexInfoRepository;

  @Override
  @Transactional
  public IndexDataResponse create(IndexDataCreateRequest request) {
    IndexInfo indexInfo = indexInfoRepository.findById(request.getIndexInfoId())
        .orElseThrow(() -> new EntityNotFoundException("IndexInfo not found"));

    if (indexDataRepository.existsByIndexInfoIdAndBaseDate(
        request.getIndexInfoId(), request.getBaseDate())) {
      throw new DuplicateException("이미 등록된 날짜의 데이터입니다.");
    }

    IndexData indexData = IndexData.builder()
        .indexInfo(indexInfo)
        .baseDate(request.getBaseDate())
        .openingPrice(request.getOpeningPrice())
        .marketPrice(request.getMarketPrice())
        .closingPrice(request.getClosingPrice())
        .highPrice(request.getHighPrice())
        .lowPrice(request.getLowPrice())
        .versus(request.getVersus())
        .fluctuationRate(request.getFluctuationRate())
        .tradingQuantity(request.getTradingQuantity())
        .tradingPrice(request.getTradingPrice())
        .marketTotalAmount(request.getMarketTotalAmount())
        .build();

    return IndexDataResponse.from(indexDataRepository.save(indexData));
  }

  @Override
  @Transactional
  public IndexDataResponse update(UUID id, IndexDataUpdateRequest request) {
    IndexData indexData = indexDataRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("IndexData not found"));

    indexData.update(
        request.getOpeningPrice(),
        request.getMarketPrice(),
        request.getClosingPrice(),
        request.getHighPrice(),
        request.getLowPrice(),
        request.getVersus(),
        request.getFluctuationRate(),
        request.getTradingQuantity(),
        request.getTradingPrice(),
        request.getMarketTotalAmount()
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
    Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
    return indexDataRepository.search(request, pageable)
        .map(IndexDataResponse::from);
  }

  @Override
  public List<IndexData> findAllForExport(IndexDataSearchRequest request) {
    return indexDataRepository.findAllForExport(request);
  }
  @Override
  public List<IndexPerformanceDto> getPerformanceFavorite(PeriodType periodType) {
    return List.of();
  }


}