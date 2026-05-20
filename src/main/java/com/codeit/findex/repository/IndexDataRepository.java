package com.codeit.findex.repository;

import com.codeit.findex.entity.IndexData;
import com.codeit.findex.entity.IndexInfo;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IndexDataRepository  extends JpaRepository<IndexData, UUID>, IndexDataRepositoryCustom {

  boolean existsByIndexInfoIdAndBaseDate(@NotNull UUID indexInfoId, LocalDate baseDate);

  Optional<IndexData> findByIndexInfoIdAndBaseDate(UUID indexInfoId, LocalDate baseDate);

  @Query("SELECT MAX(id.baseDate) FROM IndexData id WHERE id.baseDate <= :targetDate")
  Optional<LocalDate> findLatestAvailableDate(@Param("targetDate") LocalDate targetDate);

  @Query("SELECT id FROM IndexData id " +
      "JOIN FETCH id.indexInfo ii " +
      "WHERE id.baseDate = :baseDate")
  List<IndexData> findByBaseDateWithIndexInfo(@Param("baseDate") LocalDate baseDate);
  List<IndexData> findChartRawData(
      @Param("indexInfoId") UUID indexInfoId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );
    Optional<IndexData> findByIndexInfoAndBaseDate(IndexInfo indexInfo, LocalDate baseDate);
}