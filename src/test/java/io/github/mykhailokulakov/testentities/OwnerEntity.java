package io.github.mykhailokulakov.testentities;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

@Entity
@Table(name = "test_owner")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class OwnerEntity extends SoftDeletable {

  @Column(name = "handle", nullable = false, length = 200)
  private String handle;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @JoinColumn(name = "profile_id")
  @NotFound(action = NotFoundAction.IGNORE)
  private ProfileEntity profile;
}
