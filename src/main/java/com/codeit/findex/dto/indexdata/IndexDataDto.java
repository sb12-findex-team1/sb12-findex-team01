package com.codeit.findex.dto.indexdata;

import com.codeit.findex.entity.IndexData;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record IndexDataDto(
    UUID id,
    UUID indexInfoId,
    LocalDate baseDate,
    String sourceType,
    BigDecimal marketPrice,
    BigDecimal closingPrice,
    BigDecimal highPrice,
    BigDecimal lowPrice,
    BigDecimal versus,
    BigDecimal fluctuationRate,
    Long tradingQuantity,
    Long tradingPrice,
    Long marketTotalAmount
) {
  public static IndexDataDto from(IndexData entity) {
    return new IndexDataDto(

        entity.getId(),
        entity.getIndexInfo().getId(),
        entity.getBaseDate(),
        null,
        entity.getMarketPrice(),
        entity.getClosingPrice(),
        entity.getHighPrice(),
        entity.getLowPrice(),
        entity.getVersus(),
        entity.getFluctuationRate(),
        entity.getTradingQuantity(),
        entity.getTradingPrice(),
        entity.getMarketTotalAmount()
    );
  }
}