package com.jhonatan.sibv2.services;

import com.jhonatan.sibv2.dto.requests.ProdutoRequest;
import com.jhonatan.sibv2.dto.responses.CategoriaResponse;
import com.jhonatan.sibv2.dto.responses.MarcaResponse;
import com.jhonatan.sibv2.dto.responses.ProdutoResponse;
import com.jhonatan.sibv2.entities.Categoria;
import com.jhonatan.sibv2.entities.Marca;
import com.jhonatan.sibv2.entities.Produto;
import com.jhonatan.sibv2.repositories.ProdutoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ProdutoService {
    private static final Set<String> TAMANHOS = Set.of("XS", "PP", "P", "M", "G", "GG", "XG", "XGG", "XXG", "XXXG",
            "XL", "XXL", "XXXL", "36", "38", "40", "42", "44", "46", "48", "50", "52", "54", "56",
            "33", "34", "35", "37", "39", "41", "43", "45", "47");

    private final ProdutoRepository produtos;
    private final MarcaService marcas;
    private final CategoriaService categorias;

    public ProdutoService(ProdutoRepository produtos, MarcaService marcas, CategoriaService categorias) {
        this.produtos = produtos;
        this.marcas = marcas;
        this.categorias = categorias;
    }

    public List<ProdutoResponse> listar(String q, Boolean vendido) {
        return produtos.findAll().stream()
                .filter(produto -> vendido == null || produto.isSelled() == vendido)
                .filter(produto -> q == null || q.isBlank() || corresponde(produto, q))
                .map(this::resposta)
                .toList();
    }

    public ProdutoResponse buscar(int id) {
        return resposta(buscarEntidadeObrigatoria(id));
    }

    public ProdutoResponse criar(ProdutoRequest request) {
        Produto produto = new Produto();
        produto.setMarcaProd(buscarMarca(request));
        produto.setCatProd(buscarCategoria(request));
        produto.setSelled(false);
        aplicarDados(produto, request);
        return resposta(produtos.save(produto));
    }

    public ProdutoResponse editar(int id, ProdutoRequest request) {
        Produto produto = buscarEntidadeObrigatoria(id);
        if (produto.isSelled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto vendido não pode ser editado");
        }
        produto.setMarcaProd(buscarMarca(request));
        produto.setCatProd(buscarCategoria(request));
        aplicarDados(produto, request);
        return resposta(produtos.save(produto));
    }

    public void apagar(int id) {
        Produto produto = buscarEntidadeObrigatoria(id);
        if (produto.isSelled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto vendido não pode ser removido");
        }
        produtos.delete(produto);
    }

    public Produto buscarEntidadeObrigatoria(int id) {
        return produtos.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Produto não encontrado"));
    }

    private Marca buscarMarca(ProdutoRequest request) {
        if (request.getMarcaProd() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma marca");
        }
        return marcas.buscarEntidadeObrigatoria(request.getMarcaProd().getCodMarca());
    }

    private Categoria buscarCategoria(ProdutoRequest request) {
        if (request.getCatProd() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma categoria");
        }
        return categorias.buscarEntidadeObrigatoria(request.getCatProd().getCodCat());
    }

    private void aplicarDados(Produto produto, ProdutoRequest request) {
        produto.setDescProd(request.getDescProd());
        produto.setTamanhoProd(validarTamanho(request.getTamanhoProd()));
        if (request.getPrecoProd() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Preço não pode ser negativo");
        }
        produto.setPrecoProd(request.getPrecoProd());
    }

    private String validarTamanho(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!TAMANHOS.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tamanho inválido");
        }
        return normalized;
    }

    private boolean corresponde(Produto produto, String q) {
        String termo = q.toLowerCase(Locale.ROOT);
        return contem(produto.getDescProd(), termo) || contem(produto.getTamanhoProd(), termo)
                || contem(produto.getMarcaProd().getNomeMarca(), termo)
                || contem(produto.getCatProd().getNomeCat(), termo);
    }

    private boolean contem(String valor, String termo) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(termo);
    }

    public ProdutoResponse mapearResposta(Produto produto) {
        ProdutoResponse response = new ProdutoResponse();
        response.setCodProd(produto.getCodProd());
        response.setDescProd(produto.getDescProd());
        response.setTamanhoProd(produto.getTamanhoProd());
        response.setMarcaProd(respostaMarca(produto.getMarcaProd()));
        response.setCatProd(respostaCategoria(produto.getCatProd()));
        response.setSelled(produto.isSelled());
        response.setPrecoProd(produto.getPrecoProd());
        return response;
    }

    private ProdutoResponse resposta(Produto produto) {
        return mapearResposta(produto);
    }

    private MarcaResponse respostaMarca(Marca marca) {
        MarcaResponse response = new MarcaResponse();
        response.setCodMarca(marca.getCodMarca());
        response.setNomeMarca(marca.getNomeMarca());
        return response;
    }

    private CategoriaResponse respostaCategoria(Categoria categoria) {
        CategoriaResponse response = new CategoriaResponse();
        response.setCodCat(categoria.getCodCat());
        response.setNomeCat(categoria.getNomeCat());
        return response;
    }
}
