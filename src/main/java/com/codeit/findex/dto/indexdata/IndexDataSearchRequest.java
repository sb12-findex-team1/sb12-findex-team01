package com.codeit.findex.dto.indexdata;

import java.time.LocalDate;
import java.util.UUID;

public record IndexDataSearchRequest(
    UUID indexInfoId,
    LocalDate startDate,
    LocalDate endDate,
    String sortBy,
    Integer page,
    Integer size
) {

  public IndexDataSearchRequest {
    if (page == null || page < 0) {
      page = 0;
    }
    if (size == null || size <= 0) {
      size = 20;
    }
  }

  public IndexDataSearchRequest(UUID indexInfoId, LocalDate startDate, LocalDate endDate,
      String sortBy) {
    this(indexInfoId, startDate, endDate, sortBy, 0, 20);
  }
}