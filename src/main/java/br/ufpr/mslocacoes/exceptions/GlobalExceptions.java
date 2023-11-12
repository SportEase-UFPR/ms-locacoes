package br.ufpr.mslocacoes.exceptions;

import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.format.DateTimeParseException;

import static br.ufpr.mslocacoes.utils.HorarioBrasil.buscarHoraAtual;

@ControllerAdvice
public class GlobalExceptions {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<StandardError> entityNotFoundHandlerMethod(
            EntityNotFoundException e, HttpServletRequest request) {

        StandardError se = new StandardError(
                buscarHoraAtual(), 404, HttpStatus.NOT_FOUND.getReasonPhrase(), e.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(se);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<StandardError> conflictHandlerMethod(
            ConflictException e, HttpServletRequest request) {

        StandardError se = new StandardError(
                buscarHoraAtual(), 409, HttpStatus.CONFLICT.getReasonPhrase(), e.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(se);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> validationErrors(
            MethodArgumentNotValidException e, HttpServletRequest request) {

        ValidationError errors = new ValidationError(
                buscarHoraAtual(), 400, HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "field validation error", request.getRequestURI());

        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.addError(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(JsonMappingException.class)
    public ResponseEntity<StandardError> handleJsonMappingException(JsonMappingException e, HttpServletRequest request) {
        if (e.getCause() instanceof DateTimeParseException) {
            StandardError se = new StandardError(
                    buscarHoraAtual(), 400, HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    "Date field must have the format 'yyyy-mm-dd'", request.getRequestURI());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(se);

        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StandardError(
                buscarHoraAtual(), 400, HttpStatus.BAD_REQUEST.getReasonPhrase(), null, request.getRequestURI()));
    }

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<StandardError> emailExceptionHandlerMethod(
            EmailException e, HttpServletRequest request) {

        StandardError se = new StandardError(
                buscarHoraAtual(), 500, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), e.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(se);
    }

    @ExceptionHandler(BussinessException.class)
    public ResponseEntity<StandardError> bussinessExceptionHandlerMethod(
            BussinessException e, HttpServletRequest request) {

        StandardError se = new StandardError(
                buscarHoraAtual(), 412, HttpStatus.PRECONDITION_FAILED.getReasonPhrase(), e.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(se);
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<StandardError> tokenInvalidoExceptionHandlerMethod(
            TokenInvalidoException e, HttpServletRequest request) {

        StandardError se = new StandardError(
                buscarHoraAtual(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                e.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(se);
    }
}
