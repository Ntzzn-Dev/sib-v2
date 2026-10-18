package com.jhonatan.sibv2.repositories;
import com.jhonatan.sibv2.entities.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FuncionarioRepository extends JpaRepository<Funcionario, String> {
    java.util.Optional<Funcionario> findByCpfPessoaAndSenha(String cpfPessoa, String senha);
}
