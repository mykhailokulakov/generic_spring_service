package io.github.mykhailokulakov.genericspringservice.domain.entity;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "example",
    indexes = {
      @Index(name = "ix_example_name", columnList = "name"),
      @Index(name = "ix_example_status", columnList = "status"),
      @Index(name = "ix_example_occurred_at", columnList = "occurred_at")
    })
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ExampleEntity extends SoftDeletable {

  @Column(name = "name", nullable = false, length = 200)
  private String name;

  @Column(name = "description", columnDefinition = "text")
  private String description;

  @Column(name = "quantity")
  private Integer quantity;

  @Column(name = "price", precision = 19, scale = 2)
  private BigDecimal price;

  @Column(name = "occurred_at")
  private Instant occurredAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 32)
  private ExampleStatus status;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(
      name = "example_tag",
      joinColumns = @JoinColumn(name = "example_id"),
      indexes = @Index(name = "ix_example_tag_value", columnList = "tag"))
  @Column(name = "tag", nullable = false, length = 64)
  @Builder.Default
  private Set<String> tags = new HashSet<>();
}
