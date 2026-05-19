package com.codeit.findex.entity;

import com.codeit.findex.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;

@Entity
@Table(name = "index_data",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_index_data_index_info_base_date",
            columnNames = {"index_info_id", "base_date"}
        )
    }
)

@ToString(callSuper = true, exclude = "indexInfo")
@Getter
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IndexData extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "index_info_id", nullable = false)
  private IndexInfo indexInfo;

  @Column(nullable = false)
  private LocalDate baseDate;

  private BigDecimal openingPrice;
  private BigDecimal marketPrice;
  private BigDecimal closingPrice;
  private BigDecimal highPrice;
  private BigDecimal lowPrice;
  private BigDecimal versus;
  private BigDecimal fluctuationRate;
  private Long tradingQuantity;
  private Long tradingPrice;
  private Long marketTotalAmount;

  @Builder
  public IndexData(IndexInfo indexInfo, LocalDate baseDate,
      BigDecimal openingPrice, BigDecimal marketPrice,
      BigDecimal closingPrice, BigDecimal highPrice,
      BigDecimal lowPrice, BigDecimal versus,
      BigDecimal fluctuationRate, Long tradingQuantity,
      Long tradingPrice, Long marketTotalAmount) {
    this.indexInfo = indexInfo;
    this.baseDate = baseDate;
    this.openingPrice = openingPrice;
    this.marketPrice = marketPrice;
    this.closingPrice = closingPrice;
    this.highPrice = highPrice;
    this.lowPrice = lowPrice;
    this.versus = versus;
    this.fluctuationRate = fluctuationRate;
    this.tradingQuantity = tradingQuantity;
    this.tradingPrice = tradingPrice;
    this.marketTotalAmount = marketTotalAmount;
  }
  public void update(BigDecimal openingPrice, BigDecimal marketPrice,
      BigDecimal closingPrice, BigDecimal highPrice,
      BigDecimal lowPrice, BigDecimal versus,
      BigDecimal fluctuationRate, Long tradingQuantity,
      Long tradingPrice, Long marketTotalAmount) {
    this.openingPrice = openingPrice;
    this.marketPrice = marketPrice;
    this.closingPrice = closingPrice;
    this.highPrice = highPrice;
    this.lowPrice = lowPrice;
    this.versus = versus;
    this.fluctuationRate = fluctuationRate;
    this.tradingQuantity = tradingQuantity;
    this.tradingPrice = tradingPrice;
    this.marketTotalAmount = marketTotalAmount;
  }
}