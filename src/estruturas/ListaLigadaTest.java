package estruturas;

import modelo.Filme;

public class ListaLigadaTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testTamanhoAposInsercoes();
        testBuscarPrimeiroElemento();
        testBuscarUltimoDeN();
        testBuscarInexistente();
        testMilFilmes();

        System.out.println("\n=== ListaLigadaTest: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }

    private static void testTamanhoAposInsercoes() {
        ListaLigada lista = new ListaLigada();
        lista.inserir(filme(1));
        lista.inserir(filme(2));
        lista.inserir(filme(3));
        assertEquals("tamanho após 3 inserções", 3, lista.tamanho());
    }

    private static void testBuscarPrimeiroElemento() {
        ListaLigada lista = new ListaLigada();
        lista.inserir(filme(10));
        lista.inserir(filme(20));
        lista.inserir(filme(30));

        ResultadoBusca<Filme> r = lista.buscar(10);
        assertTrue("primeiro elemento encontrado", r.encontrado());
        assertEquals("comparacoes para primeiro = 1", 1, r.comparacoes());
    }

    private static void testBuscarUltimoDeN() {
        int n = 5;
        ListaLigada lista = new ListaLigada();
        for (int i = 1; i <= n; i++) lista.inserir(filme(i));

        ResultadoBusca<Filme> r = lista.buscar(n);
        assertTrue("último elemento encontrado", r.encontrado());
        assertEquals("comparacoes para último = N", n, r.comparacoes());
    }

    private static void testBuscarInexistente() {
        ListaLigada lista = new ListaLigada();
        lista.inserir(filme(1));
        lista.inserir(filme(2));
        lista.inserir(filme(3));

        ResultadoBusca<Filme> r = lista.buscar(999);
        assertTrue("miss retorna não encontrado", !r.encontrado());
        assertEquals("comparacoes em miss = tamanho", lista.tamanho(), r.comparacoes());
    }

    private static void testMilFilmes() {
        int n = 1000;
        ListaLigada lista = new ListaLigada();
        for (int i = 1; i <= n; i++) lista.inserir(filme(i));

        assertEquals("tamanho após 1000 inserções", n, lista.tamanho());

        ResultadoBusca<Filme> r = lista.buscar(n);
        assertTrue("último de 1000 encontrado", r.encontrado());
        assertEquals("comparacoes para último de 1000 = 1000", n, r.comparacoes());
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
