package com.codeit.findex.dto.syncJob;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SyncJobSearchRequest(
    String jobType,
    UUID indexInfoId,
    LocalDate baseDateFrom,
    LocalDate baseDateTo,
    String worker,
    Instant jobTimeFrom,
    Instant jobTimeTo,
    String status,
    UUID idAfter,
    String cursor,
    String sortField,
    String sortDirection,
    Integer size
) {

}
