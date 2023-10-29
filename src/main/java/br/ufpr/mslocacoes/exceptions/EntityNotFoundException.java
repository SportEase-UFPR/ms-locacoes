package br.ufpr.mslocacoes.exceptions;

public class EntityNotFoundException extends RuntimeException{

    public EntityNotFoundException(String msg) {
        super(msg);
    }
}
