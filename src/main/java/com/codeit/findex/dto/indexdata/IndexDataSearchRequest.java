package com.codeit.findex.dto.indexdata;


import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IndexDataSearchRequest {

  private UUID indexInfoId;
  private LocalDate startDate;
  private LocalDate endDate;
  private String sortBy;
  private int page = 0;
  private int size = 20;

}
