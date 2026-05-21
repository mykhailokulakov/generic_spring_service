package io.github.mykhailokulakov.genericspringservice.config;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservabilityConfig {

  @Bean
  public MeterFilter applicationTagMeterFilter(
      @Value("${spring.application.name:generic-spring-service}") String applicationName) {
    return MeterFilter.commonTags(List.of(Tag.of("application", applicationName)));
  }
}
