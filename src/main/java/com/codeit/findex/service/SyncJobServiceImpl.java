package com.codeit.findex.service;

import com.codeit.findex.dto.syncJob.IndexDataSyncRequest;
import com.codeit.findex.entity.JobType;
import com.codeit.findex.entity.Result;
import com.codeit.findex.entity.SyncJob;
import com.codeit.findex.repository.SyncJobRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncJobServiceImpl implements SyncJobService {

  private final SyncJobRepository syncJobRepository;

  @Transactional
  @Override
  public void syncIndexData(IndexDataSyncRequest request) {
    for (UUID indexInfoId : request.indexInfoIds()) {
      for (LocalDate date = request.baseDateFrom();
          !date.isAfter(request.baseDateTo());
          date = date.plusDays(1)) {

        SyncJob syncJob = SyncJob.builder()
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
}