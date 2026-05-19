package com.codeit.findex.service.impl;

import com.codeit.findex.client.IndexApiClient;
import com.codeit.findex.dto.indexdata.IndexDataSyncRequest;
import com.codeit.findex.dto.client.StockMarketIndexRequest;
import com.codeit.findex.dto.client.StockMarketIndexResponse;
import com.codeit.findex.dto.client.StockMarketIndexResponse.Item;
import com.codeit.findex.dto.syncjob.SyncJobDto;
import com.codeit.findex.entity.IndexData;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.entity.JobType;
import com.codeit.findex.entity.Result;
import com.codeit.findex.entity.SourceType;
import com.codeit.findex.repository.IndexDataRepository;
import com.codeit.findex.repository.IndexInfoRepository;
import com.codeit.findex.service.ClientIndexSyncService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ClientIndexSyncServiceImpl implements ClientIndexSyncService {

  private final IndexApiClient indexApiClient;
  private final IndexInfoRepository indexInfoRepository;
  private final IndexDataRepository indexDataRepository;

  @Transactional
  @Override
  public List<SyncJobDto> syncIndexInfo(StockMarketIndexRequest request, SourceType sourceType, String ip) {
    StockMarketIndexResponse response = indexApiClient.getStockMarketIndex(request);
    List<Item> items = response.response().body().items().item();
//    방어검증 추가?

    List<IndexInfo> savedIndexInfoList = saveIndexInfos(items, sourceType);

    return savedIndexInfoList.stream()
        .map(indexInfo -> createSyncJobDto(
            indexInfo.getId(),
            JobType.INDEX_INFO,
            indexInfo,
            LocalDate.now(),
            ip,
            Result.SUCCESS
        ))
        .toList();
  }

//  TODO: 현재 api요청을 반복문으로 일단 처리.(name, icf)의 세트 검증으로, 최적화는 PR하고
  @Override
  public List<SyncJobDto> syncIndexData(IndexDataSyncRequest request, String ip) {
    validate(request);

    List<IndexInfo> indexInfos = getTargetIndexInfos(request);
    List<SyncJobDto> syncJobDtoList = new ArrayList<>();

//    result를 활용하기 위해 transactional 제거 후 try-catch 사용
    for (IndexInfo indexInfo : indexInfos) {
      try {
        StockMarketIndexRequest stockMarketIndexRequest = createIndexDataRequest(request, indexInfo);
        StockMarketIndexResponse response = indexApiClient.getStockMarketIndex(stockMarketIndexRequest);
        List<Item> items = response.response().body().items().item();

        List<IndexData> indexDataList = updateOrInsertIndexData(items, indexInfo);

        for (IndexData indexData : indexDataList) {
          SyncJobDto syncJobDto = createSyncJobDto(
              indexData.getId(),
              JobType.INDEX_DATA,
              indexInfo,
              indexData.getBaseDate(),
              ip,
              Result.SUCCESS
          );
          syncJobDtoList.add(syncJobDto);
        }
      } catch (Exception e) {
        syncJobDtoList.addAll(createFailedSyncJobDtos(indexInfo, request, ip));
      }
    }

    return syncJobDtoList;
  }

  private void validate(IndexDataSyncRequest request) {
    if (request == null
        || request.baseDateFrom() == null
        || request.baseDateTo() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.");
    }

    if (request.baseDateFrom().isAfter(request.baseDateTo())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 날짜 범위입니다.");
    }
  }

//  프론트에서 전달할 경우 분류를 한 경우만 보낸다.
//  그리고 OPEN_API만 자동 업데이트.
  private List<IndexInfo> getTargetIndexInfos(IndexDataSyncRequest request) {
    List<IndexInfo> indexInfos;

    if (request.indexInfoIds() == null || request.indexInfoIds().isEmpty()) {
      indexInfos = indexInfoRepository.findAll();
    } else {
      indexInfos = indexInfoRepository.findAllById(request.indexInfoIds());
    }

    return indexInfos.stream()
        .filter(indexInfo -> SourceType.OPEN_API.name().equals(indexInfo.getSourceType()))
        .toList();
  }

  private List<IndexInfo> saveIndexInfos(List<Item> items, SourceType sourceType) {
    Map<String, Item> uniqueItems = new LinkedHashMap<>();

    for (Item item : items) {
      uniqueItems.putIfAbsent(createIndexInfoKey(item.idxCsf(), item.idxNm()), item);
    }

    Map<String, IndexInfo> existingIndexInfos = findExistingIndexInfos(
        new ArrayList<>(uniqueItems.values())
    );
    List<IndexInfo> result = new ArrayList<>();
    List<IndexInfo> newIndexInfos = new ArrayList<>();

    for (Item item : uniqueItems.values()) {
      String key = createIndexInfoKey(item.idxCsf(), item.idxNm());
      IndexInfo indexInfo = existingIndexInfos.get(key);

      if (indexInfo != null) {
        if (!SourceType.OPEN_API.name().equals(indexInfo.getSourceType())) {
          continue;
        }

        indexInfo.updateByOpenApi(
            Integer.parseInt(item.epyItmsCnt()),
            LocalDate.parse(item.basPntm(), DateTimeFormatter.BASIC_ISO_DATE),
            new BigDecimal(item.basIdx())
        );
        result.add(indexInfo);
      } else {
        IndexInfo newIndexInfo = IndexInfo.builder()
            .indexName(item.idxNm())
            .indexClassification(item.idxCsf())
            .sourceType(String.valueOf(sourceType))
            .employedItemsCount(Integer.parseInt(item.epyItmsCnt()))
            .baseIndex(new BigDecimal(item.basIdx()))
            .basePointInTime(LocalDate.parse(item.basPntm(), DateTimeFormatter.BASIC_ISO_DATE))
            .favorite(false)
            .build();

        newIndexInfos.add(newIndexInfo);
        result.add(newIndexInfo);
      }
    }

    if (!newIndexInfos.isEmpty()) {
      indexInfoRepository.saveAll(newIndexInfos);
    }

    return result;
  }

  private Map<String, IndexInfo> findExistingIndexInfos(List<Item> items) {
    if (items.isEmpty()) {
      return Map.of();
    }

    List<String> indexClassifications = items.stream()
        .map(Item::idxCsf)
        .distinct()
        .toList();

    List<String> indexNames = items.stream()
        .map(Item::idxNm)
        .distinct()
        .toList();

    List<IndexInfo> existingIndexInfos = indexInfoRepository
        .findByIndexClassificationInAndIndexNameIn(indexClassifications, indexNames);

    Map<String, IndexInfo> indexInfoMap = new HashMap<>();
    for (IndexInfo indexInfo : existingIndexInfos) {
      indexInfoMap.put(
          createIndexInfoKey(indexInfo.getIndexClassification(), indexInfo.getIndexName()),
          indexInfo
      );
    }

    return indexInfoMap;
  }

  private String createIndexInfoKey(String indexClassification, String indexName) {
    return indexClassification + "|" + indexName;
  }

//  N+1
  private List<IndexData> updateOrInsertIndexData(List<Item> items, IndexInfo indexInfo) {
    Map<LocalDate, IndexData> indexDataMap = new LinkedHashMap<>();

    for (Item item : items) {
      if (!matchesIndex(item, indexInfo)) {
        continue;
      }

      LocalDate baseDate = LocalDate.parse(item.basDt(), DateTimeFormatter.BASIC_ISO_DATE);
      IndexData indexData = indexDataRepository
          .findByIndexInfoAndBaseDate(indexInfo, baseDate)
          .orElseGet(() -> toIndexData(item, indexInfo));

      indexData.update(
          new BigDecimal(item.mkp()),
          new BigDecimal(item.clpr()),
          new BigDecimal(item.hipr()),
          new BigDecimal(item.lopr()),
          new BigDecimal(item.vs()),
          new BigDecimal(item.fltRt()),
          Long.parseLong(item.trqu()),
          Long.parseLong(item.trPrc()),
          Long.parseLong(item.lstgMrktTotAmt())
      );

      indexDataMap.put(baseDate, indexData);
    }

    return indexDataRepository.saveAll(indexDataMap.values());
  }

  private boolean matchesIndex(Item item, IndexInfo indexInfo) {
    return indexInfo.getIndexName().equals(item.idxNm())
        && indexInfo.getIndexClassification().equals(item.idxCsf());
  }

  private IndexData toIndexData(Item item, IndexInfo indexInfo) {
    return IndexData.builder()
        .indexInfo(indexInfo)
        .baseDate(LocalDate.parse(item.basDt(), DateTimeFormatter.BASIC_ISO_DATE))
        .marketPrice(new BigDecimal(item.mkp()))
        .closingPrice(new BigDecimal(item.clpr()))
        .highPrice(new BigDecimal(item.hipr()))
        .lowPrice(new BigDecimal(item.lopr()))
        .versus(new BigDecimal(item.vs()))
        .fluctuationRate(new BigDecimal(item.fltRt()))
        .tradingQuantity(Long.parseLong(item.trqu()))
        .tradingPrice(Long.parseLong(item.trPrc()))
        .marketTotalAmount(Long.parseLong(item.lstgMrktTotAmt()))
        .build();
  }

  private SyncJobDto createSyncJobDto(
      UUID id,
      JobType jobType,
      IndexInfo indexInfo,
      LocalDate targetDate,
      String worker,
      Result result
  ) {
    return new SyncJobDto(
        id,
        jobType,
        indexInfo.getId(),
        targetDate,
        worker,
        Instant.now(),
        result
    );
  }

  private List<SyncJobDto> createFailedSyncJobDtos(
      IndexInfo indexInfo,
      IndexDataSyncRequest request,
      String ip
  ) {
    List<SyncJobDto> syncJobDtos = new ArrayList<>();
    LocalDate date = request.baseDateFrom();

    while (!date.isAfter(request.baseDateTo())) {
      syncJobDtos.add(createSyncJobDto(
          null,
          JobType.INDEX_DATA,
          indexInfo,
          date,
          ip,
          Result.FAILED
      ));
      date = date.plusDays(1);
    }

    return syncJobDtos;
  }

  private StockMarketIndexRequest createIndexDataRequest(
      IndexDataSyncRequest request,
      IndexInfo indexInfo
  ) {
    return new StockMarketIndexRequest(
        null,
        null,
        null,
        request.baseDateFrom().format(DateTimeFormatter.BASIC_ISO_DATE),
        request.baseDateTo().format(DateTimeFormatter.BASIC_ISO_DATE),
        null,
        indexInfo.getIndexName(),
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );
  }
}
