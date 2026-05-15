package com.codeit.findex.dto.data;

public record RankedIndexPerformanceDto(
    IndexPerformanceDto performance,
    int rank
) {

}
