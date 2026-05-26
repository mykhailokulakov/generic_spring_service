package io.github.mykhailokulakov.genericspringservice.web;

import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleFilter;
import io.github.mykhailokulakov.genericspringservice.security.annotation.RequiresAdmin;
import io.github.mykhailokulakov.genericspringservice.security.annotation.RequiresUser;
import io.github.mykhailokulakov.genericspringservice.service.ExampleService;
import io.github.mykhailokulakov.genericspringservice.web.annotation.DeleteApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.annotation.MutatingApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.annotation.ReadApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.annotation.StandardApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.annotation.VersionedWriteApiResponses;
import io.github.mykhailokulakov.genericspringservice.web.dto.CreateExampleRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.ExampleResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.PageResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchExampleRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.UpdateExampleRequest;
import io.github.mykhailokulakov.genericspringservice.web.mapper.ExampleApiMapper;
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
@RequestMapping("/api/v1/examples")
@RequiredArgsConstructor
@Tag(name = "Examples")
public class ExampleController {

  private final ExampleService service;
  private final ExampleApiMapper apiMapper;

  @GetMapping
  @RequiresUser
  @Operation(summary = "Search examples", description = "Paginated search with optional filters.")
  @ApiResponse(responseCode = "200", description = "Page of matching examples.")
  @StandardApiResponses
  public PageResponse<ExampleResponse> search(
      @Valid @ParameterObject ExampleFilter filter,
      @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
    return PageResponse.of(service.search(filter, pageable).map(apiMapper::toResponse));
  }

  @GetMapping("/{id}")
  @RequiresUser
  @Operation(summary = "Get an example by id")
  @ApiResponse(responseCode = "200", description = "The example.")
  @ReadApiResponses
  public ExampleResponse get(@PathVariable UUID id) {
    return apiMapper.toResponse(service.getById(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @RequiresAdmin
  @Operation(summary = "Create an example")
  @ApiResponse(responseCode = "201", description = "The newly-created example.")
  @MutatingApiResponses
  public ExampleResponse create(@Valid @RequestBody CreateExampleRequest request) {
    return apiMapper.toResponse(service.create(apiMapper.toModel(request)));
  }

  @PutMapping("/{id}")
  @RequiresAdmin
  @Operation(
      summary = "Replace an example",
      description = "Full replacement. Requires If-Match with the current version.")
  @ApiResponse(responseCode = "200", description = "The updated example.")
  @VersionedWriteApiResponses
  public ExampleResponse replace(
      @PathVariable UUID id,
      @RequestHeader(value = "If-Match", required = false) Long expectedVersion,
      @Valid @RequestBody UpdateExampleRequest request) {
    return apiMapper.toResponse(service.replace(id, expectedVersion, apiMapper.toModel(request)));
  }

  @PatchMapping("/{id}")
  @RequiresAdmin
  @Operation(
      summary = "Patch an example",
      description =
          "Partial update. Null fields are left unchanged. Requires If-Match with the current"
              + " version.")
  @ApiResponse(responseCode = "200", description = "The updated example.")
  @VersionedWriteApiResponses
  public ExampleResponse patch(
      @PathVariable UUID id,
      @RequestHeader(value = "If-Match", required = false) Long expectedVersion,
      @Valid @RequestBody PatchExampleRequest request) {
    return apiMapper.toResponse(service.patch(id, expectedVersion, apiMapper.toModel(request)));
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
