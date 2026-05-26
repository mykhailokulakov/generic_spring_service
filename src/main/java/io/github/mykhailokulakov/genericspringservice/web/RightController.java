package io.github.mykhailokulakov.genericspringservice.web;

import io.github.mykhailokulakov.genericspringservice.common.validation.OnCreate;
import io.github.mykhailokulakov.genericspringservice.domain.model.Right;
import io.github.mykhailokulakov.genericspringservice.security.annotation.RequiresAdmin;
import io.github.mykhailokulakov.genericspringservice.security.annotation.RequiresUser;
import io.github.mykhailokulakov.genericspringservice.service.RightService;
import io.github.mykhailokulakov.genericspringservice.web.annotation.DeleteApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.annotation.MutatingApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.annotation.ReadApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.annotation.StandardApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.annotation.VersionedWriteApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/api/v1/rights")
@RequiredArgsConstructor
@Tag(name = "Rights")
public class RightController {

  private final RightService service;

  @GetMapping
  @RequiresUser
  @Operation(summary = "Search rights")
  @ApiResponse(responseCode = "200", description = "Page of matching rights.")
  @StandardApiResponses
  public PageResponse<Right> search(
      @RequestParam(value = "id", required = false) List<UUID> ids,
      @RequestParam(value = "name", required = false) String name,
      @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
    return PageResponse.of(service.search(ids, name, pageable));
  }

  @GetMapping("/{id}")
  @RequiresUser
  @Operation(summary = "Get a right by id")
  @ApiResponse(responseCode = "200", description = "The right.")
  @ReadApiResponses
  public Right get(@PathVariable UUID id) {
    return service.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @RequiresAdmin
  @Operation(summary = "Create a right")
  @ApiResponse(responseCode = "201", description = "The newly-created right.")
  @MutatingApiResponses
  public Right create(@Validated({Default.class, OnCreate.class}) @RequestBody Right request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  @RequiresAdmin
  @Operation(summary = "Replace a right")
  @ApiResponse(responseCode = "200", description = "The updated right.")
  @VersionedWriteApiResponses
  public Right replace(
      @PathVariable UUID id,
      @RequestHeader(value = "If-Match", required = false) Long expectedVersion,
      @Valid @RequestBody Right request) {
    return service.replace(id, expectedVersion, request);
  }

  @PatchMapping("/{id}")
  @RequiresAdmin
  @Operation(summary = "Patch a right")
  @ApiResponse(responseCode = "200", description = "The updated right.")
  @VersionedWriteApiResponses
  public Right patch(
      @PathVariable UUID id,
      @RequestHeader(value = "If-Match", required = false) Long expectedVersion,
      @RequestBody Right request) {
    return service.patch(id, expectedVersion, request);
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
