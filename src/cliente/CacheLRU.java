package cliente;

import estruturas.ResultadoBusca;
import modelo.Filme;

import java.util.ArrayList;
import java.util.List;

/**
 * Cache do cliente: tabela hash + lista autoajustável (política LRU).
 *
 * <p>A tabela hash dá busca em tempo médio O(1) (conta as comparações feitas no
 * balde, seguindo a convenção ADR-004). A lista duplamente encadeada mantém a
 * ordem de uso: a cabeça é o item mais recentemente usado (MRU) e a cauda o
 * menos recentemente usado (LRU). A cada acesso ou inserção o nó vai para a
 * frente; quando a capacidade é excedida, a cauda é removida (evicção LRU).</p>
 */
public final class CacheLRU {

    private static final int CAPACIDADE_HASH = 211; // primo > 50 para espalhar bem

    private final int capacidade;
    private final NoLRU[] baldes = new NoLRU[CAPACIDADE_HASH];
    private NoLRU cabeca; // MRU
    private NoLRU cauda;  // LRU
    private int tamanho;
    private final List<Integer> removidos = new ArrayList<>();

    public CacheLRU(int capacidade) {
        this.capacidade = capacidade;
    }

    /** Busca um filme pelo id. Em hit, promove o nó a MRU. */
    public ResultadoBusca<Filme> buscar(int id) {
        int comparacoes = 0;
        NoLRU no = baldes[indiceDe(id)];
        while (no != null) {
            comparacoes++;
            if (no.filme.id() == id) {
                moverParaFrente(no);
                return new ResultadoBusca<>(no.filme, comparacoes);
            }
            no = no.encadeamentoHash;
        }
        return ResultadoBusca.vazio(comparacoes);
    }

    /** Insere/atualiza um filme; promove a MRU e aplica evicção LRU se necessário. */
    public void inserir(Filme f) {
        NoLRU existente = encontrarNo(f.id());
        if (existente != null) {
            existente.filme = f;
            moverParaFrente(existente);
            return;
        }
        NoLRU novo = new NoLRU(f);
        inserirNoHash(novo);
        ligarNaFrente(novo);
        tamanho++;
        if (tamanho > capacidade) evictLRU();
    }

    private void evictLRU() {
        NoLRU vitima = cauda;
        if (vitima == null) return;
        removidos.add(vitima.filme.id());
        desligarDaLista(vitima);
        removerDoHash(vitima);
        tamanho--;
    }

    // ---- lista autoajustável -------------------------------------------------

    private void moverParaFrente(NoLRU no) {
        if (no == cabeca) return;
        desligarDaLista(no);
        ligarNaFrente(no);
    }

    private void ligarNaFrente(NoLRU no) {
        no.anterior = null;
        no.proximo = cabeca;
        if (cabeca != null) cabeca.anterior = no;
        cabeca = no;
        if (cauda == null) cauda = no;
    }

    private void desligarDaLista(NoLRU no) {
        if (no.anterior != null) no.anterior.proximo = no.proximo;
        else cabeca = no.proximo;
        if (no.proximo != null) no.proximo.anterior = no.anterior;
        else cauda = no.anterior;
        no.anterior = null;
        no.proximo = null;
    }

    // ---- tabela hash ---------------------------------------------------------

    private int indiceDe(int id) {
        return Math.floorMod(id, CAPACIDADE_HASH);
    }

    private NoLRU encontrarNo(int id) {
        NoLRU no = baldes[indiceDe(id)];
        while (no != null) {
            if (no.filme.id() == id) return no;
            no = no.encadeamentoHash;
        }
        return null;
    }

    private void inserirNoHash(NoLRU no) {
        int i = indiceDe(no.filme.id());
        no.encadeamentoHash = baldes[i];
        baldes[i] = no;
    }

    private void removerDoHash(NoLRU alvo) {
        int i = indiceDe(alvo.filme.id());
        NoLRU atual = baldes[i];
        NoLRU anterior = null;
        while (atual != null) {
            if (atual == alvo) {
                if (anterior == null) baldes[i] = atual.encadeamentoHash;
                else anterior.encadeamentoHash = atual.encadeamentoHash;
                atual.encadeamentoHash = null;
                return;
            }
            anterior = atual;
            atual = atual.encadeamentoHash;
        }
    }

    // ---- análise -------------------------------------------------------------

    /** Os n filmes mais recentemente usados, do mais recente ao menos. */
    public List<Filme> maisRecentes(int n) {
        List<Filme> out = new ArrayList<>();
        NoLRU no = cabeca;
        while (no != null && out.size() < n) {
            out.add(no.filme);
            no = no.proximo;
        }
        return out;
    }

    /** Ids removidos por evicção LRU, em ordem de remoção. */
    public List<Integer> removidos() {
        return removidos;
    }

    public int tamanho() {
        return tamanho;
    }

    public void imprimirEstado(String titulo) {
        System.out.println(titulo);
        int pos = 1;
        for (Filme f : maisRecentes(tamanho)) {
            System.out.printf("  #%-2d (MRU→LRU) id=%-5d \"%s\"%n", pos++, f.id(), f.nome());
        }
    }
}
