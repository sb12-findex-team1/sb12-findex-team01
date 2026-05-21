package com.codeit.findex.dto.indexdata;

import java.time.LocalDate;
import java.util.UUID;

public record IndexDataSearchRequest(
    UUID indexInfoId,
    LocalDate startDate,
    LocalDate endDate,
    UUID idAfter,           // 이전 페이지 마지막 요소 ID
    Object cursor,          // 커서
    String sortField,       // baseDate, marketPrice, closingPrice
    String sortDirection,
    Integer size
) {

  public IndexDataSearchRequest {
    if (size == null || size<=0)
      size = 10;
    if (sortField == null)
      sortField = "baseDate";
    if (sortDirection == null)
      sortDirection = "desc";
  }
}