package com.iso.hypo.admin.papi.controller.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.iso.hypo.admin.papi.dto.ErrorDto;
import com.iso.hypo.common.exception.DomainException;

public final class ControllerErrorHandler {

    private ControllerErrorHandler() {
        // utility
    }

    public static ResponseEntity<Object> buildErrorResponse(DomainException e, com.iso.hypo.common.context.RequestContext requestContext, String context) {
        String tracking = requestContext != null ? requestContext.getTrackingNumber() : null;
        ErrorDto error = new ErrorDto(tracking, e.getCode(), e.getMessage(), context);

        String code = e.getCode();
        if (code != null && code.trim().equals("400")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        if (code != null && code.trim().equals("403")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        if (code != null && code.trim().equals("404")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}