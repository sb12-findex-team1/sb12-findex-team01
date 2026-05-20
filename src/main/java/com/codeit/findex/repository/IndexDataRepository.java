package com.codeit.findex.repository;

import com.codeit.findex.dto.indexdata.IndexDataSearchRequest;
import com.codeit.findex.entity.IndexData;
import com.codeit.findex.entity.IndexInfo;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IndexDataRepository extends JpaRepository<IndexData, UUID> {

  @Query("SELECT id FROM IndexData id " +
      "WHERE id.indexInfo.id = :indexInfoId " +
      "AND id.baseDate BETWEEN :startDate AND :endDate " +
      "ORDER BY id.baseDate DESC")
  List<IndexData> findChartRawData(
      @Param("indexInfoId") UUID indexInfoId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );

  @Query("SELECT MAX(id.baseDate) FROM IndexData id WHERE id.baseDate <= :targetDate")
  Optional<LocalDate> findLatestAvailableDate(@Param("targetDate") LocalDate targetDate);

  @Query("SELECT id FROM IndexData id " +
      "JOIN FETCH id.indexInfo ii " +
      "WHERE id.baseDate = :baseDate")
  List<IndexData> findByBaseDateWithIndexInfo(@Param("baseDate") LocalDate baseDate);

  Optional<IndexData> findByIndexInfoAndBaseDate(IndexInfo indexInfo, LocalDate baseDate);

  List<IndexData> findAllForExport(IndexDataSearchRequest request);

  Slice<IndexData> search(IndexDataSearchRequest request, Pageable pageable);

  boolean existsByIndexInfoIdAndBaseDate(@NotNull UUID uuid, @NotNull LocalDate localDate);
}
