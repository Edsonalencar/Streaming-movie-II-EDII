package servidor;

import estruturas.splay.ArvoreSplay;
import estruturas.listaLigada.ListaLigada;
import estruturas.listaLigada.NoLista;
import estruturas.ResultadoBusca;
import estruturas.ResultadoBuscaNome;
import estruturas.tabelaHash.TabelaHash;
import modelo.Filme;

import java.util.List;

// Servidor da plataforma. Mantém o catálogo numa lista ligada, indexada por uma tabela hash
// (id -> nó da lista) para acesso O(1). Toda busca bem-sucedida alimenta uma árvore splay de
// popularidade global.
public final class Servidor {
    private final ListaLigada catalogo = new ListaLigada();
    private final TabelaHash indice = new TabelaHash();
    private final ArvoreSplay popularidade = new ArvoreSplay();

    public void inserir(Filme f) {
        catalogo.inserir(f);
        indice.inserir(f.id(), catalogo.getCauda());
    }

    // Busca sequencial na lista ligada (sem usar o índice).
    public ResultadoBusca<Filme> buscarSemIndice(int id) {
        ResultadoBusca<Filme> r = catalogo.buscar(id);
        if (r.encontrado()) popularidade.inserir(r.valor());
        return r;
    }

    // Busca via tabela hash, apontando direto para o nó na lista.
    public ResultadoBusca<Filme> buscarComIndice(int id) {
        ResultadoBusca<NoLista> resultado = indice.buscar(id);
        if (!resultado.encontrado()) {
            return ResultadoBusca.vazio(resultado.comparacoes());
        }
        Filme f = resultado.valor().filme();
        popularidade.inserir(f);
        return new ResultadoBusca<>(f, resultado.comparacoes());
    }

    // Busca por parte do nome. Não há índice por título, então a varredura é sequencial na lista.
    public ResultadoBuscaNome buscarPorNome(String termo) {
        ResultadoBuscaNome r = catalogo.buscarPorNome(termo);
        for (Filme f : r.resultados()) popularidade.inserir(f);
        return r;
    }

    public int tamanho() {
        return catalogo.tamanho();
    }

    // análise de popularidade global

    // Os n filmes mais próximos da raiz da splay de popularidade.
    public List<Filme> maisProximosDaRaiz(int n) {
        return popularidade.maisProximosDaRaiz(n);
    }

    // Filme atualmente na raiz (conteúdo mais popular no momento).
    public Filme filmeMaisPopular() {
        return popularidade.raiz();
    }
}
