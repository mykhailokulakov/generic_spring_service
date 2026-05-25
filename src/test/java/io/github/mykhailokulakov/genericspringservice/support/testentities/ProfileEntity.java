package io.github.mykhailokulakov.genericspringservice.support.testentities;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "test_profile")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ProfileEntity extends SoftDeletable {

  @Column(name = "bio", nullable = false, length = 500)
  private String bio;
}
