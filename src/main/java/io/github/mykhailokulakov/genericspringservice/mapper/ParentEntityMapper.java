package io.github.mykhailokulakov.genericspringservice.mapper;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Parent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ParentEntityMapper extends EntityMapper<ParentEntity, Parent> {

  @Override
  @Mapping(target = "children", ignore = true)
  ParentEntity toEntity(Parent model);

  @Override
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "children", ignore = true)
  void applyReplacement(Parent replacement, @MappingTarget ParentEntity entity);
}
