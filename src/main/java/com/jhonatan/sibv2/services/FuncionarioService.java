package com.jhonatan.sibv2.services;

import com.jhonatan.sibv2.dto.requests.FuncionarioRequest;
import com.jhonatan.sibv2.dto.responses.FuncionarioResponse;
import com.jhonatan.sibv2.entities.Endereco;
import com.jhonatan.sibv2.entities.Funcionario;
import com.jhonatan.sibv2.repositories.FuncionarioRepository;
import com.jhonatan.sibv2.utils.FormatadorDocumento;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class FuncionarioService {
    private final FuncionarioRepository funcionarioRepository;
    private final EnderecoService enderecoService;

    public FuncionarioService(FuncionarioRepository funcionarioRepository, EnderecoService enderecoService) {
        this.funcionarioRepository = funcionarioRepository;
        this.enderecoService = enderecoService;
    }

    public List<FuncionarioResponse> listar(String pesquisa) {
        String termo = pesquisa == null || pesquisa.isBlank() ? null : pesquisa.trim();
        List<Funcionario> funcionarios = funcionarioRepository.findAll();
        return funcionarios.stream()
                .filter(funcionario -> termo == null || corresponde(funcionario, termo))
                .map(this::mapearResposta)
                .toList();
    }

    public FuncionarioResponse buscar(String cpf) {
        return mapearResposta(buscarEntidadeObrigatoria(FormatadorDocumento.normalizarCpf(cpf)));
    }

    public FuncionarioResponse criar(FuncionarioRequest request) {
        String cpf = FormatadorDocumento.normalizarCpf(request.getCpfPessoa());
        Endereco end = enderecoService.buscarReferencia(request.getEndPessoa());
        String senha = senhaObrigatoria(request.getSenha());
        boolean primeiroFuncionario = funcionarioRepository.count() == 0;
        int perfil = primeiroFuncionario
                ? Funcionario.PERFIL_ADMIN
                : perfilValidoOuPadrao(request.getPerfil());

        Funcionario funcionario = new Funcionario();
        funcionario.setCpfPessoa(cpf);
        funcionario.setNomePessoa(request.getNomePessoa());
        funcionario.setTelPessoa(request.getTelPessoa());
        funcionario.setSalarioFunc(request.getSalarioFunc());
        funcionario.setEndPessoa(end);
        funcionario.setSenha(senha);
        funcionario.setPerfil(perfil);

        Funcionario criado = funcionarioRepository.save(funcionario);
        enderecoService.associarPessoa(criado.getEndPessoa(), criado.getCpfPessoa());

        return mapearResposta(criado);
    }

    public FuncionarioResponse editar(String cpf, FuncionarioRequest request) {
        Funcionario funcionario = buscarEntidadeObrigatoria(FormatadorDocumento.normalizarCpf(cpf));

        Endereco end = enderecoService.buscarReferencia(request.getEndPessoa());
        String senha = senhaObrigatoria(request.getSenha());
        int perfilAnterior = funcionario.getPerfil();

        funcionario.setNomePessoa(request.getNomePessoa());
        funcionario.setTelPessoa(request.getTelPessoa());
        funcionario.setSalarioFunc(request.getSalarioFunc());
        funcionario.setEndPessoa(end);
        funcionario.setSenha(senha);

        if (request.getPerfil() != null) {
            funcionario.setPerfil(perfilValidoOuPadrao(request.getPerfil()));
        } else {
            funcionario.setPerfil(perfilAnterior);
        }

        if(perfilAnterior == Funcionario.PERFIL_ADMIN) {
            minimoAdmin(funcionario);
        }

        Funcionario atualizado = funcionarioRepository.save(funcionario);
        enderecoService.associarPessoa(atualizado.getEndPessoa(), atualizado.getCpfPessoa());

        return mapearResposta(atualizado);
    }

    public void apagar(String cpf) {
        Funcionario funcionario = buscarEntidadeObrigatoria(FormatadorDocumento.normalizarCpf(cpf));
        minimoAdmin(funcionario);
        funcionarioRepository.delete(funcionario);
    }

    public Funcionario buscarEntidadeObrigatoria(String cpf) {
        return funcionarioRepository.findById(cpf).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Funcionário não encontrado"));
    }

    private boolean corresponde(Funcionario funcionario, String pesquisa) {
        String termo = pesquisa.toLowerCase(Locale.ROOT);
        return contem(funcionario.getNomePessoa(), termo) || contem(FormatadorDocumento.formatarCpf(funcionario.getCpfPessoa()), termo)
                || contem(funcionario.getTelPessoa(), termo)
                || funcionario.getSalarioFunc().toPlainString().contains(termo);
    }

    private boolean contem(String valor, String termo) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(termo);
    }

    public FuncionarioResponse mapearResposta(Funcionario funcionario) {
        FuncionarioResponse response = new FuncionarioResponse();
        response.setCpfPessoa(FormatadorDocumento.formatarCpf(funcionario.getCpfPessoa()));
        response.setNomePessoa(funcionario.getNomePessoa());
        response.setTelPessoa(funcionario.getTelPessoa());
        response.setSalarioFunc(funcionario.getSalarioFunc());
        response.setPerfil(funcionario.getPerfil());
        var endereco = funcionario.getEndPessoa() != null ? funcionario.getEndPessoa()
                : enderecoService.buscarPorPessoa(funcionario.getCpfPessoa());
        if (endereco != null) {
            response.setEndPessoa(enderecoService.mapearResposta(endereco));
        }
        return response;
    }

    private int perfilValidoOuPadrao(Integer perfil) {
        if (perfil == null) return Funcionario.PERFIL_ATENDENTE;
        if (perfil != Funcionario.PERFIL_ADMIN && perfil != Funcionario.PERFIL_ATENDENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Perfil de funcionário inválido.");
        }
        return perfil;
    }

    private void minimoAdmin(Funcionario funcionario){
        if (funcionario.getPerfil() != Funcionario.PERFIL_ADMIN
                && funcionarioRepository.findAll().stream().noneMatch(outro ->
                !outro.getCpfPessoa().equals(funcionario.getCpfPessoa())
                        && outro.getPerfil() == Funcionario.PERFIL_ADMIN)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O sistema precisa manter ao menos um administrador.");
        }
    }

    private String senhaObrigatoria(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A senha do funcionário é obrigatória.");
        }
        return senha;
    }
}
