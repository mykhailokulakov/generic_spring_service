package io.github.mykhailokulakov.genericspringservice.web.mapper;

import io.github.mykhailokulakov.genericspringservice.domain.model.Right;
import io.github.mykhailokulakov.genericspringservice.domain.model.RightPatch;
import io.github.mykhailokulakov.genericspringservice.web.dto.CreateRightRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchRightRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.RightResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.UpdateRightRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RightApiMapper {

  RightResponse toResponse(Right model);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  Right toModel(CreateRightRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  Right toModel(UpdateRightRequest request);

  RightPatch toModel(PatchRightRequest request);
}
