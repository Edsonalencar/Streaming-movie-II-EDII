package estruturas.tabelaHash;

import estruturas.listaLigada.NoLista;

class EntradaHash {
    int id;
    NoLista refNo;
    EntradaHash proximo;

    EntradaHash(int id, NoLista refNo) {
        this.id = id;
        this.refNo = refNo;
    }
}
