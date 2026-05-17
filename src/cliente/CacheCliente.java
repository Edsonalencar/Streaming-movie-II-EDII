package cliente;

import estruturas.ArvoreAVL;
import estruturas.ResultadoBusca;
import modelo.Filme;

public final class CacheCliente {
    private final ArvoreAVL arvore;

    public CacheCliente() {
        this.arvore = new ArvoreAVL(50);
    }

    public ResultadoBusca<Filme> buscar(int id) {
        return arvore.buscar(id);
    }

    public void inserir(Filme f) {
        arvore.inserir(f);
    }

    public void imprimirEstado(String titulo) {
        System.out.println(titulo);
        System.out.print(arvore.imprimirEmOrdem());
    }

    public int tamanho() {
        return arvore.tamanho();
    }
}
