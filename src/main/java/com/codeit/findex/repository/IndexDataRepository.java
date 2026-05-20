package com.codeit.findex.repository;

import com.codeit.findex.entity.IndexData;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndexDataRepository  extends JpaRepository<IndexData, UUID>, IndexDataRepositoryCustom {

  boolean existsByIndexInfoIdAndBaseDate(@NotNull UUID indexInfoId, LocalDate baseDate);
  Optional<IndexData> findByIndexInfoIdAndBaseDate(UUID indexInfoId, LocalDate baseDate);

}
