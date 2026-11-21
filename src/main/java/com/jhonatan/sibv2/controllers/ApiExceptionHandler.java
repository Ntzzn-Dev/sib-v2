package com.jhonatan.sibv2.controllers;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail handleExpectedError(ResponseStatusException exception) {
        String detail = exception.getReason() == null ? "Não foi possível concluir a solicitação." : exception.getReason();
        return ProblemDetail.forStatusAndDetail(exception.getStatusCode(), detail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleInvalidJson(HttpMessageNotReadableException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "O JSON enviado está inválido ou falta um campo obrigatório.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleConflict(DataIntegrityViolationException exception) {
        SQLException sqlException = findSqlException(exception);
        if (sqlException != null) {
            if (sqlException.getErrorCode() == 515) {
                return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                        "Um campo obrigatório não foi informado.");
            }
            if (sqlException.getErrorCode() == 547) {
                return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                        "Um valor informado viola uma regra ou relacionamento do banco de dados.");
            }
            if (sqlException.getErrorCode() == 2601 || sqlException.getErrorCode() == 2627) {
                return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                        "Já existe um registro com os mesmos dados únicos.");
            }
        }
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "O registro está relacionado a outros dados e não pode ser removido ou alterado.");
    }

    @ExceptionHandler(DataAccessException.class)
    ProblemDetail handleDatabaseError(DataAccessException exception) {
        SQLException sqlException = findSqlException(exception);
        if (sqlException != null && sqlException.getErrorCode() >= 51001
                && sqlException.getErrorCode() <= 51028) {
            return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                    "A operação viola uma regra de negócio: " + sqlException.getMessage());
        }
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Não foi possível concluir a operação no banco de dados.");
    }

    private SQLException findSqlException(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof SQLException sqlException) {
                return sqlException;
            }
            current = current.getCause();
        }
        return null;
    }
}
