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

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
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

  private final EntityManager entityManager;


  @Override
  public List<SyncJobDto> syncIndexInfos() {

    List<SyncJobDto> result = new ArrayList<>();

    List<IndexInfo> indexInfos = indexInfoRepository.findAll();

    for (IndexInfo indexInfo : indexInfos) {
      SyncJob syncJob = SyncJob.builder()
          .indexInfo(indexInfo)
          .jobType(JobType.INDEX_INFO)
          .targetDate(LocalDate.now())
          .worker("127.0.0.1")
          .jobTime(Instant.now())
          .result(Result.SUCCESS)
          .build();

      SyncJob savedSyncJob = syncJobRepository.save(syncJob);

      result.add(toDto(savedSyncJob));
    }

    return result;
  }

  @Transactional
  @Override
  public List<SyncJobDto> syncIndexData(IndexDataSyncRequest request) {

    List<SyncJobDto> result = new ArrayList<>();

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

        SyncJob savedSyncJob = syncJobRepository.save(syncJob);

        result.add(toDto(savedSyncJob));
      }
    }

    return result;
  }

  @Transactional(readOnly = true)
  @Override
  public SyncJobListResponse findAll(SyncJobSearchRequest request) {
    int size = resolveSize(request.size());

    List<SyncJob> syncJobs = searchSyncJobs(request, size);

    List<SyncJobDto> content = syncJobs.stream()
        .limit(size)
        .map(this::toDto)
        .toList();

    boolean hasNext = syncJobs.size() > size;

    int totalElements = countSyncJobs(request);

    String nextCursor = null;
    String nextIdAfter = null;

    if (hasNext && !content.isEmpty()) {
      SyncJobDto last = content.get(content.size() - 1);

      nextCursor = getNextCursor(last, request.sortField());
      nextIdAfter = last.id().toString();
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

  private List<SyncJob> searchSyncJobs(SyncJobSearchRequest request, int size) {
    StringBuilder jpql = new StringBuilder("""
          select s
          from SyncJob s
          join fetch s.indexInfo i
          where 1 = 1
        """);

    appendSearchConditions(jpql, request);
    appendCursorCondition(jpql, request);
    appendOrderBy(jpql, request);

    TypedQuery<SyncJob> query = entityManager.createQuery(
        jpql.toString(), SyncJob.class
    );

    setSearchParameters(query, request);

    setCursorParameters(query, request);

    query.setMaxResults(size + 1);

    return query.getResultList();
  }

  private int countSyncJobs(SyncJobSearchRequest request) {
    StringBuilder jpql = new StringBuilder("""
          select count(s)
          from SyncJob s
          join s.indexInfo i
          where 1 = 1
        """);

    appendSearchConditions(jpql, request);

    TypedQuery<Long> query = entityManager.createQuery(
        jpql.toString(), Long.class
    );

    setSearchParameters(query, request);

    return query.getSingleResult().intValue();
  }

  private void appendSearchConditions(StringBuilder jpql, SyncJobSearchRequest request) {

    if (request.jobType() != null && !request.jobType().isBlank()) {
      jpql.append(" and s.jobType = :jobType");
    }

    if (request.indexInfoId() != null) {
      jpql.append(" and i.id = :indexInfoId");
    }

    if (request.baseDateFrom() != null) {
      jpql.append(" and s.targetDate >= :baseDateFrom");
    }

    if (request.baseDateTo() != null) {
      jpql.append(" and s.targetDate <= :baseDateTo");
    }

    if (request.worker() != null && !request.worker().isBlank()) {
      jpql.append(" and s.worker = :worker");
    }

    if (request.jobTimeFrom() != null) {
      jpql.append(" and s.jobTime >= :jobTimeFrom");
    }

    if (request.jobTimeTo() != null) {
      jpql.append(" and s.jobTime <= :jobTimeTo");
    }

    if (request.status() != null && !request.status().isBlank()) {
      jpql.append(" and s.result = :result");
    }
  }

  private void setSearchParameters(TypedQuery<?> query, SyncJobSearchRequest request) {

    if (request.jobType() != null && !request.jobType().isBlank()) {
      query.setParameter("jobType", JobType.valueOf(request.jobType()));
    }

    if (request.indexInfoId() != null) {
      query.setParameter("indexInfoId", request.indexInfoId());
    }

    if (request.baseDateFrom() != null) {
      query.setParameter("baseDateFrom", request.baseDateFrom());
    }

    if (request.baseDateTo() != null) {
      query.setParameter("baseDateTo", request.baseDateTo());
    }

    if (request.worker() != null && !request.worker().isBlank()) {
      query.setParameter("worker", request.worker());
    }

    if (request.jobTimeFrom() != null) {
      query.setParameter("jobTimeFrom", request.jobTimeFrom());
    }

    if (request.jobTimeTo() != null) {
      query.setParameter("jobTimeTo", request.jobTimeTo());
    }

    if (request.status() != null && !request.status().isBlank()) {
      query.setParameter("result", Result.valueOf(request.status()));
    }
  }

  private void appendCursorCondition(StringBuilder jpql, SyncJobSearchRequest request) {

    if (request.cursor() == null || request.cursor().isBlank() || request.idAfter() == null) {
      return;
    }

    String sortField = resolveSortField(request.sortField());
    Sort.Direction sortDirection = resolveSortDirection(request.sortDirection());

    if (sortField.equals("jobTime")) {
      if (sortDirection.isDescending()) {
        jpql.append("""
             and (s.jobTime < :cursorJobTime
            or (s.jobTime = :cursorJobTime and s.id < :idAfter))
            """);
      } else {
        jpql.append("""
             and (s.jobTime > :cursorJobTime
            or (s.jobTime = :cursorJobTime and s.id > :idAfter))
            """);
      }
    }

    if (sortField.equals("targetDate")) {
      if (sortDirection.isDescending()) {
        jpql.append("""
             and (s.targetDate < :cursorTargetDate
            or (s.targetDate = :cursorTargetDate and s.id < :idAfter))
            """);
      } else {
        jpql.append("""
             and (s.targetDate > :cursorTargetDate
            or (s.targetDate = :cursorTargetDate and s.id > :idAfter))
            """);
      }
    }
  }

  private void appendOrderBy(StringBuilder jpql, SyncJobSearchRequest request) {
    String sortField = resolveSortField(request.sortField());
    Sort.Direction sortDirection = resolveSortDirection(request.sortDirection());

    String direction = sortDirection.isDescending() ? "desc" : "asc";

    jpql.append(" order by s.")
        .append(sortField)
        .append(" ")
        .append(direction)
        .append(", s.id ")
        .append(direction);
  }

  private void setCursorParameters(TypedQuery<?> query, SyncJobSearchRequest request) {

    if (request.cursor() == null || request.cursor().isBlank() || request.idAfter() == null) {
      return;
    }

    String sortField = resolveSortField(request.sortField());

    if (sortField.equals("jobTime")) {
      query.setParameter("cursorJobTime", Instant.parse(request.cursor()));
    }

    if (sortField.equals("targetDate")) {
      query.setParameter("cursorTargetDate", LocalDate.parse(request.cursor()));
    }

    query.setParameter("idAfter", request.idAfter());
  }

  private String getNextCursor(SyncJobDto dto, String sortField) {
    String resolvedSortField = resolveSortField(sortField);

    if (resolvedSortField.equals("targetDate")) {
      return dto.targetDate().toString();
    }

    return dto.jobTime().toString();
  }

}