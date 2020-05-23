package br.com.alura.estoque.retrofit;

import java.util.List;

import br.com.alura.estoque.model.Produto;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ProdutoService {

    @GET("produto")
    Call<List<Produto>> buscarTodos();
}
