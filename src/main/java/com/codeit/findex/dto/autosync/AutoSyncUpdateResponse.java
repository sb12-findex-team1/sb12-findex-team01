package com.codeit.findex.dto.autosync;

import java.util.UUID;

public record AutoSyncUpdateResponse(
    UUID id,
    UUID indexInfoId,
    String indexClassification,
    String indexName,
    boolean enabled
) { }
