package cliente;

import modelo.Filme;

/**
 * Nó da lista autoajustável (duplamente encadeada) do cache LRU.
 * A cabeça da lista guarda o item mais recentemente usado (MRU) e a cauda o
 * menos recentemente usado (LRU).
 */
class NoLRU {
    Filme filme;
    NoLRU anterior;
    NoLRU proximo;
    NoLRU encadeamentoHash; // encadeamento separado para o balde da tabela hash

    NoLRU(Filme filme) {
        this.filme = filme;
    }
}
