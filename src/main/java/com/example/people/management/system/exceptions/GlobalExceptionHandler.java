package com.example.people.management.system.exceptions;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<ErrorMessage> handleImport(FileProcessingException ex) {
        ErrorMessage body = ErrorMessage.builder()
                .dateTime(LocalDateTime.now())
                .code(BAD_REQUEST.value())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ImportConcurencyException.class)
    public ResponseEntity<ErrorMessage> handleConcurrency(ImportConcurencyException ex) {
        ErrorMessage body = ErrorMessage.builder()
                .dateTime(LocalDateTime.now())
                .code(HttpStatus.TOO_MANY_REQUESTS.value())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorMessage> handleJPAOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        ErrorMessage body = ErrorMessage.builder()
                .dateTime(LocalDateTime.now())
                .code(CONFLICT.value())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(CONFLICT).body(body);
    }

    @ExceptionHandler(CsvImportException.class)
    public ResponseEntity<ErrorMessage> handleCsvError(CsvImportException ex) {
        ErrorMessage body = ErrorMessage.builder()
                .dateTime(LocalDateTime.now())
                .code(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(NotAnEmployeeException.class)
    public ResponseEntity<String> handleNotEmployee(NotAnEmployeeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(InvalidPositionPeriodException.class)
    public ResponseEntity<String> handleInvalidPeriod(InvalidPositionPeriodException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(PositionOverlapException.class)
    public ResponseEntity<String> handleOverlap(PositionOverlapException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorMessage> ConflictException(ConflictException e, HttpServletRequest request) {
        ErrorMessage body = ErrorMessage.builder()
                .dateTime(LocalDateTime.now())
                .code(HttpStatus.CONFLICT.value())
                .status(HttpStatus.CONFLICT.getReasonPhrase())
                .message(e.getMessage())
                .uri(request.getRequestURI())
                .method(request.getMethod())
                .build();

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(VersionConflictException.class)
    public ResponseEntity<ErrorMessage> handleVersionConflictException(VersionConflictException e, HttpServletRequest request) {
        ErrorMessage body = ErrorMessage.builder()
                .dateTime(LocalDateTime.now())
                .code(HttpStatus.CONFLICT.value())
                .status(HttpStatus.CONFLICT.getReasonPhrase())
                .message(e.getMessage())
                .uri(request.getRequestURI())
                .method(request.getMethod())
                .build();

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<Violation> violations = ex.getConstraintViolations()
                .stream()
                .map(v -> new Violation(v.getPropertyPath().toString(), v.getMessage()))
                .collect(Collectors.toList());

        return ResponseEntity
                .status(BAD_REQUEST)
                .body(ValidationErrorResponse.builder()
                        .violations(violations)
                        .build());
    }

    @ExceptionHandler(EmptyCsvFileException.class)
    public ResponseEntity<ErrorMessage> handleEmptyCsvFileException(EmptyCsvFileException e, HttpServletRequest request) {
        return new ResponseEntity<>(ErrorMessage.builder()
                .dateTime(LocalDateTime.now())
                .code(BAD_REQUEST.value())
                .status(BAD_REQUEST.getReasonPhrase())
                .message(e.getMessage())
                .uri(request.getRequestURI())
                .method(request.getMethod())
                .build(), BAD_REQUEST);
    }

    @ExceptionHandler(ImportProcessingException.class)
    public ResponseEntity<ErrorMessage> handleImportProcessingException(ImportProcessingException e, HttpServletRequest request) {
        return new ResponseEntity<>(ErrorMessage.builder()
                .dateTime(LocalDateTime.now())
                .code(INTERNAL_SERVER_ERROR.value())
                .status(INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An error occurred during data processing: " + e.getMessage())
                .uri(request.getRequestURI())
                .method(request.getMethod())
                .build(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ImportLockException.class)
    public ResponseEntity<ErrorMessage> handleImportLockException(ImportLockException e, HttpServletRequest request) {
        return new ResponseEntity<>(ErrorMessage.builder()
                .dateTime(LocalDateTime.now())
                .code(CONFLICT.value())
                .status(CONFLICT.getReasonPhrase())
                .message(e.getMessage())
                .uri(request.getRequestURI())
                .method(request.getMethod())
                .build(), CONFLICT);
    }

    @ExceptionHandler(StatusUpdateException.class)
    public ResponseEntity<ErrorMessage> handleStatusUpdateException(StatusUpdateException e, HttpServletRequest request) {
        return new ResponseEntity<>(ErrorMessage.builder()
                .dateTime(LocalDateTime.now())
                .code(INTERNAL_SERVER_ERROR.value())
                .status(INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An internal error occurred while updating import status.")
                .uri(request.getRequestURI())
                .method(request.getMethod())
                .build(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorMessage> handleIOException(IOException e, HttpServletRequest request) {
        return new ResponseEntity<>(ErrorMessage.builder()
                .dateTime(LocalDateTime.now())
                .code(INTERNAL_SERVER_ERROR.value())
                .status(INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An error occurred while reading request data or file.")
                .uri(request.getRequestURI())
                .method(request.getMethod())
                .build(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorMessage> handleMaxSizeException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        return new ResponseEntity<>(ErrorMessage.builder()
                .dateTime(LocalDateTime.now())
                .code(PAYLOAD_TOO_LARGE.value())
                .status(PAYLOAD_TOO_LARGE.getReasonPhrase())
                .message("The uploaded file exceeds the maximum allowed size.")
                .uri(request.getRequestURI())
                .method(request.getMethod())
                .build(), PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleGenericException(Exception e, HttpServletRequest request) {
        return new ResponseEntity<>(ErrorMessage.builder()
                .dateTime(LocalDateTime.now())
                .code(INTERNAL_SERVER_ERROR.value())
                .status(INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected internal error occurred. Please contact support.")
                .uri(request.getRequestURI())
                .method(request.getMethod())
                .build(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FileEmptyException.class)
    public ResponseEntity<String> handleFileEmpty(FileEmptyException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(ImportException.class)
    public ResponseEntity<String> handleImportError(ImportException ex) {
        return ResponseEntity.status(500).body(ex.getMessage());
    }


}
