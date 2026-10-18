package com.jhonatan.sibv2.controllers;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

final class ApiSupport {
    private ApiSupport() {}

    static String norm(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    static boolean has(String value, String query) {
        return norm(value).contains(norm(query));
    }

    static <T> List<T> filter(List<T> values, String query, Predicate<T> matcher) {
        return query == null || query.isBlank() ? values : values.stream().filter(matcher).toList();
    }

    static <T, ID> T required(JpaRepository<T, ID> repository, ID id, String name) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, name + " não encontrado"));
    }

    static void invalid(String message) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    static String cpf(String value) {
        String digits = value == null ? "" : value.replaceAll("\\D", "");

        if (digits.length() != 11) {
            invalid("CPF inválido: informe 11 dígitos");
        }

        return digits.substring(0, 3) + "." + digits.substring(3, 6) + "."
                + digits.substring(6, 9) + "-" + digits.substring(9);
    }

    static String cep(String value) {
        String digits = value == null ? "" : value.replaceAll("\\D", "");

        if (digits.length() != 8) {
            invalid("CEP inválido: informe 8 dígitos");
        }

        return digits.substring(0, 5) + "-" + digits.substring(5);
    }
}
