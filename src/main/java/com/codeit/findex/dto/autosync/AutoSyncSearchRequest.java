package com.codeit.findex.dto.autosync;

import java.util.UUID;

public record AutoSyncSearchRequest(
    UUID indexInfoId,
    Boolean enabled,
    UUID idAfter,
    String cursor,
    String sortField,
    String sortDirection,
    Integer size
) { }
