/**
 * Reusable OpenAPI/Swagger meta-annotations that bundle the standard error responses every endpoint
 * needs (401, 403, 404, 400, 409, 412 — all returning {@code application/problem+json} with the
 * {@link org.springframework.http.ProblemDetail} schema).
 *
 * <p>Same pattern as {@code @RequiresUser} / {@code @RequiresAdmin}: declare a
 * {@code @Target(METHOD)} meta-annotation that stacks the relevant {@code @ApiResponse} entries,
 * then apply it at the controller method site. A wording change in {@link
 * io.github.mykhailokulakov.genericspringservice.web.annotation.ApiResponseDescriptions} is one
 * edit, not one per endpoint.
 *
 * <p>When adding a new endpoint shape that needs a different set of error responses, add a new
 * meta-annotation here rather than copy-pasting {@code @ApiResponse} blocks onto the controller
 * method. A forthcoming ArchUnit rule will enforce that any annotation meta-annotated with
 * {@code @ApiResponse} resides in this package.
 */
package io.github.mykhailokulakov.genericspringservice.web.annotation;
