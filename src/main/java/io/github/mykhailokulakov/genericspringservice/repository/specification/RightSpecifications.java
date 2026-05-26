package io.github.mykhailokulakov.genericspringservice.repository.specification;

import io.github.mykhailokulakov.genericspringservice.domain.entity.RightEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.RightEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.model.RightFilter;
import java.util.ArrayList;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class RightSpecifications {

  private RightSpecifications() {}

  private static final char LIKE_ESCAPE = '\\';

  public static Specification<RightEntity> matches(RightFilter f) {
    if (f == null) {
      return Specification.unrestricted();
    }
    var parts = new ArrayList<Specification<RightEntity>>();
    var nameSpec = nameContains(f.name());
    if (nameSpec != null) {
      parts.add(nameSpec);
    }
    if (parts.isEmpty()) {
      return Specification.unrestricted();
    }
    return Specification.allOf(parts);
  }

  private static Specification<RightEntity> nameContains(String value) {
    if (!StringUtils.hasText(value)) return null;
    var pattern = "%" + escapeLikePattern(value).toLowerCase(Locale.ROOT) + "%";
    return (root, q, cb) -> cb.like(cb.lower(root.get(RightEntity_.name)), pattern, LIKE_ESCAPE);
  }

  private static String escapeLikePattern(String value) {
    return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }
}
