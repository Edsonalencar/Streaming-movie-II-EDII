package cliente;

import estruturas.ResultadoBusca;
import estruturas.ResultadoBuscaNome;
import modelo.Filme;

import java.util.List;

public class CacheLRUTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testBuscarMissCacheVazio();
        testHitContaComparacoes();
        testCapacidadeNuncaExcede();
        testEvictaMenosRecentementeUsado();
        testHitPromoveAFrente();
        testRegistraRemovidos();
        testMaisRecentesOrdem();
        testBuscarPorNomeNoCache();
        testBuscarPorNomeNaoPromoveMRU();

        System.out.println("\n=== CacheLRUTest: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }

    private static void testBuscarMissCacheVazio() {
        CacheLRU c = new CacheLRU(3);
        assertTrue("buscar em cache vazio — não encontrado", !c.buscar(1).encontrado());
    }

    private static void testHitContaComparacoes() {
        CacheLRU c = new CacheLRU(3);
        c.inserir(filme(1));
        ResultadoBusca<Filme> r = c.buscar(1);
        assertTrue("hit — encontrado", r.encontrado());
        assertTrue("hit — comparacoes >= 1", r.comparacoes() >= 1);
    }

    private static void testCapacidadeNuncaExcede() {
        CacheLRU c = new CacheLRU(3);
        for (int i = 1; i <= 10; i++) c.inserir(filme(i));
        assertEquals("tamanho nunca excede capacidade", 3, c.tamanho());
    }

    private static void testEvictaMenosRecentementeUsado() {
        CacheLRU c = new CacheLRU(3);
        c.inserir(filme(1));
        c.inserir(filme(2));
        c.inserir(filme(3)); // ordem MRU→LRU: 3,2,1
        c.inserir(filme(4)); // evicta 1 (LRU)
        assertTrue("filme 1 (LRU) foi removido", !c.buscar(1).encontrado());
        assertTrue("filme 4 presente", c.buscar(4).encontrado());
        assertTrue("filme 2 presente", c.buscar(2).encontrado());
    }

    private static void testHitPromoveAFrente() {
        CacheLRU c = new CacheLRU(3);
        c.inserir(filme(1));
        c.inserir(filme(2));
        c.inserir(filme(3)); // MRU→LRU: 3,2,1
        c.buscar(1);         // promove 1 → MRU→LRU: 1,3,2
        c.inserir(filme(4)); // evicta 2 (agora LRU), não 1
        assertTrue("filme 1 sobrevive após ser promovido por hit", c.buscar(1).encontrado());
        assertTrue("filme 2 foi evictado", !c.buscar(2).encontrado());
    }

    private static void testRegistraRemovidos() {
        CacheLRU c = new CacheLRU(2);
        c.inserir(filme(1));
        c.inserir(filme(2));
        c.inserir(filme(3)); // evicta 1
        assertTrue("lista de removidos contém id=1", c.removidos().contains(1));
    }

    private static void testMaisRecentesOrdem() {
        CacheLRU c = new CacheLRU(5);
        c.inserir(filme(1));
        c.inserir(filme(2));
        c.inserir(filme(3));
        List<Filme> rec = c.maisRecentes(3);
        assertEquals("primeiro mais recente é o último inserido (id=3)", 3, rec.get(0).id());
        assertEquals("último da lista é o mais antigo (id=1)", 1, rec.get(2).id());
    }

    private static void testBuscarPorNomeNoCache() {
        CacheLRU c = new CacheLRU(5);
        c.inserir(filmeNomeado(1, "O Poderoso Chefão"));
        c.inserir(filmeNomeado(2, "Matrix"));
        c.inserir(filmeNomeado(3, "O Poderoso Chefão: Parte II"));
        ResultadoBuscaNome r = c.buscarPorNome("poderoso");
        assertEquals("busca local por \"poderoso\" casa 2 filmes do cache", 2, r.quantidade());
    }

    private static void testBuscarPorNomeNaoPromoveMRU() {
        CacheLRU c = new CacheLRU(5);
        c.inserir(filmeNomeado(1, "Matrix"));
        c.inserir(filmeNomeado(2, "Avatar"));
        c.inserir(filmeNomeado(3, "Duna")); // MRU→LRU: 3,2,1
        c.buscarPorNome("matrix");          // navegação read-only: não deve promover
        assertEquals("busca por nome não altera o MRU (segue id=3)", 3, c.maisRecentes(1).get(0).id());
    }

    private static Filme filme(int id) {
        return new Filme(id, "Filme " + id, "Sinopse", 2020, "Ação");
    }

    private static Filme filmeNomeado(int id, String nome) {
        return new Filme(id, nome, "Sinopse", 2020, "Ação");
    }

    private static void assertEquals(String label, int expected, int actual) {
        if (expected == actual) { System.out.println("  PASS: " + label + " [" + actual + "]"); passed++; }
        else { System.out.println("  FAIL: " + label + " — expected " + expected + " got " + actual); failed++; }
    }

    private static void assertTrue(String label, boolean condition) {
        if (condition) { System.out.println("  PASS: " + label); passed++; }
        else { System.out.println("  FAIL: " + label); failed++; }
    }
}
