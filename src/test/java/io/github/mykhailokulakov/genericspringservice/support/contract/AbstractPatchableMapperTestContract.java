package io.github.mykhailokulakov.genericspringservice.support.contract;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import io.github.mykhailokulakov.genericspringservice.domain.model.DomainModel;
import io.github.mykhailokulakov.genericspringservice.mapper.PatchableMapper;
import org.junit.jupiter.api.Test;

public abstract class AbstractPatchableMapperTestContract<
        E extends SoftDeletable, M extends DomainModel>
    extends AbstractMapperTestContract<E, M> {

  @SuppressWarnings("unchecked")
  private PatchableMapper<E, M> patchableMapper() {
    return (PatchableMapper<E, M>) mapper();
  }

  @Test
  void applyPatch_onNullIsNoOp() {
    var entity = mapper().toEntity(newModel());
    var originalId = entity.getId();

    patchableMapper().applyPatch(null, entity);

    assertThat(entity.getId()).isEqualTo(originalId);
  }

  @Test
  void applyPatch_updatesProvidedFields() {
    var entity = mapper().toEntity(newModel());
    var patch = newModel();

    patchableMapper().applyPatch(patch, entity);

    assertThat(entity).isNotNull();
  }
}
