package br.com.maike.order_managment_ssm.exceptions;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {

    public NotFoundException() {
        super("Não encontrado.");
    }
    public NotFoundException(String message) {
        super(String.format("O %s não foi encontrado.", message));
    }

    public NotFoundException(Long id) {
        super(String.format("O ID (%s) não foi encontrado na base de dados.", id));
    }
}

