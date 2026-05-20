package com.codeit.findex.service;

import com.codeit.findex.dto.client.StockMarketIndexRequest;
import com.codeit.findex.dto.indexdata.IndexDataSyncRequest;
import com.codeit.findex.entity.IndexData;
import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.entity.SourceType;
import java.util.List;

public interface ClientIndexSyncService {

  List<IndexInfo> syncIndexInfo(StockMarketIndexRequest request, SourceType sourceType, String ip);

  List<IndexData> syncIndexData(IndexDataSyncRequest request, String ip);

}
