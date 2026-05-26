package io.github.mykhailokulakov.genericspringservice.web;

import io.github.mykhailokulakov.genericspringservice.domain.model.LeftFilter;
import io.github.mykhailokulakov.genericspringservice.security.annotation.RequiresAdmin;
import io.github.mykhailokulakov.genericspringservice.security.annotation.RequiresUser;
import io.github.mykhailokulakov.genericspringservice.service.LeftService;
import io.github.mykhailokulakov.genericspringservice.web.annotation.DeleteApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.annotation.MutatingApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.annotation.ReadApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.annotation.StandardApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.annotation.VersionedWriteApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.dto.CreateLeftRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.LeftResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.PageResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchLeftRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.UpdateLeftRequest;
import io.github.mykhailokulakov.genericspringservice.web.mapper.LeftApiMapper;
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
@RequestMapping("/api/v1/lefts")
@RequiredArgsConstructor
@Tag(name = "Lefts")
public class LeftController {

  private final LeftService service;
  private final LeftApiMapper apiMapper;

  @GetMapping
  @RequiresUser
  @Operation(summary = "Search lefts", description = "Paginated search with optional filters.")
  @ApiResponse(responseCode = "200", description = "Page of matching lefts.")
  @StandardApiResponses
  public PageResponse<LeftResponse> search(
      @Valid @ParameterObject LeftFilter filter,
      @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
    return PageResponse.of(service.search(filter, pageable).map(apiMapper::toResponse));
  }

  @GetMapping("/{id}")
  @RequiresUser
  @Operation(summary = "Get a left by id")
  @ApiResponse(responseCode = "200", description = "The left.")
  @ReadApiResponses
  public LeftResponse get(@PathVariable UUID id) {
    return apiMapper.toResponse(service.getById(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @RequiresAdmin
  @Operation(summary = "Create a left")
  @ApiResponse(responseCode = "201", description = "The newly-created left.")
  @MutatingApiResponses
  public LeftResponse create(@Valid @RequestBody CreateLeftRequest request) {
    return apiMapper.toResponse(service.create(apiMapper.toModel(request)));
  }

  @PutMapping("/{id}")
  @RequiresAdmin
  @Operation(summary = "Replace a left", description = "Full replacement. Requires If-Match.")
  @ApiResponse(responseCode = "200", description = "The updated left.")
  @VersionedWriteApiResponses
  public LeftResponse replace(
      @PathVariable UUID id,
      @RequestHeader(value = "If-Match", required = false) Long expectedVersion,
      @Valid @RequestBody UpdateLeftRequest request) {
    return apiMapper.toResponse(service.replace(id, expectedVersion, apiMapper.toModel(request)));
  }

  @PatchMapping("/{id}")
  @RequiresAdmin
  @Operation(summary = "Patch a left", description = "Partial update. Requires If-Match.")
  @ApiResponse(responseCode = "200", description = "The updated left.")
  @VersionedWriteApiResponses
  public LeftResponse patch(
      @PathVariable UUID id,
      @RequestHeader(value = "If-Match", required = false) Long expectedVersion,
      @Valid @RequestBody PatchLeftRequest request) {
    return apiMapper.toResponse(service.patch(id, expectedVersion, apiMapper.toModel(request)));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @RequiresAdmin
  @Operation(summary = "Soft-delete a left")
  @ApiResponse(responseCode = "204", description = "Deleted.")
  @DeleteApiResponses
  public void delete(@PathVariable UUID id) {
    service.softDelete(id);
  }
}
