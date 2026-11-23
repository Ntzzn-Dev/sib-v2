package com.jhonatan.sibv2.services;

import com.jhonatan.sibv2.dto.requests.EnderecoRequest;
import com.jhonatan.sibv2.dto.responses.EstadoResponse;
import com.jhonatan.sibv2.dto.responses.EnderecoResponse;
import com.jhonatan.sibv2.entities.Endereco;
import com.jhonatan.sibv2.entities.Funcionario;
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

    private final EnderecoRepository enderecoRepository;

    public EnderecoService(EnderecoRepository enderecoRepository) {
        this.enderecoRepository = enderecoRepository;
    }

    public List<EnderecoResponse> listar(String pesquisa, boolean incluirInativos) {
        return enderecoRepository.findAll().stream().filter(endereco -> incluirInativos || endereco.isAtivo())
                .filter(endereco -> pesquisa == null || pesquisa.isBlank() || corresponde(endereco, pesquisa))
                .map(this::mapearResposta)
                .toList();
    }

    public List<EstadoResponse> listarUfs() {
        return ESTADOS;
    }

    public EnderecoResponse buscar(int id) {
        return mapearResposta(buscarObrigatorio(id));
    }

    public EnderecoResponse criar(EnderecoRequest request) {
        Endereco endereco = new Endereco();

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

        return mapearResposta(enderecoRepository.save(endereco));
    }

    public EnderecoResponse editar(int id, EnderecoRequest request) {
        Endereco endereco = buscarObrigatorio(id);

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

        return mapearResposta(enderecoRepository.save(endereco));
    }

    public void apagar(int id) {
        Endereco endereco = buscarObrigatorio(id);
        endereco.setAtivo(false);
        enderecoRepository.save(endereco);
    }

    public EnderecoResponse reativar(int id) {
        Endereco endereco = enderecoRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Endereço não encontrado"));
        endereco.setAtivo(true);
        return mapearResposta(enderecoRepository.save(endereco));
    }

    public Endereco buscarReferencia(EnderecoRequest request) {
        if (request == null || request.getCodEnd() <= 0) {
            return null;
        }
        return buscarObrigatorio(request.getCodEnd());
    }

    public Endereco buscarPorPessoa(String cpfPessoa) {
        return enderecoRepository.findFirstByCpfPessoaAndAtivoTrue(cpfPessoa).orElse(null);
    }

    public void associarPessoa(Endereco endereco, String cpfPessoa) {
        if (endereco == null) return;
        endereco.setCpfPessoa(cpfPessoa);
        enderecoRepository.save(endereco);
    }

    public void desassociarPessoa(Endereco endereco, String cpfPessoa) {
        if (endereco == null || !cpfPessoa.equals(endereco.getCpfPessoa())) return;
        endereco.setCpfPessoa(null);
        enderecoRepository.save(endereco);
    }

    public void salvarParaPessoa(Funcionario funcionario, EnderecoRequest request) {
        Endereco endereco = buscarPorPessoa(funcionario.getCpfPessoa());
        if (endereco == null && funcionario.getEndPessoa() != null) endereco = funcionario.getEndPessoa();
        if (endereco == null) endereco = new Endereco();
        if (request.getUf() == null || request.getUf().isBlank()
                || request.getCep() == null || request.getCep().isBlank()
                || request.getCidade() == null || request.getCidade().isBlank()
                || request.getBairro() == null || request.getBairro().isBlank()
                || request.getLogradouro() == null || request.getLogradouro().isBlank()
                || request.getNumero() == null || request.getNumero().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Preencha UF, CEP, cidade, bairro, logradouro e número para atualizar o endereço.");
        }
        String uf = request.getUf().trim().toUpperCase(Locale.ROOT);
        if (ESTADOS.stream().noneMatch(estado -> estado.getUf().equals(uf))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UF inválida");
        }
        endereco.setUf(uf);
        endereco.setCep(FormatadorDocumento.formatarCep(request.getCep()));
        endereco.setCidade(request.getCidade().trim());
        endereco.setBairro(request.getBairro().trim());
        endereco.setLogradouro(request.getLogradouro().trim());
        endereco.setNumero(request.getNumero().trim());
        endereco.setComplemento(request.getComplemento());
        endereco.setCpfPessoa(funcionario.getCpfPessoa());
        funcionario.setEndPessoa(enderecoRepository.save(endereco));
    }

    private Endereco buscarObrigatorio(int id) {
        Endereco endereco = enderecoRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Endereço não encontrado"));
        if (!endereco.isAtivo()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereço não encontrado");
        return endereco;
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
        response.setAtivo(endereco.isAtivo());
        return response;
    }

}
