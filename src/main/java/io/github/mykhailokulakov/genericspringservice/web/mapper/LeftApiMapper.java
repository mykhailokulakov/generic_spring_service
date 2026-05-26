package io.github.mykhailokulakov.genericspringservice.web.mapper;

import io.github.mykhailokulakov.genericspringservice.domain.model.Left;
import io.github.mykhailokulakov.genericspringservice.domain.model.LeftPatch;
import io.github.mykhailokulakov.genericspringservice.web.dto.CreateLeftRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.LeftResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchLeftRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.UpdateLeftRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LeftApiMapper {

  LeftResponse toResponse(Left model);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  Left toModel(CreateLeftRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  Left toModel(UpdateLeftRequest request);

  LeftPatch toModel(PatchLeftRequest request);
}
