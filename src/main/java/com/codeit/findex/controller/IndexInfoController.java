package com.codeit.findex.controller;


import com.codeit.findex.dto.indexinfo.IndexInfoCreateRequest;
import com.codeit.findex.dto.indexinfo.IndexInfoResponse;

import com.codeit.findex.dto.indexinfo.IndexInfoUpdateRequest;
import com.codeit.findex.service.impl.IndexInfoServiceImpl;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/index-infos")
public class IndexInfoController {

  private final IndexInfoServiceImpl indexInfoService;

  @PostMapping
  public ResponseEntity<IndexInfoResponse> create(@RequestBody IndexInfoCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(indexInfoService.create(request));
  }

  @GetMapping("/{id}")
  public ResponseEntity<IndexInfoResponse> findById(@PathVariable UUID id) {
    return ResponseEntity.ok(indexInfoService.findById(id));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<IndexInfoResponse> update(@PathVariable UUID id,
      @RequestBody IndexInfoUpdateRequest request) {
    return ResponseEntity.ok(indexInfoService.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    indexInfoService.delete(id);
    return ResponseEntity.noContent().build();
  }

}