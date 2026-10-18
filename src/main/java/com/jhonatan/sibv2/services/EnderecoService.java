package com.jhonatan.sibv2.services;

import com.jhonatan.sibv2.dto.requests.EnderecoRequest;
import com.jhonatan.sibv2.dto.responses.EnderecoResponse;
import com.jhonatan.sibv2.entities.Endereco;
import com.jhonatan.sibv2.repositories.EnderecoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class EnderecoService {
    private static final String UFS = "AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO";

    private final EnderecoRepository enderecos;

    public EnderecoService(EnderecoRepository enderecos) {
        this.enderecos = enderecos;
    }

    public List<EnderecoResponse> listar(String q) {
        return enderecos.findAll().stream()
                .filter(endereco -> q == null || q.isBlank() || corresponde(endereco, q))
                .map(this::resposta)
                .toList();
    }

    public EnderecoResponse buscar(int id) {
        return resposta(buscarObrigatorio(id));
    }

    public EnderecoResponse criar(EnderecoRequest request) {
        Endereco endereco = new Endereco();
        aplicarDados(endereco, request);
        return resposta(enderecos.save(endereco));
    }

    public EnderecoResponse editar(int id, EnderecoRequest request) {
        Endereco endereco = buscarObrigatorio(id);
        aplicarDados(endereco, request);
        return resposta(enderecos.save(endereco));
    }

    public void apagar(int id) {
        enderecos.delete(buscarObrigatorio(id));
    }

    public Endereco buscarReferencia(EnderecoRequest request) {
        if (request == null || request.getCodEnd() <= 0) {
            return null;
        }
        return buscarObrigatorio(request.getCodEnd());
    }

    private Endereco buscarObrigatorio(int id) {
        return enderecos.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Endereço não encontrado"));
    }

    private void aplicarDados(Endereco endereco, EnderecoRequest request) {
        String uf = request.getUf().trim().toUpperCase(Locale.ROOT);
        if (!uf.matches(UFS)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UF inválida");
        }

        endereco.setUf(uf);
        endereco.setCep(formatarCep(request.getCep()));
        endereco.setCidade(request.getCidade());
        endereco.setBairro(request.getBairro());
        endereco.setLogradouro(request.getLogradouro());
        endereco.setNumero(request.getNumero());
        endereco.setComplemento(request.getComplemento());
    }

    private String formatarCep(String value) {
        String digits = value == null ? "" : value.replaceAll("\\D", "");
        if (digits.length() != 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CEP inválido: informe 8 dígitos");
        }
        return digits.substring(0, 5) + "-" + digits.substring(5);
    }

    private boolean corresponde(Endereco endereco, String q) {
        String termo = q.toLowerCase(Locale.ROOT);
        return contem(endereco.getUf(), termo) || contem(endereco.getCep(), termo)
                || contem(endereco.getCidade(), termo) || contem(endereco.getBairro(), termo)
                || contem(endereco.getLogradouro(), termo) || contem(endereco.getNumero(), termo)
                || contem(endereco.getComplemento(), termo);
    }

    private boolean contem(String valor, String termo) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(termo);
    }

    public EnderecoResponse mapearResposta(Endereco endereco) {
        EnderecoResponse response = new EnderecoResponse();
        response.setCodEnd(endereco.getCodEnd());
        response.setUf(endereco.getUf());
        response.setCep(endereco.getCep());
        response.setCidade(endereco.getCidade());
        response.setBairro(endereco.getBairro());
        response.setLogradouro(endereco.getLogradouro());
        response.setNumero(endereco.getNumero());
        response.setComplemento(endereco.getComplemento());
        return response;
    }

    private EnderecoResponse resposta(Endereco endereco) {
        return mapearResposta(endereco);
    }
}
