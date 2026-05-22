package com.codeit.findex.dto.indexdata;

import java.util.List;
import java.util.UUID;

public record IndexChartDto(
    UUID indexInfoId,                    // 지수 정보 ID
    String indexClassification,          // 지수 분류 (예: "KOSPI시리즈")
    String indexName,                    // 지수 명칭 (예: "IT서비스")
    String periodType,                   // 기간 타입 (예: "DAILY")
    List<ChartDataPoint> dataPoints,     // 지수 차트 데이터 리스트
    List<ChartDataPoint> ma5DataPoints,  // 5일 이동평균선 데이터
    List<ChartDataPoint> ma20DataPoints  // 20일 이동평균선 데이터
) {

}
