package io.github.mykhailokulakov.genericspringservice.repository.specification;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.model.ChildFilter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ChildSpecifications {

  private ChildSpecifications() {}

  private static final char LIKE_ESCAPE = '\\';

  public static Specification<ChildEntity> matches(ChildFilter f) {
    if (f == null) {
      return Specification.unrestricted();
    }
    var parts = new ArrayList<Specification<ChildEntity>>();
    var valueSpec = valueContains(f.value());
    if (valueSpec != null) {
      parts.add(valueSpec);
    }
    var parentSpec = parentIdEquals(f.parentId());
    if (parentSpec != null) {
      parts.add(parentSpec);
    }
    if (parts.isEmpty()) {
      return Specification.unrestricted();
    }
    return Specification.allOf(parts);
  }

  private static Specification<ChildEntity> valueContains(String value) {
    if (!StringUtils.hasText(value)) return null;
    var pattern = "%" + escapeLikePattern(value).toLowerCase(Locale.ROOT) + "%";
    return (root, q, cb) -> cb.like(cb.lower(root.get(ChildEntity_.value)), pattern, LIKE_ESCAPE);
  }

  private static Specification<ChildEntity> parentIdEquals(UUID parentId) {
    if (parentId == null) return null;
    return (root, q, cb) -> cb.equal(root.get(ChildEntity_.parent).get(ParentEntity_.id), parentId);
  }

  private static String escapeLikePattern(String value) {
    return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }
}
