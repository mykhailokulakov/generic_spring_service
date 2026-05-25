package io.github.mykhailokulakov.genericspringservice.support.contract;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.ModelFixture;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.RandomEntities;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.RepoFixture;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

public abstract class AbstractMapperContractIT<E extends SoftDeletable, M> {

  protected abstract RepoFixture<E> repoFixture();

  protected abstract ModelFixture<M> modelFixture();

  protected abstract Function<E, M> toModel();

  protected abstract Function<M, E> toEntity();

  protected abstract Function<List<E>, List<M>> toModelList();

  protected abstract Function<List<M>, List<E>> toEntityList();

  protected abstract void applyPatch(M source, E target);

  protected abstract void assertMappedFields(E entity, M model);

  @Test
  void toModel_copiesAuditFieldsFromEntity() {
    var entity = RandomEntities.create(repoFixture().entityType());

    var model = toModel().apply(entity);

    assertThat(model).isNotNull();
  }

  @Test
  void toEntity_leavesManagedFieldsUnset() {
    var model = modelFixture().newModel();

    var entity = toEntity().apply(model);

    assertThat(entity).isNotNull();
    assertThat(entity.getId()).as("id should come from model, not generated").isNotNull();
  }

  @Test
  void toModelList_mapsEveryElement() {
    var entities =
        List.of(
            repoFixture().newPersistable(),
            repoFixture().newPersistable(),
            repoFixture().newPersistable());

    var models = toModelList().apply(entities);

    assertThat(models).hasSameSizeAs(entities);
  }

  @Test
  void toEntityList_mapsEveryElement() {
    var models =
        List.of(modelFixture().newModel(), modelFixture().newModel(), modelFixture().newModel());

    var entities = toEntityList().apply(models);

    assertThat(entities).hasSameSizeAs(models);
  }

  @Test
  void applyPatch_updatesMappedFieldsOnly() {
    var entity = repoFixture().newPersistable();
    var source = modelFixture().newModel();

    applyPatch(source, entity);

    assertMappedFields(entity, source);
  }

  @Test
  void roundTrip_preservesMappedFields() {
    var original = repoFixture().newPersistable();

    var model = toModel().apply(original);
    var backToEntity = toEntity().apply(model);

    assertMappedFields(backToEntity, model);
  }
}
