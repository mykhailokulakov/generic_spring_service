package io.github.mykhailokulakov.genericspringservice.domain.entity;

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
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id", nullable = false)
  @NotFound(action = NotFoundAction.IGNORE)
  @ToString.Exclude
  private ParentEntity parent;
}
