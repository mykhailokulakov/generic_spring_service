package io.github.mykhailokulakov.genericspringservice.repository.specification;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.model.ParentFilter;
import java.util.ArrayList;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ParentSpecifications {

  private ParentSpecifications() {}

  private static final char LIKE_ESCAPE = '\\';

  public static Specification<ParentEntity> matches(ParentFilter f) {
    if (f == null) {
      return Specification.unrestricted();
    }
    var parts = new ArrayList<Specification<ParentEntity>>();
    var labelSpec = labelContains(f.label());
    if (labelSpec != null) {
      parts.add(labelSpec);
    }
    if (parts.isEmpty()) {
      return Specification.unrestricted();
    }
    return Specification.allOf(parts);
  }

  private static Specification<ParentEntity> labelContains(String value) {
    if (!StringUtils.hasText(value)) return null;
    var pattern = "%" + escapeLikePattern(value).toLowerCase(Locale.ROOT) + "%";
    return (root, q, cb) -> cb.like(cb.lower(root.get(ParentEntity_.label)), pattern, LIKE_ESCAPE);
  }

  private static String escapeLikePattern(String value) {
    return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }
}
