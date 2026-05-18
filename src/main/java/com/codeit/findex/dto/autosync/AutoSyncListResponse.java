package com.codeit.findex.dto.autosync;

import java.util.List;

public record AutoSyncListResponse(
    List<AutoSyncResponse> content,
    String nextCursor,
    String nextIdAfter,
    int size,
    int totalElements,
    boolean hasNext
) { }
