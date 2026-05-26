package io.github.mykhailokulakov.genericspringservice.web.mapper;

import io.github.mykhailokulakov.genericspringservice.domain.model.Owner;
import io.github.mykhailokulakov.genericspringservice.domain.model.OwnerPatch;
import io.github.mykhailokulakov.genericspringservice.web.dto.CreateOwnerRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.OwnerResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchOwnerRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.UpdateOwnerRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OwnerApiMapper {

  OwnerResponse toResponse(Owner model);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  Owner toModel(CreateOwnerRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "exampleId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  Owner toModel(UpdateOwnerRequest request);

  OwnerPatch toModel(PatchOwnerRequest request);
}
