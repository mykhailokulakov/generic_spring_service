package io.github.mykhailokulakov.genericspringservice.web;

import io.github.mykhailokulakov.genericspringservice.common.validation.OnCreate;
import io.github.mykhailokulakov.genericspringservice.domain.model.Example;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import io.github.mykhailokulakov.genericspringservice.security.annotation.RequiresAdmin;
import io.github.mykhailokulakov.genericspringservice.security.annotation.RequiresUser;
import io.github.mykhailokulakov.genericspringservice.service.ExampleService;
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
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
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
@RequestMapping("/api/v1/examples")
@RequiredArgsConstructor
@Tag(name = "Examples")
public class ExampleController {

  private final ExampleService service;

  @GetMapping
  @RequiresUser
  @Operation(summary = "Search examples")
  @ApiResponse(responseCode = "200", description = "Page of matching examples.")
  @StandardApiResponses
  public PageResponse<Example> search(
      @RequestParam(value = "id", required = false) List<UUID> ids,
      @RequestParam(value = "name", required = false) String name,
      @RequestParam(value = "description", required = false) String description,
      @RequestParam(value = "minQuantity", required = false) Integer minQuantity,
      @RequestParam(value = "maxQuantity", required = false) Integer maxQuantity,
      @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
      @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
      @RequestParam(value = "occurredFrom", required = false) Instant occurredFrom,
      @RequestParam(value = "occurredTo", required = false) Instant occurredTo,
      @RequestParam(value = "status", required = false) Set<ExampleStatus> statuses,
      @RequestParam(value = "tag", required = false) Set<String> tags,
      @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
    return PageResponse.of(
        service.search(
            ids,
            name,
            description,
            minQuantity,
            maxQuantity,
            minPrice,
            maxPrice,
            occurredFrom,
            occurredTo,
            statuses,
            tags,
            pageable));
  }

  @GetMapping("/{id}")
  @RequiresUser
  @Operation(summary = "Get an example by id")
  @ApiResponse(responseCode = "200", description = "The example.")
  @ReadApiResponses
  public Example get(@PathVariable UUID id) {
    return service.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @RequiresAdmin
  @Operation(summary = "Create an example")
  @ApiResponse(responseCode = "201", description = "The newly-created example.")
  @MutatingApiResponses
  public Example create(@Validated({Default.class, OnCreate.class}) @RequestBody Example request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  @RequiresAdmin
  @Operation(summary = "Replace an example")
  @ApiResponse(responseCode = "200", description = "The updated example.")
  @VersionedWriteApiResponses
  public Example replace(
      @PathVariable UUID id,
      @RequestHeader(value = "If-Match", required = false) Long expectedVersion,
      @Valid @RequestBody Example request) {
    return service.replace(id, expectedVersion, request);
  }

  @PatchMapping("/{id}")
  @RequiresAdmin
  @Operation(summary = "Patch an example")
  @ApiResponse(responseCode = "200", description = "The updated example.")
  @VersionedWriteApiResponses
  public Example patch(
      @PathVariable UUID id,
      @RequestHeader(value = "If-Match", required = false) Long expectedVersion,
      @RequestBody Example request) {
    return service.patch(id, expectedVersion, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @RequiresAdmin
  @Operation(summary = "Soft-delete an example")
  @ApiResponse(responseCode = "204", description = "Deleted.")
  @DeleteApiResponses
  public void delete(@PathVariable UUID id) {
    service.softDelete(id);
  }
}
