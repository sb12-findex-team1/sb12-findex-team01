package com.codeit.findex.service.impl;

import com.codeit.findex.dto.client.StockMarketIndexRequest;
import com.codeit.findex.dto.indexdata.IndexDataSyncRequest;
import com.codeit.findex.dto.syncjob.SyncJobDto;
import com.codeit.findex.dto.syncjob.SyncJobListResponse;
import com.codeit.findex.dto.syncjob.SyncJobSearchRequest;
import com.codeit.findex.dto.syncjob.SyncJobStatsDto;
import com.codeit.findex.entity.IndexData;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.entity.JobType;
import com.codeit.findex.entity.Result;
import com.codeit.findex.entity.SourceType;
import com.codeit.findex.entity.SyncJob;
import com.codeit.findex.repository.SyncJobRepository;
import com.codeit.findex.service.ClientIndexSyncService;
import com.codeit.findex.service.SyncJobService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SyncJobServiceImpl implements SyncJobService {

  private static final int DEFAULT_SIZE = 10;
  private static final int MAX_SIZE = 100;
  private static final String NULL_TARGET_DATE_CURSOR = "__NULL__";

  private final SyncJobRepository syncJobRepository;
  private final ClientIndexSyncService clientIndexSyncService;

  private final EntityManager entityManager;


  @Transactional
  @Override
  public List<SyncJobDto> syncIndexInfos(StockMarketIndexRequest request, String ip) {

    List<SyncJobDto> result = new ArrayList<>();

    List<IndexInfo> indexInfos = clientIndexSyncService.syncIndexInfo(request, SourceType.OPEN_API,
        ip);

    for (IndexInfo indexInfo : indexInfos) {
      SyncJob syncJob = SyncJob.builder()
          .indexInfo(indexInfo)
          .jobType(JobType.INDEX_INFO)
          .targetDate(null)
          .worker(ip)
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
  public List<SyncJobDto> syncIndexData(IndexDataSyncRequest request, String ip) {

    List<SyncJobDto> result = new ArrayList<>();
    List<IndexData> indexDataList = clientIndexSyncService.syncIndexData(request, ip);

    for (IndexData indexData : indexDataList) {
      SyncJob syncJob = SyncJob.builder()
          .indexInfo(indexData.getIndexInfo())
          .jobType(JobType.INDEX_DATA)
          .targetDate(indexData.getBaseDate())
          .worker(ip)
          .jobTime(Instant.now())
          .result(Result.SUCCESS)
          .build();

      SyncJob savedSyncJob = syncJobRepository.save(syncJob);

      result.add(toDto(savedSyncJob));
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
        syncJob.getJobType(),
        syncJob.getIndexInfo().getId(),
        syncJob.getTargetDate(),
        syncJob.getWorker(),
        syncJob.getJobTime(),
        syncJob.getResult()
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
      jpql.append(" and s.worker like :worker");
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
      query.setParameter("worker", "%" + request.worker() + "%");
    }

    if (request.jobTimeFrom() != null) {
      query.setParameter("jobTimeFrom", toInstant(request.jobTimeFrom()));
    }

    if (request.jobTimeTo() != null) {
      query.setParameter("jobTimeTo", toInstant(request.jobTimeTo()));
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
      boolean nullCursor = NULL_TARGET_DATE_CURSOR.equals(request.cursor());

      if (sortDirection.isDescending()) {
        if (nullCursor) {
          jpql.append("""
               and s.targetDate is null
               and s.id < :idAfter
              """);
        } else {
          jpql.append("""
               and (
                 s.targetDate < :cursorTargetDate
                 or (s.targetDate = :cursorTargetDate and s.id < :idAfter)
                 or s.targetDate is null
               )
              """);
        }
      } else {
        if (nullCursor) {
          jpql.append("""
               and (
                 (s.targetDate is null and s.id > :idAfter)
                 or s.targetDate is not null
               )
              """);
        } else {
          jpql.append("""
               and s.targetDate is not null
               and (
                 s.targetDate > :cursorTargetDate
                 or (s.targetDate = :cursorTargetDate and s.id > :idAfter)
               )
              """);
        }
      }
    }
  }

  private void appendOrderBy(StringBuilder jpql, SyncJobSearchRequest request) {
    String sortField = resolveSortField(request.sortField());
    Sort.Direction sortDirection = resolveSortDirection(request.sortDirection());

    String direction = sortDirection.isDescending() ? "desc" : "asc";

    if (sortField.equals("targetDate")) {
      if (sortDirection.isDescending()) {
        jpql.append("""
             order by case when s.targetDate is null then 1 else 0 end asc,
                      s.targetDate desc,
                      s.id desc
            """);
      } else {
        jpql.append("""
             order by case when s.targetDate is null then 0 else 1 end asc,
                      s.targetDate asc,
                      s.id asc
            """);
      }
      return;
    }

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

    if (sortField.equals("targetDate")
        && !NULL_TARGET_DATE_CURSOR.equals(request.cursor())) {
      query.setParameter("cursorTargetDate", LocalDate.parse(request.cursor()));
    }

    query.setParameter("idAfter", request.idAfter());
  }

  private String getNextCursor(SyncJobDto dto, String sortField) {
    String resolvedSortField = resolveSortField(sortField);

    if (resolvedSortField.equals("targetDate")) {
      return dto.targetDate() == null
          ? NULL_TARGET_DATE_CURSOR
          : dto.targetDate().toString();
    }

    return dto.jobTime().toString();
  }

  @Transactional(readOnly = true)
  @Override
  public SyncJobStatsDto getStats() {
    Instant from = Instant.now().minus(7, ChronoUnit.DAYS);

    long totalSuccess =
        syncJobRepository.countByResultAndJobTimeAfter(Result.SUCCESS, from);

    long totalFailed =
        syncJobRepository.countByResultAndJobTimeAfter(Result.FAILED, from);

    Instant latestSync = syncJobRepository.findTopByOrderByJobTimeDesc()
        .map(SyncJob::getJobTime)
        .orElse(null);

    return new SyncJobStatsDto(totalSuccess, totalFailed, latestSync);
  }

  private Instant toInstant(LocalDateTime dateTime) {
    return dateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant();
  }

}
