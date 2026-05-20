package com.codeit.findex.entity;

import com.codeit.findex.dto.indexdata.IndexDataUpdateRequest;
import com.codeit.findex.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IndexData extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "index_info_id", nullable = false)
  private IndexInfo indexInfo;

  @Column(name = "base_date", nullable = false)
  private LocalDate baseDate;

  @Column(name = "opening_price", precision = 10, scale = 2)
  private BigDecimal openingPrice;

  @Column(name = "market_price", precision = 20, scale = 2)
  private BigDecimal marketPrice;

  @Column(name = "closing_price", precision = 20, scale = 2)
  private BigDecimal closingPrice;

  @Column(name = "high_price", precision = 20, scale = 2)
  private BigDecimal highPrice;

  @Column(name = "low_price", precision = 20, scale = 2)
  private BigDecimal lowPrice;

  @Column(name = "versus", precision = 20, scale = 2)
  private BigDecimal versus;

  @Column(name = "fluctuation_rate", precision = 10, scale = 2)
  private BigDecimal fluctuationRate;

  @Column(name = "trading_quantity")
  private Long tradingQuantity;

  @Column(name = "trading_price")
  private Long tradingPrice;

  @Column(name = "market_total_amount")
  private Long marketTotalAmount;

  public void update(IndexDataUpdateRequest request) {
    if (request.openingPrice() != null) this.openingPrice = request.openingPrice();
    if (request.marketPrice() != null) this.marketPrice = request.marketPrice();
    if (request.closingPrice() != null) this.closingPrice = request.closingPrice();
    if (request.highPrice() != null) this.highPrice = request.highPrice();
    if (request.lowPrice() != null) this.lowPrice = request.lowPrice();
    if (request.versus() != null) this.versus = request.versus();
    if (request.fluctuationRate() != null) this.fluctuationRate = request.fluctuationRate();
    if (request.tradingQuantity() != null) this.tradingQuantity = request.tradingQuantity();
    if (request.tradingPrice() != null) this.tradingPrice = request.tradingPrice();
    if (request.marketTotalAmount() != null) this.marketTotalAmount = request.marketTotalAmount();
  }
}