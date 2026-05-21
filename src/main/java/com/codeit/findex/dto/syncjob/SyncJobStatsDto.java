package com.codeit.findex.dto.syncjob;

import java.time.Instant;

public record SyncJobStatsDto(
    long totalSuccess,
    long totalFailed,
    Instant latestSync
) {

}
