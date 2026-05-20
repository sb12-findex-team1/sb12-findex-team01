package com.codeit.findex.dto.syncJob;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SyncJobDto(
    UUID id,
    String jobType,
    UUID indexInfoId,
    LocalDate targetDate,
    String worker,
    Instant jobTime,
    String result
) {

}
