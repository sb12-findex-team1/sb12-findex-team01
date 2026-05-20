package com.codeit.findex.entity;

import com.codeit.findex.dto.indexinfo.IndexInfoUpdateRequest;
import com.codeit.findex.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "index_infos",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_index_infos_index_classification_index_name",
            columnNames = {"index_classification", "index_name"}
        )
    }
)
@Getter
@ToString(callSuper = true, exclude = {
    "syncJobs",
    "indexData"
})
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IndexInfo extends BaseEntity {

  @Column(name = "index_name", nullable = false, length = 100)
  private String indexName;

  @Column(name = "index_classification", nullable = false, length = 50)
  private String indexClassification;

  @Column(name = "source_type", nullable = false, length = 20)
  private String sourceType;

  @Column(name = "employed_items_count")
  private Integer employedItemsCount;

  @Column(name = "base_index", precision = 10, scale = 2)
  private BigDecimal baseIndex;

  @Column(name = "base_point_in_time")
  private LocalDate basePointInTime;

  @Column(name = "favorite", nullable = false)
  private boolean favorite;

  @OneToMany(
      mappedBy = "indexInfo",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true
  )
  private List<IndexData> indexData = new ArrayList<>();

  @OneToMany(
      mappedBy = "indexInfo",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true
  )
  private List<SyncJob> syncJobs = new ArrayList<>();

  public void updateByOpenApi(
      Integer employedItemsCount,
      LocalDate basePointInTime,
      BigDecimal baseIndex
  ) {
    this.employedItemsCount = employedItemsCount;
    this.basePointInTime = basePointInTime;
    this.baseIndex = baseIndex;
  }
}

  public void update(IndexInfoUpdateRequest request) {
    this.employedItemsCount = request.employedItemsCount();
    this.basePointInTime = request.basePointInTime();
    this.baseIndex = request.baseIndex();
    this.favorite = request.favorite();
  }
}
