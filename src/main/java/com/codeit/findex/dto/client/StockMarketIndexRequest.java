package com.codeit.findex.dto.client;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record StockMarketIndexRequest(
    Integer pageNo,
    Integer numOfRows,
    String basDt,
    String beginBasDt,
    String endBasDt,
    String likeBasDt,
    String idxNm,
    String likeIdxNm,
    Integer beginEpyItmsCnt,
    Integer endEpyItmsCnt,
    BigDecimal beginFltRt,
    BigDecimal endFltRt,
    Long beginTrqu,
    Long endTrqu,
    Long beginTrPrc,
    Long endTrPrc,
    Long beginLstgMrktTotAmt,
    Long endLstgMrktTotAmt,
    BigDecimal beginLsYrEdVsFltRg,
    BigDecimal endLsYrEdVsFltRg,
    BigDecimal beginLsYrEdVsFltRt,
    BigDecimal endLsYrEdVsFltRt
){

  public Map<String, ?> toQueryParams() {
    LinkedHashMap<String, Object> queryParams = new LinkedHashMap<>();
    putIfNotNull(queryParams, "pageNo", pageNo);
    putIfNotNull(queryParams, "numOfRows", numOfRows);
    putIfNotNull(queryParams, "basDt", basDt);
    putIfNotNull(queryParams, "beginBasDt", beginBasDt);
    putIfNotNull(queryParams, "endBasDt", endBasDt);
    putIfNotNull(queryParams, "likeBasDt", likeBasDt);
    putIfNotNull(queryParams, "idxNm", idxNm);
    putIfNotNull(queryParams, "likeIdxNm", likeIdxNm);
    putIfNotNull(queryParams, "beginEpyItmsCnt", beginEpyItmsCnt);
    putIfNotNull(queryParams, "endEpyItmsCnt", endEpyItmsCnt);
    putIfNotNull(queryParams, "beginFltRt", beginFltRt);
    putIfNotNull(queryParams, "endFltRt", endFltRt);
    putIfNotNull(queryParams, "beginTrqu", beginTrqu);
    putIfNotNull(queryParams, "endTrqu", endTrqu);
    putIfNotNull(queryParams, "beginTrPrc", beginTrPrc);
    putIfNotNull(queryParams, "endTrPrc", endTrPrc);
    putIfNotNull(queryParams, "beginLstgMrktTotAmt", beginLstgMrktTotAmt);
    putIfNotNull(queryParams, "endLstgMrktTotAmt", endLstgMrktTotAmt);
    putIfNotNull(queryParams, "beginLsYrEdVsFltRg", beginLsYrEdVsFltRg);
    putIfNotNull(queryParams, "endLsYrEdVsFltRg", endLsYrEdVsFltRg);
    putIfNotNull(queryParams, "beginLsYrEdVsFltRt", beginLsYrEdVsFltRt);
    putIfNotNull(queryParams, "endLsYrEdVsFltRt", endLsYrEdVsFltRt);
    return toUnmodifiableMap(queryParams);
  }

//  모두 null 입력시 10개만 출력되기 때문에 임의로 200으로 할당.
  public static StockMarketIndexRequest getIndexInfo() {
    return new StockMarketIndexRequest(
        1,
        200,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );
  }

  void putIfNotNull(Map<String, Object> queryParams, String name, Object value) {
    if (value != null) {
      queryParams.put(name, value);
    }
  }

  static Map<String, ?> toUnmodifiableMap(LinkedHashMap<String, Object> queryParams) {
    return Collections.unmodifiableMap(queryParams);
  }
}
