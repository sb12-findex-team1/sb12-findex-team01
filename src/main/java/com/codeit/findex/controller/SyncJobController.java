package com.codeit.findex.controller;

import com.codeit.findex.dto.syncJob.IndexDataSyncRequest;
import com.codeit.findex.dto.syncJob.SyncJobDto;
import com.codeit.findex.dto.syncJob.SyncJobListResponse;
import com.codeit.findex.dto.syncJob.SyncJobSearchRequest;
import com.codeit.findex.service.SyncJobService;

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

  @PostMapping("/index-data")
  public ResponseEntity<List<SyncJobDto>> syncIndexData(
      @Valid
      @RequestBody
      IndexDataSyncRequest request
  ) {

    List<SyncJobDto> result = syncJobService.syncIndexData(request);
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
}
