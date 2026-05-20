package com.codeit.findex.dto.indexdata;

import java.math.BigDecimal;

public record IndexDataUpdateRequest(
    BigDecimal openingPrice,
    BigDecimal marketPrice,
    BigDecimal closingPrice,
    BigDecimal highPrice,
    BigDecimal lowPrice,
    BigDecimal versus,
    BigDecimal fluctuationRate,
    Long tradingQuantity,
    Long tradingPrice,
    Long marketTotalAmount
) {}