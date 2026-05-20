package com.codeit.findex.dto.indexinfo;

public record IndexInfoSearchRequest(
    String indexClassification,  // 분류명 검색 (부분일치)
    String indexName,            // 지수명 검색 (부분일치)
    Boolean favorite,            // 즐겨찾기 필터 (완전일치)
    String sortField,            // 정렬 기준 (indexClassification, indexName, employedItemsCount)
    String sortDirection         // 정렬 방향 (ASC, DESC)
) {}