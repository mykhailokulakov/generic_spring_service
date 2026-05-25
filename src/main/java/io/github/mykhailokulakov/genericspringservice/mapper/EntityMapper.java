package io.github.mykhailokulakov.genericspringservice.mapper;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import org.mapstruct.MappingTarget;

public interface EntityMapper<E extends SoftDeletable, M> {

  M toModel(E entity);

  E toEntity(M model);

  void applyReplacement(M replacement, @MappingTarget E entity);
}
