package io.github.mykhailokulakov.genericspringservice.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

@Schema(description = "Stable JSON pagination envelope.")
public record PageResponse<T>(
    @Schema(description = "Page contents.") List<T> content,
    @Schema(description = "Zero-based page index.") int page,
    @Schema(description = "Page size.") int size,
    @Schema(description = "Total number of matching elements across all pages.") long totalElements,
    @Schema(description = "Total number of pages.") int totalPages,
    @Schema(description = "True if this is the first page.") boolean first,
    @Schema(description = "True if this is the last page.") boolean last) {

  public static <T> PageResponse<T> of(Page<T> page) {
    return new PageResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isFirst(),
        page.isLast());
  }
}
