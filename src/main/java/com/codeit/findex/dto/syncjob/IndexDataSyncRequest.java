package com.codeit.findex.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndexDataSyncRequest {
  private List<UUID> indexInfoIds;
  private Instant baseDateFrom;
  private Instant baseDateTo;
}
