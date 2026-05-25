package io.github.mykhailokulakov.genericspringservice.mapper;

import io.github.mykhailokulakov.genericspringservice.domain.entity.LeftEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Left;
import io.github.mykhailokulakov.genericspringservice.domain.model.LeftPatch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LeftEntityMapper
    extends EntityMapper<LeftEntity, Left>, PatchableMapper<LeftEntity, LeftPatch> {

  @Override
  @Mapping(target = "rights", ignore = true)
  LeftEntity toEntity(Left model);

  @Override
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "rights", ignore = true)
  void applyReplacement(Left replacement, @MappingTarget LeftEntity entity);
}
