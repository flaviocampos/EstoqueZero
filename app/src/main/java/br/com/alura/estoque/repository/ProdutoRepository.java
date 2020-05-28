package br.com.alura.estoque.repository;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
import br.com.alura.estoque.retrofit.ProdutoService;
import br.com.alura.estoque.retrofit.callback.BaseCallback;
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

        call.enqueue(new BaseCallback<>(new BaseCallback.RespostaCallback<List<Produto>>() {
            @Override
            public void quandoSucesso(List<Produto> produtosNovos) {
                atualizaInterno(produtosNovos, callBack);
            }

            @Override
            public void quandoFalha(String erro) {
                callBack.quandoFalha(erro);
            }
        }));
    }

    private void atualizaInterno(List<Produto> produtosNovos, DadosCarregadosCallBack<List<Produto>> callBack) {
        new BaseAsyncTask<>(() -> {
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
        call.enqueue(new BaseCallback<>(new BaseCallback.RespostaCallback<Produto>() {
            @Override
            public void quandoSucesso(Produto produtoSalvo) {
                salvaInterno(produtoSalvo, callBack);
            }

            @Override
            public void quandoFalha(String erro) {
                callBack.quandoFalha(erro);
            }
        }));
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

    public void edita(Produto produto, DadosCarregadosCallBack<Produto> callBack) {
        editaNaAPI(produto, callBack);
    }

    private void editaNaAPI(Produto produto, DadosCarregadosCallBack<Produto> callBack) {
        Call<Produto> call = service.edita(produto.getId(), produto);
        call.enqueue(new BaseCallback<>(new BaseCallback.RespostaCallback<Produto>() {
            @Override
            public void quandoSucesso(Produto resultado) {
                editaInterno(produto, callBack);
            }

            @Override
            public void quandoFalha(String erro) {
                callBack.quandoFalha(erro);
            }
        }));
    }

    private void editaInterno(Produto produto, DadosCarregadosCallBack<Produto> callBack) {
        new BaseAsyncTask<>(() -> {
            dao.atualiza(produto);
            return produto;
        }, callBack::quandoSucesso)
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
