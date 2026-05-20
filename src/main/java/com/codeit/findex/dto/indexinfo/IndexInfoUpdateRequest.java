package com.codeit.findex.dto.indexinfo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexInfoUpdateRequest(
    Integer employedItemsCount,  // 채용 종목 수
    BigDecimal baseIndex,        // 기준 지수
    LocalDate basePointInTime,   // 기준 시점
    boolean favorite             // 즐겨찾기 여부
) {}