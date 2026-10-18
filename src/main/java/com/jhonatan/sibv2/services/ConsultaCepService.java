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
                    "Não foi possível consultar o CEP agora. Tente novamente.", exception);
        }

        if (endereco == null || endereco.path("erro").asBoolean(false)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CEP não encontrado");
        }

        CepConsultaResponse response = new CepConsultaResponse();
        response.setCep(endereco.path("cep").asText(cepFormatado));
        response.setUf(endereco.path("uf").asText());
        response.setCidade(endereco.path("localidade").asText());
        response.setBairro(endereco.path("bairro").asText());
        response.setLogradouro(endereco.path("logradouro").asText());
        response.setComplemento(endereco.path("complemento").asText());
        return response;
    }
}
