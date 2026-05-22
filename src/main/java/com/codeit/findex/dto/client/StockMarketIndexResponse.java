package com.codeit.findex.dto.client;

import java.util.List;

public record StockMarketIndexResponse(
    Response response
) {
  public record Response(
      Header header,
      Body body
  ) {
  }

  public record Header(
      String resultCode,
      String resultMsg
  ) {
  }

  public record Body(
      Integer numOfRows,
      Integer pageNo,
      Integer totalCount,
      Items items
  ) {
  }

  public record Items(
      List<Item> item
  ) {
  }

  public record Item(
      String basDt,
      String idxNm,
      String idxCsf,
      String epyItmsCnt,
      String clpr,
      String vs,
      String fltRt,
      String mkp,
      String hipr,
      String lopr,
      String trqu,
      String trPrc,
      String lstgMrktTotAmt,
      String lsYrEdVsFltRg,
      String lsYrEdVsFltRt,
      String yrWRcrdHgst,
      String yrWRcrdHgstDt,
      String yrWRcrdLwst,
      String yrWRcrdLwstDt,
      String basPntm,
      String basIdx
  ) {
  }
}
