package io.github.mykhailokulakov.genericspringservice.web;

import io.github.mykhailokulakov.genericspringservice.common.validation.OnCreate;
import io.github.mykhailokulakov.genericspringservice.domain.model.Parent;
import io.github.mykhailokulakov.genericspringservice.security.annotation.RequiresAdmin;
import io.github.mykhailokulakov.genericspringservice.security.annotation.RequiresUser;
import io.github.mykhailokulakov.genericspringservice.service.ParentService;
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
@RequestMapping("/api/v1/parents")
@RequiredArgsConstructor
@Tag(name = "Parents")
public class ParentController {

  private final ParentService service;

  @GetMapping
  @RequiresUser
  @Operation(summary = "Search parents")
  @ApiResponse(responseCode = "200", description = "Page of matching parents.")
  @StandardApiResponses
  public PageResponse<Parent> search(
      @RequestParam(value = "id", required = false) List<UUID> ids,
      @RequestParam(value = "label", required = false) String label,
      @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
    return PageResponse.of(service.search(ids, label, pageable));
  }

  @GetMapping("/{id}")
  @RequiresUser
  @Operation(summary = "Get a parent by id")
  @ApiResponse(responseCode = "200", description = "The parent.")
  @ReadApiResponses
  public Parent get(@PathVariable UUID id) {
    return service.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @RequiresAdmin
  @Operation(summary = "Create a parent")
  @ApiResponse(responseCode = "201", description = "The newly-created parent.")
  @MutatingApiResponses
  public Parent create(@Validated({Default.class, OnCreate.class}) @RequestBody Parent request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  @RequiresAdmin
  @Operation(summary = "Replace a parent")
  @ApiResponse(responseCode = "200", description = "The updated parent.")
  @VersionedWriteApiResponses
  public Parent replace(
      @PathVariable UUID id,
      @RequestHeader(value = "If-Match", required = false) Long expectedVersion,
      @Valid @RequestBody Parent request) {
    return service.replace(id, expectedVersion, request);
  }

  @PatchMapping("/{id}")
  @RequiresAdmin
  @Operation(summary = "Patch a parent")
  @ApiResponse(responseCode = "200", description = "The updated parent.")
  @VersionedWriteApiResponses
  public Parent patch(
      @PathVariable UUID id,
      @RequestHeader(value = "If-Match", required = false) Long expectedVersion,
      @RequestBody Parent request) {
    return service.patch(id, expectedVersion, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @RequiresAdmin
  @Operation(summary = "Soft-delete a parent")
  @ApiResponse(responseCode = "204", description = "Deleted.")
  @DeleteApiResponses
  public void delete(@PathVariable UUID id) {
    service.softDelete(id);
  }
}
