package com.codeit.findex.controller;

import com.codeit.findex.dto.syncJob.IndexDataSyncRequest;
import com.codeit.findex.dto.syncJob.SyncJobDto;
import com.codeit.findex.service.SyncJobService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sync-jobs")
public class SyncJobController {

  private final SyncJobService syncJobService;

  @PostMapping("/index-data")
  public ResponseEntity<Void> syncIndexData(
      @Valid
      @RequestBody
      IndexDataSyncRequest request
  ) {

    syncJobService.syncIndexData(request);
    return ResponseEntity.ok().build();
  }

  @GetMapping
  public ResponseEntity<List<SyncJobDto>> findAll() {
    return ResponseEntity.ok(syncJobService.findAll());
  }
}
