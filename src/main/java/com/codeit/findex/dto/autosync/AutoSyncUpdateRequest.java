package com.codeit.findex.dto.autosync;

import jakarta.validation.constraints.NotNull;

public record AutoSyncUpdateRequest(
    @NotNull boolean enabled
) { }
