package com.codeit.findex.dto.indexdata;

public record RankedIndexPerformanceDto(
    IndexPerformanceDto performance,
    int rank
) {

}
