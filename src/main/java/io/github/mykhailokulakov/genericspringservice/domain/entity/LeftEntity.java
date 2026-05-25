package io.github.mykhailokulakov.genericspringservice.domain.entity;

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
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "left_item")
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public class LeftEntity extends SoftDeletable {

  @Column(name = "code", nullable = false, length = 100)
  private String code;

  @ManyToMany
  @JoinTable(
      name = "left_right_item",
      joinColumns = @JoinColumn(name = "left_id"),
      inverseJoinColumns = @JoinColumn(name = "right_id"))
  @Builder.Default
  @ToString.Exclude
  private Set<RightEntity> rights = new HashSet<>();
}
