package com.codeit.findex.service.impl;


import com.codeit.findex.dto.indexinfo.IndexInfoCreateRequest;
import com.codeit.findex.dto.indexinfo.IndexInfoResponse;

import com.codeit.findex.dto.indexinfo.IndexInfoUpdateRequest;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.repository.IndexInfoRepository;
import com.codeit.findex.service.IndexInfoService;

import java.util.UUID;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor

public class IndexInfoServiceImpl implements IndexInfoService {


  private final IndexInfoRepository indexInfoRepository;


  @Transactional
  @Override
  public IndexInfoResponse create(IndexInfoCreateRequest request) {
    if (indexInfoRepository.existsByIndexClassificationAndIndexName(
        request.indexClassification(), request.indexName())) {
      throw new IllegalArgumentException("이미 존재하는 지수 정보입니다.");
    }

    IndexInfo indexInfo = IndexInfo.builder()
        .indexClassification(request.indexClassification())
        .indexName(request.indexName())
        .employedItemsCount(request.employedItemsCount())
        .basePointInTime(request.basePointInTime())
        .baseIndex(request.baseIndex())
        .favorite(request.favorite())
        .sourceType("USER")
        .build();

    return toResponse(indexInfoRepository.save(indexInfo));
  }

  @Transactional
  @Override
  public IndexInfoResponse update(UUID id, IndexInfoUpdateRequest request) {
    IndexInfo indexInfo = indexInfoRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지수 정보입니다."));
    indexInfo.update(request);
    return toResponse(indexInfo);
  }

  @Transactional
  @Override
  public void delete(UUID id) {
    IndexInfo indexInfo = indexInfoRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지수 정보입니다."));
    indexInfoRepository.delete(indexInfo);
  }

  @Transactional(readOnly = true)
  @Override
  public IndexInfoResponse findById(UUID id) {
    IndexInfo indexInfo = indexInfoRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지수 정보입니다."));
    return toResponse(indexInfo);
  }

  private IndexInfoResponse toResponse(IndexInfo indexInfo) {
    return new IndexInfoResponse(
        indexInfo.getId(),
        indexInfo.getIndexClassification(),
        indexInfo.getIndexName(),
        indexInfo.getEmployedItemsCount(),
        indexInfo.getBasePointInTime(),
        indexInfo.getBaseIndex(),
        indexInfo.getSourceType(),
        indexInfo.isFavorite(),
        indexInfo.getCreatedAt(),
        indexInfo.getUpdatedAt()
    );
  }
}
