package com.codeit.findex.repository.querydsl;

import com.codeit.findex.entity.IndexInfo;
import com.codeit.findex.entity.QIndexInfo;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class IndexInfoQueryRepositoryImpl implements IndexInfoQueryRepository {

  private final JPAQueryFactory queryFactory;
  private static final QIndexInfo i = QIndexInfo.indexInfo;

  @Override
  public List<IndexInfo> findIndexInfoList(
      String indexClassification,
      String indexName,
      Boolean favorite,
      String sortField,
      String sortDirection,
      String sortValue,
      UUID idAfter,
      int limit
  ) {
    return queryFactory
        .selectFrom(i)
        .where(
            classificationFilter(indexClassification),
            indexNameFilter(indexName),
            favoriteFilter(favorite),
            cursorCondition(sortField, sortDirection, sortValue, idAfter)
        )
        .orderBy(orderBy(sortField, sortDirection))
        .limit(limit)
        .fetch();
  }

  // 분류명 검색
  private BooleanExpression classificationFilter(String indexClassification) {
    if (indexClassification == null || indexClassification.isBlank()) return null;
    return i.indexClassification.containsIgnoreCase(indexClassification);
  }

  // 지수명 검색
  private BooleanExpression indexNameFilter(String indexName) {
    if (indexName == null || indexName.isBlank()) return null;
    return i.indexName.containsIgnoreCase(indexName);
  }

  // 즐겨찾기 여부 검색
  private BooleanExpression favoriteFilter(Boolean favorite) {
    if (favorite == null) return null;
    return i.favorite.eq(favorite);
  }

  // 커서 기반 페이지네이션 조건 (정렬 필드 값 + id 기준)
  private BooleanExpression cursorCondition(
      String sortField, String sortDirection, String sortValue, UUID idAfter) {
    if (sortValue == null || idAfter == null) return null;

    boolean isAsc = !"DESC".equalsIgnoreCase(sortDirection);

    return switch (sortField) {
      case "indexName" -> isAsc
          ? i.indexName.gt(sortValue)
          .or(i.indexName.eq(sortValue).and(i.id.gt(idAfter)))
          : i.indexName.lt(sortValue)
              .or(i.indexName.eq(sortValue).and(i.id.lt(idAfter)));
      case "employedItemsCount" -> {
        int count = Integer.parseInt(sortValue);
        yield isAsc
            ? i.employedItemsCount.gt(count)
            .or(i.employedItemsCount.eq(count).and(i.id.gt(idAfter)))
            : i.employedItemsCount.lt(count)
                .or(i.employedItemsCount.eq(count).and(i.id.lt(idAfter)));
      }
      default -> isAsc
          ? i.indexClassification.gt(sortValue)
          .or(i.indexClassification.eq(sortValue).and(i.id.gt(idAfter)))
          : i.indexClassification.lt(sortValue)
              .or(i.indexClassification.eq(sortValue).and(i.id.lt(idAfter)));
    };
  }

  // 정렬 조건 생성 (기본 분류명 정렬)
  private OrderSpecifier<?>[] orderBy(String sortField, String sortDirection) {
    boolean isAsc = !"DESC".equalsIgnoreCase(sortDirection);

    OrderSpecifier<?> primary = switch (sortField) {
      case "indexName" -> isAsc ? i.indexName.asc() : i.indexName.desc();
      case "employedItemsCount" -> isAsc ? i.employedItemsCount.asc() : i.employedItemsCount.desc();
      default -> isAsc ? i.indexClassification.asc() : i.indexClassification.desc();
    };

    // 같은 값일 경우 id 기준 정렬
    OrderSpecifier<?> secondary = isAsc ? i.id.asc() : i.id.desc();

    return new OrderSpecifier[]{primary, secondary};
  }
}