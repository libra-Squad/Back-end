package com.pair.pair_backend.metadata.dto;

public record CodeItemResponse(
        String id,
        String code,
        String label,
        Object value
) {
}