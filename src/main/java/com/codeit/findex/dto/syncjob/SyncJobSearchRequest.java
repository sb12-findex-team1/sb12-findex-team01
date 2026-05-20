package com.codeit.findex.dto.syncjob;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SyncJobSearchRequest(
    String jobType,
    UUID indexInfoId,
    LocalDate baseDateFrom,
    LocalDate baseDateTo,
    String worker,
    LocalDateTime jobTimeFrom,
    LocalDateTime jobTimeTo,
    String status,
    UUID idAfter,
    String cursor,
    String sortField,
    String sortDirection,
    Integer size
) {

}
