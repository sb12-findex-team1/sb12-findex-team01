
package com.codeit.findex.repository.impl;

import com.codeit.findex.dto.indexdata.IndexDataSearchRequest;
import com.codeit.findex.entity.IndexData;
import com.codeit.findex.entity.QIndexData;
import com.codeit.findex.repository.IndexDataRepositoryCustom;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class IndexDataRepositoryImpl implements IndexDataRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  private static final QIndexData indexData = QIndexData.indexData;

  @Override
  public Page<IndexData> search(IndexDataSearchRequest request, Pageable pageable) {
    List<IndexData> content = queryFactory
        .selectFrom(indexData)
        .where(
            indexInfoIdEq(request.indexInfoId()),
            baseDateGoe(request.startDate()),
            baseDateLoe(request.endDate())
        )
        .orderBy(getOrderSpecifier(request.sortBy()))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    Long total = queryFactory
        .select(indexData.count())
        .from(indexData)
        .where(
            indexInfoIdEq(request.indexInfoId()),
            baseDateGoe(request.startDate()),
            baseDateLoe(request.endDate())
        )
        .fetchOne();

    return new PageImpl<>(content, pageable, total == null ? 0 : total);
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
        .orderBy(getOrderSpecifier(request.sortBy()))
        .fetch();
  }

  private BooleanExpression indexInfoIdEq(UUID id) {
    return id != null ? indexData.indexInfo.id.eq(id) : null;
  }

  private BooleanExpression baseDateGoe(LocalDate date) {
    return date != null ? indexData.baseDate.goe(date) : null;
  }

  private BooleanExpression baseDateLoe(LocalDate date) {
    return date != null ? indexData.baseDate.loe(date) : null;
  }

  private OrderSpecifier<?> getOrderSpecifier(String sortBy) {
    return "date_asc".equals(sortBy)
        ? indexData.baseDate.asc()
        : indexData.baseDate.desc();
  }
}