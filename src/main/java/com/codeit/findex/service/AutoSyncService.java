package com.codeit.findex.service;

import com.codeit.findex.dto.autosync.AutoSyncListResponse;
import com.codeit.findex.dto.autosync.AutoSyncSearchRequest;
import com.codeit.findex.dto.autosync.AutoSyncUpdateRequest;
import com.codeit.findex.dto.autosync.AutoSyncUpdateResponse;
import java.util.UUID;

public interface AutoSyncService {

  AutoSyncUpdateResponse updateAutoSync(UUID id, AutoSyncUpdateRequest request);

  AutoSyncListResponse findAutoSyncList(AutoSyncSearchRequest request);

}
