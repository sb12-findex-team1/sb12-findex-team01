package com.codeit.findex.dto.indexdata;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IndexDataCreateRequest {

  @NotNull
  private UUID indexInfoId;

  @NotNull
  private LocalDate baseDate;

  private BigDecimal openingPrice;
  private BigDecimal marketPrice;
  private BigDecimal closingPrice;
  private BigDecimal highPrice;
  private BigDecimal lowPrice;
  private BigDecimal versus;
  private BigDecimal fluctuationRate;
  private Long tradingQuantity;
  private Long tradingPrice;
  private Long marketTotalAmount;
}