package io.github.mykhailokulakov.genericspringservice.support.fixtures;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class ExampleFixtures {

  private static final String[] TAG_POOL = {
    "alpha", "beta", "gamma", "delta", "epsilon", "zeta", "eta", "theta", "iota", "kappa"
  };

  private ExampleFixtures() {}

  public static ExampleEntity aDraftExample() {
    return builder().build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static ExampleEntity randomActive() {
    return builder().withRandomDefaults().build();
  }

  public static final class Builder {
    private String name = "example";
    private String description = "default description";
    private Integer quantity = 1;
    private BigDecimal price = new BigDecimal("1.00");
    private Instant occurredAt = Instant.parse("2026-01-01T00:00:00Z");
    private ExampleStatus status = ExampleStatus.ACTIVE;
    private Set<String> tags = new HashSet<>();

    private Builder() {}

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder quantity(Integer quantity) {
      this.quantity = quantity;
      return this;
    }

    public Builder price(BigDecimal price) {
      this.price = price;
      return this;
    }

    public Builder occurredAt(Instant occurredAt) {
      this.occurredAt = occurredAt;
      return this;
    }

    public Builder status(ExampleStatus status) {
      this.status = status;
      return this;
    }

    public Builder tags(Set<String> tags) {
      this.tags = new HashSet<>(tags);
      return this;
    }

    public Builder addTag(String tag) {
      this.tags.add(tag);
      return this;
    }

    public Builder withRandomDefaults() {
      var rnd = ThreadLocalRandom.current();
      var suffix = UUID.randomUUID().toString().substring(0, 8);
      this.name = "example-" + suffix;
      this.description = "random description " + suffix;
      this.quantity = rnd.nextInt(1, 1_000);
      this.price =
          BigDecimal.valueOf(rnd.nextDouble(0.01d, 10_000.0d)).setScale(2, RoundingMode.HALF_UP);
      this.occurredAt =
          Instant.parse("2026-01-01T00:00:00Z").plus(rnd.nextLong(0, 365), ChronoUnit.DAYS);
      this.status = ExampleStatus.ACTIVE;
      int tagCount = rnd.nextInt(0, 4);
      var randomTags = new HashSet<String>();
      while (randomTags.size() < tagCount) {
        randomTags.add(TAG_POOL[rnd.nextInt(TAG_POOL.length)]);
      }
      this.tags = randomTags;
      return this;
    }

    public ExampleEntity build() {
      return ExampleEntity.builder()
          .name(name)
          .description(description)
          .quantity(quantity)
          .price(price)
          .occurredAt(occurredAt)
          .status(status)
          .tags(new HashSet<>(tags))
          .build();
    }
  }
}
