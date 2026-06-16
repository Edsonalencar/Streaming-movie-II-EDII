package simulacao;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class SimuladorTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testInicializarTamanhos();
        testTresClientesComCache50();
        testExecutarSemExcecao();
        testExecutarContemSecoes();
        testCacheHitMenorQueSemIndice();
        testComIndiceMenorQueSemIndice();
        testHuffmanIntegridade();
        testEvicaoLRUVisivel();

        System.out.println("\n=== SimuladorTest: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }

    private static Simulador novoMudo() {
        Simulador sim = new Simulador();
        sim.comDelays = false;
        return sim;
    }

    private static String capturarExecutar(Simulador sim) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(baos));
        try {
            sim.executar();
        } finally {
            System.setOut(original);
        }
        return baos.toString();
    }

    private static void testInicializarTamanhos() {
        Simulador sim = novoMudo();
        PrintStream original = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        sim.inicializar();
        System.setOut(original);
        assertEquals("servidor.tamanho() = 1000 após inicializar", 1000, sim.servidor.tamanho());
        assertEquals("3 clientes cadastrados", 3, sim.clientes.size());
    }

    private static void testTresClientesComCache50() {
        Simulador sim = novoMudo();
        PrintStream original = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        sim.inicializar();
        System.setOut(original);
        boolean todos50 = sim.clientes.stream().allMatch(c -> c.tamanho() == 50);
        assertTrue("cada cliente tem 50 filmes pré-carregados", todos50);
    }

    private static void testExecutarSemExcecao() {
        try {
            capturarExecutar(novoMudo());
            assertTrue("executar() termina sem exceção", true);
        } catch (Exception e) {
            assertTrue("executar() não deve lançar exceção: " + e, false);
        }
    }

    private static void testExecutarContemSecoes() {
        String out = capturarExecutar(novoMudo());
        assertTrue("contém bateria de consultas", out.contains("Bateria de consultas"));
        assertTrue("contém análise LRU", out.contains("cache LRU"));
        assertTrue("contém análise de preferências (splay cliente)", out.contains("preferências"));
        assertTrue("contém análise de popularidade (splay servidor)", out.contains("popularidade"));
        assertTrue("contém compressão de Huffman", out.contains("Huffman"));
        assertTrue("contém relatório comparativo", out.contains("Relatório comparativo"));
    }

    private static void testCacheHitMenorQueSemIndice() {
        Simulador sim = novoMudo();
        capturarExecutar(sim);
        double mediaHit = sim.totalCacheHit / (double) sim.qtdCacheHit;
        double mediaSem = sim.totalSemIndice / (double) sim.qtdSemIndice;
        assertTrue("média cache hit (" + mediaHit + ") << sem índice (" + mediaSem + ")",
                mediaHit * 10 < mediaSem);
    }

    private static void testComIndiceMenorQueSemIndice() {
        Simulador sim = novoMudo();
        capturarExecutar(sim);
        double mediaCom = sim.totalComIndice / (double) sim.qtdComIndice;
        double mediaSem = sim.totalSemIndice / (double) sim.qtdSemIndice;
        assertTrue("média com índice (" + mediaCom + ") << sem índice (" + mediaSem + ")",
                mediaCom * 10 < mediaSem);
    }

    private static void testHuffmanIntegridade() {
        String out = capturarExecutar(novoMudo());
        assertTrue("nenhuma falha de integridade na compressão de Huffman",
                !out.contains("FALHA INTEGRIDADE"));
    }

    private static void testEvicaoLRUVisivel() {
        String out = capturarExecutar(novoMudo());
        assertTrue("seção LRU mostra registros removidos",
                out.contains("removidos pela política LRU"));
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
