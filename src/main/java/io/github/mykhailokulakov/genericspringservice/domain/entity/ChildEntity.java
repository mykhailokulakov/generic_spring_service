package io.github.mykhailokulakov.genericspringservice.domain.entity;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "child")
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public class ChildEntity extends SoftDeletable {

  @Column(name = "value", nullable = false, length = 200)
  private String value;

  @ManyToOne
  @JoinColumn(name = "parent_id", nullable = false)
  @ToString.Exclude
  private ParentEntity parent;
}
