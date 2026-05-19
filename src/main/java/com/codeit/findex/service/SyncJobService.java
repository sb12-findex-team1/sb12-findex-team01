package com.codeit.findex.service;

import com.codeit.findex.dto.syncJob.IndexDataSyncRequest;

public interface SyncJobService {

  void syncIndexData(IndexDataSyncRequest request);

}
