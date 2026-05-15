package com.codeit.dto.data;

public record RankedIndexPerformanceDto(
    IndexPerformanceDto performance,
    int rank
) {

}
