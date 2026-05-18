package com.codeit.findex.service.impl;

import com.codeit.findex.dto.autosync.AutoSyncUpdateRequest;
import com.codeit.findex.dto.autosync.AutoSyncUpdateResponse;
import com.codeit.findex.entity.AutoSync;
import com.codeit.findex.repository.AutoSyncRepository;
import com.codeit.findex.service.AutoSyncService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutoSyncServiceImpl implements AutoSyncService {

  private final AutoSyncRepository autoSyncRepository;

  @Transactional
  @Override
  public AutoSyncUpdateResponse updateAutoSync(UUID id, AutoSyncUpdateRequest request) {
    AutoSync autoSync = autoSyncRepository.findById(id).orElseThrow(
        () -> new IllegalArgumentException("autoSync 존재 안 함")
    );

    if (request.enabled()) {
      autoSync.enable();
    } else {
      autoSync.disable();
    }

    return new AutoSyncUpdateResponse(autoSync.getId(),
        autoSync.getIndexInfo().getId(),
        autoSync.getIndexInfo().getIndexClassification(),
        autoSync.getIndexInfo().getIndexName(),
        autoSync.isEnabled()
    );
  }

}
