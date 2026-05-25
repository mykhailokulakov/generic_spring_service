package io.github.mykhailokulakov.genericspringservice.mapper;

import io.github.mykhailokulakov.genericspringservice.domain.entity.OwnerEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Owner;
import io.github.mykhailokulakov.genericspringservice.domain.model.OwnerPatch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OwnerEntityMapper
    extends EntityMapper<OwnerEntity, Owner>, PatchableMapper<OwnerEntity, OwnerPatch> {

  @Override
  @Mapping(target = "exampleId", source = "example.id")
  Owner toModel(OwnerEntity entity);

  @Override
  @Mapping(target = "example", ignore = true)
  OwnerEntity toEntity(Owner model);

  @Override
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "example", ignore = true)
  void applyReplacement(Owner replacement, @MappingTarget OwnerEntity entity);
}
