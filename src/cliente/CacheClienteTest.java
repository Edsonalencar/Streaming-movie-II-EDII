package cliente;

import estruturas.ResultadoBusca;
import modelo.Filme;

public class CacheClienteTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testBuscarVazioNaoEncontrado();
        testInserirEBuscar();
        testTamanhoVazioE1();
        testImprimirEstadoNaoLancaExcecao();
        testPreWarm50();

        System.out.println("\n=== CacheClienteTest: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }

    private static void testBuscarVazioNaoEncontrado() {
        CacheCliente cache = new CacheCliente();
        ResultadoBusca<Filme> r = cache.buscar(1);
        assertTrue("buscar id=1 em cache vazio — não encontrado", !r.encontrado());
    }

    private static void testInserirEBuscar() {
        CacheCliente cache = new CacheCliente();
        Filme f = filme(42);
        cache.inserir(f);
        ResultadoBusca<Filme> r = cache.buscar(42);
        assertTrue("buscar após inserir — encontrado", r.encontrado());
        assertEquals("buscar após inserir — id correto", 42, r.valor().id());
    }

    private static void testTamanhoVazioE1() {
        CacheCliente cache = new CacheCliente();
        assertEquals("tamanho em cache vazio = 0", 0, cache.tamanho());
        cache.inserir(filme(1));
        assertEquals("tamanho após 1 inserção = 1", 1, cache.tamanho());
    }

    private static void testImprimirEstadoNaoLancaExcecao() {
        CacheCliente cache = new CacheCliente();
        cache.inserir(filme(10));
        cache.imprimirEstado("=== Test Header ===");
        // Visual output verified: titulo line + one AVL node line printed above
        assertEquals("tamanho não alterado após imprimirEstado", 1, cache.tamanho());
    }

    private static void testPreWarm50() {
        CacheCliente cache = new CacheCliente();
        for (int i = 1; i <= 50; i++) cache.inserir(filme(i));
        assertEquals("tamanho após 50 inserções = 50", 50, cache.tamanho());
        for (int id : new int[]{1, 10, 25, 50}) {
            ResultadoBusca<Filme> r = cache.buscar(id);
            assertTrue("buscar id=" + id + " após pre-warm — encontrado", r.encontrado());
        }
    }

    private static Filme filme(int id) {
        return new Filme(id, "Filme " + id, "Sinopse", 2020, "Ação");
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
