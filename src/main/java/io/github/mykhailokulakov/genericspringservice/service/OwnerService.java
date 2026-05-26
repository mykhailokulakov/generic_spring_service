package io.github.mykhailokulakov.genericspringservice.service;

import io.github.mykhailokulakov.genericspringservice.domain.entity.OwnerEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Owner;
import io.github.mykhailokulakov.genericspringservice.domain.model.OwnerFilter;
import io.github.mykhailokulakov.genericspringservice.domain.model.OwnerPatch;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.exception.NotFoundException;
import io.github.mykhailokulakov.genericspringservice.mapper.OwnerEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.ExampleRepository;
import io.github.mykhailokulakov.genericspringservice.repository.OwnerRepository;
import io.github.mykhailokulakov.genericspringservice.repository.specification.OwnerSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OwnerService extends AbstractCrudService<OwnerEntity, Owner, OwnerPatch, OwnerFilter> {

  private final OwnerRepository repository;
  private final ExampleRepository exampleRepository;
  private final OwnerEntityMapper mapper;

  @Override
  protected OwnerRepository repository() {
    return repository;
  }

  @Override
  protected OwnerEntityMapper mapper() {
    return mapper;
  }

  @Override
  protected OwnerEntityMapper patchMapper() {
    return mapper;
  }

  @Override
  protected Specification<OwnerEntity> toSpecification(OwnerFilter filter) {
    return OwnerSpecifications.matches(filter);
  }

  @Override
  protected ErrorCode notFoundCode() {
    return ErrorCode.OWNER_NOT_FOUND;
  }

  @Override
  public Owner create(Owner toCreate) {
    var entity = mapper.toEntity(toCreate);
    entity.setExample(
        exampleRepository
            .findById(toCreate.exampleId())
            .orElseThrow(
                () -> new NotFoundException(ErrorCode.EXAMPLE_NOT_FOUND, toCreate.exampleId())));
    return mapper.toModel(repository.save(entity));
  }
}
