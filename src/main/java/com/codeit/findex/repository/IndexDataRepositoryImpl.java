package com.codeit.findex.repository;

import com.codeit.findex.dto.indexdata.IndexDataSearchRequest;
import com.codeit.findex.entity.IndexData;
import com.codeit.findex.entity.QIndexData;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
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
        // 동일 값 tie-breaking
        .orderBy(
            getOrderSpecifier(request.sortField(), request.sortDirection()),
            "asc".equalsIgnoreCase(request.sortDirection()) ? indexData.id.asc() : indexData.id.desc()
        )
        .limit(request.size() + 1)  // +1 체크
        .fetch();
  }

  private BooleanExpression cursorAfter(
      String cursor, UUID idAfter, String sortField, String sortDirection) {
    if (cursor == null || idAfter == null) return null;

    boolean isAsc = "asc".equalsIgnoreCase(sortDirection);

    return switch (sortField) {
      case "tradingQuantity"   -> buildLongCursor(indexData.tradingQuantity,   Long.parseLong(cursor), idAfter, isAsc);
      case "tradingPrice"      -> buildLongCursor(indexData.tradingPrice,      Long.parseLong(cursor), idAfter, isAsc);
      case "marketTotalAmount" -> buildLongCursor(indexData.marketTotalAmount, Long.parseLong(cursor), idAfter, isAsc);
      case "marketPrice"       -> buildBigDecimalCursor(indexData.marketPrice,      new java.math.BigDecimal(cursor), idAfter, isAsc);
      case "closingPrice"      -> buildBigDecimalCursor(indexData.closingPrice,     new java.math.BigDecimal(cursor), idAfter, isAsc);
      case "highPrice"         -> buildBigDecimalCursor(indexData.highPrice,        new java.math.BigDecimal(cursor), idAfter, isAsc);
      case "lowPrice"          -> buildBigDecimalCursor(indexData.lowPrice,         new java.math.BigDecimal(cursor), idAfter, isAsc);
      case "versus"            -> buildBigDecimalCursor(indexData.versus,           new java.math.BigDecimal(cursor), idAfter, isAsc);
      case "fluctuationRate"   -> buildBigDecimalCursor(indexData.fluctuationRate,  new java.math.BigDecimal(cursor), idAfter, isAsc);
      default                  -> buildDateCursor(indexData.baseDate, LocalDate.parse(cursor), idAfter, isAsc);
    };
  }

  private BooleanExpression buildLongCursor(
      com.querydsl.core.types.dsl.NumberPath<Long> path, Long value, UUID idAfter, boolean isAsc) {
    return isAsc
        ? path.gt(value).or(path.eq(value).and(indexData.id.gt(idAfter)))
        : path.lt(value).or(path.eq(value).and(indexData.id.lt(idAfter)));
  }

  private BooleanExpression buildBigDecimalCursor(
      com.querydsl.core.types.dsl.NumberPath<java.math.BigDecimal> path, java.math.BigDecimal value, UUID idAfter, boolean isAsc) {
    return isAsc
        ? path.gt(value).or(path.eq(value).and(indexData.id.gt(idAfter)))
        : path.lt(value).or(path.eq(value).and(indexData.id.lt(idAfter)));
  }

  private BooleanExpression buildDateCursor(
      com.querydsl.core.types.dsl.DatePath<LocalDate> path, LocalDate value, UUID idAfter, boolean isAsc) {
    return isAsc
        ? path.gt(value).or(path.eq(value).and(indexData.id.gt(idAfter)))
        : path.lt(value).or(path.eq(value).and(indexData.id.lt(idAfter)));
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

  @Override
  public long countBySearchCondition(IndexDataSearchRequest request) {
    Long count = queryFactory
        .select(indexData.count())
        .from(indexData)
        .where(
            indexInfoIdEq(request.indexInfoId()),
            baseDateGoe(request.startDate()),
            baseDateLoe(request.endDate())
        )
        .fetchOne();
    return count != null ? count : 0L;
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



}