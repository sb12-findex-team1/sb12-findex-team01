package com.codeit.findex.config;

import com.codeit.findex.client.IndexApiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IndexApiProperties.class)
public class IndexApiConfig {
}
