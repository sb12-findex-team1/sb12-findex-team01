package com.codeit.findex.csv;

import com.codeit.findex.dto.indexdata.IndexDataSearchRequest;
import com.codeit.findex.entity.IndexData;
import com.codeit.findex.service.IndexDataService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IndexDataCsvExporter {

  private final IndexDataService indexDataService;

  private static final String[] HEADERS = {
      "ID", "지수명", "기준일자", "종가", "시가", "고가", "저가", "거래량"
  };

  public void export(IndexDataSearchRequest request, HttpServletResponse response)
      throws IOException {

    List<IndexData> dataList = indexDataService.findAllForExport(request);

    response.setContentType("text/csv; charset=UTF-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=\"index_data_" + LocalDate.now() + ".csv\"");

    // Excel 한글 깨짐 방지: UTF-8 BOM
    response.getOutputStream().write(0xEF);
    response.getOutputStream().write(0xBB);
    response.getOutputStream().write(0xBF);

    try (PrintWriter writer = new PrintWriter(
        new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {

      writer.println(String.join(",", HEADERS));

      for (IndexData data : dataList) {
        writer.println(String.join(",",
            String.valueOf(data.getId()),
            data.getIndexInfo().getIndexName(),
            data.getBaseDate().toString(),
            nullSafe(data.getClosingPrice()),
//            nullSafe(data.getOpeningPrice()),
            nullSafe(data.getHighPrice()),
            nullSafe(data.getLowPrice()),
            data.getTradingQuantity() != null ? String.valueOf(data.getTradingQuantity()) : ""
        ));
      }
      writer.flush();
    }
  }

  private String nullSafe(BigDecimal value) {
    return value != null ? value.toPlainString() : "";
  }
}

