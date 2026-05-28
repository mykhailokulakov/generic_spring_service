package io.github.mykhailokulakov.genericspringservice.support.contract;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.common.persistence.Identifiable;
import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import jakarta.persistence.OneToOne;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public interface OneToOneRepositoryTestContract<E extends SoftDeletable>
    extends RepositoryContractAccess<E> {

  @Test
  default void givenEntityWithOneToOneTarget_whenReloaded_thenForeignKeyIsPreserved() {
    var oneToOneField =
        ContractReflection.findOwningSide(entityType(), OneToOne.class)
            .orElseThrow(
                () ->
                    new AssertionError(
                        entityType().getSimpleName()
                            + " implements OneToOneRepositoryTestContract but has no owning"
                            + " @OneToOne field"));
    var targetType = oneToOneField.getType();

    var ids =
        new Object() {
          UUID entityId;
          UUID targetId;
        };

    tx().executeWithoutResult(
            status -> {
              var target = newRelatedEntity(targetType);
              em().persist(target);
              var entity = newEntity();
              ContractReflection.setField(entity, oneToOneField, target);
              em().persist(entity);
              em().flush();
              ids.entityId = entity.getId();
              ids.targetId = ((Identifiable) target).getId();
            });

    tx().executeWithoutResult(
            status -> {
              var reloaded = em().find(entityType(), ids.entityId);
              var target = (Identifiable) ContractReflection.getField(reloaded, oneToOneField);
              assertThat(target).isNotNull();
              assertThat(target.getId()).isEqualTo(ids.targetId);
            });
  }
}
