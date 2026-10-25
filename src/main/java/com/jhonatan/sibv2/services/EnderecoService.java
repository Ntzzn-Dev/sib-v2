package com.jhonatan.sibv2.services;

import com.jhonatan.sibv2.dto.requests.EnderecoRequest;
import com.jhonatan.sibv2.dto.responses.EstadoResponse;
import com.jhonatan.sibv2.dto.responses.EnderecoResponse;
import com.jhonatan.sibv2.entities.Endereco;
import com.jhonatan.sibv2.repositories.EnderecoRepository;
import com.jhonatan.sibv2.utils.FormatadorDocumento;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class EnderecoService {
    private static final List<EstadoResponse> ESTADOS = List.of(
            new EstadoResponse("AC", "Acre"),
            new EstadoResponse("AL", "Alagoas"),
            new EstadoResponse("AP", "Amapá"),
            new EstadoResponse("AM", "Amazonas"),
            new EstadoResponse("BA", "Bahia"),
            new EstadoResponse("CE", "Ceará"),
            new EstadoResponse("DF", "Distrito Federal"),
            new EstadoResponse("ES", "Espírito Santo"),
            new EstadoResponse("GO", "Goiás"),
            new EstadoResponse("MA", "Maranhão"),
            new EstadoResponse("MT", "Mato Grosso"),
            new EstadoResponse("MS", "Mato Grosso do Sul"),
            new EstadoResponse("MG", "Minas Gerais"),
            new EstadoResponse("PA", "Pará"),
            new EstadoResponse("PB", "Paraíba"),
            new EstadoResponse("PR", "Paraná"),
            new EstadoResponse("PE", "Pernambuco"),
            new EstadoResponse("PI", "Piauí"),
            new EstadoResponse("RJ", "Rio de Janeiro"),
            new EstadoResponse("RN", "Rio Grande do Norte"),
            new EstadoResponse("RS", "Rio Grande do Sul"),
            new EstadoResponse("RO", "Rondônia"),
            new EstadoResponse("RR", "Roraima"),
            new EstadoResponse("SC", "Santa Catarina"),
            new EstadoResponse("SP", "São Paulo"),
            new EstadoResponse("SE", "Sergipe"),
            new EstadoResponse("TO", "Tocantins"));

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

    public List<EstadoResponse> listarUfs() {
        return ESTADOS;
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
        if (ESTADOS.stream().noneMatch(estado -> estado.getUf().equals(uf))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UF inválida");
        }

        endereco.setUf(uf);
        endereco.setCep(FormatadorDocumento.formatarCep(request.getCep()));
        endereco.setCidade(request.getCidade());
        endereco.setBairro(request.getBairro());
        endereco.setLogradouro(request.getLogradouro());
        endereco.setNumero(request.getNumero());
        endereco.setComplemento(request.getComplemento());
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
