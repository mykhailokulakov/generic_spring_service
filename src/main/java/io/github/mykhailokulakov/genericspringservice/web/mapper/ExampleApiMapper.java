package io.github.mykhailokulakov.genericspringservice.web.mapper;

import io.github.mykhailokulakov.genericspringservice.domain.model.Example;
import io.github.mykhailokulakov.genericspringservice.web.dto.CreateExampleRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.ExampleResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.UpdateExampleRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExampleApiMapper {

  ExampleResponse toResponse(Example model);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  Example toModel(CreateExampleRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  Example toModel(UpdateExampleRequest request);
}
