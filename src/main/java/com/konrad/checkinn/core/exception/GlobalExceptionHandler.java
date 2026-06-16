package com.konrad.checkinn.core.exception;

import com.konrad.checkinn.core.dto.ErrorResponseDTO;
import com.konrad.checkinn.listing.exception.ListingNotFoundException;
import com.konrad.checkinn.listingPhoto.exception.IllegalFileFormatException;
import com.konrad.checkinn.listingPhoto.exception.ListingPhotoNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ErrorResponseDTO buildError(int status, String errorTitle, String message) {
        ErrorResponseDTO dto = new ErrorResponseDTO();
        dto.setTimestamp(LocalDateTime.now());
        dto.setError(errorTitle);
        dto.setMessage(message);
        dto.setStatus(status);
        return dto;
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserAlreadyExist(UserAlreadyExistException exception) {
        ErrorResponseDTO errorDetails = buildError(HttpStatus.CONFLICT.value(), "User Already Exist", exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDetails);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationError(MethodArgumentNotValidException exception) {
        StringBuilder sb = new StringBuilder();
        sb.append("Validation Error(s) ");
        exception.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            sb.append(fieldName).append(": ").append(errorMessage).append(", ");
        });

        ErrorResponseDTO errorDetails = buildError(
                HttpStatus.BAD_REQUEST.value(), sb.toString(), exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
    }

    @ExceptionHandler(InvalidCredentialException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidCredential(InvalidCredentialException exception){
        ErrorResponseDTO errorDetails = buildError(HttpStatus.UNAUTHORIZED.value(), "Invalid Credentials", exception.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDetails);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidRequestException(InvalidRequestException exception){
        ErrorResponseDTO errorDetails = buildError(HttpStatus.BAD_REQUEST.value(), "Invalid Request", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
    }

    @ExceptionHandler(StorageOperationException.class)
    public ResponseEntity<ErrorResponseDTO> handleStorageOperationException(StorageOperationException exception){
        ErrorResponseDTO errorDetails = buildError(HttpStatus.SERVICE_UNAVAILABLE.value(), "Storage Operation Failed : ", exception.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorDetails);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericError(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                buildError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", "An unexpected error occurred")
        );
    }

    @ExceptionHandler(IllegalFileFormatException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalFileFormat(IllegalFileFormatException exception) {
        ErrorResponseDTO errorDetails = buildError(HttpStatus.BAD_REQUEST.value(), "Invalid File Format", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
    }

    @ExceptionHandler(ListingPhotoNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleListingPhotoNotFound(ListingPhotoNotFoundException exception) {
        ErrorResponseDTO errorDetails = buildError(HttpStatus.NOT_FOUND.value(), "Photo Not Found", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
    }

    @ExceptionHandler(ListingNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleListingNotFound(ListingNotFoundException exception) {
        ErrorResponseDTO errorDetails = buildError(HttpStatus.NOT_FOUND.value(), "Listing Not Found", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFound(UserNotFoundException exception) {
        ErrorResponseDTO errorDetails = buildError(HttpStatus.NOT_FOUND.value(), "User Not Found", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException exception) {
        ErrorResponseDTO errorDetails = buildError(HttpStatus.FORBIDDEN.value(), "Forbidden", "You do not have permission to perform this action");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorDetails);
    }

}
