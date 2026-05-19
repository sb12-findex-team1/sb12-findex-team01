package com.codeit.findex.dto.syncJob;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record IndexDataSyncRequest(
    List<UUID> indexInfoIds,
    LocalDate baseDateFrom,
    LocalDate baseDateTo
) {

}
