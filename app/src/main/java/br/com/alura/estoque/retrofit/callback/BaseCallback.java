package br.com.alura.estoque.retrofit.callback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BaseCallback<T> implements Callback<T> {


    private final RespostaCallback<T> callback;

    public BaseCallback(RespostaCallback<T> callback) {
        this.callback = callback;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            final T resultado = response.body();
            if (response != null) {
                callback.quandoSucesso(resultado);
            }
        } else {
            callback.quandoFalha("Resposta nāo sucedida");
        }

    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        callback.quandoFalha("Resposta nāo sucedida");
    }

    public interface RespostaCallback<T> {
        void quandoSucesso(T resultado);

        void quandoFalha(String erro);
    }
}
