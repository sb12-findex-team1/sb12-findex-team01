package com.codeit.findex.service;

import com.codeit.findex.dto.syncJob.IndexDataSyncRequest;
import com.codeit.findex.dto.syncJob.SyncJobDto;
import com.codeit.findex.dto.syncJob.SyncJobListResponse;
import com.codeit.findex.dto.syncJob.SyncJobSearchRequest;
import java.util.List;

public interface SyncJobService {

  List<SyncJobDto> syncIndexData(IndexDataSyncRequest request);

  SyncJobListResponse findAll(SyncJobSearchRequest request);

}
