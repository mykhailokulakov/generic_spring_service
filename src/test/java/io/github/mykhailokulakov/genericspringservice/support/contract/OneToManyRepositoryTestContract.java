package io.github.mykhailokulakov.genericspringservice.support.contract;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.common.persistence.Identifiable;
import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public interface OneToManyRepositoryTestContract<E extends SoftDeletable>
    extends RepositoryContractAccess<E> {

  @SuppressWarnings("unchecked")
  @Test
  default void givenParentWithChildren_whenSoftDeleted_thenChildrenAreAlsoSoftDeleted() {
    var collectionField =
        ContractReflection.findField(
                entityType(),
                f ->
                    f.isAnnotationPresent(OneToMany.class)
                        && ContractReflection.hasCascadeRemove(f.getAnnotation(OneToMany.class)))
            .orElseThrow(
                () ->
                    new AssertionError(
                        entityType().getSimpleName()
                            + " implements OneToManyRepositoryTestContract but has no @OneToMany"
                            + " with cascade REMOVE"));

    var childType =
        (Class<? extends Identifiable>) ContractReflection.collectionElementType(collectionField);
    var backRefField =
        ContractReflection.findBackReference(childType, entityType())
            .orElseThrow(
                () ->
                    new AssertionError(
                        childType.getSimpleName()
                            + " has no @ManyToOne back-reference to "
                            + entityType().getSimpleName()));

    var ids =
        new Object() {
          UUID parentId;
          UUID childId;
        };

    tx().executeWithoutResult(
            status -> {
              var parent = newEntity();
              em().persist(parent);
              em().flush();

              var child = newRelatedEntity(childType);
              ContractReflection.setField(child, backRefField, parent);
              em().persist(child);

              Collection<Object> children =
                  Set.class.isAssignableFrom(collectionField.getType())
                      ? new HashSet<>(Set.of(child))
                      : new ArrayList<>(List.of(child));
              ContractReflection.setField(parent, collectionField, children);
              em().flush();

              ids.parentId = parent.getId();
              ids.childId = child.getId();
            });

    tx().executeWithoutResult(
            status -> {
              var loaded = em().find(entityType(), ids.parentId);
              em().remove(loaded);
              em().flush();
            });

    assertThat(dbHelper().existsIncludingDeleted(entityType(), ids.parentId)).isTrue();
    assertThat(dbHelper().isSoftDeleted(entityType(), ids.parentId)).isTrue();
    assertThat(dbHelper().existsIncludingDeleted(childType, ids.childId)).isTrue();
    assertThat(dbHelper().isSoftDeleted(childType, ids.childId))
        .as("cascade soft-delete: child is also soft-deleted when parent is")
        .isTrue();
  }
}
