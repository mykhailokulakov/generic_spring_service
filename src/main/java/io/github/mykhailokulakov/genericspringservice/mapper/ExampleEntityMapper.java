package io.github.mykhailokulakov.genericspringservice.mapper;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Example;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchExampleRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ExampleEntityMapper {

  Example toModel(ExampleEntity entity);

  ExampleEntity toEntity(Example model);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void applyReplacement(Example replacement, @MappingTarget ExampleEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void applyPatch(PatchExampleRequest patch, @MappingTarget ExampleEntity entity);
}
