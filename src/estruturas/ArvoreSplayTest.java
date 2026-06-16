package estruturas;

import modelo.Filme;

import java.util.List;

public class ArvoreSplayTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testVazia();
        testInserirVaiParaRaiz();
        testAcessarTrazParaRaiz();
        testAcessarInexistente();
        testContaComparacoes();
        testMaisAcessados();
        testMaisProximosDaRaiz();
        testPreservaTodasAsChaves();

        System.out.println("\n=== ArvoreSplayTest: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }

    private static void testVazia() {
        ArvoreSplay s = new ArvoreSplay();
        assertTrue("árvore nova está vazia", s.vazia());
        assertTrue("raiz de árvore vazia é null", s.raiz() == null);
    }

    private static void testInserirVaiParaRaiz() {
        ArvoreSplay s = new ArvoreSplay();
        s.inserir(filme(10));
        s.inserir(filme(20));
        s.inserir(filme(5));
        assertEquals("último inserido fica na raiz", 5, s.raiz().id());
    }

    private static void testAcessarTrazParaRaiz() {
        ArvoreSplay s = new ArvoreSplay();
        for (int id : new int[]{10, 20, 5, 30, 1}) s.inserir(filme(id));
        s.acessar(20);
        assertEquals("nó acessado vira raiz", 20, s.raiz().id());
    }

    private static void testAcessarInexistente() {
        ArvoreSplay s = new ArvoreSplay();
        s.inserir(filme(10));
        ResultadoBusca<Filme> r = s.acessar(999);
        assertTrue("acessar inexistente — não encontrado", !r.encontrado());
    }

    private static void testContaComparacoes() {
        ArvoreSplay s = new ArvoreSplay();
        for (int id : new int[]{10, 20, 5, 30, 1}) s.inserir(filme(id));
        ResultadoBusca<Filme> r = s.acessar(30);
        assertTrue("acessar conta comparações >= 1", r.encontrado() && r.comparacoes() >= 1);
    }

    private static void testMaisAcessados() {
        ArvoreSplay s = new ArvoreSplay();
        s.inserir(filme(1));
        s.inserir(filme(2));
        s.inserir(filme(1)); // reinserção conta como novo acesso
        s.acessar(1);        // id=1 agora tem mais acessos
        List<Filme> top = s.maisAcessados(1);
        assertEquals("mais acessado é o id=1", 1, top.get(0).id());
    }

    private static void testMaisProximosDaRaiz() {
        ArvoreSplay s = new ArvoreSplay();
        for (int id : new int[]{10, 20, 5, 30, 1}) s.inserir(filme(id));
        s.acessar(20); // 20 vira raiz
        List<Filme> prox = s.maisProximosDaRaiz(3);
        assertEquals("primeiro mais próximo da raiz é a própria raiz (id=20)", 20, prox.get(0).id());
        assertTrue("retorna no máximo 3", prox.size() <= 3);
    }

    private static void testPreservaTodasAsChaves() {
        ArvoreSplay s = new ArvoreSplay();
        int[] ids = {10, 20, 5, 30, 1, 25, 15};
        for (int id : ids) s.inserir(filme(id));
        boolean todas = true;
        for (int id : ids) if (!s.acessar(id).encontrado()) { todas = false; break; }
        assertTrue("todas as chaves continuam acessíveis após splays", todas);
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
