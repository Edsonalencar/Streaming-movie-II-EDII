package estruturas.listaLigada;

import estruturas.ResultadoBusca;
import estruturas.ResultadoBuscaNome;
import modelo.Filme;

import java.util.ArrayList;
import java.util.List;

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

    // Busca por parte do nome: varre a lista inteira acumulando os títulos que contêm o trecho.
    public ResultadoBuscaNome buscarPorNome(String termo) {
        String alvo = termo.toLowerCase();
        int comparacoes = 0;
        List<Filme> achados = new ArrayList<>();
        NoLista atual = cabeca;
        while (atual != null) {
            comparacoes++;
            if (atual.filme.nome().toLowerCase().contains(alvo)) {
                achados.add(atual.filme);
            }
            atual = atual.proximo;
        }
        return new ResultadoBuscaNome(achados, comparacoes);
    }

    public int tamanho() {
        return tamanho;
    }

    public NoLista getCauda() {
        return cauda;
    }
}
