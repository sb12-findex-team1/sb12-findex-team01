package com.codeit.findex.service;

import com.codeit.findex.dto.syncJob.IndexDataSyncRequest;
import com.codeit.findex.dto.syncJob.SyncJobListResponse;
import com.codeit.findex.dto.syncJob.SyncJobSearchRequest;

public interface SyncJobService {

  void syncIndexData(IndexDataSyncRequest request);

  SyncJobListResponse findAll(SyncJobSearchRequest request);

}
