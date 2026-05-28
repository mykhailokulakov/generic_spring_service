package io.github.mykhailokulakov.genericspringservice.support.contract;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.common.persistence.Identifiable;
import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.RandomEntities;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public interface ManyToOneRepositoryTestContract<E extends SoftDeletable>
    extends RepositoryContractAccess<E> {

  @Test
  default void manyToOne_foreignKeySurvivesReload() {
    var manyToOneField =
        ContractReflection.findFirstField(entityType(), ManyToOne.class)
            .orElseThrow(
                () ->
                    new AssertionError(
                        entityType().getSimpleName()
                            + " implements ManyToOneRepositoryTestContract but has no @ManyToOne"
                            + " field"));
    var targetType = manyToOneField.getType();

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
              ContractReflection.setField(entity, manyToOneField, target);
              em().persist(entity);
              em().flush();
              ids.entityId = entity.getId();
              ids.targetId = ((Identifiable) target).getId();
            });

    tx().executeWithoutResult(
            status -> {
              var reloaded = em().find(entityType(), ids.entityId);
              var target = (Identifiable) ContractReflection.getField(reloaded, manyToOneField);
              assertThat(target.getId()).isEqualTo(ids.targetId);
            });
  }
}
