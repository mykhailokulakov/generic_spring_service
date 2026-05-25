package io.github.mykhailokulakov.testentities;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
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
@Table(name = "test_left")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class LeftEntity extends SoftDeletable {

  @Column(name = "code", nullable = false, length = 100)
  private String code;

  @ManyToMany
  @JoinTable(
      name = "test_left_right",
      joinColumns = @JoinColumn(name = "left_id"),
      inverseJoinColumns = @JoinColumn(name = "right_id"))
  @Builder.Default
  private Set<RightEntity> rights = new HashSet<>();
}
