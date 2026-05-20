package com.codeit.findex.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "public-data.index")
public record IndexApiProperties(
    String baseUrl,
    String stockMarketIndexPath,
    String bondMarketIndexPath,
    String derivationProductMarketIndexPath,
    String serviceKey,
    String resultType
) {
}
