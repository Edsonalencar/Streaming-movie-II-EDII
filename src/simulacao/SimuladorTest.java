package simulacao;

import estruturas.ResultadoBusca;
import modelo.Filme;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class SimuladorTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testInicializarTamanhos();
        testSomar();
        testConsultasCacheHitComparacoes();
        testConsultasSemIndiceComparacoes();
        testConsultasComIndiceComparacoes();
        testConsultasInvalidasNaoEncontrado();
        testExecutarSemExcecao();
        testExecutarContemCabecalhos();
        testExecutarEvicaoVisivel();
        testRelatorioMediaCacheHitMenorQueSemIndice();

        System.out.println("\n=== SimuladorTest: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }

    // --- Unit: inicializar ---

    private static void testInicializarTamanhos() {
        Simulador sim = new Simulador();
        sim.inicializar();
        assertEquals("servidor.tamanho() = 1000 após inicializar", 1000, sim.servidor.tamanho());
        assertEquals("cache.tamanho() = 50 após inicializar", 50, sim.cache.tamanho());
    }

    // --- Unit: somar ---

    private static void testSomar() {
        Filme f = new Filme(1, "X", "S", 2020, "Ação");
        ResultadoBusca<Filme> a = new ResultadoBusca<>(null, 7);
        ResultadoBusca<Filme> b = new ResultadoBusca<>(f, 13);
        ResultadoBusca<Filme> total = Simulador.somar(a, b);
        assertEquals("somar: comparacoes = 7 + 13 = 20", 20, total.comparacoes());
        assertTrue("somar: valor vem de b", total.valor() == f);
    }

    // --- Unit: consultasCacheHit ---

    private static void testConsultasCacheHitComparacoes() {
        Simulador sim = new Simulador();
        sim.inicializar();
        // IDs_CACHE_HIT = {1, 5, 10, 20, 35, 50} — all pre-warmed, so AVL search only
        // capture output silently
        PrintStream original = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        sim.totalCacheHit = 0;
        sim.consultasCacheHit();
        System.setOut(original);

        double media = sim.totalCacheHit / 6.0;
        assertTrue("cache hit média ≤ 7 comparações (foi " + media + ")", media <= 7);
    }

    // --- Unit: consultasSemIndice ---

    private static void testConsultasSemIndiceComparacoes() {
        Simulador sim = new Simulador();
        sim.inicializar();
        PrintStream original = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        sim.totalSemIndice = 0;
        sim.consultasSemIndice();
        System.setOut(original);

        double media = sim.totalSemIndice / 6.0;
        assertTrue("sem índice: comparações ≥ 1 (foi " + media + ")", media >= 1);
        assertTrue("sem índice: comparações ≤ 1000 + 7 (foi " + media + ")", media <= 1007);
    }

    // --- Unit: consultasComIndice ---

    private static void testConsultasComIndiceComparacoes() {
        Simulador sim = new Simulador();
        sim.inicializar();
        PrintStream original = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        sim.totalComIndice = 0;
        sim.consultasComIndice();
        System.setOut(original);

        double media = sim.totalComIndice / 6.0;
        // AVL miss (up to 7) + hash probe (≤ 5) = ≤ 12 total per query
        assertTrue("com índice: média ≤ 12 comparações (foi " + media + ")", media <= 12);
    }

    // --- Unit: consultasInvalidas ---

    private static void testConsultasInvalidasNaoEncontrado() {
        Simulador sim = new Simulador();
        sim.inicializar();
        // We check that IDs 0 and 9999 produce "NÃO ENCONTRADO" lines
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(baos));
        sim.consultasInvalidas();
        System.setOut(original);
        String output = baos.toString();
        assertTrue("inválidas: saída contém 'NÃO ENCONTRADO' para id=0",
                output.contains("id=0") && output.contains("NÃO ENCONTRADO"));
        assertTrue("inválidas: saída contém 'NÃO ENCONTRADO' para id=9999",
                output.contains("id=9999") && output.contains("NÃO ENCONTRADO"));
    }

    // --- Integration: full executar ---

    private static void testExecutarSemExcecao() {
        Simulador sim = new Simulador();
        PrintStream original = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        try {
            sim.executar();
            assertTrue("executar() termina sem exceção", true);
        } catch (Exception e) {
            System.setOut(original);
            assertTrue("executar() não deve lançar exceção: " + e, false);
            return;
        }
        System.setOut(original);
    }

    private static void testExecutarContemCabecalhos() {
        Simulador sim = new Simulador();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(baos));
        sim.executar();
        System.setOut(original);
        String output = baos.toString();

        assertTrue("saída contém 'Consultas inválidas'",
                output.contains("Consultas inválidas"));
        assertTrue("saída contém 'cache hit'",
                output.contains("cache hit"));
        assertTrue("saída contém 'sem indexação'",
                output.contains("sem indexação"));
        assertTrue("saída contém 'indexação'",
                output.contains("indexação"));
    }

    private static void testExecutarEvicaoVisivel() {
        // Run executar and capture the two AVL state dumps
        Simulador sim = new Simulador();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(baos));
        sim.executar();
        System.setOut(original);
        String output = baos.toString();

        // The final dump must contain IDs from IDS_COM_INDICE (55, 130, 260, 410, 610, 860)
        // that were NOT in the initial pre-warm (IDs 1-50).
        boolean finalContainsNewId = output.contains("id=55") || output.contains("id=130")
                || output.contains("id=260") || output.contains("id=410")
                || output.contains("id=610") || output.contains("id=860");
        assertTrue("estado final contém pelo menos um ID inserido via índice (evicção LRU visível)",
                finalContainsNewId);
    }

    private static void testRelatorioMediaCacheHitMenorQueSemIndice() {
        Simulador sim = new Simulador();
        PrintStream original = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        sim.executar();
        System.setOut(original);

        double mediaCacheHit = sim.totalCacheHit / 6.0;
        double mediaSemIndice = sim.totalSemIndice / 6.0;
        assertTrue("média cache hit (" + mediaCacheHit + ") << média sem índice (" + mediaSemIndice + ")",
                mediaCacheHit * 10 < mediaSemIndice);
    }

    // --- Helpers ---

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
