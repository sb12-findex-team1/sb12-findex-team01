package com.codeit.findex.service;

import com.codeit.findex.dto.indexinfo.IndexInfoCreateRequest;
import com.codeit.findex.dto.indexinfo.IndexInfoResponse;
import com.codeit.findex.dto.indexinfo.IndexInfoUpdateRequest;
import java.util.UUID;

public interface IndexInfoService {

  IndexInfoResponse create(IndexInfoCreateRequest request);

  IndexInfoResponse update(UUID id, IndexInfoUpdateRequest request);

  void delete(UUID id);

  IndexInfoResponse findById(UUID id);

}