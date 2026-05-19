package com.codeit.findex.dto.syncJob;

import java.util.List;

public record SyncJobListResponse(
    List<SyncJobDto> content,
    String nextCursor,
    String nextIdAfter,
    int size,
    int totalElements,
    boolean hasNext
) {

}
