package io.github.mykhailokulakov.genericspringservice.domain.entity;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;

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
@ToString
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

  // @BatchSize bounds tag fetching when iterating a page of entities: instead
  // of one SELECT per entity (the N+1 trap), Hibernate batches up to 50 owner
  // IDs into a single `WHERE example_id IN (...)` query. Combined with the
  // repository's plain (non-fetch-joined) paginated method, this keeps the
  // main query paginated at SQL level — fetch-joining the collection would
  // force Hibernate to apply firstResult/maxResults in memory.
  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(
      name = "example_tag",
      joinColumns = @JoinColumn(name = "example_id"),
      indexes = @Index(name = "ix_example_tag_value", columnList = "tag"))
  @Column(name = "tag", nullable = false, length = 64)
  @BatchSize(size = 50)
  @Builder.Default
  @ToString.Exclude
  private Set<String> tags = new HashSet<>();
}
