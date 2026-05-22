package com.codeit.findex.dto.indexdata;

import java.util.List;
import java.util.UUID;

public record CursorPageResponseIndexDataDto<T>(
      List<T> content,
      String nextCursor,
      UUID nextIdAfter,
      int size,
      long totalElements,
      boolean hasNext
) {

}
