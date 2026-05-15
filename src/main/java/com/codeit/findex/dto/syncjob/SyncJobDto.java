package com.codeit.findex.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncJobDto {
  private UUID id;
  private String jobType;
  private Long indexInfoId;
  private Instant targetDate;
  private String worker;
  private Instant jobTime;
  private String result;
}
