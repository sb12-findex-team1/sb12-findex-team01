package com.codeit.findex.service.impl;

import com.codeit.findex.client.IndexApiClient;
import com.codeit.findex.dto.client.StockMarketIndexRequest;
import com.codeit.findex.dto.client.StockMarketIndexResponse;
import com.codeit.findex.dto.client.StockMarketIndexResponse.Item;
import com.codeit.findex.dto.indexdata.IndexDataSyncRequest;
import com.codeit.findex.entity.AutoSync;
import com.codeit.findex.entity.IndexData;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.entity.SourceType;
import com.codeit.findex.repository.AutoSyncRepository;
import com.codeit.findex.repository.IndexDataRepository;
import com.codeit.findex.repository.IndexInfoRepository;
import com.codeit.findex.service.ClientIndexSyncService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
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
  private final AutoSyncRepository autoSyncRepository;

  @Transactional
  @Override
  public List<IndexInfo> syncIndexInfo(
      StockMarketIndexRequest request,
      SourceType sourceType,
      String ip
  ) {
    StockMarketIndexResponse response = indexApiClient.getStockMarketIndex(request);
    validateStockMarketIndexResponse(response);

    List<Item> items = response.response().body().items().item();

    return saveIndexInfos(items, sourceType);
  }

  @Override
  public List<IndexData> syncIndexData(IndexDataSyncRequest request, String ip) {
    validateIndexDataSyncRequest(request);

    List<IndexInfo> indexInfos = getTargetIndexInfos(request);
    if (indexInfos.isEmpty()) {
      return List.of();
    }

    Map<String, IndexInfo> requestIndexInfoMap = new HashMap<>();
    for (IndexInfo indexInfo : indexInfos) {
      requestIndexInfoMap.put(
          createIndexInfoKey(indexInfo.getIndexClassification(), indexInfo.getIndexName()),
          indexInfo
      );
    }

    List<IndexData> existingDataList = indexDataRepository
        .findByIndexInfoIdInAndBaseDateBetween(
            request.indexInfoIds(),
            request.baseDateFrom(),
            request.baseDateTo()
        );
    Map<String, IndexData> existingDataMap = new HashMap<>();
    for (IndexData data : existingDataList) {
      existingDataMap.put(
          createIndexDataKey(data.getIndexInfo().getId(), data.getBaseDate()),
          data
      );
    }

    List<IndexData> result = new ArrayList<>();
    List<IndexData> newIndexDataList = new ArrayList<>();

    int pageNo = 1;
    while (true) {

      StockMarketIndexRequest stockMarketIndexRequest = createIndexDataRequest(request, pageNo);
      StockMarketIndexResponse response = indexApiClient.getStockMarketIndex(
          stockMarketIndexRequest);
      validateStockMarketIndexResponse(response);

      StockMarketIndexResponse.Body body = response.response().body();
      List<Item> items = body.items().item();
      for (Item item : items) {
        IndexInfo indexInfo = requestIndexInfoMap.get(
            createIndexInfoKey(item.idxCsf(), item.idxNm())
        );

        if (indexInfo == null) {
          continue;
        }

        LocalDate baseDate = LocalDate.parse(item.basDt(), DateTimeFormatter.BASIC_ISO_DATE);
        String dataKey = createIndexDataKey(indexInfo.getId(), baseDate);

        IndexData indexData = existingDataMap.get(dataKey);

        if (indexData == null) {
          indexData = toIndexData(item, indexInfo);
          newIndexDataList.add(indexData);
        } else {
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
        }
        result.add(indexData);
      }
      if (body.numOfRows() * body.pageNo() >= body.totalCount()) {
        break;
      }
      pageNo++;
    }

    indexDataRepository.saveAll(newIndexDataList);
    return result;
  }

  private void validateIndexDataSyncRequest(IndexDataSyncRequest request) {
    if (request == null
        || request.baseDateFrom() == null
        || request.baseDateTo() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.");
    }

    if (request.indexInfoIds() == null || request.indexInfoIds().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "동기화할 지수 ID 목록이 필요합니다.");
    }

    if (request.baseDateFrom().isAfter(request.baseDateTo())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 날짜 범위입니다.");
    }
  }

  private void validateStockMarketIndexResponse(StockMarketIndexResponse response) {
    if (response == null || response.response() == null) {
      throw new IllegalStateException("Open API 응답이 비어 있습니다.");
    }

    StockMarketIndexResponse.Response apiResponse = response.response();
    if (apiResponse.header() == null) {
      throw new IllegalStateException("Open API 응답 헤더가 없습니다.");
    }

//    [resultCode=00, resultMsg=NORMAL SERVICE.] 공공데이터의 response
    String resultCode = apiResponse.header().resultCode();
    if (!"00".equals(resultCode)) {
      throw new IllegalStateException(
          "Open API 요청 실패: " + apiResponse.header().resultMsg()
      );
    }

    StockMarketIndexResponse.Body body = apiResponse.body();
    if (body == null || body.items() == null || body.items().item() == null) {
      throw new IllegalStateException("Open API 응답 데이터가 없습니다.");
    }
  }

  //  프론트에서 전달할 경우 분류를 한 경우만 보낸다.
  //  그리고 OPEN_API만 자동 업데이트.
  private List<IndexInfo> getTargetIndexInfos(IndexDataSyncRequest request) {
    return indexInfoRepository.findAllById(request.indexInfoIds()).stream()
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
      List<IndexInfo> savedIndexInfos = indexInfoRepository.saveAll(newIndexInfos);

      List<AutoSync> autoSyncs = savedIndexInfos.stream()
          .map(indexInfo -> AutoSync.builder()
              .indexInfo(indexInfo)
              .enabled(false)
              .build())
          .collect(Collectors.toList());

      autoSyncRepository.saveAll(autoSyncs);
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

  private String createIndexDataKey(UUID indexInfoId, LocalDate baseDate) {
    return indexInfoId + "|" + baseDate;
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

  private StockMarketIndexRequest createIndexDataRequest(
      IndexDataSyncRequest request,
      int pageNo
  ) {
    return new StockMarketIndexRequest(
        pageNo,
        10000,//아니면 페이지네이션으로 여러번 받아와야하는데 현재는 단순하게 1만개를 리밋으로 처리
        null,
        request.baseDateFrom().format(DateTimeFormatter.BASIC_ISO_DATE),
        request.baseDateTo().format(DateTimeFormatter.BASIC_ISO_DATE),
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
        null,
        null,
        null
    );
  }
}
