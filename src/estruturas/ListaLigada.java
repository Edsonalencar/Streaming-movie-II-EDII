package estruturas;

import modelo.Filme;

public class ListaLigada {
    NoLista cabeca;
    private NoLista cauda;
    private int tamanho;

    public void inserir(Filme f) {
        NoLista novo = new NoLista(f);
        if (cabeca == null) {
            cabeca = novo;
            cauda = novo;
        } else {
            cauda.proximo = novo;
            cauda = novo;
        }
        tamanho++;
    }

    public ResultadoBusca<Filme> buscar(int id) {
        int comparacoes = 0;
        NoLista atual = cabeca;
        while (atual != null) {
            comparacoes++;
            if (atual.filme.id() == id) {
                return new ResultadoBusca<>(atual.filme, comparacoes);
            }
            atual = atual.proximo;
        }
        return ResultadoBusca.vazio(comparacoes);
    }

    public int tamanho() {
        return tamanho;
    }

    // Returns the most-recently-inserted node so Servidor can pass it to TabelaHash.
    public NoLista getCauda() {
        return cauda;
    }
}
