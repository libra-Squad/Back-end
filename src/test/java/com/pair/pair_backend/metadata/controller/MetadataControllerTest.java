package com.pair.pair_backend.metadata.controller;

import com.pair.pair_backend.metadata.dto.CodeItemResponse;
import com.pair.pair_backend.metadata.dto.MetadataResponse;
import com.pair.pair_backend.metadata.service.MetadataService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MetadataControllerTest {
    private final MetadataService service = mock(MetadataService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new MetadataController(service)).build();

    @Test
    void returnsMetadataItems() throws Exception {
        var item = new CodeItemResponse("SEOUL", "SEOUL", "서울", "SEOUL");
        when(service.getMetadata("regions")).thenReturn(
                new MetadataResponse(new MetadataResponse.Data("regions", List.of(item))));

        mvc.perform(get("/api/v1/metadata/regions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.kind").value("regions"))
                .andExpect(jsonPath("$.data.items[0].id").value("SEOUL"))
                .andExpect(jsonPath("$.data.items[0].code").value("SEOUL"))
                .andExpect(jsonPath("$.data.items[0].label").value("서울"))
                .andExpect(jsonPath("$.data.items[0].value").value("SEOUL"));
    }

    @Test
    void returnsNotFoundCodeForUnsupportedKind() throws Exception {
        when(service.getMetadata("unsupported")).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "METADATA_KIND_NOT_FOUND"));

        mvc.perform(get("/api/v1/metadata/unsupported"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("METADATA_KIND_NOT_FOUND"));
    }
}
