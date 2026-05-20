package com.codeit.findex.dto.indexinfo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record IndexInfoResponse(
    UUID id,
    String indexClassification,
    String indexName,
    Integer employedItemsCount,
    LocalDate basePointInTime,
    BigDecimal baseIndex,
    String sourceType,
    boolean favorite,
    Instant createdAt,
    Instant updatedAt
) {}