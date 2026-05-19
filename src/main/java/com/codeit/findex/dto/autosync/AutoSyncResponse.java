package com.codeit.findex.dto.autosync;

import java.util.UUID;

public record AutoSyncResponse(
    UUID id,
    UUID indexInfoId,
    String indexClassification,
    String indexName,
    boolean enabled
) { }
