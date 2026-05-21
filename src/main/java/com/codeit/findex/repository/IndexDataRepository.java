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

  @Query("select id from IndexData id " +
      "where id.indexInfo.id = :indexInfoId " +
      "and id.baseDate between :startDate and :endDate " +
      "order by id.baseDate asc ")
  List<IndexData> findChartRawData(
      @Param("indexInfoId") UUID indexInfoId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );

  @Query("select count(id) from IndexData id " +
      "where id.indexInfo.id = :indexInfoId " +
      "and id.baseDate between :startDate and :endDate")
  long countByPeriod(
      @Param("indexInfoId") UUID indexInfoId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );

  @Query("select MAX(id.baseDate) from IndexData id where id.baseDate <= :targetDate")
  Optional<LocalDate> findLatestAvailableDate(@Param("targetDate") LocalDate targetDate);

  @Query("select id from IndexData id " +
      "join fetch id.indexInfo ii " +
      "where id.baseDate = :baseDate")
  List<IndexData> findByBaseDateWithIndexInfo(@Param("baseDate") LocalDate baseDate);

  @Query("""
      SELECT d
      FROM IndexData d
      JOIN FETCH d.indexInfo i
      WHERE i.id IN :indexInfoIds
        AND d.baseDate BETWEEN :startDate AND :endDate
      """)
  List<IndexData> findByIndexInfoIdInAndBaseDateBetween(
      @Param("indexInfoIds") List<UUID> indexInfoIds,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );
  Optional<IndexData> findByIndexInfoAndBaseDate(IndexInfo indexInfo, LocalDate baseDate);

//  List<IndexData> findAllForExport(IndexDataSearchRequest request);

//  Slice<IndexData> search(IndexDataSearchRequest request, Pageable pageable);

  boolean existsByIndexInfoIdAndBaseDate(@NotNull UUID uuid, @NotNull LocalDate localDate);
}
