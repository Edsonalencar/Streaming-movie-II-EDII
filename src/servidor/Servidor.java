package servidor;

import estruturas.ArvoreSplay;
import estruturas.ListaLigada;
import estruturas.NoLista;
import estruturas.ResultadoBusca;
import estruturas.TabelaHash;
import modelo.Filme;

import java.util.List;

/**
 * Servidor (backend/database) da plataforma de streaming.
 *
 * <p>Mantém o catálogo mestre numa {@link ListaLigada} (persistência simulada),
 * indexada por uma {@link TabelaHash} (id → nó da lista) para acesso O(1).
 * Toda busca bem-sucedida alimenta uma {@link ArvoreSplay} de popularidade
 * global: os filmes mais acessados pela base inteira migram para perto da
 * raiz.</p>
 */
public final class Servidor {
    private final ListaLigada catalogo = new ListaLigada();
    private final TabelaHash indice = new TabelaHash();
    private final ArvoreSplay popularidade = new ArvoreSplay();

    public void inserir(Filme f) {
        catalogo.inserir(f);
        indice.inserir(f.id(), catalogo.getCauda());
    }

    /** Busca sequencial na lista ligada (sem usar o índice). */
    public ResultadoBusca<Filme> buscarSemIndice(int id) {
        ResultadoBusca<Filme> r = catalogo.buscar(id);
        if (r.encontrado()) popularidade.inserir(r.valor());
        return r;
    }

    /** Busca via tabela hash (índice), apontando direto para o nó na lista. */
    public ResultadoBusca<Filme> buscarComIndice(int id) {
        ResultadoBusca<NoLista> resultado = indice.buscar(id);
        if (!resultado.encontrado()) {
            return ResultadoBusca.vazio(resultado.comparacoes());
        }
        Filme f = resultado.valor().filme();
        popularidade.inserir(f);
        return new ResultadoBusca<>(f, resultado.comparacoes());
    }

    public int tamanho() {
        return catalogo.tamanho();
    }

    // ---- análise de popularidade global --------------------------------------

    /** Os n filmes mais próximos da raiz da árvore splay de popularidade. */
    public List<Filme> maisProximosDaRaiz(int n) {
        return popularidade.maisProximosDaRaiz(n);
    }

    /** Filme atualmente na raiz (conteúdo mais popular no momento). */
    public Filme filmeMaisPopular() {
        return popularidade.raiz();
    }
}
