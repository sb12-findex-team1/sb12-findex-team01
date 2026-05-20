package com.codeit.findex.service.impl;

import com.codeit.findex.entity.AutoSync;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.entity.JobType;
import com.codeit.findex.entity.SyncJob;
import com.codeit.findex.repository.AutoSyncRepository;
import com.codeit.findex.repository.SyncJobRepository;
import com.codeit.findex.service.AutoIndexDataSyncService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

//todo: api 연동 로직 추가해야함
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoIndexDataSyncServiceImpl implements AutoIndexDataSyncService {

  private final AutoSyncRepository autoSyncRepository;
  private final SyncJobRepository syncJobRepository;

  @Override
  public void syncEnabledIndexes() {
    List<AutoSync> enabledAutoSyncs = autoSyncRepository.findEnabledAutoSyncs();

    for (AutoSync autoSync : enabledAutoSyncs) {
      syncOneIndex(autoSync);
    }
  }

  private void syncOneIndex(AutoSync autoSync) {
    IndexInfo indexInfo = autoSync.getIndexInfo();

    LocalDate startDate = resolveStartDate(indexInfo);
    LocalDate endDate = resolveLatestDate();

    if (startDate.isAfter(endDate)) {
      log.info(
          "[AutoSync] 연동할 데이터 없음. indexInfoId={}, indexName={}, startDate={}, endDate={}",
          indexInfo.getId(),
          indexInfo.getIndexName(),
          startDate,
          endDate
      );
      return;
    }

    LocalDate targetDate = startDate;

    while (!targetDate.isAfter(endDate)) {
      syncOneDate(indexInfo, targetDate);
      targetDate = targetDate.plusDays(1);
    }

  }

  private LocalDate resolveStartDate(IndexInfo indexInfo) {
    return syncJobRepository.findLastSuccessfulAutoSyncDate(indexInfo.getId())
        .map(lastSyncDate -> lastSyncDate.plusDays(1))
        .orElse(resolveDefaultStartDate());
  }

  private LocalDate resolveDefaultStartDate() {
    return LocalDate.now().minusDays(1);
  }

  private LocalDate resolveLatestDate() {
    return LocalDate.now();
  }

  private void syncOneDate(IndexInfo indexInfo, LocalDate targetDate) {
    try {
      log.info(
          "[AutoSync] 지수 데이터 자동 연동 시작. indexInfoId={}, indexName={}, targetDate={}",
          indexInfo.getId(),
          indexInfo.getIndexName(),
          targetDate
      );

      //todo: 여기 api 연동 로직 추가 필요

      //todo: worker 필드 값 뭐 들어가는지 체크 필요. + 연동 로직에 같이 저장하는지도 체크 필요
      syncJobRepository.save(
          SyncJob.success(indexInfo, JobType.INDEX_DATA, targetDate, "system")
      );

      log.info(
          "[AutoSync] 지수 데이터 자동 연동 성공. indexInfoId={}, targetDate={}",
          indexInfo.getId(),
          targetDate
      );

    } catch (Exception e) {
      syncJobRepository.save(
          SyncJob.failed(indexInfo, JobType.INDEX_DATA, targetDate, "system")
      );

      log.error(
          "[AutoSync] 지수 데이터 자동 연동 실패. indexInfoId={}, targetDate={}",
          indexInfo.getId(),
          targetDate,
          e
      );
    }
  }

}
