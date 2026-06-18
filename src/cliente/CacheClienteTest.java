package cliente;

import estruturas.ResultadoBusca;
import estruturas.ResultadoBuscaNome;
import modelo.Filme;

public class CacheClienteTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testBuscarVazioNaoEncontrado();
        testInserirEBuscar();
        testTamanhoVazioE1();
        testNome();
        testRegistrarAcessoAtualizaPreferencia();
        testCincoMaisAcessados();
        testBuscarPorNomeLocalEncontra();
        testBuscarPorNomeLocalVazio();
        testImprimirEstadoNaoLancaExcecao();

        System.out.println("\n=== CacheClienteTest: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }

    private static void testBuscarVazioNaoEncontrado() {
        CacheCliente cache = new CacheCliente("Teste", 50);
        ResultadoBusca<Filme> r = cache.buscar(1);
        assertTrue("buscar id=1 em cache vazio — não encontrado", !r.encontrado());
    }

    private static void testInserirEBuscar() {
        CacheCliente cache = new CacheCliente("Teste", 50);
        cache.inserir(filme(42));
        ResultadoBusca<Filme> r = cache.buscar(42);
        assertTrue("buscar após inserir — encontrado", r.encontrado());
        assertEquals("buscar após inserir — id correto", 42, r.valor().id());
    }

    private static void testTamanhoVazioE1() {
        CacheCliente cache = new CacheCliente("Teste", 50);
        assertEquals("tamanho em cache vazio = 0", 0, cache.tamanho());
        cache.inserir(filme(1));
        assertEquals("tamanho após 1 inserção = 1", 1, cache.tamanho());
    }

    private static void testNome() {
        CacheCliente cache = new CacheCliente("Ana", 50);
        assertTrue("nome do cliente = Ana", "Ana".equals(cache.nome()));
    }

    private static void testRegistrarAcessoAtualizaPreferencia() {
        CacheCliente cache = new CacheCliente("Teste", 50);
        cache.registrarAcesso(filme(7));
        cache.registrarAcesso(filme(9));
        Filme raiz = cache.preferenciaAtual();
        assertTrue("preferência atual = último acesso (id=9)", raiz != null && raiz.id() == 9);
    }

    private static void testCincoMaisAcessados() {
        CacheCliente cache = new CacheCliente("Teste", 50);
        for (int i = 0; i < 3; i++) cache.registrarAcesso(filme(100)); // mais acessado
        cache.registrarAcesso(filme(200));
        assertTrue("filme 100 está entre os 5 mais acessados",
                cache.cincoMaisAcessados().stream().anyMatch(f -> f.id() == 100));
    }

    private static void testImprimirEstadoNaoLancaExcecao() {
        CacheCliente cache = new CacheCliente("Teste", 50);
        cache.inserir(filme(1));
        try {
            cache.imprimirEstadoCache("== estado ==");
            assertTrue("imprimirEstadoCache não lança exceção", true);
        } catch (Exception e) {
            assertTrue("imprimirEstadoCache não lança exceção: " + e, false);
        }
    }

    private static void testBuscarPorNomeLocalEncontra() {
        CacheCliente cache = new CacheCliente("Teste", 50);
        cache.inserir(new Filme(1, "Interestelar", "Sinopse", 2014, "Ficção Científica"));
        cache.inserir(new Filme(2, "Matrix", "Sinopse", 1999, "Ficção Científica"));
        ResultadoBuscaNome r = cache.buscarPorNomeLocal("inter");
        assertTrue("busca local por \"inter\" encontra Interestelar",
                r.quantidade() == 1 && r.resultados().get(0).id() == 1);
    }

    private static void testBuscarPorNomeLocalVazio() {
        CacheCliente cache = new CacheCliente("Teste", 50);
        cache.inserir(new Filme(1, "Matrix", "Sinopse", 1999, "Ficção Científica"));
        ResultadoBuscaNome r = cache.buscarPorNomeLocal("avatar");
        assertTrue("termo ausente no cache local — sem resultados", r.vazio());
    }

    private static Filme filme(int id) {
        return new Filme(id, "Filme " + id, "Sinopse", 2020, "Ação");
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
