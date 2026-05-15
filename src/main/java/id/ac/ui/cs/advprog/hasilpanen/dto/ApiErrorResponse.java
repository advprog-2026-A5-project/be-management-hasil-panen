package id.ac.ui.cs.advprog.hasilpanen.dto;

import java.time.Instant;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        ApiErrorCode error,
        String message,
        String path) {
}
