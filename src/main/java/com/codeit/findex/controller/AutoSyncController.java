package com.codeit.findex.controller;

import com.codeit.findex.dto.autosync.AutoSyncListResponse;
import com.codeit.findex.dto.autosync.AutoSyncSearchRequest;
import com.codeit.findex.dto.autosync.AutoSyncUpdateRequest;
import com.codeit.findex.dto.autosync.AutoSyncUpdateResponse;
import com.codeit.findex.service.AutoSyncService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/auto-sync-configs")
public class AutoSyncController {

  private final AutoSyncService autoSyncService;

  @PatchMapping("/{id}")
  public ResponseEntity<AutoSyncUpdateResponse> updateAutoSync(
      @PathVariable("id") UUID autoSyncId,
      @Valid @RequestBody AutoSyncUpdateRequest request
  ) {

    return ResponseEntity.ok(autoSyncService.updateAutoSync(autoSyncId, request));
  }

  @GetMapping
  public ResponseEntity<AutoSyncListResponse> findAutoSyncList(
      @ModelAttribute AutoSyncSearchRequest request
  ){
    return ResponseEntity.ok(autoSyncService.findAutoSyncList(request));
  }

}
