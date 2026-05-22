package com.codeit.findex.service.impl;

import com.codeit.findex.dto.autosync.AutoSyncListResponse;
import com.codeit.findex.dto.autosync.AutoSyncResponse;
import com.codeit.findex.dto.autosync.AutoSyncSearchRequest;
import com.codeit.findex.dto.autosync.AutoSyncUpdateRequest;
import com.codeit.findex.dto.autosync.AutoSyncUpdateResponse;
import com.codeit.findex.entity.AutoSync;
import com.codeit.findex.repository.AutoSyncRepository;
import com.codeit.findex.service.AutoSyncService;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class AutoSyncServiceImpl implements AutoSyncService {

  private static final int DEFAULT_SIZE = 10;
  private static final int MAX_SIZE = 100;

  private final AutoSyncRepository autoSyncRepository;
  private final ObjectMapper objectMapper;

  @Transactional
  @Override
  public AutoSyncUpdateResponse updateAutoSync(UUID id, AutoSyncUpdateRequest request) {
    AutoSync autoSync = autoSyncRepository.findById(id).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "자동 연동 설정을 찾을 수 없습니다.")
    );

    if (request.enabled()) {
      autoSync.enable();
    } else {
      autoSync.disable();
    }

    return new AutoSyncUpdateResponse(autoSync.getId(),
        autoSync.getIndexInfo().getId(),
        autoSync.getIndexInfo().getIndexClassification(),
        autoSync.getIndexInfo().getIndexName(),
        autoSync.isEnabled()
    );
  }

  @Transactional(readOnly = true)
  @Override
  public AutoSyncListResponse findAutoSyncList(AutoSyncSearchRequest request) {
    int size = resolveSize(request.size());
    UUID idAfter = resolveIdAfter(request);
    PageRequest pageRequest = createPageRequest(request, size);

    List<AutoSync> autoSyncs = autoSyncRepository.findAutoSyncList(
        request.indexInfoId(),
        request.enabled(),
        idAfter,
        pageRequest
    );

    List<AutoSyncResponse> content = toContent(autoSyncs, size);
    boolean hasNext = autoSyncs.size() > size;

    int totalElements = autoSyncRepository.countAutoSyncList(
        request.indexInfoId(),
        request.enabled()
    );

    return createListResponse(content, size, totalElements, hasNext);
  }

  private PageRequest createPageRequest(AutoSyncSearchRequest request, int size) {
    String sortField = resolveSortField(request.sortField());
    Sort.Direction sortDirection = resolveSortDirection(request.sortDirection());

    return PageRequest.of(
        0,
        size + 1,
        Sort.by(
            new Sort.Order(sortDirection, sortField),
            new Sort.Order(sortDirection, "id")
        )
    );
  }

  private List<AutoSyncResponse> toContent(List<AutoSync> autoSyncs, int size) {
    return autoSyncs.stream()
        .limit(size)
        .map(this::toResponse)
        .toList();
  }

  private AutoSyncListResponse createListResponse(
      List<AutoSyncResponse> content,
      int size,
      int totalElements,
      boolean hasNext
  ) {
    String nextCursor = null;
    String nextIdAfter = null;

    if (hasNext && !content.isEmpty()) {
      UUID lastId = content.get(content.size() - 1).id();
      nextCursor = encodeCursor(lastId);
      nextIdAfter = lastId.toString();
    }

    return new AutoSyncListResponse(
        content,
        nextCursor,
        nextIdAfter,
        size,
        totalElements,
        hasNext
    );
  }

  private AutoSyncResponse toResponse(AutoSync autoSync) {
    return new AutoSyncResponse(
        autoSync.getId(),
        autoSync.getIndexInfo().getId(),
        autoSync.getIndexInfo().getIndexClassification(),
        autoSync.getIndexInfo().getIndexName(),
        autoSync.isEnabled()
    );
  }

  private int resolveSize(Integer size) {
    if (size == null || size <= 0) {
      return DEFAULT_SIZE;
    }

    return Math.min(size, MAX_SIZE);
  }

  private String resolveSortField(String sortField) {
    if (sortField == null || sortField.isBlank()) {
      return "indexInfo.indexName";
    }

    return switch (sortField) {
      case "indexInfo.indexName" -> "indexInfo.indexName";
      case "enabled" -> "enabled";
      default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 정렬 필드입니다: " + sortField);
    };
  }

  private Sort.Direction resolveSortDirection(String sortDirection) {
    if (sortDirection == null || sortDirection.isBlank()) {
      return Sort.Direction.ASC;
    }

    return switch (sortDirection.toLowerCase()) {
      case "asc" -> Sort.Direction.ASC;
      case "desc" -> Sort.Direction.DESC;
      default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 정렬 방향입니다: " + sortDirection);
    };
  }

  private UUID resolveIdAfter(AutoSyncSearchRequest request) {
    if (request.cursor() != null && !request.cursor().isBlank()) {
      return decodeCursor(request.cursor());
    }

    return request.idAfter();
  }

  private String encodeCursor(UUID id) {
    try {
      String json = objectMapper.writeValueAsString(Map.of("id", id.toString()));
      return Base64.getEncoder()
          .encodeToString(json.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw new IllegalStateException("커서 생성에 실패했습니다.", e);
    }
  }

  private UUID decodeCursor(String cursor) {
    try {
      byte[] decodedBytes = Base64.getDecoder().decode(cursor);
      String json = new String(decodedBytes, StandardCharsets.UTF_8);

      Map<String, String> cursorMap = objectMapper.readValue(json, Map.class);

      return UUID.fromString(cursorMap.get("id"));
    } catch (Exception e) {
      throw new IllegalArgumentException("유효하지 않은 커서입니다.", e);
    }
  }

}
