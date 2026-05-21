package com.codeit.findex.dto.indexdata;

import com.codeit.findex.entity.IndexData;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

public record IndexDataResponse <T> (
    List<T> content,
    String nextCursor,
    UUID nextIdAfter,
    int size,
    long totalElements,
    boolean hasNext
) {
}

