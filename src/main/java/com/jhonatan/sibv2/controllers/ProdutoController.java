package com.jhonatan.sibv2.controllers;

import com.jhonatan.sibv2.entities.Categoria;
import com.jhonatan.sibv2.entities.Marca;
import com.jhonatan.sibv2.entities.Produto;
import com.jhonatan.sibv2.repositories.CategoriaRepository;
import com.jhonatan.sibv2.repositories.MarcaRepository;
import com.jhonatan.sibv2.repositories.ProdutoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.jhonatan.sibv2.controllers.ApiSupport.*;

@RestController
@RequestMapping("/api/produtos")
@CrossOrigin
public class ProdutoController {

    private static final Set<String> TAMANHOS = Set.of("XS", "PP", "P", "M", "G", "GG", "XG", "XGG", "XXG", "XXXG",
            "XL", "XXL", "XXXL", "36", "38", "40", "42", "44", "46", "48", "50", "52", "54", "56",
            "33", "34", "35", "37", "39", "41", "43", "45", "47");

    private final ProdutoRepository produtos;
    private final MarcaRepository marcas;
    private final CategoriaRepository categorias;

    public ProdutoController(ProdutoRepository produtos, MarcaRepository marcas, CategoriaRepository categorias) {
        this.produtos = produtos;
        this.marcas = marcas;
        this.categorias = categorias;
    }

    @GetMapping
    public List<Produto> listar(@RequestParam(required = false) String q, @RequestParam(required = false) Boolean vendido) {
        return produtos.findAll().stream().filter(p -> vendido == null || p.isSelled() == vendido)
                .filter(p -> q == null || q.isBlank() || has(p.getDescProd(), q) || has(p.getTamanhoProd(), q)
                        || has(p.getMarcaProd().getNomeMarca(), q) || has(p.getCatProd().getNomeCat(), q)).toList();
    }

    @GetMapping("/{id}")
    public Produto buscar(@PathVariable int id) {
        return required(produtos, id, "Produto");
    }

    @PostMapping public Produto criar(@RequestBody Produto produto) {
        produto.setCodProd(0);
        produto.setMarcaProd(marcaRef(produto.getMarcaProd()));
        produto.setCatProd(categoriaRef(produto.getCatProd()));
        produto.setSelled(false);
        produto.setTamanhoProd(tamanho(produto.getTamanhoProd()));
        validarPreco(produto.getPrecoProd());
        return produtos.save(produto);
    }

    @PutMapping("/{id}")
    public Produto editar(@PathVariable int id, @RequestBody Produto dados) {
        Produto produto = required(produtos, id, "Produto");

        if (produto.isSelled()) {
            invalid("Produto vendido não pode ser editado");
        }

        produto.setDescProd(dados.getDescProd());
        produto.setTamanhoProd(tamanho(dados.getTamanhoProd()));
        produto.setMarcaProd(marcaRef(dados.getMarcaProd()));
        produto.setCatProd(categoriaRef(dados.getCatProd()));
        produto.setPrecoProd(dados.getPrecoProd());
        validarPreco(produto.getPrecoProd());
        return produtos.save(produto);
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable int id) {
        Produto produto = required(produtos, id, "Produto");
        if (produto.isSelled()) {
            invalid("Produto vendido não pode ser removido");
        }
        produtos.delete(produto);
    }

    private Marca marcaRef(Marca value) {
        return required(marcas, value.getCodMarca(), "Marca");
    }

    private Categoria categoriaRef(Categoria value) {
        return required(categorias, value.getCodCat(), "Categoria");
    }

    private static String tamanho(String value) {
        String normalized = norm(value).toUpperCase(Locale.ROOT);
        if (!TAMANHOS.contains(normalized)) {
            invalid("Tamanho inválido");
        }
        return normalized;
    }

    private static void validarPreco(double preco) {
        if (preco < 0) {
            invalid("Preço não pode ser negativo");
        }
    }
}
