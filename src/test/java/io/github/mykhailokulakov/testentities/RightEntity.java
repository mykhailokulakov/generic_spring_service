package io.github.mykhailokulakov.testentities;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "test_right")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class RightEntity extends SoftDeletable {

  @Column(name = "name", nullable = false, length = 200)
  private String name;

  @ManyToMany(mappedBy = "rights")
  @Builder.Default
  private Set<LeftEntity> lefts = new HashSet<>();
}
