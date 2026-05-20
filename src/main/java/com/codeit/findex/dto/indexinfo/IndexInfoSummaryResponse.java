package com.codeit.findex.dto.indexinfo;

import java.util.UUID;

public record IndexInfoSummaryResponse(
    UUID id,
    String indexClassification,
    String indexName
) {}