package io.github.mykhailokulakov.testentities;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "test_parent")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ParentEntity extends SoftDeletable {

  @Column(name = "label", nullable = false, length = 200)
  private String label;

  @OneToMany(mappedBy = "parent", cascade = CascadeType.PERSIST)
  @Builder.Default
  private List<ChildEntity> children = new ArrayList<>();
}
