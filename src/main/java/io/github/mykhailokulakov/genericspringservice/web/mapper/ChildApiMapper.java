package io.github.mykhailokulakov.genericspringservice.web.mapper;

import io.github.mykhailokulakov.genericspringservice.domain.model.Child;
import io.github.mykhailokulakov.genericspringservice.domain.model.ChildPatch;
import io.github.mykhailokulakov.genericspringservice.web.dto.ChildResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.CreateChildRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchChildRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.UpdateChildRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChildApiMapper {

  ChildResponse toResponse(Child model);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  Child toModel(CreateChildRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "parentId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  Child toModel(UpdateChildRequest request);

  ChildPatch toModel(PatchChildRequest request);
}
