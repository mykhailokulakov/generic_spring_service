package io.github.mykhailokulakov.genericspringservice.repository.specification;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.entity.OwnerEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.OwnerEntity_;
import io.github.mykhailokulakov.genericspringservice.domain.model.OwnerFilter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class OwnerSpecifications {

  private OwnerSpecifications() {}

  private static final char LIKE_ESCAPE = '\\';

  public static Specification<OwnerEntity> matches(OwnerFilter f) {
    if (f == null) {
      return Specification.unrestricted();
    }
    var parts = new ArrayList<Specification<OwnerEntity>>();
    var handleSpec = handleContains(f.handle());
    if (handleSpec != null) parts.add(handleSpec);
    var exampleSpec = exampleIdEquals(f.exampleId());
    if (exampleSpec != null) parts.add(exampleSpec);
    if (parts.isEmpty()) {
      return Specification.unrestricted();
    }
    return Specification.allOf(parts);
  }

  private static Specification<OwnerEntity> handleContains(String value) {
    if (!StringUtils.hasText(value)) return null;
    var pattern = "%" + escapeLikePattern(value).toLowerCase(Locale.ROOT) + "%";
    return (root, q, cb) -> cb.like(cb.lower(root.get(OwnerEntity_.handle)), pattern, LIKE_ESCAPE);
  }

  private static Specification<OwnerEntity> exampleIdEquals(UUID exampleId) {
    if (exampleId == null) return null;
    return (root, q, cb) ->
        cb.equal(root.get(OwnerEntity_.example).get(ExampleEntity_.id), exampleId);
  }

  private static String escapeLikePattern(String value) {
    return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }
}
