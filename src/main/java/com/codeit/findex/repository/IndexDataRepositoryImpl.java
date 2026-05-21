package com.codeit.findex.repository;

import com.codeit.findex.dto.indexdata.IndexDataSearchRequest;
import com.codeit.findex.entity.IndexData;
import com.codeit.findex.entity.QIndexData;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IndexDataRepositoryImpl implements IndexDataRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  private final QIndexData indexData = QIndexData.indexData;

  @Override
  public List<IndexData> search(IndexDataSearchRequest request) {
    return queryFactory
        .selectFrom(indexData)
        .where(
            indexInfoIdEq(request.indexInfoId()),
            baseDateGoe(request.startDate()),
            baseDateLoe(request.endDate()),
            idAfterCursor(request.idAfter())
        )
        .orderBy(getOrderSpecifier(request.sortField(), request.sortDirection()))
        .limit(request.size())
        .fetch();
  }
  private BooleanExpression idAfterCursor(Long idAfter) {
    if (idAfter == null) return null;
    // idAfter번째 이후 데이터 → baseDate 오프셋으로 처리
    return indexData.baseDate.lt(
        queryFactory
            .select(indexData.baseDate)
            .from(indexData)
            .orderBy(indexData.baseDate.desc())
            .offset(idAfter)
            .limit(1)
            .fetchOne()
    );
  }

  private OrderSpecifier<?> getOrderSpecifier(String sortField, String sortDirection) {
    boolean isAsc = "asc".equalsIgnoreCase(sortDirection);
    return switch (sortField) {
      case "marketPrice"      -> isAsc ? indexData.marketPrice.asc()      : indexData.marketPrice.desc();
      case "closingPrice"     -> isAsc ? indexData.closingPrice.asc()     : indexData.closingPrice.desc();
      case "highPrice"        -> isAsc ? indexData.highPrice.asc()        : indexData.highPrice.desc();
      case "lowPrice"         -> isAsc ? indexData.lowPrice.asc()         : indexData.lowPrice.desc();
      case "versus"           -> isAsc ? indexData.versus.asc()           : indexData.versus.desc();
      case "fluctuationRate"  -> isAsc ? indexData.fluctuationRate.asc()  : indexData.fluctuationRate.desc();
      case "tradingQuantity"  -> isAsc ? indexData.tradingQuantity.asc()  : indexData.tradingQuantity.desc();
      case "tradingPrice"     -> isAsc ? indexData.tradingPrice.asc()     : indexData.tradingPrice.desc();
      case "marketTotalAmount"-> isAsc ? indexData.marketTotalAmount.asc(): indexData.marketTotalAmount.desc();
      default                 -> isAsc ? indexData.baseDate.asc()         : indexData.baseDate.desc();
    };
  }

  @Override
  public List<IndexData> findAllForExport(IndexDataSearchRequest request) {
    return queryFactory
        .selectFrom(indexData)
        .where(
            indexInfoIdEq(request.indexInfoId()),
            baseDateGoe(request.startDate()),
            baseDateLoe(request.endDate())
        )
        .orderBy(getOrderSpecifier(request.sortField(), request.sortDirection()))
        .fetch();
  }

  private BooleanExpression indexInfoIdEq(UUID indexInfoId) {
    return indexInfoId != null ? indexData.indexInfo.id.eq(UUID.fromString(indexInfoId.toString())) : null;
  }

  private BooleanExpression baseDateGoe(LocalDate startDate) {
    return startDate != null ? indexData.baseDate.goe(startDate) : null;
  }

  private BooleanExpression baseDateLoe(LocalDate endDate) {
    return endDate != null ? indexData.baseDate.loe(endDate) : null;
  }

  private OrderSpecifier<?> getOrderSpecifier(String sortBy) {
    if ("date_asc".equals(sortBy)) return indexData.baseDate.asc();
    return indexData.baseDate.desc();
  }
}