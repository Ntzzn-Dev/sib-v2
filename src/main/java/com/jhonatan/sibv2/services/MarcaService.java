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
    private final MarcaRepository marcaRepository;

    public MarcaService(MarcaRepository marcaRepository) {
        this.marcaRepository = marcaRepository;
    }

    public List<MarcaResponse> listar(String pesquisa, boolean incluirInativos) {
        return marcaRepository.findAll().stream().filter(marca -> incluirInativos || marca.isAtivo())
                .filter(marca -> pesquisa == null || pesquisa.isBlank()
                        || contem(marca.getNomeMarca(), pesquisa))
                .map(this::mapearResposta)
                .toList();
    }

    public MarcaResponse buscar(int id) {
        return mapearResposta(buscarEntidadeObrigatoria(id));
    }

    public MarcaResponse criar(MarcaRequest request) {
        Marca marca = new Marca();
        marca.setNomeMarca(request.getNomeMarca());
        return mapearResposta(marcaRepository.save(marca));
    }

    public MarcaResponse editar(int id, MarcaRequest request) {
        Marca marca = buscarEntidadeObrigatoria(id);
        marca.setNomeMarca(request.getNomeMarca());
        return mapearResposta(marcaRepository.save(marca));
    }

    public void apagar(int id) {
        Marca marca = buscarEntidadeObrigatoria(id);
        marca.setAtivo(false);
        marcaRepository.save(marca);
    }

    public MarcaResponse reativar(int id) {
        Marca marca = marcaRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca não encontrada"));
        marca.setAtivo(true);
        return mapearResposta(marcaRepository.save(marca));
    }

    public Marca buscarEntidadeObrigatoria(int id) {
        Marca marca = marcaRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca não encontrada"));
        if (!marca.isAtivo()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca não encontrada");
        return marca;
    }

    private boolean contem(String valor, String q) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT));
    }

    public MarcaResponse mapearResposta(Marca marca) {
        MarcaResponse response = new MarcaResponse();
        response.setCodMarca(marca.getCodMarca());
        response.setNomeMarca(marca.getNomeMarca());
        response.setAtivo(marca.isAtivo());
        return response;
    }

}
