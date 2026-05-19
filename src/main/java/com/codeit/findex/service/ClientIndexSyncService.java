package com.codeit.findex.service;

import com.codeit.findex.dto.indexdata.IndexDataSyncRequest;
import com.codeit.findex.dto.client.StockMarketIndexRequest;
import com.codeit.findex.dto.syncjob.SyncJobDto;
import com.codeit.findex.entity.SourceType;
import java.util.List;

public interface ClientIndexSyncService {
  List<SyncJobDto> syncIndexInfo(StockMarketIndexRequest request, SourceType sourceType, String ip);

  List<SyncJobDto> syncIndexData(IndexDataSyncRequest request, String ip);

}