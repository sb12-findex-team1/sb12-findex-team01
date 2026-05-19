package com.codeit.findex.dto.indexdata;

import com.codeit.findex.entity.IndexData;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IndexDataResponse {

  private UUID id;
  private UUID indexInfoId;
  private String indexName;
  private LocalDate baseDate;
  private LocalDate openingPrice;
  private BigDecimal marketPrice;
  private BigDecimal closingPrice;
  private BigDecimal highPrice;
  private BigDecimal lowPrice;
  private BigDecimal versus;
  private BigDecimal fluctuationRate;
  private Long tradingQuantity;
  private Long tradingPrice;
  private Long marketTotalAmount;
  private Instant createdAt;

  public static IndexDataResponse from(IndexData entity) {
    return IndexDataResponse.builder()
        .id(entity.getId())
        .indexInfoId(entity.getIndexInfo().getId())
        .indexName(entity.getIndexInfo().getIndexName())
        .baseDate(entity.getBaseDate())
        .marketPrice(entity.getMarketPrice())
        .closingPrice(entity.getClosingPrice())
        .highPrice(entity.getHighPrice())
        .lowPrice(entity.getLowPrice())
        .versus(entity.getVersus())
        .fluctuationRate(entity.getFluctuationRate())
        .tradingQuantity(entity.getTradingQuantity())
        .tradingPrice(entity.getTradingPrice())
        .marketTotalAmount(entity.getMarketTotalAmount())
        .createdAt(entity.getCreatedAt())
        .build();
  }
}
