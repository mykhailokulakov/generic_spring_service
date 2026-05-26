package io.github.mykhailokulakov.genericspringservice.web;

import io.github.mykhailokulakov.genericspringservice.domain.model.ChildFilter;
import io.github.mykhailokulakov.genericspringservice.security.annotation.RequiresAdmin;
import io.github.mykhailokulakov.genericspringservice.security.annotation.RequiresUser;
import io.github.mykhailokulakov.genericspringservice.service.ChildService;
import io.github.mykhailokulakov.genericspringservice.web.annotation.DeleteApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.annotation.MutatingApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.annotation.ReadApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.annotation.StandardApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.annotation.VersionedWriteApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.dto.ChildResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.CreateChildRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.PageResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchChildRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.UpdateChildRequest;
import io.github.mykhailokulakov.genericspringservice.web.mapper.ChildApiMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/children")
@RequiredArgsConstructor
@Tag(name = "Children")
public class ChildController {

  private final ChildService service;
  private final ChildApiMapper apiMapper;

  @GetMapping
  @RequiresUser
  @Operation(summary = "Search children", description = "Paginated search with optional filters.")
  @ApiResponse(responseCode = "200", description = "Page of matching children.")
  @StandardApiResponses
  public PageResponse<ChildResponse> search(
      @Valid @ParameterObject ChildFilter filter,
      @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
    return PageResponse.of(service.search(filter, pageable).map(apiMapper::toResponse));
  }

  @GetMapping("/{id}")
  @RequiresUser
  @Operation(summary = "Get a child by id")
  @ApiResponse(responseCode = "200", description = "The child.")
  @ReadApiResponses
  public ChildResponse get(@PathVariable UUID id) {
    return apiMapper.toResponse(service.getById(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @RequiresAdmin
  @Operation(summary = "Create a child")
  @ApiResponse(responseCode = "201", description = "The newly-created child.")
  @MutatingApiResponses
  public ChildResponse create(@Valid @RequestBody CreateChildRequest request) {
    return apiMapper.toResponse(service.create(apiMapper.toModel(request)));
  }

  @PutMapping("/{id}")
  @RequiresAdmin
  @Operation(
      summary = "Replace a child",
      description = "Full replacement. Requires If-Match with the current version.")
  @ApiResponse(responseCode = "200", description = "The updated child.")
  @VersionedWriteApiResponses
  public ChildResponse replace(
      @PathVariable UUID id,
      @RequestHeader(value = "If-Match", required = false) Long expectedVersion,
      @Valid @RequestBody UpdateChildRequest request) {
    return apiMapper.toResponse(service.replace(id, expectedVersion, apiMapper.toModel(request)));
  }

  @PatchMapping("/{id}")
  @RequiresAdmin
  @Operation(
      summary = "Patch a child",
      description =
          "Partial update. Null fields are left unchanged. Requires If-Match with the current"
              + " version.")
  @ApiResponse(responseCode = "200", description = "The updated child.")
  @VersionedWriteApiResponses
  public ChildResponse patch(
      @PathVariable UUID id,
      @RequestHeader(value = "If-Match", required = false) Long expectedVersion,
      @Valid @RequestBody PatchChildRequest request) {
    return apiMapper.toResponse(service.patch(id, expectedVersion, apiMapper.toModel(request)));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @RequiresAdmin
  @Operation(summary = "Soft-delete a child")
  @ApiResponse(responseCode = "204", description = "Deleted.")
  @DeleteApiResponses
  public void delete(@PathVariable UUID id) {
    service.softDelete(id);
  }
}
