package io.github.mykhailokulakov.genericspringservice.domain.entity;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

@Entity
@Table(name = "owner")
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public class OwnerEntity extends SoftDeletable {

  @Column(name = "handle", nullable = false, length = 200)
  private String handle;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "example_id")
  @NotFound(action = NotFoundAction.IGNORE)
  @ToString.Exclude
  private ExampleEntity example;
}
