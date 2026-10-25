package com.jhonatan.sibv2.services;

import com.jhonatan.sibv2.dto.responses.CepConsultaResponse;
import com.jhonatan.sibv2.utils.FormatadorDocumento;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;

@Service
public class ConsultaCepService {
    private final RestClient viaCep;

    public ConsultaCepService() {
        this.viaCep = RestClient.create("https://viacep.com.br/ws");
    }

    public CepConsultaResponse buscar(String cep) {
        String cepFormatado = FormatadorDocumento.formatarCep(cep);
        String cepNumerico = cepFormatado.replace("-", "");
        JsonNode endereco;

        try {
            endereco = viaCep.get()
                    .uri("/{cep}/json/", cepNumerico)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Não foi possível carregar o endereço pelo CEP. Verifique sua conexão ou preencha os campos manualmente.", exception);
        }

        if (endereco == null || endereco.path("erro").asBoolean(false)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CEP não encontrado");
        }

        CepConsultaResponse response = new CepConsultaResponse();
        response.setCep(endereco.path("cep").asString(cepFormatado));
        response.setUf(endereco.path("uf").asString());
        response.setCidade(endereco.path("localidade").asString());
        response.setBairro(endereco.path("bairro").asString());
        response.setLogradouro(endereco.path("logradouro").asString());
        response.setComplemento(endereco.path("complemento").asString());
        return response;
    }
}
