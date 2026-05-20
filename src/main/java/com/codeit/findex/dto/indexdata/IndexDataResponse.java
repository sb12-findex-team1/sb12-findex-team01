package com.codeit.findex.dto.indexdata;

import com.codeit.findex.entity.IndexData;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record IndexDataResponse(
    UUID id,
    UUID indexInfoId,
    String indexName,
    LocalDate baseDate,
    BigDecimal openingPrice,
    BigDecimal marketPrice,
    BigDecimal closingPrice,
    BigDecimal highPrice,
    BigDecimal lowPrice,
    BigDecimal versus,
    BigDecimal fluctuationRate,
    Long tradingQuantity,
    Long tradingPrice,
    Long marketTotalAmount,
    Instant createdAt
) {
  public static IndexDataResponse from(IndexData entity) {
    return new IndexDataResponse(
        entity.getId(),
        entity.getIndexInfo().getId(),
        entity.getIndexInfo().getIndexName(),
        entity.getBaseDate(),
        entity.getOpeningPrice(),
        entity.getMarketPrice(),
        entity.getClosingPrice(),
        entity.getHighPrice(),
        entity.getLowPrice(),
        entity.getVersus(),
        entity.getFluctuationRate(),
        entity.getTradingQuantity(),
        entity.getTradingPrice(),
        entity.getMarketTotalAmount(),
        entity.getCreatedAt()
    );
  }
}