package io.github.mykhailokulakov.genericspringservice.mapper;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Parent;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ParentEntityMapper
    extends EntityMapper<ParentEntity, Parent>, PatchableMapper<ParentEntity, Parent> {

  @Override
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "children", ignore = true)
  void applyPatch(Parent model, @MappingTarget ParentEntity entity);

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
