package io.github.mykhailokulakov.genericspringservice.web.mapper;

import io.github.mykhailokulakov.genericspringservice.domain.model.Parent;
import io.github.mykhailokulakov.genericspringservice.domain.model.ParentPatch;
import io.github.mykhailokulakov.genericspringservice.web.dto.CreateParentRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.ParentResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchParentRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.UpdateParentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ParentApiMapper {

  ParentResponse toResponse(Parent model);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  Parent toModel(CreateParentRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  Parent toModel(UpdateParentRequest request);

  ParentPatch toModel(PatchParentRequest request);
}
