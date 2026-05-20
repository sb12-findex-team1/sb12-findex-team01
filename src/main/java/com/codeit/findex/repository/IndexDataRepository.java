package com.codeit.findex.repository;

import com.codeit.findex.entity.IndexData;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
}
