package io.github.mykhailokulakov.genericspringservice.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Permits null but rejects blank (empty or whitespace-only) strings. The null-safe counterpart to
 * {@link jakarta.validation.constraints.NotBlank} for PATCH DTOs, where null means "leave
 * unchanged" (see DESIGN.md §3.4) and so cannot itself be a constraint violation.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NullOrNotBlankValidator.class)
public @interface NullOrNotBlank {

  String message() default "must be null or not blank";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
