package io.github.mykhailokulakov.genericspringservice.common.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class SoftDeletable extends Versioned {

  @Column(name = "deleted_at")
  @Setter(AccessLevel.PROTECTED)
  private Instant deletedAt;

  public void markDeleted() {
    this.deletedAt = Instant.now();
  }

  public boolean isDeleted() {
    return deletedAt != null;
  }
}
