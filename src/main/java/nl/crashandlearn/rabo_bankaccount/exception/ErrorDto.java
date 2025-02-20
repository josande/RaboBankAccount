package nl.crashandlearn.rabo_bankaccount.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.util.Date;

public record ErrorDto(
        @Schema(example = "400 Bad Request") String httpStatus,
        @Schema(example = "Bad Request") String message,
        @Schema(example = "2025-01-01T12:34:56.123Z") Date time) {
    ErrorDto(HttpStatus httpStatus, String message, Date time) {
        this(httpStatus.toString(), message, time);
    }
}