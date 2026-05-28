package io.github.mykhailokulakov.genericspringservice.support.contract;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.common.persistence.Identifiable;
import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.RandomEntities;
import jakarta.persistence.OneToOne;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public interface OneToOneRepositoryContract<E extends SoftDeletable>
    extends RepositoryContractAccess<E> {

  @Test
  default void oneToOne_foreignKeySurvivesReload() {
    var oneToOneField =
        AbstractRepositoryTestContract.findOwningSide(entityType(), OneToOne.class)
            .orElseThrow(
                () ->
                    new AssertionError(
                        entityType().getSimpleName()
                            + " implements OneToOneRepositoryContract but has no owning @OneToOne"
                            + " field"));
    var targetType = oneToOneField.getType();

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
              AbstractRepositoryTestContract.setField(entity, oneToOneField, target);
              em().persist(entity);
              em().flush();
              ids.entityId = entity.getId();
              ids.targetId = ((Identifiable) target).getId();
            });

    tx().executeWithoutResult(
            status -> {
              var reloaded = em().find(entityType(), ids.entityId);
              var target =
                  (Identifiable) AbstractRepositoryTestContract.getField(reloaded, oneToOneField);
              assertThat(target).isNotNull();
              assertThat(target.getId()).isEqualTo(ids.targetId);
            });
  }
}
