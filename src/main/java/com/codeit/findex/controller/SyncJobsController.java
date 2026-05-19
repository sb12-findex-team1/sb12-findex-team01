package com.codeit.findex.controller;

import com.codeit.findex.dto.indexdata.IndexDataSyncRequest;
import com.codeit.findex.dto.client.StockMarketIndexRequest;
import com.codeit.findex.dto.syncjob.SyncJobDto;
import com.codeit.findex.entity.SourceType;
import com.codeit.findex.service.ClientIndexSyncService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class SyncJobsController {

  private final ClientIndexSyncService clientIndexSyncService;

  @PostMapping("/index-infos")
  public ResponseEntity<List<SyncJobDto>> syncIndexInfo(
      HttpServletRequest httpRequest
  ) {
    String ip = httpRequest.getRemoteAddr();
    StockMarketIndexRequest request = StockMarketIndexRequest.getIndexInfo();

    List<SyncJobDto> syncJobs = clientIndexSyncService.syncIndexInfo(request, SourceType.OPEN_API, ip);
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(syncJobs);
  }
  @PostMapping("/index-data")
  public ResponseEntity<List<SyncJobDto>> syncIndexData(
      @RequestBody IndexDataSyncRequest request,
      HttpServletRequest httpRequest
  ) {
    String ip = httpRequest.getRemoteAddr();
    List<SyncJobDto> syncJobs = clientIndexSyncService.syncIndexData(request, ip);
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(syncJobs);
  }
}
