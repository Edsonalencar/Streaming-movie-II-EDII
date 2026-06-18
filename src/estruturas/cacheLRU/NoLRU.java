package estruturas.cacheLRU;

import modelo.Filme;

// Nó da lista duplamente encadeada do cache LRU.
class NoLRU {
    Filme filme;
    NoLRU anterior;
    NoLRU proximo;
    NoLRU encadeamentoHash; // encadeamento separado para o balde da tabela hash

    NoLRU(Filme filme) {
        this.filme = filme;
    }
}
