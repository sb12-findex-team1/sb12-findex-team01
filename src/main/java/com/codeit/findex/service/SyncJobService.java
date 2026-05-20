package com.codeit.findex.service;

import com.codeit.findex.dto.client.StockMarketIndexRequest;
import com.codeit.findex.dto.indexdata.IndexDataSyncRequest;
import com.codeit.findex.dto.syncjob.SyncJobDto;
import com.codeit.findex.dto.syncjob.SyncJobListResponse;
import com.codeit.findex.dto.syncjob.SyncJobSearchRequest;
import com.codeit.findex.dto.syncjob.SyncJobStatsDto;
import java.util.List;

public interface SyncJobService {

  List<SyncJobDto> syncIndexInfos(StockMarketIndexRequest request, String ip);

  List<SyncJobDto> syncIndexData(IndexDataSyncRequest request, String ip);

  SyncJobListResponse findAll(SyncJobSearchRequest request);

  SyncJobStatsDto getStats();

}
