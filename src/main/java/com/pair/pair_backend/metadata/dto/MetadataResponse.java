package com.pair.pair_backend.metadata.dto;

import java.util.List;

public record MetadataResponse(Data data) {
    public record Data(String kind, List<CodeItemResponse> items) {
    }
}
