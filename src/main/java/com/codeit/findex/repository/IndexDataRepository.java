package com.codeit.findex.repository;

import com.codeit.findex.entity.IndexData;
import com.codeit.findex.entity.IndexInfo;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndexDataRepository  extends JpaRepository<IndexData, UUID>, IndexDataRepositoryCustom {

  boolean existsByIndexInfoIdAndBaseDate(@NotNull UUID indexInfoId, LocalDate baseDate);
  Optional<IndexData> findByIndexInfoIdAndBaseDate(UUID indexInfoId, LocalDate baseDate);

  @Query("SELECT MAX(id.baseDate) FROM IndexData id WHERE id.baseDate <= :targetDate")
  Optional<LocalDate> findLatestAvailableDate(@Param("targetDate") LocalDate targetDate);

  @Query("SELECT id FROM IndexData id " +
      "JOIN FETCH id.indexInfo ii " +
      "WHERE id.baseDate = :baseDate")
  List<IndexData> findByBaseDateWithIndexInfo(@Param("baseDate") LocalDate baseDate);

  Optional<IndexData> findByIndexInfoAndBaseDate(IndexInfo indexInfo, LocalDate baseDate);
}
