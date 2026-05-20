package com.codeit.findex.dto.indexinfo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexInfoCreateRequest(

    @NotBlank String indexName,            // 지수명
    @NotBlank String indexClassification,  // 지수 분류명
    @NotNull Integer employedItemsCount,  // 채용 종목 수
    @NotNull BigDecimal baseIndex,        // 기준 지수
    @NotNull LocalDate basePointInTime,     // 기준 시점
    boolean favorite             // 즐겨찾기
) {}