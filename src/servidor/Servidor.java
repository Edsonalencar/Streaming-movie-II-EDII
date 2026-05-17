package servidor;

import estruturas.ListaLigada;
import estruturas.NoLista;
import estruturas.ResultadoBusca;
import estruturas.TabelaHash;
import modelo.Filme;

public final class Servidor {
    private final ListaLigada catalogo = new ListaLigada();
    private final TabelaHash indice = new TabelaHash();

    public void inserir(Filme f) {
        catalogo.inserir(f);
        indice.inserir(f.id(), catalogo.getCauda());
    }

    public ResultadoBusca<Filme> buscarSemIndice(int id) {
        return catalogo.buscar(id);
    }

    public ResultadoBusca<Filme> buscarComIndice(int id) {
        ResultadoBusca<NoLista> resultado = indice.buscar(id);
        if (!resultado.encontrado()) {
            return ResultadoBusca.vazio(resultado.comparacoes());
        }
        return new ResultadoBusca<>(resultado.valor().filme(), resultado.comparacoes());
    }

    public int tamanho() {
        return catalogo.tamanho();
    }
}
