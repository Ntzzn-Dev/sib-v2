package com.jhonatan.sibv2.services;

import com.jhonatan.sibv2.dto.requests.ProdutoRequest;
import com.jhonatan.sibv2.dto.responses.CategoriaResponse;
import com.jhonatan.sibv2.dto.responses.MarcaResponse;
import com.jhonatan.sibv2.dto.responses.ProdutoResponse;
import com.jhonatan.sibv2.entities.Categoria;
import com.jhonatan.sibv2.entities.Marca;
import com.jhonatan.sibv2.entities.Produto;
import com.jhonatan.sibv2.repositories.ProdutoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProdutoService {
    private final ProdutoRepository produtoRepository;
    private final MarcaService marcaService;
    private final CategoriaService categoriaService;
    private final EntityManager entityManager;

    public ProdutoService(ProdutoRepository produtoRepository, MarcaService marcaService,
                          CategoriaService categoriaService, EntityManager entityManager) {
        this.produtoRepository = produtoRepository;
        this.marcaService = marcaService;
        this.categoriaService = categoriaService;
        this.entityManager = entityManager;
    }

    public List<ProdutoResponse> listar(String pesquisa, Boolean vendido) {
        String termo = pesquisa == null || pesquisa.isBlank() ? null : pesquisa.trim();

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("dbo.sp_produtos_listar");
        query.registerStoredProcedureParameter("q", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("vendido", Boolean.class, ParameterMode.IN);
        query.setParameter("q", termo);
        query.setParameter("vendido", vendido);
        @SuppressWarnings("unchecked")
        List<Number> resultados = query.getResultList();

        List<Integer> ids = resultados.stream().map(Number::intValue).toList();
        Map<Integer, Produto> porCodigo = produtoRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Produto::getCodProd, Function.identity()));
        return ids.stream().map(porCodigo::get).filter(java.util.Objects::nonNull)
                .map(this::mapearResposta).toList();
    }

    public ProdutoResponse buscar(int id) {
        return mapearResposta(buscarEntidadeObrigatoria(id));
    }

    public ProdutoResponse criar(ProdutoRequest request) {
        String tamanho = request.getTamanhoProd() == null ? null
                : request.getTamanhoProd().trim().toUpperCase(Locale.ROOT);
        Marca marca = buscarMarca(request);
        Categoria categoria = buscarCategoria(request);

        Produto produto = new Produto();
        produto.setMarcaProd(marca);
        produto.setCatProd(categoria);
        produto.setSelled(false);
        produto.setDescProd(request.getDescProd());
        produto.setPrecoProd(request.getPrecoProd());
        produto.setTamanhoProd(tamanho);
        return mapearResposta(produtoRepository.save(produto));
    }

    public ProdutoResponse editar(int id, ProdutoRequest request) {
        Produto produto = buscarEntidadeObrigatoria(id);
        String tamanho = request.getTamanhoProd() == null ? null
                : request.getTamanhoProd().trim().toUpperCase(Locale.ROOT);
        Marca marca = buscarMarca(request);
        Categoria categoria = buscarCategoria(request);

        produto.setMarcaProd(marca);
        produto.setCatProd(categoria);
        produto.setDescProd(request.getDescProd());
        produto.setPrecoProd(request.getPrecoProd());
        produto.setTamanhoProd(tamanho);

        return mapearResposta(produtoRepository.save(produto));
    }

    public void apagar(int id) {
        Produto produto = buscarEntidadeObrigatoria(id);
        produtoRepository.delete(produto);
    }

    public Produto buscarEntidadeObrigatoria(int id) {
        return produtoRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Produto não encontrado"));
    }

    private Marca buscarMarca(ProdutoRequest request) {
        if (request.getMarcaProd() == null || request.getMarcaProd().getCodMarca() <= 0) return null;
        return marcaService.buscarEntidadeObrigatoria(request.getMarcaProd().getCodMarca());
    }

    private Categoria buscarCategoria(ProdutoRequest request) {
        if (request.getCatProd() == null || request.getCatProd().getCodCat() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria do produto é obrigatória.");
        }
        return categoriaService.buscarEntidadeObrigatoria(request.getCatProd().getCodCat());
    }

    public ProdutoResponse mapearResposta(Produto produto) {
        ProdutoResponse response = new ProdutoResponse();
        response.setCodProd(produto.getCodProd());
        response.setDescProd(produto.getDescProd());
        response.setTamanhoProd(produto.getTamanhoProd());
        response.setMarcaProd(mapearMarcaResposta(produto.getMarcaProd()));
        response.setCatProd(mapearCategoriaResposta(produto.getCatProd()));
        response.setSelled(produto.isSelled());

        StoredProcedureQuery disponibilidade = entityManager.createStoredProcedureQuery("dbo.sp_produto_disponibilidade");
        disponibilidade.registerStoredProcedureParameter("cod_prod", Integer.class, ParameterMode.IN);
        disponibilidade.setParameter("cod_prod", produto.getCodProd());

        response.setDisponivel((Boolean) disponibilidade.getSingleResult());
        response.setPrecoProd(produto.getPrecoProd());
        return response;
    }

    private MarcaResponse mapearMarcaResposta(Marca marca) {
        if (marca == null) return null;
        MarcaResponse response = new MarcaResponse();
        response.setCodMarca(marca.getCodMarca());
        response.setNomeMarca(marca.getNomeMarca());
        return response;
    }

    private CategoriaResponse mapearCategoriaResposta(Categoria categoria) {
        CategoriaResponse response = new CategoriaResponse();
        response.setCodCat(categoria.getCodCat());
        response.setNomeCat(categoria.getNomeCat());
        return response;
    }
}
