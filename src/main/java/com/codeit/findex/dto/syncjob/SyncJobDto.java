package com.codeit.findex.dto.syncjob;

import com.codeit.findex.entity.JobType;
import com.codeit.findex.entity.Result;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SyncJobDto(
    UUID id,
    JobType jobType,
    UUID indexInfoId,
    LocalDate targetDate,
    String worker,
    Instant jobTime,
    Result result
) {

}
