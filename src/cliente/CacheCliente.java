package cliente;

import estruturas.ArvoreSplay;
import estruturas.ResultadoBusca;
import modelo.Filme;

import java.util.List;

/**
 * Representa um cliente (frontend/app) da plataforma de streaming.
 *
 * <p>Compõe duas estruturas:</p>
 * <ul>
 *   <li>{@link CacheLRU} — cache local (tabela hash + lista autoajustável) para
 *       evitar requisições à rede;</li>
 *   <li>{@link ArvoreSplay} — registro de preferências, onde o conteúdo mais
 *       recentemente consumido fica na raiz, apoiando recomendações.</li>
 * </ul>
 */
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

    /** Busca no cache local (hit promove o item a MRU). */
    public ResultadoBusca<Filme> buscar(int id) {
        return cache.buscar(id);
    }

    /** Insere/atualiza o filme no cache local (não registra preferência). */
    public void inserir(Filme f) {
        cache.inserir(f);
    }

    /** Registra o consumo de um filme: atualiza cache e árvore de preferências. */
    public void registrarAcesso(Filme f) {
        cache.inserir(f);
        preferencias.inserir(f);
    }

    // ---- análise -------------------------------------------------------------

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
