package io.github.mykhailokulakov.genericspringservice.mapper;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Child;
import io.github.mykhailokulakov.genericspringservice.domain.model.ChildPatch;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ChildEntityMapper
    extends EntityMapper<ChildEntity, Child>, PatchableMapper<ChildEntity, ChildPatch> {

  @Override
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "parent", ignore = true)
  void applyPatch(ChildPatch patch, @MappingTarget ChildEntity entity);

  @Override
  @Mapping(target = "parentId", source = "parent.id")
  Child toModel(ChildEntity entity);

  @Override
  @Mapping(target = "parent", ignore = true)
  ChildEntity toEntity(Child model);

  @Override
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "parent", ignore = true)
  void applyReplacement(Child replacement, @MappingTarget ChildEntity entity);
}
