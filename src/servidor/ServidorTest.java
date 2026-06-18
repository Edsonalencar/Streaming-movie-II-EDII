package servidor;

import estruturas.ResultadoBusca;
import estruturas.ResultadoBuscaNome;
import modelo.Filme;

public class ServidorTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        // Unit tests
        testTamanhoApos10Insercoes();
        testBuscarSemIndiceDecimo();
        testBuscarSemIndiceInexistente();
        testBuscarComIndiceEncontrado();
        testBuscarComIndiceInexistente();
        testBuscarPorNomeEncontraVarios();
        testBuscarPorNomeAlimentaPopularidade();
        testBuscarPorNomeSemResultado();
        // Integration tests
        testIntegracaoSemIndiceMediaEntre400e600();
        testIntegracaoComIndiceMediaAtE5();

        System.out.println("\n=== ServidorTest: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }

    private static void testTamanhoApos10Insercoes() {
        Servidor s = new Servidor();
        for (int i = 1; i <= 10; i++) s.inserir(filme(i));
        assertEquals("tamanho após 10 inserções = 10", 10, s.tamanho());
    }

    private static void testBuscarSemIndiceDecimo() {
        Servidor s = new Servidor();
        for (int i = 1; i <= 10; i++) s.inserir(filme(i));
        ResultadoBusca<Filme> r = s.buscarSemIndice(10);
        assertTrue("buscarSemIndice 10º — encontrado", r.encontrado());
        assertEquals("buscarSemIndice 10º — comparacoes = 10", 10, r.comparacoes());
    }

    private static void testBuscarSemIndiceInexistente() {
        Servidor s = new Servidor();
        for (int i = 1; i <= 10; i++) s.inserir(filme(i));
        ResultadoBusca<Filme> r = s.buscarSemIndice(9999);
        assertTrue("buscarSemIndice inexistente — não encontrado", !r.encontrado());
        assertEquals("buscarSemIndice inexistente — comparacoes = 10", 10, r.comparacoes());
    }

    private static void testBuscarComIndiceEncontrado() {
        Servidor s = new Servidor();
        for (int i = 1; i <= 10; i++) s.inserir(filme(i));
        for (int i = 1; i <= 10; i++) {
            ResultadoBusca<Filme> r = s.buscarComIndice(i);
            assertTrue("buscarComIndice id=" + i + " — encontrado", r.encontrado());
            assertTrue("buscarComIndice id=" + i + " — comparacoes <= 3", r.comparacoes() <= 3);
        }
    }

    private static void testBuscarComIndiceInexistente() {
        Servidor s = new Servidor();
        for (int i = 1; i <= 10; i++) s.inserir(filme(i));
        ResultadoBusca<Filme> r = s.buscarComIndice(9999);
        assertTrue("buscarComIndice inexistente — não encontrado", !r.encontrado());
    }

    private static void testIntegracaoSemIndiceMediaEntre400e600() {
        Servidor s = new Servidor();
        for (int i = 1; i <= 1000; i++) s.inserir(filme(i));
        int[] ids = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};
        long total = 0;
        for (int id : ids) total += s.buscarSemIndice(id).comparacoes();
        double media = total / (double) ids.length;
        assertTrue("integração sem índice: média entre 400 e 600 (foi " + media + ")",
                media >= 400 && media <= 600);
    }

    private static void testIntegracaoComIndiceMediaAtE5() {
        Servidor s = new Servidor();
        for (int i = 1; i <= 1000; i++) s.inserir(filme(i));
        int[] ids = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};
        long total = 0;
        for (int id : ids) total += s.buscarComIndice(id).comparacoes();
        double media = total / (double) ids.length;
        assertTrue("integração com índice: média ≤ 5 (foi " + media + ")", media <= 5);
    }

    private static void testBuscarPorNomeEncontraVarios() {
        Servidor s = new Servidor();
        s.inserir(filmeNomeado(1, "O Senhor dos Anéis: A Sociedade do Anel"));
        s.inserir(filmeNomeado(2, "Matrix"));
        s.inserir(filmeNomeado(3, "O Senhor dos Anéis: O Retorno do Rei"));
        ResultadoBuscaNome r = s.buscarPorNome("senhor dos anéis");
        assertEquals("busca por nome casa os 2 títulos da franquia", 2, r.quantidade());
        assertEquals("varredura sequencial visita o catálogo inteiro", s.tamanho(), r.comparacoes());
    }

    private static void testBuscarPorNomeAlimentaPopularidade() {
        Servidor s = new Servidor();
        s.inserir(filmeNomeado(1, "Matrix"));
        s.inserir(filmeNomeado(2, "Avatar"));
        assertTrue("popularidade vazia antes da busca", s.filmeMaisPopular() == null);
        s.buscarPorNome("matrix");
        assertTrue("título casado alimenta a splay de popularidade", s.filmeMaisPopular() != null);
    }

    private static void testBuscarPorNomeSemResultado() {
        Servidor s = new Servidor();
        for (int i = 1; i <= 10; i++) s.inserir(filme(i));
        ResultadoBuscaNome r = s.buscarPorNome("titulo inexistente");
        assertTrue("termo ausente — nenhum resultado", r.vazio());
        assertEquals("miss varre o catálogo inteiro", 10, r.comparacoes());
    }

    private static Filme filme(int id) {
        return new Filme(id, "Filme " + id, "Sinopse", 2020, "Ação");
    }

    private static Filme filmeNomeado(int id, String nome) {
        return new Filme(id, nome, "Sinopse", 2020, "Ação");
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
