package io.github.mykhailokulakov.genericspringservice.web;

import io.github.mykhailokulakov.genericspringservice.security.annotation.RequiresAdmin;
import io.github.mykhailokulakov.genericspringservice.security.annotation.RequiresUser;
import io.github.mykhailokulakov.genericspringservice.service.OwnerService;
import io.github.mykhailokulakov.genericspringservice.web.annotation.DeleteApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.annotation.MutatingApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.annotation.ReadApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.annotation.StandardApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.annotation.VersionedWriteApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.dto.CreateOwnerRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.OwnerResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.PageResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchOwnerRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.UpdateOwnerRequest;
import io.github.mykhailokulakov.genericspringservice.web.mapper.OwnerApiMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/owners")
@RequiredArgsConstructor
@Tag(name = "Owners")
public class OwnerController {

  private final OwnerService service;
  private final OwnerApiMapper apiMapper;

  @GetMapping
  @RequiresUser
  @Operation(summary = "Search owners", description = "Paginated search with optional filters.")
  @ApiResponse(responseCode = "200", description = "Page of matching owners.")
  @StandardApiResponses
  public PageResponse<OwnerResponse> search(
      @RequestParam(value = "id", required = false) List<UUID> ids,
      @RequestParam(value = "exampleId", required = false) List<UUID> exampleIds,
      @RequestParam(value = "handle", required = false) String handle,
      @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
    return PageResponse.of(
        service.search(ids, exampleIds, handle, pageable).map(apiMapper::toResponse));
  }

  @GetMapping("/{id}")
  @RequiresUser
  @Operation(summary = "Get an owner by id")
  @ApiResponse(responseCode = "200", description = "The owner.")
  @ReadApiResponses
  public OwnerResponse get(@PathVariable UUID id) {
    return apiMapper.toResponse(service.getById(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @RequiresAdmin
  @Operation(summary = "Create an owner")
  @ApiResponse(responseCode = "201", description = "The newly-created owner.")
  @MutatingApiResponses
  public OwnerResponse create(@Valid @RequestBody CreateOwnerRequest request) {
    return apiMapper.toResponse(service.create(apiMapper.toModel(request)));
  }

  @PutMapping("/{id}")
  @RequiresAdmin
  @Operation(summary = "Replace an owner", description = "Full replacement. Requires If-Match.")
  @ApiResponse(responseCode = "200", description = "The updated owner.")
  @VersionedWriteApiResponses
  public OwnerResponse replace(
      @PathVariable UUID id,
      @RequestHeader(value = "If-Match", required = false) Long expectedVersion,
      @Valid @RequestBody UpdateOwnerRequest request) {
    return apiMapper.toResponse(service.replace(id, expectedVersion, apiMapper.toModel(request)));
  }

  @PatchMapping("/{id}")
  @RequiresAdmin
  @Operation(summary = "Patch an owner", description = "Partial update. Requires If-Match.")
  @ApiResponse(responseCode = "200", description = "The updated owner.")
  @VersionedWriteApiResponses
  public OwnerResponse patch(
      @PathVariable UUID id,
      @RequestHeader(value = "If-Match", required = false) Long expectedVersion,
      @Valid @RequestBody PatchOwnerRequest request) {
    return apiMapper.toResponse(service.patch(id, expectedVersion, apiMapper.toModel(request)));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @RequiresAdmin
  @Operation(summary = "Soft-delete an owner")
  @ApiResponse(responseCode = "204", description = "Deleted.")
  @DeleteApiResponses
  public void delete(@PathVariable UUID id) {
    service.softDelete(id);
  }
}
