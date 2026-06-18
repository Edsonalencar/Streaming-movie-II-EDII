package cliente;

import estruturas.cacheLRU.CacheLRU;
import estruturas.splay.ArvoreSplay;
import estruturas.ResultadoBusca;
import estruturas.ResultadoBuscaNome;
import modelo.Filme;

import java.util.List;

// Cliente da plataforma. Compõe o cache local (CacheLRU) com uma árvore splay de preferências,
// onde o conteúdo consumido mais recentemente fica na raiz.
public final class CacheCliente {

    private final String nome;
    private final CacheLRU cache;
    private final ArvoreSplay preferencias = new ArvoreSplay();

    public CacheCliente(String nome, int capacidadeCache) {
        this.nome = nome;
        this.cache = new CacheLRU(capacidadeCache);
    }

    public String nome() {
        return nome;
    }

    // Busca no cache local (hit promove o item a MRU).
    public ResultadoBusca<Filme> buscar(int id) {
        return cache.buscar(id);
    }

    // Busca no cache local por parte do nome, sem tocar o servidor.
    public ResultadoBuscaNome buscarPorNomeLocal(String termo) {
        return cache.buscarPorNome(termo);
    }

    // Insere/atualiza o filme no cache (não registra preferência).
    public void inserir(Filme f) {
        cache.inserir(f);
    }

    // Registra o consumo de um filme: atualiza o cache e a árvore de preferências.
    public void registrarAcesso(Filme f) {
        cache.inserir(f);
        preferencias.inserir(f);
    }


    public List<Filme> dezMaisRecentes() {
        return cache.maisRecentes(10);
    }

    public List<Integer> removidosDoCache() {
        return cache.removidos();
    }

    public Filme preferenciaAtual() {
        return preferencias.raiz();
    }

    public List<Filme> cincoMaisAcessados() {
        return preferencias.maisAcessados(5);
    }

    public List<String> cincoMaisAcessadosDescritos() {
        return preferencias.maisAcessadosDescritos(5);
    }

    public int tamanho() {
        return cache.tamanho();
    }

    public void imprimirEstadoCache(String titulo) {
        cache.imprimirEstado(titulo);
    }
}
