package estruturas.splay;

import estruturas.ResultadoBusca;
import modelo.Filme;

import java.util.ArrayList;
import java.util.List;

public final class ArvoreSplay {

    private NoSplay raiz;
    private int comparacoesUltimaOperacao;

    // Insere o filme (ou conta mais um acesso, se já existir) e leva o nó à raiz.
    public void inserir(Filme f) {
        if (raiz == null) {
            raiz = new NoSplay(f);
            return;
        }
        raiz = splay(raiz, f.id());
        if (raiz.filme.id() == f.id()) {
            raiz.filme = f;
            raiz.contadorAcessos++;
            return;
        }
        NoSplay novo = new NoSplay(f);
        if (f.id() < raiz.filme.id()) {
            novo.direita = raiz;
            novo.esquerda = raiz.esquerda;
            raiz.esquerda = null;
        } else {
            novo.esquerda = raiz;
            novo.direita = raiz.direita;
            raiz.direita = null;
        }
        raiz = novo;
    }

    // Acessa um filme pelo id: traz à raiz, conta o acesso e devolve as comparações da busca.
    public ResultadoBusca<Filme> acessar(int id) {
        if (raiz == null) return ResultadoBusca.vazio(0);
        raiz = splay(raiz, id);
        int comparacoes = comparacoesUltimaOperacao;
        if (raiz.filme.id() == id) {
            raiz.contadorAcessos++;
            return new ResultadoBusca<>(raiz.filme, comparacoes);
        }
        return ResultadoBusca.vazio(comparacoes);
    }

    // Splay top-down: traz o nó da chave id (ou o último visitado, se não existir) para a raiz.
    private NoSplay splay(NoSplay raizAtual, int id) {
        comparacoesUltimaOperacao = 0;
        NoSplay cabecalho = new NoSplay(null); // árvores temporárias esquerda/direita
        NoSplay menores = cabecalho;
        NoSplay maiores = cabecalho;
        NoSplay t = raizAtual;

        while (true) {
            comparacoesUltimaOperacao++;
            if (id < t.filme.id()) {
                if (t.esquerda == null) break;
                comparacoesUltimaOperacao++;
                if (id < t.esquerda.filme.id()) {
                    t = rotacaoDireita(t);     // zig-zig
                    if (t.esquerda == null) break;
                }
                maiores.esquerda = t;          // liga à direita (chaves maiores)
                maiores = t;
                t = t.esquerda;
            } else if (id > t.filme.id()) {
                if (t.direita == null) break;
                comparacoesUltimaOperacao++;
                if (id > t.direita.filme.id()) {
                    t = rotacaoEsquerda(t);    // zig-zig
                    if (t.direita == null) break;
                }
                menores.direita = t;           // liga à esquerda (chaves menores)
                menores = t;
                t = t.direita;
            } else {
                break;
            }
        }
        // remonta a árvore com t na raiz
        menores.direita = t.esquerda;
        maiores.esquerda = t.direita;
        t.esquerda = cabecalho.direita;
        t.direita = cabecalho.esquerda;
        return t;
    }

    private NoSplay rotacaoDireita(NoSplay no) {
        NoSplay x = no.esquerda;
        no.esquerda = x.direita;
        x.direita = no;
        return x;
    }

    private NoSplay rotacaoEsquerda(NoSplay no) {
        NoSplay x = no.direita;
        no.direita = x.esquerda;
        x.esquerda = no;
        return x;
    }

    // Filme atualmente na raiz (preferência/popularidade mais relevante); pode ser null.
    public Filme raiz() {
        return raiz == null ? null : raiz.filme;
    }

    // Os n filmes mais próximos da raiz, percorrendo a árvore por nível.
    public List<Filme> maisProximosDaRaiz(int n) {
        List<Filme> out = new ArrayList<>();
        if (raiz == null) return out;
        List<NoSplay> nivel = new ArrayList<>();
        nivel.add(raiz);
        while (!nivel.isEmpty() && out.size() < n) {
            List<NoSplay> proximo = new ArrayList<>();
            for (NoSplay no : nivel) {
                if (out.size() < n) out.add(no.filme);
                if (no.esquerda != null) proximo.add(no.esquerda);
                if (no.direita != null) proximo.add(no.direita);
            }
            nivel = proximo;
        }
        return out;
    }

    // Os n filmes com maior contador de acessos (frequência).
    public List<Filme> maisAcessados(int n) {
        List<Filme> out = new ArrayList<>();
        for (NoSplay no : ordenarPorAcessos(n)) out.add(no.filme);
        return out;
    }

    // Como maisAcessados, mas formatado como "id=N (Kx)" para exibição.
    public List<String> maisAcessadosDescritos(int n) {
        List<String> out = new ArrayList<>();
        for (NoSplay no : ordenarPorAcessos(n)) {
            out.add("id=" + no.filme.id() + " (" + no.contadorAcessos + "x)");
        }
        return out;
    }

    private List<NoSplay> ordenarPorAcessos(int n) {
        List<NoSplay> todos = new ArrayList<>();
        coletar(raiz, todos);
        todos.sort((a, b) -> Integer.compare(b.contadorAcessos, a.contadorAcessos));
        return todos.subList(0, Math.min(n, todos.size()));
    }

    public int acessosDaRaiz() {
        return raiz == null ? 0 : raiz.contadorAcessos;
    }

    private void coletar(NoSplay no, List<NoSplay> acc) {
        if (no == null) return;
        coletar(no.esquerda, acc);
        acc.add(no);
        coletar(no.direita, acc);
    }

    public boolean vazia() {
        return raiz == null;
    }
}
