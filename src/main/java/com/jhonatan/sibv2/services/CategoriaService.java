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
    private final CategoriaRepository categorias;

    public CategoriaService(CategoriaRepository categorias) {
        this.categorias = categorias;
    }

    public List<CategoriaResponse> listar(String q) {
        return categorias.findAll().stream()
                .filter(categoria -> q == null || q.isBlank()
                        || contem(categoria.getNomeCat(), q))
                .map(this::resposta)
                .toList();
    }

    public CategoriaResponse buscar(int id) {
        return resposta(buscarObrigatoria(id));
    }

    public Categoria buscarEntidadeObrigatoria(int id) {
        return buscarObrigatoria(id);
    }

    public CategoriaResponse criar(CategoriaRequest request) {
        Categoria categoria = new Categoria();
        categoria.setNomeCat(request.getNomeCat());
        return resposta(categorias.save(categoria));
    }

    public CategoriaResponse editar(int id, CategoriaRequest request) {
        Categoria categoria = buscarObrigatoria(id);
        categoria.setNomeCat(request.getNomeCat());
        return resposta(categorias.save(categoria));
    }

    public void apagar(int id) {
        categorias.delete(buscarObrigatoria(id));
    }

    private Categoria buscarObrigatoria(int id) {
        return categorias.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));
    }

    private boolean contem(String valor, String q) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT));
    }

    public CategoriaResponse mapearResposta(Categoria categoria) {
        CategoriaResponse response = new CategoriaResponse();
        response.setCodCat(categoria.getCodCat());
        response.setNomeCat(categoria.getNomeCat());
        return response;
    }

    private CategoriaResponse resposta(Categoria categoria) {
        return mapearResposta(categoria);
    }
}
