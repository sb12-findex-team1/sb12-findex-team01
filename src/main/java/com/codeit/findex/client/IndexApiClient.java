package com.codeit.findex.client;

import com.codeit.findex.dto.client.StockMarketIndexRequest;
import com.codeit.findex.dto.client.StockMarketIndexResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

//https://docs.spring.io/spring-framework/reference/integration/rest-clients.html

@Component
public class IndexApiClient {

  private static final String SERVICE_KEY_PARAM = "serviceKey";
  private static final String RESULT_TYPE_PARAM = "resultType";

  private final RestClient restClient;
  private final IndexApiProperties properties;

  public IndexApiClient(IndexApiProperties properties) {
    this.properties = properties;
    this.restClient = RestClient.builder()
        .baseUrl(properties.baseUrl())
        .build();
  }

  private StockMarketIndexResponse get(String path, Map<String, ?> queryParams) {
    if (!StringUtils.hasText(properties.serviceKey())) {
      throw new IllegalStateException("public-data.index.service-key is required.");
    }

    UriComponentsBuilder builder = UriComponentsBuilder
        .fromUriString(properties.baseUrl())
        .path(path)
        .queryParam(SERVICE_KEY_PARAM, properties.serviceKey())
        .queryParam(RESULT_TYPE_PARAM, properties.resultType());

    if (queryParams != null) {
      queryParams.forEach((name, value) -> builder.queryParam(
          name,
          UriUtils.encodeQueryParam(String.valueOf(value), StandardCharsets.UTF_8)
      ));
    }

    URI uri = builder.build(true).toUri();

    return restClient.get()
        .uri(uri)
        .retrieve()
        .body(StockMarketIndexResponse.class);
  }

  public StockMarketIndexResponse getStockMarketIndex(StockMarketIndexRequest request) {
    return get(properties.stockMarketIndexPath(), request.toQueryParams());
  }
}
