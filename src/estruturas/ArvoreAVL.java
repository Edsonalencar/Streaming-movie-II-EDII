package estruturas;

import modelo.Filme;

public final class ArvoreAVL {
    NoAVL raiz;
    private int tamanho;
    private long relogio;
    private final int capacidade;

    public ArvoreAVL(int capacidade) {
        this.capacidade = capacidade;
    }

    public ResultadoBusca<Filme> buscar(int id) {
        return buscarRecursivo(raiz, id, 0);
    }

    private ResultadoBusca<Filme> buscarRecursivo(NoAVL no, int id, int comparacoes) {
        if (no == null) return ResultadoBusca.vazio(comparacoes);
        comparacoes++;
        if (id == no.filme.id()) {
            no.ultimoAcesso = ++relogio;
            return new ResultadoBusca<>(no.filme, comparacoes);
        }
        if (id < no.filme.id()) return buscarRecursivo(no.esquerda, id, comparacoes);
        return buscarRecursivo(no.direita, id, comparacoes);
    }

    public void inserir(Filme f) {
        if (tamanho == capacidade) evictLRU();
        raiz = inserirRecursivo(raiz, f);
    }

    private NoAVL inserirRecursivo(NoAVL no, Filme f) {
        if (no == null) {
            tamanho++;
            return new NoAVL(f, ++relogio);
        }
        if (f.id() < no.filme.id()) {
            no.esquerda = inserirRecursivo(no.esquerda, f);
        } else if (f.id() > no.filme.id()) {
            no.direita = inserirRecursivo(no.direita, f);
        } else {
            no.filme = f;
            no.ultimoAcesso = ++relogio;
            return no;
        }
        atualizarAltura(no);
        return balancear(no);
    }

    void evictLRU() {
        if (raiz == null) return;
        NoAVL minNo = encontrarMinUltimoAcesso(raiz, null);
        if (minNo != null) {
            raiz = remover(raiz, minNo.filme.id());
            tamanho--;
        }
    }

    private NoAVL encontrarMinUltimoAcesso(NoAVL no, NoAVL minAtual) {
        if (no == null) return minAtual;
        minAtual = encontrarMinUltimoAcesso(no.esquerda, minAtual);
        if (minAtual == null || no.ultimoAcesso < minAtual.ultimoAcesso) {
            minAtual = no;
        }
        return encontrarMinUltimoAcesso(no.direita, minAtual);
    }

    private NoAVL remover(NoAVL no, int id) {
        if (no == null) return null;
        if (id < no.filme.id()) {
            no.esquerda = remover(no.esquerda, id);
        } else if (id > no.filme.id()) {
            no.direita = remover(no.direita, id);
        } else {
            if (no.esquerda == null) return no.direita;
            if (no.direita == null) return no.esquerda;
            NoAVL suc = minNode(no.direita);
            no.filme = suc.filme;
            no.ultimoAcesso = suc.ultimoAcesso;
            no.direita = remover(no.direita, suc.filme.id());
        }
        atualizarAltura(no);
        return balancear(no);
    }

    private NoAVL minNode(NoAVL no) {
        while (no.esquerda != null) no = no.esquerda;
        return no;
    }

    private int altura(NoAVL no) {
        return no == null ? 0 : no.altura;
    }

    private void atualizarAltura(NoAVL no) {
        no.altura = 1 + Math.max(altura(no.esquerda), altura(no.direita));
    }

    private int fatorBalanco(NoAVL no) {
        return no == null ? 0 : altura(no.esquerda) - altura(no.direita);
    }

    private NoAVL balancear(NoAVL no) {
        int fb = fatorBalanco(no);
        if (fb > 1 && fatorBalanco(no.esquerda) >= 0)
            return rotacaoDireita(no);
        if (fb > 1 && fatorBalanco(no.esquerda) < 0) {
            no.esquerda = rotacaoEsquerda(no.esquerda);
            return rotacaoDireita(no);
        }
        if (fb < -1 && fatorBalanco(no.direita) <= 0)
            return rotacaoEsquerda(no);
        if (fb < -1 && fatorBalanco(no.direita) > 0) {
            no.direita = rotacaoDireita(no.direita);
            return rotacaoEsquerda(no);
        }
        return no;
    }

    private NoAVL rotacaoDireita(NoAVL y) {
        NoAVL x = y.esquerda;
        NoAVL T2 = x.direita;
        x.direita = y;
        y.esquerda = T2;
        atualizarAltura(y);
        atualizarAltura(x);
        return x;
    }

    private NoAVL rotacaoEsquerda(NoAVL x) {
        NoAVL y = x.direita;
        NoAVL T2 = y.esquerda;
        y.esquerda = x;
        x.direita = T2;
        atualizarAltura(x);
        atualizarAltura(y);
        return y;
    }

    public String imprimirEmOrdem() {
        StringBuilder sb = new StringBuilder();
        imprimirRecursivo(raiz, sb);
        return sb.toString();
    }

    private void imprimirRecursivo(NoAVL no, StringBuilder sb) {
        if (no == null) return;
        imprimirRecursivo(no.esquerda, sb);
        sb.append(String.format("  [id=%-6d | bf=%-3d | ts=%-6d]%n",
                no.filme.id(), fatorBalanco(no), no.ultimoAcesso));
        imprimirRecursivo(no.direita, sb);
    }

    public int tamanho() {
        return tamanho;
    }

    // Package-private helpers for tests
    int fatorBalancoRaiz() { return fatorBalanco(raiz); }
    int raizId()    { return raiz == null ? -1 : raiz.filme.id(); }
    int esquerdaId(){ return (raiz == null || raiz.esquerda == null) ? -1 : raiz.esquerda.filme.id(); }
    int direitaId() { return (raiz == null || raiz.direita == null) ? -1 : raiz.direita.filme.id(); }
}
