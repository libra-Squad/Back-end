package com.pair.pair_backend.metadata.catalog;

import com.pair.pair_backend.metadata.dto.CodeItemResponse;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class MetadataCatalog {
    private final Map<String, List<CodeItemResponse>> itemsByKind;

    public MetadataCatalog(ObjectMapper objectMapper,
                           @Value("classpath:metadata/metadata-catalog.json") Resource source) {
        try (var input = source.getInputStream()) {
            itemsByKind = objectMapper.readValue(input,
                    new TypeReference<Map<String, List<CodeItemResponse>>>() {});
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load metadata catalog", exception);
        }
    }

    public List<CodeItemResponse> findByKind(String kind) {
        return itemsByKind.get(kind);
    }
}
