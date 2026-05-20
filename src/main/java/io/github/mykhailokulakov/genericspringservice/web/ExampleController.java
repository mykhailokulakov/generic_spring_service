package io.github.mykhailokulakov.genericspringservice.web;

import io.github.mykhailokulakov.genericspringservice.mapper.ExampleApiMapper;
import io.github.mykhailokulakov.genericspringservice.security.annotation.RequiresAdmin;
import io.github.mykhailokulakov.genericspringservice.security.annotation.RequiresUser;
import io.github.mykhailokulakov.genericspringservice.service.ExampleService;
import io.github.mykhailokulakov.genericspringservice.web.dto.CreateExampleRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.ExampleFilter;
import io.github.mykhailokulakov.genericspringservice.web.dto.ExampleResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.PageResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchExampleRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.UpdateExampleRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
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
  public PageResponse<ExampleResponse> search(
      @Valid @ParameterObject ExampleFilter filter, @ParameterObject Pageable pageable) {
    return PageResponse.of(service.search(filter, pageable).map(apiMapper::toResponse));
  }

  @GetMapping("/{id}")
  @RequiresUser
  public ExampleResponse get(@PathVariable UUID id) {
    return apiMapper.toResponse(service.getById(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @RequiresAdmin
  public ExampleResponse create(@Valid @RequestBody CreateExampleRequest request) {
    return apiMapper.toResponse(service.create(apiMapper.toModel(request)));
  }

  @PutMapping("/{id}")
  @RequiresAdmin
  public ExampleResponse replace(
      @PathVariable UUID id,
      @RequestHeader("If-Match") Long expectedVersion,
      @Valid @RequestBody UpdateExampleRequest request) {
    return apiMapper.toResponse(service.replace(id, expectedVersion, apiMapper.toModel(request)));
  }

  @PatchMapping("/{id}")
  @RequiresAdmin
  public ExampleResponse patch(
      @PathVariable UUID id,
      @RequestHeader("If-Match") Long expectedVersion,
      @Valid @RequestBody PatchExampleRequest request) {
    return apiMapper.toResponse(service.patch(id, expectedVersion, request));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @RequiresAdmin
  public void delete(@PathVariable UUID id) {
    service.softDelete(id);
  }
}
