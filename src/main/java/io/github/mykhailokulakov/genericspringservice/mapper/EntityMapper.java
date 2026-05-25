package io.github.mykhailokulakov.genericspringservice.mapper;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import io.github.mykhailokulakov.genericspringservice.domain.model.DomainModel;
import org.mapstruct.MappingTarget;

public interface EntityMapper<E extends SoftDeletable, M extends DomainModel> {

  M toModel(E entity);

  E toEntity(M model);

  void applyReplacement(M replacement, @MappingTarget E entity);
}
