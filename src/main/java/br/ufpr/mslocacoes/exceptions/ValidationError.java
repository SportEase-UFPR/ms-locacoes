package br.ufpr.mslocacoes.exceptions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter @Getter
@NoArgsConstructor
public class ValidationError extends StandardError {
    private List<FieldMessage> errors = new ArrayList<>();

    public ValidationError(LocalDateTime timestamp, Integer status, String error, String message, String path) {
        super(timestamp, status, error, message, path);
    }

    public void addError(String fieldName, String message) {
        this.errors.add(new FieldMessage(fieldName, message));
    }

}
