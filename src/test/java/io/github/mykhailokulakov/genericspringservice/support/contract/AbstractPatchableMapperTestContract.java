package io.github.mykhailokulakov.genericspringservice.support.contract;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import io.github.mykhailokulakov.genericspringservice.domain.model.DomainModel;
import io.github.mykhailokulakov.genericspringservice.mapper.PatchableMapper;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.RandomModels;
import java.lang.reflect.ParameterizedType;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

public abstract class AbstractPatchableMapperTestContract<
        E extends SoftDeletable, M extends DomainModel, P>
    extends AbstractMapperTestContract<E, M> {

  private Class<P> patchType;

  @SuppressWarnings("unchecked")
  protected Class<P> patchType() {
    if (patchType == null) {
      var superclass = (ParameterizedType) getClass().getGenericSuperclass();
      patchType = (Class<P>) superclass.getActualTypeArguments()[2];
    }
    return patchType;
  }

  @SuppressWarnings("unchecked")
  private PatchableMapper<E, P> patchableMapper() {
    return (PatchableMapper<E, P>) mapper();
  }

  protected P newPatch() {
    return RandomModels.create(patchType());
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
    var patch = Instancio.create(patchType());

    patchableMapper().applyPatch(patch, entity);

    assertThat(entity).isNotNull();
  }
}
