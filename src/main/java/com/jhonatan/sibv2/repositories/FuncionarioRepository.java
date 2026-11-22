package com.jhonatan.sibv2.repositories;

import com.jhonatan.sibv2.entities.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FuncionarioRepository extends JpaRepository<Funcionario, String> {
    Optional<Funcionario> findByEmailIgnoreCaseAndSenha(String email, String senha);
    Optional<Funcionario> findByEmailIgnoreCase(String email);
}
