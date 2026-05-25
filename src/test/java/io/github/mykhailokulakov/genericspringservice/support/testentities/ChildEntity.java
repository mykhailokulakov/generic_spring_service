package io.github.mykhailokulakov.genericspringservice.support.testentities;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "test_child")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ChildEntity extends SoftDeletable {

  @Column(name = "value", nullable = false, length = 200)
  private String value;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id", nullable = false)
  private ParentEntity parent;
}
