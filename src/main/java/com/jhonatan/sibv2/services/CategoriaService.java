package com.jhonatan.sibv2.services;

import com.jhonatan.sibv2.dto.requests.CategoriaRequest;
import com.jhonatan.sibv2.dto.responses.CategoriaResponse;
import com.jhonatan.sibv2.entities.Categoria;
import com.jhonatan.sibv2.repositories.CategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<CategoriaResponse> listar(String pesquisa, boolean incluirInativos) {
        return categoriaRepository.findAll().stream().filter(categoria -> incluirInativos || categoria.isAtivo())
                .filter(categoria -> pesquisa == null || pesquisa.isBlank()
                        || contem(categoria.getNomeCat(), pesquisa))
                .map(this::mapearResposta)
                .toList();
    }

    public CategoriaResponse buscar(int id) {
        return mapearResposta(buscarEntidadeObrigatoria(id));
    }

    public CategoriaResponse criar(CategoriaRequest request) {
        Categoria categoria = new Categoria();
        categoria.setNomeCat(request.getNomeCat());
        return mapearResposta(categoriaRepository.save(categoria));
    }

    public CategoriaResponse editar(int id, CategoriaRequest request) {
        Categoria categoria = buscarEntidadeObrigatoria(id);
        categoria.setNomeCat(request.getNomeCat());
        return mapearResposta(categoriaRepository.save(categoria));
    }

    public void apagar(int id) {
        Categoria categoria = buscarEntidadeObrigatoria(id);
        categoria.setAtivo(false);
        categoriaRepository.save(categoria);
    }

    public CategoriaResponse reativar(int id) {
        Categoria categoria = categoriaRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));
        categoria.setAtivo(true);
        return mapearResposta(categoriaRepository.save(categoria));
    }

    public Categoria buscarEntidadeObrigatoria(int id) {
        Categoria categoria = categoriaRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));
        if (!categoria.isAtivo()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada");
        return categoria;
    }

    private boolean contem(String valor, String q) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT));
    }

    public CategoriaResponse mapearResposta(Categoria categoria) {
        CategoriaResponse response = new CategoriaResponse();
        response.setCodCat(categoria.getCodCat());
        response.setNomeCat(categoria.getNomeCat());
        response.setAtivo(categoria.isAtivo());
        return response;
    }

}
