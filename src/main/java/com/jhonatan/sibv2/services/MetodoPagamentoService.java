package com.jhonatan.sibv2.services;

import com.jhonatan.sibv2.dto.requests.MetodoPagamentoRequest;
import com.jhonatan.sibv2.dto.responses.MetodoPagamentoResponse;
import com.jhonatan.sibv2.entities.MetodoPagamento;
import com.jhonatan.sibv2.repositories.MetodoPagamentoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class MetodoPagamentoService {
    private final MetodoPagamentoRepository metodoPagamentoRepository;

    public MetodoPagamentoService(MetodoPagamentoRepository metodoPagamentoRepository) {
        this.metodoPagamentoRepository = metodoPagamentoRepository;
    }

    public List<MetodoPagamentoResponse> listar(String pesquisa) {
        return metodoPagamentoRepository.findAll().stream()
                .filter(metodo -> pesquisa == null || pesquisa.isBlank()
                        || contem(metodo.getDescMetodoPag(), pesquisa)
                        || (metodo.isActiveMetodoPag() ? "ativo" : "desativado").contains(pesquisa.toLowerCase(Locale.ROOT)))
                .map(this::mapearResposta)
                .toList();
    }

    public MetodoPagamentoResponse buscar(int id) {
        return mapearResposta(buscarObrigatorio(id));
    }

    public MetodoPagamentoResponse criar(MetodoPagamentoRequest request) {
        MetodoPagamento metodo = new MetodoPagamento();
        metodo.setDescMetodoPag(request.getDescMetodoPag());
        metodo.setActiveMetodoPag(request.isActiveMetodoPag());
        return mapearResposta(metodoPagamentoRepository.save(metodo));
    }

    public MetodoPagamentoResponse editar(int id, MetodoPagamentoRequest request) {
        MetodoPagamento metodo = buscarObrigatorio(id);
        metodo.setDescMetodoPag(request.getDescMetodoPag());
        metodo.setActiveMetodoPag(request.isActiveMetodoPag());
        return mapearResposta(metodoPagamentoRepository.save(metodo));
    }

    public void apagar(int id) {
        metodoPagamentoRepository.delete(buscarObrigatorio(id));
    }

    private MetodoPagamento buscarObrigatorio(int id) {
        return metodoPagamentoRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Método de pagamento não encontrado"));
    }

    private boolean contem(String valor, String q) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT));
    }

    public MetodoPagamentoResponse mapearResposta(MetodoPagamento metodo) {
        MetodoPagamentoResponse response = new MetodoPagamentoResponse();
        response.setCodMetodoPag(metodo.getCodMetodoPag());
        response.setDescMetodoPag(metodo.getDescMetodoPag());
        response.setActiveMetodoPag(metodo.isActiveMetodoPag());
        return response;
    }

}
