package com.codeit.findex.dto.indexinfo;

import java.util.List;

public record IndexInfoListResponse<T>(
    List<T> content,
    String nextCursor,
    String nextIdAfter,
    int size,
    int totalElements,
    boolean hasNext
) {}