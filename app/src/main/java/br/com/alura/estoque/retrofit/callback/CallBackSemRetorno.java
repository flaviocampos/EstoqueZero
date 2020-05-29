package br.com.alura.estoque.retrofit.callback;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallBackSemRetorno implements Callback<Void> {

    private final RespostaCallback callback;

    public CallBackSemRetorno(RespostaCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onResponse(Call<Void> call, Response<Void> response) {
        if (response.isSuccessful()) {
            callback.quandoSucesso();
        } else {
            callback.quandoFalha("Resposta nāo sucedida");
        }
    }

    @Override
    public void onFailure(Call<Void> call, Throwable t) {
        callback.quandoFalha("Resposta nāo sucedida");
    }

    public interface RespostaCallback {
        void quandoSucesso();

        void quandoFalha(String erro);
    }
}
