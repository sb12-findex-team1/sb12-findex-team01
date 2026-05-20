package com.codeit.findex.scheduler;

import com.codeit.findex.service.AutoIndexDataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutoSyncScheduler {

  private final AutoIndexDataSyncService autoIndexDataSyncService;

  @Scheduled(
      cron = "${findex.auto-sync.cron}",
      zone = "${findex.auto-sync.zone}"
  )
  public void runAutoSync() {
    log.info("[AutoSyncScheduler] 자동 지수 데이터 연동 시작");

    autoIndexDataSyncService.syncEnabledIndexes();

    log.info("[AutoSyncScheduler] 자동 지수 데이터 연동 종료");
  }

}
