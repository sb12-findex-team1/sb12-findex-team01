package com.codeit.findex.service;

import com.codeit.findex.dto.syncJob.IndexDataSyncRequest;
import com.codeit.findex.dto.syncJob.SyncJobDto;
import com.codeit.findex.dto.syncJob.SyncJobListResponse;
import com.codeit.findex.dto.syncJob.SyncJobSearchRequest;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.entity.JobType;
import com.codeit.findex.entity.Result;
import com.codeit.findex.entity.SyncJob;
import com.codeit.findex.repository.IndexInfoRepository;
import com.codeit.findex.repository.SyncJobRepository;

import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncJobServiceImpl implements SyncJobService {

  private static final int DEFAULT_SIZE = 10;
  private static final int MAX_SIZE = 100;

  private final SyncJobRepository syncJobRepository;
  private final IndexInfoRepository indexInfoRepository;


  @Transactional
  @Override
  public void syncIndexData(IndexDataSyncRequest request) {
    for (UUID indexInfoId : request.indexInfoIds()) {
      for (LocalDate date = request.baseDateFrom();
          !date.isAfter(request.baseDateTo());
          date = date.plusDays(1)) {

        IndexInfo indexInfo = indexInfoRepository.findById(indexInfoId)
            .orElseThrow(() ->
                new IllegalArgumentException("indexInfo 없음"));


        SyncJob syncJob = SyncJob.builder()
            .indexInfo(indexInfo)
            .jobType(JobType.INDEX_DATA)
            .targetDate(date)
            .worker("127.0.0.1")
            .jobTime(Instant.now())
            .result(Result.SUCCESS)
            .build();

        syncJobRepository.save(syncJob);
      }
    }
  }

  @Transactional(readOnly = true)
  @Override
  public SyncJobListResponse findAll(SyncJobSearchRequest request) {
    int size = resolveSize(request.size());
    PageRequest pageRequest = createPageRequest(request, size);

    List<SyncJob> syncJobs = syncJobRepository.findSyncJobList(pageRequest);

    List<SyncJobDto> content = syncJobs.stream()
        .limit(size)
        .map(this::toDto)
        .toList();

    boolean hasNext = syncJobs.size() > size;

    int totalElements = syncJobRepository.countSyncJobList();

    String nextCursor = null;
    String nextIdAfter = null;

    if (hasNext && !content.isEmpty()) {
      UUID lastId = content.get(content.size() - 1).id();
      nextCursor = lastId.toString();
      nextIdAfter = lastId.toString();
    }

    return new SyncJobListResponse(
        content,
        nextCursor,
        nextIdAfter,
        size,
        totalElements,
        hasNext
    );
  }

  private SyncJobDto toDto(SyncJob syncJob) {
    return new SyncJobDto(
        syncJob.getId(),
        syncJob.getJobType().name(),
        syncJob.getIndexInfo().getId(),
        syncJob.getTargetDate(),
        syncJob.getWorker(),
        syncJob.getJobTime(),
        syncJob.getResult().name()
    );
  }

  private int resolveSize(Integer size) {
    if (size == null || size <= 0) {
      return DEFAULT_SIZE;
    }

    return Math.min(size, MAX_SIZE);
  }

  private PageRequest createPageRequest(SyncJobSearchRequest request, int size) {
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

  private String resolveSortField(String sortField) {
    if (sortField == null || sortField.isBlank()) {
      return "jobTime";
    }

    return switch (sortField) {
      case "targetDate" -> "targetDate";
      case "jobTime" -> "jobTime";
      default -> "jobTime";
    };
  }

  private Sort.Direction resolveSortDirection(String sortDirection) {
    if (sortDirection == null || sortDirection.isBlank()) {
      return Sort.Direction.DESC;
    }

    return switch (sortDirection.toLowerCase()) {
      case "asc" -> Sort.Direction.ASC;
      case "desc" -> Sort.Direction.DESC;
      default -> Sort.Direction.DESC;
    };
  }


}