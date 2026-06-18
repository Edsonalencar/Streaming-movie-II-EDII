package estruturas.listaLigada;

import modelo.Filme;

public class NoLista {
    Filme filme;
    NoLista proximo;

    NoLista(Filme filme) {
        this.filme = filme;
    }

    public Filme filme() {
        return filme;
    }
}
