package estruturas;

import modelo.Filme;

import java.util.ArrayList;
import java.util.List;

/**
 * Árvore splay chaveada por id de filme.
 *
 * <p>Usada em duas frentes na simulação:</p>
 * <ul>
 *   <li><b>Cliente</b> — registra o histórico de consumo do usuário; o item mais
 *       recentemente acessado fica na raiz (preferência atual).</li>
 *   <li><b>Servidor</b> — registra a popularidade global; os conteúdos mais
 *       acessados pela base inteira migram para perto da raiz.</li>
 * </ul>
 *
 * <p>A cada acesso o nó é levado à raiz pela operação de <i>splay</i>
 * (zig / zig-zig / zig-zag, implementação top-down de Sleator–Tarjan), e um
 * contador de acessos é incrementado para apoiar as análises de frequência.
 * As comparações de busca são contadas seguindo a convenção ADR-004.</p>
 */
public final class ArvoreSplay {

    private NoSplay raiz;
    private int comparacoesUltimaOperacao;

    /** Insere um filme (ou registra novo acesso, se já existir) e faz splay até a raiz. */
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

    /** Acessa um filme pelo id: traz à raiz, conta acesso e devolve as comparações da busca. */
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

    /**
     * Splay top-down: reorganiza a árvore trazendo o nó com a chave {@code id}
     * (ou o último nó visitado, em caso de ausência) para a raiz.
     */
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

    // ---- análise -------------------------------------------------------------

    /** Filme atualmente na raiz (preferência/popularidade mais relevante). Pode ser null. */
    public Filme raiz() {
        return raiz == null ? null : raiz.filme;
    }

    /** Os n filmes mais próximos da raiz, em ordem de nível (BFS). */
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

    /** Os n filmes com maior contador de acessos (popularidade por frequência). */
    public List<Filme> maisAcessados(int n) {
        List<Filme> out = new ArrayList<>();
        for (NoSplay no : ordenarPorAcessos(n)) out.add(no.filme);
        return out;
    }

    /** Como {@link #maisAcessados}, mas descrito como "id=N (Kx)" para exibição. */
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

    /** Acessos acumulados do filme na raiz (0 se vazia). */
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
