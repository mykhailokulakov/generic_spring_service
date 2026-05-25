package io.github.mykhailokulakov.genericspringservice.domain.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record ChildPatch(String value) {}
