package io.github.mykhailokulakov.genericspringservice.domain.entity;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

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

  @OneToOne
  @JoinColumn(name = "example_id")
  @ToString.Exclude
  private ExampleEntity example;
}
