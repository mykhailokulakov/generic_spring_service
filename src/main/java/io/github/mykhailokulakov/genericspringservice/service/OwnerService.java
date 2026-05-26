package io.github.mykhailokulakov.genericspringservice.service;

import io.github.mykhailokulakov.genericspringservice.domain.entity.OwnerEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Owner;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.exception.NotFoundException;
import io.github.mykhailokulakov.genericspringservice.mapper.OwnerEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.ExampleRepository;
import io.github.mykhailokulakov.genericspringservice.repository.OwnerRepository;
import io.github.mykhailokulakov.genericspringservice.repository.specification.OwnerSpecifications;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OwnerService extends AbstractCrudService<OwnerEntity, Owner> {

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
  protected ErrorCode notFoundCode() {
    return ErrorCode.OWNER_NOT_FOUND;
  }

  @Transactional(readOnly = true)
  public Page<Owner> search(
      List<UUID> ids, List<UUID> exampleIds, String handle, Pageable pageable) {
    return repository
        .findAll(OwnerSpecifications.matches(ids, exampleIds, handle), pageable)
        .map(mapper::toModel);
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
