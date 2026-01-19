package com.poc.ai.dto;

import com.fasterxml.jackson.annotation.JsonClassDescription;

@JsonClassDescription("Search query for GitHub repositories.")
public record ChatRequestDto(
        String message
) {
}
