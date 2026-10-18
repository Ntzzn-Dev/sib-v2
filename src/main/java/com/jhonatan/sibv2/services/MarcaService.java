package com.jhonatan.sibv2.services;

import com.jhonatan.sibv2.dto.requests.MarcaRequest;
import com.jhonatan.sibv2.dto.responses.MarcaResponse;
import com.jhonatan.sibv2.entities.Marca;
import com.jhonatan.sibv2.repositories.MarcaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class MarcaService {
    private final MarcaRepository marcas;

    public MarcaService(MarcaRepository marcas) {
        this.marcas = marcas;
    }

    public List<MarcaResponse> listar(String q) {
        return marcas.findAll().stream()
                .filter(marca -> q == null || q.isBlank()
                        || contem(marca.getNomeMarca(), q))
                .map(this::resposta)
                .toList();
    }

    public MarcaResponse buscar(int id) {
        return resposta(buscarObrigatoria(id));
    }

    public Marca buscarEntidadeObrigatoria(int id) {
        return buscarObrigatoria(id);
    }

    public MarcaResponse criar(MarcaRequest request) {
        Marca marca = new Marca();
        marca.setNomeMarca(request.getNomeMarca());
        return resposta(marcas.save(marca));
    }

    public MarcaResponse editar(int id, MarcaRequest request) {
        Marca marca = buscarObrigatoria(id);
        marca.setNomeMarca(request.getNomeMarca());
        return resposta(marcas.save(marca));
    }

    public void apagar(int id) {
        marcas.delete(buscarObrigatoria(id));
    }

    private Marca buscarObrigatoria(int id) {
        return marcas.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca não encontrada"));
    }

    private boolean contem(String valor, String q) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT));
    }

    public MarcaResponse mapearResposta(Marca marca) {
        MarcaResponse response = new MarcaResponse();
        response.setCodMarca(marca.getCodMarca());
        response.setNomeMarca(marca.getNomeMarca());
        return response;
    }

    private MarcaResponse resposta(Marca marca) {
        return mapearResposta(marca);
    }
}
