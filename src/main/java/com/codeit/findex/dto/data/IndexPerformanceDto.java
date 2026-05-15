package com.codeit.findex.dto.data;

import java.util.UUID;

public record IndexPerformanceDto(
    UUID indexInfoId,
    String indexClassification,
    String indexName,
    Double versus,
    Double fluctuationRate,
    Double currentPrice,
    Double beforePrice
) {

}
