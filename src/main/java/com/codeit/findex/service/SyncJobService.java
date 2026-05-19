package com.codeit.findex.service;

import com.codeit.findex.dto.syncJob.IndexDataSyncRequest;
import com.codeit.findex.dto.syncJob.SyncJobDto;
import java.util.List;

public interface SyncJobService {

  void syncIndexData(IndexDataSyncRequest request);

  List<SyncJobDto> findAll();

}
