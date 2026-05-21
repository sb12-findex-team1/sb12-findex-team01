package com.codeit.findex.dto.indexdata;

import java.time.LocalDate;
import java.util.UUID;

public record IndexDataSearchRequest(
    UUID indexInfoId,
    LocalDate startDate,
    LocalDate endDate,
    Long idAfter,           // 이전 페이지 마지막 요소 ID
    String cursor,          // 커서
    String sortField,       // baseDate, marketPrice, closingPrice
    String sortDirection,
    int size
) {

  public IndexDataSearchRequest {
    if (size <= 0)
      size = 10;
    if (sortField == null)
      sortField = "baseDate";
    if (sortDirection == null)
      sortDirection = "desc";
  }
}