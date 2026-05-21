package com.codeit.findex.repository;

import com.codeit.findex.dto.indexdata.IndexDataSearchRequest;
import com.codeit.findex.entity.IndexData;
import com.codeit.findex.entity.QIndexData;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
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
            cursorAfter(request.cursor(), request.idAfter(), request.sortField(), request.sortDirection())
        )
        .orderBy(
            getOrderSpecifier(request.sortField(), request.sortDirection()),
            indexData.id.asc()  // 동일 값 tie-breaking
        )
        .limit(request.size() + 1)  // +1 체크
        .fetch();
  }

  private BooleanExpression cursorAfter(Object cursor, UUID idAfter, String sortField, String sortDirection) {
    if (cursor == null || idAfter == null) return null;

    boolean isAsc = "asc".equalsIgnoreCase(sortDirection);
    ComparableExpression<Comparable<Object>> path = getSortPath(sortField);

    @SuppressWarnings("unchecked")
    Comparable<Object> cursorValue = (Comparable<Object>) cursor;


    // 같은 값이면 id로 tie-break, 다른 값이면 방향에 따라
    return isAsc
        ? path.gt(cursorValue).or(path.eq(cursorValue).and(indexData.id.gt(idAfter)))
        : path.lt(cursorValue).or(path.eq(cursorValue).and(indexData.id.gt(idAfter)));
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
  @SuppressWarnings("unchecked")
  private ComparableExpression<Comparable<Object>> getSortPath(String sortField) {
    return (ComparableExpression<Comparable<Object>>) switch (sortField) {
      case "marketPrice"       -> indexData.marketPrice;
      case "closingPrice"      -> indexData.closingPrice;
      case "highPrice"         -> indexData.highPrice;
      case "lowPrice"          -> indexData.lowPrice;
      case "versus"            -> indexData.versus;
      case "fluctuationRate"   -> indexData.fluctuationRate;
      case "tradingQuantity"   -> indexData.tradingQuantity;
      case "tradingPrice"      -> indexData.tradingPrice;
      case "marketTotalAmount" -> indexData.marketTotalAmount;
      default                  -> indexData.baseDate;
    };
  }


}