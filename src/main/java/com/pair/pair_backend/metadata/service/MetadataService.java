package com.pair.pair_backend.metadata.service;

import com.pair.pair_backend.metadata.catalog.MetadataCatalog;
import com.pair.pair_backend.metadata.dto.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MetadataService {
    private final MetadataCatalog catalog;

    public MetadataService(MetadataCatalog catalog) {
        this.catalog = catalog;
    }

    public MetadataResponse getMetadata(String kind) {
        var items = catalog.findByKind(kind);
        if (items == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "METADATA_KIND_NOT_FOUND");
        }
        return new MetadataResponse(new MetadataResponse.Data(kind, items));
    }
}
