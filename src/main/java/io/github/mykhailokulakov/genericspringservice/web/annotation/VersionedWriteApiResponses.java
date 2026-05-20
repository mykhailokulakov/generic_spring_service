package io.github.mykhailokulakov.genericspringservice.web.annotation;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.http.ProblemDetail;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
  @ApiResponse(
      responseCode = "400",
      description = ApiResponseDescriptions.VALIDATION_FAILED,
      content =
          @Content(
              mediaType = "application/problem+json",
              schema = @Schema(implementation = ProblemDetail.class))),
  @ApiResponse(
      responseCode = "401",
      description = ApiResponseDescriptions.UNAUTHENTICATED,
      content =
          @Content(
              mediaType = "application/problem+json",
              schema = @Schema(implementation = ProblemDetail.class))),
  @ApiResponse(
      responseCode = "403",
      description = ApiResponseDescriptions.FORBIDDEN,
      content =
          @Content(
              mediaType = "application/problem+json",
              schema = @Schema(implementation = ProblemDetail.class))),
  @ApiResponse(
      responseCode = "404",
      description = ApiResponseDescriptions.NOT_FOUND,
      content =
          @Content(
              mediaType = "application/problem+json",
              schema = @Schema(implementation = ProblemDetail.class))),
  @ApiResponse(
      responseCode = "409",
      description = ApiResponseDescriptions.CONFLICT,
      content =
          @Content(
              mediaType = "application/problem+json",
              schema = @Schema(implementation = ProblemDetail.class))),
  @ApiResponse(
      responseCode = "412",
      description = ApiResponseDescriptions.IF_MATCH_PRECONDITION,
      content =
          @Content(
              mediaType = "application/problem+json",
              schema = @Schema(implementation = ProblemDetail.class)))
})
public @interface VersionedWriteApiResponses {}
