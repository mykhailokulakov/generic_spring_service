package io.github.mykhailokulakov.genericspringservice.repository.specification;

import io.github.mykhailokulakov.genericspringservice.domain.entity.LeftEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.LeftEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.model.LeftFilter;
import java.util.ArrayList;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class LeftSpecifications {

  private LeftSpecifications() {}

  private static final char LIKE_ESCAPE = '\\';

  public static Specification<LeftEntity> matches(LeftFilter f) {
    if (f == null) {
      return Specification.unrestricted();
    }
    var parts = new ArrayList<Specification<LeftEntity>>();
    var codeSpec = codeContains(f.code());
    if (codeSpec != null) {
      parts.add(codeSpec);
    }
    if (parts.isEmpty()) {
      return Specification.unrestricted();
    }
    return Specification.allOf(parts);
  }

  private static Specification<LeftEntity> codeContains(String value) {
    if (!StringUtils.hasText(value)) return null;
    var pattern = "%" + escapeLikePattern(value).toLowerCase(Locale.ROOT) + "%";
    return (root, q, cb) -> cb.like(cb.lower(root.get(LeftEntity_.code)), pattern, LIKE_ESCAPE);
  }

  private static String escapeLikePattern(String value) {
    return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }
}
