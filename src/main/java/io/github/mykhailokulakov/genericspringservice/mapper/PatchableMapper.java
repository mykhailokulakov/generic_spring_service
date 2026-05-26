package io.github.mykhailokulakov.genericspringservice.mapper;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import io.github.mykhailokulakov.genericspringservice.domain.model.DomainModel;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

public interface PatchableMapper<E extends SoftDeletable, M extends DomainModel> {

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void applyPatch(M model, @MappingTarget E entity);
}
