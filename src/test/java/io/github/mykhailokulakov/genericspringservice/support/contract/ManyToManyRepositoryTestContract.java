package io.github.mykhailokulakov.genericspringservice.support.contract;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.common.persistence.Identifiable;
import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.RandomEntities;
import jakarta.persistence.ManyToMany;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public interface ManyToManyRepositoryTestContract<E extends SoftDeletable>
    extends RepositoryContractAccess<E> {

  @Test
  default void manyToMany_associationSurvivesReload() {
    var manyToManyField =
        ContractReflection.findOwningSide(entityType(), ManyToMany.class)
            .orElseThrow(
                () ->
                    new AssertionError(
                        entityType().getSimpleName()
                            + " implements ManyToManyRepositoryTestContract but has no owning"
                            + " @ManyToMany field"));
    var targetType = ContractReflection.collectionElementType(manyToManyField);

    var ids =
        new Object() {
          UUID entityId;
          UUID targetId;
        };

    tx().executeWithoutResult(
            status -> {
              var target = RandomEntities.create(targetType);
              em().persist(target);
              var entity = newEntity();
              var targets =
                  Set.class.isAssignableFrom(manyToManyField.getType())
                      ? Set.of(target)
                      : List.of(target);
              ContractReflection.setField(entity, manyToManyField, targets);
              em().persist(entity);
              em().flush();
              ids.entityId = entity.getId();
              ids.targetId = ((Identifiable) target).getId();
            });

    tx().executeWithoutResult(
            status -> {
              var reloaded = em().find(entityType(), ids.entityId);
              var targets = (Collection<?>) ContractReflection.getField(reloaded, manyToManyField);
              assertThat(targets).hasSize(1);
              assertThat(((Identifiable) targets.iterator().next()).getId())
                  .isEqualTo(ids.targetId);
            });
  }
}
