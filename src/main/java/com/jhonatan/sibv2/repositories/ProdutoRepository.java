package com.jhonatan.sibv2.repositories;
import com.jhonatan.sibv2.entities.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {}
