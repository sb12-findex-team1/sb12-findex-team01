package com.codeit.findex.controller;

import com.codeit.findex.dto.client.StockMarketIndexRequest;
import com.codeit.findex.dto.indexdata.IndexDataSyncRequest;
import com.codeit.findex.dto.syncjob.SyncJobDto;
import com.codeit.findex.dto.syncjob.SyncJobListResponse;
import com.codeit.findex.dto.syncjob.SyncJobSearchRequest;
import com.codeit.findex.dto.syncjob.SyncJobStatsDto;
import com.codeit.findex.service.SyncJobService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sync-jobs")
public class SyncJobController {

  private final SyncJobService syncJobService;

  @PostMapping("/index-infos")
  public ResponseEntity<List<SyncJobDto>> syncIndexInfo(
      HttpServletRequest httpRequest
  ) {
    String ip = httpRequest.getRemoteAddr();
    if (ip.equals("0:0:0:0:0:0:0:1")) {
      ip = "127.0.0.1";
    }
    StockMarketIndexRequest request = StockMarketIndexRequest.getIndexInfo();

    List<SyncJobDto> result = syncJobService.syncIndexInfos(request, ip);
    return ResponseEntity
        .status(HttpStatus.ACCEPTED)
        .body(result);
  }

  @PostMapping("/index-data")
  public ResponseEntity<List<SyncJobDto>> syncIndexData(
      @Valid
      @RequestBody
      IndexDataSyncRequest request,
      HttpServletRequest httpRequest
  ) {
    String ip = httpRequest.getRemoteAddr();
    if (ip.equals("0:0:0:0:0:0:0:1")) {
      ip = "127.0.0.1";
    }
    
    List<SyncJobDto> result = syncJobService.syncIndexData(request, ip);
    return ResponseEntity
        .status(HttpStatus.ACCEPTED)
        .body(result);
  }

  @GetMapping
  public ResponseEntity<SyncJobListResponse> findAll(
      @ModelAttribute SyncJobSearchRequest request
  ) {
    return ResponseEntity.ok(syncJobService.findAll(request));
  }

  @GetMapping("/stats")
  public ResponseEntity<SyncJobStatsDto> getStats() {
    return ResponseEntity.ok(syncJobService.getStats());
  }
}
