package com.codeit.findex.service.impl;

import com.codeit.findex.dto.indexinfo.IndexInfoListResponse;
import com.codeit.findex.dto.indexinfo.IndexInfoCreateRequest;
import com.codeit.findex.dto.indexinfo.IndexInfoResponse;
import com.codeit.findex.dto.indexinfo.IndexInfoSearchRequest;
import com.codeit.findex.dto.indexinfo.IndexInfoSummaryResponse;
import com.codeit.findex.dto.indexinfo.IndexInfoUpdateRequest;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.repository.IndexInfoRepository;
import com.codeit.findex.service.IndexInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor

public class IndexInfoServiceImpl implements IndexInfoService {

  private static final int DEFAULT_SIZE = 10;
  private static final int MAX_SIZE = 100;

  private final IndexInfoRepository indexInfoRepository;
  private final ObjectMapper objectMapper;

  @Transactional
  @Override
  public IndexInfoResponse create(IndexInfoCreateRequest request) {
    if (indexInfoRepository.existsByIndexClassificationAndIndexName(
        request.indexClassification(), request.indexName())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 지수 정보입니다.");
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
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 지수 정보입니다."));
    indexInfo.update(request);
    indexInfoRepository.flush();
    return toResponse(indexInfo);
  }

  @Transactional
  @Override
  public void delete(UUID id) {
    IndexInfo indexInfo = indexInfoRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 지수 정보입니다."));
    indexInfoRepository.delete(indexInfo);
  }

  @Transactional(readOnly = true)
  @Override
  public IndexInfoResponse findById(UUID id) {
    IndexInfo indexInfo = indexInfoRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 지수 정보입니다."));
    return toResponse(indexInfo);
  }

  @Transactional(readOnly = true)
  @Override
  public IndexInfoListResponse<IndexInfoResponse> findAll(
      IndexInfoSearchRequest request, String cursor, UUID idAfter, int size) {

    int resolvedSize = resolveSize(size);
    String sortField = resolveSortField(request.sortField());
    Sort.Direction sortDirection = resolveSortDirection(request.sortDirection());

    String sortValue = null;
    UUID resolvedIdAfter = idAfter;

    if (cursor != null && !cursor.isBlank()) {
      Map<String, String> cursorMap = decodeCursor(cursor);
      resolvedIdAfter = UUID.fromString(cursorMap.get("id"));
      sortValue = cursorMap.get("sortValue");
    }

    List<IndexInfo> rows = indexInfoRepository.findIndexInfoList(
        request.indexClassification(),
        request.indexName(),
        request.favorite(),
        sortField,
        sortDirection.name(),
        sortValue,
        resolvedIdAfter,
        resolvedSize + 1
    );

    boolean hasNext = rows.size() > resolvedSize;
    List<IndexInfoResponse> content = rows.stream()
        .limit(resolvedSize)
        .map(this::toResponse)
        .toList();

    int totalElements = indexInfoRepository.countIndexInfoList(
        request.indexClassification(),
        request.indexName(),
        request.favorite()
    );

    String nextCursor = null;
    String nextIdAfter = null;
    if (hasNext && !content.isEmpty()) {
      IndexInfo last = rows.get(resolvedSize - 1);
      nextCursor = encodeCursor(last, sortField);
      nextIdAfter = last.getId().toString();
    }

    return new IndexInfoListResponse<>(content, nextCursor, nextIdAfter, resolvedSize, totalElements, hasNext);
  }
  // 정렬 기준 값 + id를 cursor에 저장
    private String encodeCursor(IndexInfo indexInfo, String sortField) {
      try {
        String sortValue = switch (sortField) {
          case "indexName" -> indexInfo.getIndexName();
          case "employedItemsCount" -> String.valueOf(indexInfo.getEmployedItemsCount());
          default -> indexInfo.getIndexClassification();
        };
        String json = objectMapper.writeValueAsString(Map.of(
            "id", indexInfo.getId().toString(),
            "sortValue", sortValue
        ));
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
      } catch (Exception e) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "커서 생성에 실패했습니다.");
      }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> decodeCursor(String cursor) {
      try {
        byte[] decoded = Base64.getDecoder().decode(cursor);
        String json = new String(decoded, StandardCharsets.UTF_8);
        return (Map<String, String>) objectMapper.readValue(json, Map.class);
      } catch (Exception e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 커서입니다.");
      }
    }

    private int resolveSize(int size) {
      if (size <= 0) return DEFAULT_SIZE;
      return Math.min(size, MAX_SIZE);
    }

    private String resolveSortField(String sortField) {
      if (sortField == null || sortField.isBlank()) return "indexClassification";
      return switch (sortField) {
        case "indexClassification", "indexName", "employedItemsCount" -> sortField;
        default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 정렬 필드입니다: " + sortField);
      };
    }

    private Sort.Direction resolveSortDirection(String sortDirection) {
      if (sortDirection == null || sortDirection.isBlank()) return Sort.Direction.ASC;
      return switch (sortDirection.toLowerCase()) {
        case "asc" -> Sort.Direction.ASC;
        case "desc" -> Sort.Direction.DESC;
        default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 정렬 방향입니다: " + sortDirection);
      };
    }

  @Transactional(readOnly = true)
  @Override
  public List<IndexInfoSummaryResponse> findAllSummaries() {
    return indexInfoRepository.findAll().stream()
        .map(i -> new IndexInfoSummaryResponse(
            i.getId(),
            i.getIndexClassification(),
            i.getIndexName()
        ))
        .toList();
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