package br.com.alura.estoque.repository;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
import br.com.alura.estoque.retrofit.ProdutoService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProdutoRepository {

    final private ProdutoDAO dao;
    private final ProdutoService service;

    public ProdutoRepository(ProdutoDAO dao) {
        this.dao = dao;
        service = new EstoqueRetrofit().getProdutoService();
    }

    public void buscaProdutos(DadosCarregadosCallBack<List<Produto>> callBack) {
        buscaProdutosInternos(callBack);
    }

    private void buscaProdutosInternos(DadosCarregadosCallBack<List<Produto>> callBack) {
        new BaseAsyncTask<>(dao::buscaTodos,
                resultado -> {
                    callBack.quandoSucesso(resultado);
                    buscaProdutosNaAPI(callBack);
                }).execute();
    }

    private void buscaProdutosNaAPI(DadosCarregadosCallBack<List<Produto>> callBack) {

        Call<List<Produto>> call = service.buscarTodos();

        call.enqueue(new Callback<List<Produto>>() {
            @Override
            public void onResponse(Call<List<Produto>> call, Response<List<Produto>> response) {
                if (response.isSuccessful()) {
                    List<Produto> produtosNovos = response.body();
                    if (produtosNovos != null) {
                        atualizaInterno(produtosNovos, callBack);
                    }
                } else {
                    callBack.quandoFalha("Resposta nāo sucedida!");
                }
            }

            @Override
            public void onFailure(Call<List<Produto>> call, Throwable t) {
                callBack.quandoFalha("Resposta nāo sucedida!");
            }
        });
    }

    private void atualizaInterno(List<Produto> produtosNovos, DadosCarregadosCallBack<List<Produto>> callBack) {
        new BaseAsyncTask<>(()->{
            dao.salva(produtosNovos);
            return dao.buscaTodos();
        }, callBack::quandoSucesso)
                .execute();
    }

    public void salva(Produto produto, DadosCarregadosCallBack<Produto> callBack) {
        salvaNaAPI(produto, callBack);
    }

    private void salvaNaAPI(Produto produto, DadosCarregadosCallBack<Produto> callBack) {
        final Call<Produto> call = service.salva(produto);
        call.enqueue(new Callback<Produto>() {
            @Override
            public void onResponse(Call<Produto> call, Response<Produto> response) {
                if (response.isSuccessful()) {
                    final Produto produtoSalvo = response.body();
                    if (produtoSalvo != null)
                        salvaInterno(produtoSalvo, callBack);
                } else {
                    callBack.quandoFalha("Nāo foi possível Salvar!");
                }
            }

            @Override
            public void onFailure(Call<Produto> call, Throwable t) {
                callBack.quandoFalha("Erro comunicaçāo: " + t.getMessage());
            }
        });
    }

    private void salvaInterno(Produto produto, DadosCarregadosCallBack<Produto> callBack) {
        new BaseAsyncTask<>(() -> {
            long id = dao.salva(produto);
            return dao.buscaProduto(id);
        }, produtoSalvo ->
                callBack.quandoSucesso(produtoSalvo)
        )
                .execute();
    }

    /* Trocou de listener para CAllBack
    public interface DadosCarregadosListener<T> {
        void quandoCarregados(T resultado);
    }
    */

    public interface DadosCarregadosCallBack<T> {
        void quandoSucesso(T resultado);

        void quandoFalha(String erro);
    }

}
