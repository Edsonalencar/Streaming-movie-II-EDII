package estruturas;

public class TabelaHash {
    private static final int CAPACIDADE = 2003;
    EntradaHash[] baldes;

    public TabelaHash() {
        baldes = new EntradaHash[CAPACIDADE];
    }

    public void inserir(int id, NoLista refNo) {
        int indice = Math.floorMod(id, CAPACIDADE);
        EntradaHash nova = new EntradaHash(id, refNo);
        nova.proximo = baldes[indice];
        baldes[indice] = nova;
    }

    public ResultadoBusca<NoLista> buscar(int id) {
        int indice = Math.floorMod(id, CAPACIDADE);
        int comparacoes = 1; // acesso ao balde conta como 1 comparação (convenção ADR-004)
        EntradaHash entrada = baldes[indice];
        while (entrada != null) {
            comparacoes++;
            if (entrada.id == id) {
                return new ResultadoBusca<>(entrada.refNo, comparacoes);
            }
            entrada = entrada.proximo;
        }
        return ResultadoBusca.vazio(comparacoes);
    }
}
