package com.codeit.findex.service;

import com.codeit.findex.dto.autosync.AutoSyncUpdateRequest;
import com.codeit.findex.dto.autosync.AutoSyncUpdateResponse;
import java.util.UUID;

//todo: 여긴 service 어노테이션 안 붙여??
public interface AutoSyncService {

  AutoSyncUpdateResponse updateAutoSync(UUID id, AutoSyncUpdateRequest request);

}
