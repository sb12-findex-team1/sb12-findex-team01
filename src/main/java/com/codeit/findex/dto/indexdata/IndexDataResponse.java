package com.codeit.findex.dto.indexdata;

import com.codeit.findex.entity.IndexData;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

public record IndexDataResponse(
    UUID id,
    UUID indexInfoId,
    LocalDate baseDate,
    BigDecimal marketPrice,
    BigDecimal closingPrice,
    BigDecimal highPrice,
    BigDecimal lowPrice,
    BigDecimal versus,
    BigDecimal fluctuationRate,
    Long tradingQuantity,
    Long tradingPrice,
    Long marketTotalAmount,
    String sourceType
) {
  public static IndexDataResponse from(IndexData indexData) {
    return new IndexDataResponse(
        indexData.getId(),
        indexData.getIndexInfo().getId(),
        indexData.getBaseDate(),
        indexData.getMarketPrice(),
        indexData.getClosingPrice(),
        indexData.getHighPrice(),
        indexData.getLowPrice(),
        indexData.getVersus(),
        indexData.getFluctuationRate(),
        indexData.getTradingQuantity(),
        indexData.getTradingPrice(),
        indexData.getMarketTotalAmount(),
        indexData.getIndexInfo().getSourceType()
    );
  }
}