package com.codeit.findex.service;

import com.codeit.findex.dto.indexinfo.IndexInfoListResponse;
import com.codeit.findex.dto.indexinfo.IndexInfoCreateRequest;
import com.codeit.findex.dto.indexinfo.IndexInfoResponse;
import com.codeit.findex.dto.indexinfo.IndexInfoSearchRequest;
import com.codeit.findex.dto.indexinfo.IndexInfoUpdateRequest;
import java.util.UUID;

public interface IndexInfoService {

  IndexInfoResponse create(IndexInfoCreateRequest request);

  IndexInfoResponse update(UUID id, IndexInfoUpdateRequest request);

  void delete(UUID id);

  IndexInfoResponse findById(UUID id);

  IndexInfoListResponse<IndexInfoResponse> findAll(
      IndexInfoSearchRequest request, String cursor, UUID idAfter, int size);
}