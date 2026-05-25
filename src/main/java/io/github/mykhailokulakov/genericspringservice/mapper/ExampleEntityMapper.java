package io.github.mykhailokulakov.genericspringservice.mapper;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Example;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExamplePatch;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExampleEntityMapper
    extends EntityMapper<ExampleEntity, Example>, PatchableMapper<ExampleEntity, ExamplePatch> {}
