package io.github.mykhailokulakov.genericspringservice.mapper;

import io.github.mykhailokulakov.genericspringservice.domain.entity.RightEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Right;
import io.github.mykhailokulakov.genericspringservice.domain.model.RightPatch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RightEntityMapper
    extends EntityMapper<RightEntity, Right>, PatchableMapper<RightEntity, RightPatch> {

  @Override
  @Mapping(target = "lefts", ignore = true)
  RightEntity toEntity(Right model);

  @Override
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "lefts", ignore = true)
  void applyReplacement(Right replacement, @MappingTarget RightEntity entity);
}
