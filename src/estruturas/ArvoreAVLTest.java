package estruturas;

import modelo.Filme;

public class ArvoreAVLTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testCinquentaFilmesTamanhoEBalanceado();
        testFilme51EvictaEMantemTamanho50();
        testBuscarHitComparacoesEUltimoAcesso();
        testBuscarMissRetornaNaoEncontrado();
        testEvictLRUNaoRemoveNoRecentementeAcessado();
        testImprimirEmOrdemContemTodosIds();
        testRotacaoLL();
        testRotacaoRR();
        testRotacaoLR();
        testRotacaoRL();
        testBuscarMissCaminhoCompleto();
        testCapacidadeNuncaExcedeCapacidade();

        System.out.println("\n=== ArvoreAVLTest: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }

    // Test 1: 50 distinct films → tamanho=50, root balance factor in {-1,0,1}
    private static void testCinquentaFilmesTamanhoEBalanceado() {
        ArvoreAVL avl = new ArvoreAVL(50);
        for (int i = 1; i <= 50; i++) avl.inserir(filme(i));
        assertEquals("50 filmes — tamanho = 50", 50, avl.tamanho());
        int fb = avl.fatorBalancoRaiz();
        assertTrue("50 filmes — fator balanco raiz em {-1,0,1}: " + fb,
                fb >= -1 && fb <= 1);
    }

    // Test 2: inserting film 51 at capacity evicts one node, net tamanho=50
    private static void testFilme51EvictaEMantemTamanho50() {
        ArvoreAVL avl = new ArvoreAVL(50);
        for (int i = 1; i <= 50; i++) avl.inserir(filme(i));
        assertEquals("pré-condição: tamanho = 50", 50, avl.tamanho());
        avl.inserir(filme(51));
        assertEquals("após filme 51: tamanho ainda = 50", 50, avl.tamanho());
    }

    // Test 3: buscar hit — comparacoes = depth+1, ultimoAcesso updated
    private static void testBuscarHitComparacoesEUltimoAcesso() {
        ArvoreAVL avl = new ArvoreAVL(50);
        // RR rotation: 10→20→30 yields {20, left=10, right=30}
        avl.inserir(filme(10)); // relogio=1
        avl.inserir(filme(20)); // relogio=2
        avl.inserir(filme(30)); // relogio=3
        // root=20 (depth 0), 10 and 30 at depth 1

        ResultadoBusca<Filme> r0 = avl.buscar(20);
        assertTrue("hit na raiz (depth=0) — encontrado", r0.encontrado());
        assertEquals("hit na raiz (depth=0) — comparacoes = 1", 1, r0.comparacoes());

        // buscar updates ultimoAcesso (relogio was 3, becomes 4 after hit on root)
        NoAVL no20 = encontrar(avl.raiz, 20);
        assertTrue("hit na raiz — ultimoAcesso atualizado (> 3)", no20 != null && no20.ultimoAcesso > 3);

        ResultadoBusca<Filme> r1 = avl.buscar(10);
        assertTrue("hit depth=1 — encontrado", r1.encontrado());
        assertEquals("hit depth=1 — comparacoes = 2", 2, r1.comparacoes());
    }

    // Test 4: buscar miss — found=false, comparacoes = search path length
    private static void testBuscarMissRetornaNaoEncontrado() {
        ArvoreAVL avl = new ArvoreAVL(50);
        avl.inserir(filme(10));
        avl.inserir(filme(20));
        avl.inserir(filme(30));
        // tree: {20, left=10, right=30}

        ResultadoBusca<Filme> miss = avl.buscar(5);
        assertTrue("miss — não encontrado", !miss.encontrado());
        // buscar(5): visit 20 → visit 10 → null: 2 comparacoes
        assertEquals("miss id=5 — comparacoes = 2", 2, miss.comparacoes());
    }

    // Test 5: evictLRU does NOT remove the most-recently-accessed node
    private static void testEvictLRUNaoRemoveNoRecentementeAcessado() {
        ArvoreAVL avl = new ArvoreAVL(50);
        avl.inserir(filme(10)); // ts=1
        avl.inserir(filme(20)); // ts=2
        avl.inserir(filme(30)); // ts=3
        // tree: {20, left=10, right=30}

        // Hit on 20 → gives it the highest timestamp
        avl.buscar(20); // ts of 20 = 4

        // evictLRU must remove node with ts=1 (node 10), not node 20
        avl.evictLRU();
        assertEquals("após evictLRU — tamanho = 2", 2, avl.tamanho());

        ResultadoBusca<Filme> r20 = avl.buscar(20);
        assertTrue("nó 20 (mais recente) sobrevive ao evictLRU", r20.encontrado());

        ResultadoBusca<Filme> r10 = avl.buscar(10);
        assertTrue("nó 10 (menos recente) foi evicted", !r10.encontrado());
    }

    // Test 6: imprimirEmOrdem contains all 50 IDs in sorted order
    private static void testImprimirEmOrdemContemTodosIds() {
        ArvoreAVL avl = new ArvoreAVL(50);
        for (int i = 1; i <= 50; i++) avl.inserir(filme(i));
        String saida = avl.imprimirEmOrdem();
        assertTrue("imprimirEmOrdem não vazio", !saida.isEmpty());
        for (int i = 1; i <= 50; i++) {
            assertTrue("id=" + i + " presente na saída", saida.contains("id=" + i));
        }
        // Verify sorted order by checking IDs appear in increasing order in the string
        int lastPos = -1;
        boolean emOrdem = true;
        for (int i = 1; i <= 50; i++) {
            int pos = saida.indexOf("id=" + i + " ");
            if (pos < 0) pos = saida.indexOf("id=" + i + "|"); // fallback
            if (pos < 0) {
                // search more broadly
                pos = saida.indexOf("=" + i + " ");
            }
            if (pos <= lastPos && lastPos >= 0) {
                emOrdem = false;
                break;
            }
            if (pos >= 0) lastPos = pos;
        }
        assertTrue("imprimirEmOrdem em ordem crescente de IDs", emOrdem);
    }

    // Test 7: LL rotation — insert 30, 20, 10 → root=20, left=10, right=30
    private static void testRotacaoLL() {
        ArvoreAVL avl = new ArvoreAVL(50);
        avl.inserir(filme(30));
        avl.inserir(filme(20));
        avl.inserir(filme(10)); // triggers LL rotation
        assertEquals("LL — raiz = 20", 20, avl.raizId());
        assertEquals("LL — esquerda = 10", 10, avl.esquerdaId());
        assertEquals("LL — direita = 30", 30, avl.direitaId());
    }

    // Test 8: RR rotation — insert 10, 20, 30 → root=20, left=10, right=30
    private static void testRotacaoRR() {
        ArvoreAVL avl = new ArvoreAVL(50);
        avl.inserir(filme(10));
        avl.inserir(filme(20));
        avl.inserir(filme(30)); // triggers RR rotation
        assertEquals("RR — raiz = 20", 20, avl.raizId());
        assertEquals("RR — esquerda = 10", 10, avl.esquerdaId());
        assertEquals("RR — direita = 30", 30, avl.direitaId());
    }

    // LR rotation: insert 30, 10, 20 → root=20, left=10, right=30
    private static void testRotacaoLR() {
        ArvoreAVL avl = new ArvoreAVL(50);
        avl.inserir(filme(30));
        avl.inserir(filme(10));
        avl.inserir(filme(20)); // triggers LR rotation
        assertEquals("LR — raiz = 20", 20, avl.raizId());
        assertEquals("LR — esquerda = 10", 10, avl.esquerdaId());
        assertEquals("LR — direita = 30", 30, avl.direitaId());
    }

    // RL rotation: insert 10, 30, 20 → root=20, left=10, right=30
    private static void testRotacaoRL() {
        ArvoreAVL avl = new ArvoreAVL(50);
        avl.inserir(filme(10));
        avl.inserir(filme(30));
        avl.inserir(filme(20)); // triggers RL rotation
        assertEquals("RL — raiz = 20", 20, avl.raizId());
        assertEquals("RL — esquerda = 10", 10, avl.esquerdaId());
        assertEquals("RL — direita = 30", 30, avl.direitaId());
    }

    // Additional: miss at max depth covers full path
    private static void testBuscarMissCaminhoCompleto() {
        ArvoreAVL avl = new ArvoreAVL(50);
        avl.inserir(filme(20));
        // single-node tree: buscar miss visits only root
        ResultadoBusca<Filme> miss = avl.buscar(999);
        assertTrue("miss em árvore de 1 nó — não encontrado", !miss.encontrado());
        assertEquals("miss em árvore de 1 nó — comparacoes = 1", 1, miss.comparacoes());
    }

    // Integration: multiple evictions, tamanho never exceeds capacidade
    private static void testCapacidadeNuncaExcedeCapacidade() {
        ArvoreAVL avl = new ArvoreAVL(50);
        for (int i = 1; i <= 70; i++) {
            avl.inserir(filme(i));
            assertTrue("tamanho ≤ 50 após inserir id=" + i, avl.tamanho() <= 50);
        }
        assertEquals("tamanho final = 50", 50, avl.tamanho());
    }

    // --- helpers ---

    private static NoAVL encontrar(NoAVL no, int id) {
        if (no == null) return null;
        if (no.filme.id() == id) return no;
        if (id < no.filme.id()) return encontrar(no.esquerda, id);
        return encontrar(no.direita, id);
    }

    private static Filme filme(int id) {
        return new Filme(id, "Filme " + id, "Sinopse", 2020, "Acao");
    }

    private static void assertEquals(String label, int expected, int actual) {
        if (expected == actual) {
            System.out.println("  PASS: " + label + " [" + actual + "]");
            passed++;
        } else {
            System.out.println("  FAIL: " + label + " — expected " + expected + " got " + actual);
            failed++;
        }
    }

    private static void assertTrue(String label, boolean condition) {
        if (condition) {
            System.out.println("  PASS: " + label);
            passed++;
        } else {
            System.out.println("  FAIL: " + label);
            failed++;
        }
    }
}
