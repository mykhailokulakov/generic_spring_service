package io.github.mykhailokulakov.genericspringservice.web;

import io.github.mykhailokulakov.genericspringservice.domain.model.RightFilter;
import io.github.mykhailokulakov.genericspringservice.security.annotation.RequiresAdmin;
import io.github.mykhailokulakov.genericspringservice.security.annotation.RequiresUser;
import io.github.mykhailokulakov.genericspringservice.service.RightService;
import io.github.mykhailokulakov.genericspringservice.web.annotation.DeleteApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.annotation.MutatingApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.annotation.ReadApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.annotation.StandardApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.annotation.VersionedWriteApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.dto.CreateRightRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.PageResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchRightRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.RightResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.UpdateRightRequest;
import io.github.mykhailokulakov.genericspringservice.web.mapper.RightApiMapper;
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
@RequestMapping("/api/v1/rights")
@RequiredArgsConstructor
@Tag(name = "Rights")
public class RightController {

  private final RightService service;
  private final RightApiMapper apiMapper;

  @GetMapping
  @RequiresUser
  @Operation(summary = "Search rights", description = "Paginated search with optional filters.")
  @ApiResponse(responseCode = "200", description = "Page of matching rights.")
  @StandardApiResponses
  public PageResponse<RightResponse> search(
      @Valid @ParameterObject RightFilter filter,
      @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
    return PageResponse.of(service.search(filter, pageable).map(apiMapper::toResponse));
  }

  @GetMapping("/{id}")
  @RequiresUser
  @Operation(summary = "Get a right by id")
  @ApiResponse(responseCode = "200", description = "The right.")
  @ReadApiResponses
  public RightResponse get(@PathVariable UUID id) {
    return apiMapper.toResponse(service.getById(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @RequiresAdmin
  @Operation(summary = "Create a right")
  @ApiResponse(responseCode = "201", description = "The newly-created right.")
  @MutatingApiResponses
  public RightResponse create(@Valid @RequestBody CreateRightRequest request) {
    return apiMapper.toResponse(service.create(apiMapper.toModel(request)));
  }

  @PutMapping("/{id}")
  @RequiresAdmin
  @Operation(
      summary = "Replace a right",
      description = "Full replacement. Requires If-Match with the current version.")
  @ApiResponse(responseCode = "200", description = "The updated right.")
  @VersionedWriteApiResponses
  public RightResponse replace(
      @PathVariable UUID id,
      @RequestHeader(value = "If-Match", required = false) Long expectedVersion,
      @Valid @RequestBody UpdateRightRequest request) {
    return apiMapper.toResponse(service.replace(id, expectedVersion, apiMapper.toModel(request)));
  }

  @PatchMapping("/{id}")
  @RequiresAdmin
  @Operation(
      summary = "Patch a right",
      description =
          "Partial update. Null fields are left unchanged. Requires If-Match with the current"
              + " version.")
  @ApiResponse(responseCode = "200", description = "The updated right.")
  @VersionedWriteApiResponses
  public RightResponse patch(
      @PathVariable UUID id,
      @RequestHeader(value = "If-Match", required = false) Long expectedVersion,
      @Valid @RequestBody PatchRightRequest request) {
    return apiMapper.toResponse(service.patch(id, expectedVersion, apiMapper.toModel(request)));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @RequiresAdmin
  @Operation(summary = "Soft-delete a right")
  @ApiResponse(responseCode = "204", description = "Deleted.")
  @DeleteApiResponses
  public void delete(@PathVariable UUID id) {
    service.softDelete(id);
  }
}
