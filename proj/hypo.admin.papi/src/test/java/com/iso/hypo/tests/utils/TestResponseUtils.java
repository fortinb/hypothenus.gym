package com.iso.hypo.tests.utils;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iso.hypo.admin.papi.dto.ErrorDto;
import com.iso.hypo.common.application.dto.PageResultDto;

/**
 * Small test helper to centralize conversion of ResponseEntity<JsonNode>
 * into DTOs, PageResultDto objects or ErrorDto.
 *
 * Usage examples:
 *  - MyDto d = TestResponseUtils.toDto(response, MyDto.class, objectMapper);
 *  - ErrorDto err = TestResponseUtils.toError(response, objectMapper);
 *  - PageResultDto<MyDto> page = TestResponseUtils.toPage(response, new TypeReference<PageResultDto<MyDto>>() {}, objectMapper);
 *  - MyDto d = TestResponseUtils.parseOrThrow(response, MyDto.class, objectMapper);
 */
public final class TestResponseUtils {

    private TestResponseUtils() {}

    public static <T> T toDto(ResponseEntity<JsonNode> response, Class<T> clazz, ObjectMapper mapper) {
        if (response == null) return null;
        JsonNode body = response.getBody();
        if (body == null || body.isEmpty()) return null;
        return mapper.convertValue(body, clazz);
    }

    public static ErrorDto toError(ResponseEntity<JsonNode> response, ObjectMapper mapper) {
        if (response == null) return null;
        JsonNode body = response.getBody();
        if (body == null || body.isEmpty()) return null;
        return mapper.convertValue(body, ErrorDto.class);
    }

    public static <T> PageResultDto<T> toPage(ResponseEntity<JsonNode> response, TypeReference<PageResultDto<T>> typeRef, ObjectMapper mapper) {
        if (response == null) return null;
        JsonNode body = response.getBody();
        if (body == null || body.isEmpty()) return null;
        return mapper.convertValue(body, typeRef);
    }
}