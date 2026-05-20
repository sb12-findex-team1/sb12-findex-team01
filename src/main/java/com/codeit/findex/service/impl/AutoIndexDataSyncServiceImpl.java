package com.codeit.findex.service.impl;

import com.codeit.findex.dto.indexdata.IndexDataSyncRequest;
import com.codeit.findex.entity.AutoSync;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.repository.AutoSyncRepository;
import com.codeit.findex.repository.SyncJobRepository;
import com.codeit.findex.service.AutoIndexDataSyncService;
import com.codeit.findex.service.SyncJobService;
import java.net.InetAddress;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoIndexDataSyncServiceImpl implements AutoIndexDataSyncService {

  private final AutoSyncRepository autoSyncRepository;
  private final SyncJobRepository syncJobRepository;
  private final SyncJobService syncJobService;

  @Override
  public void syncEnabledIndexes() {
    String workerIp = resolveWorkerIp();
    List<AutoSync> enabledAutoSyncs = autoSyncRepository.findEnabledAutoSyncs();

    // 중간 체크
    log.info(
        "[AutoSync] 자동 연동 대상 조회 완료. count={}, workerIp={}",
        enabledAutoSyncs.size(),
        workerIp
    );

    for (AutoSync autoSync : enabledAutoSyncs) {
      syncOneIndex(autoSync, workerIp);
    }
  }

  private void syncOneIndex(AutoSync autoSync, String workerIp) {
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

    syncDateRange(indexInfo, startDate, endDate, workerIp);

  }

  private void syncDateRange(IndexInfo indexInfo, LocalDate startDate, LocalDate endDate,
      String workerIp
  ) {
    try {
      log.info(
          "[AutoSync] 지수 데이터 자동 연동 시작. indexInfoId={}, indexName={}, startDate={}, endDate={}, workerIp={}",
          indexInfo.getId(),
          indexInfo.getIndexName(),
          startDate,
          endDate,
          workerIp
      );

      IndexDataSyncRequest request = new IndexDataSyncRequest(
          List.of(indexInfo.getId()),
          startDate,
          endDate
      );

      syncJobService.syncIndexData(request, workerIp);

      log.info(
          "[AutoSync] 지수 데이터 자동 연동 성공. indexInfoId={}, indexName={}, startDate={}, endDate={}, workerIp={}",
          indexInfo.getId(),
          indexInfo.getIndexName(),
          startDate,
          endDate,
          workerIp
      );

    } catch (Exception e) {
      log.error(
          "[AutoSync] 지수 데이터 자동 연동 실패. indexInfoId={}, indexName={}, startDate={}, endDate={}, workerIp={}",
          indexInfo.getId(),
          indexInfo.getIndexName(),
          startDate,
          endDate,
          workerIp,
          e
      );
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

  private String resolveWorkerIp() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (Exception e) {
      log.warn("[AutoSync] worker IP 조회 실패. 기본값 127.0.0.1 사용", e);
      return "127.0.0.1";
    }
  }

}
