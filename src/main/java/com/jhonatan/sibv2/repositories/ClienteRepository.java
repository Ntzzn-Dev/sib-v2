package com.jhonatan.sibv2.repositories;
import com.jhonatan.sibv2.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ClienteRepository extends JpaRepository<Cliente, String> {}
