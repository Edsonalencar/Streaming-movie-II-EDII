package simulacao;

import modelo.Filme;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GeradorDeFilmesTest {

    private static int passed = 0;
    private static int failed = 0;

    private static final Set<String> CATEGORIAS_VALIDAS = new HashSet<>(
            Arrays.asList("Ação", "Drama", "Comédia", "Terror", "Ficção"));

    public static void main(String[] args) {
        testGerar1000RetornaExatamente1000();
        testIDsSequenciaisSemDuplicatas();
        testReproducibilidade();
        testCamposValidos();

        System.out.println("\n=== GeradorDeFilmesTest: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }

    private static void testGerar1000RetornaExatamente1000() {
        List<Filme> filmes = GeradorDeFilmes.gerar(1000);
        assertEquals("gerar(1000) retorna exatamente 1000 elementos", 1000, filmes.size());
    }

    private static void testIDsSequenciaisSemDuplicatas() {
        List<Filme> filmes = GeradorDeFilmes.gerar(1000);
        Set<Integer> ids = new HashSet<>();
        boolean sequencial = true;
        for (int i = 0; i < filmes.size(); i++) {
            int expectedId = i + 1;
            int actualId = filmes.get(i).id();
            if (actualId != expectedId) {
                sequencial = false;
                break;
            }
            ids.add(actualId);
        }
        assertTrue("IDs 1..1000 sem duplicatas e sequenciais", sequencial && ids.size() == 1000);
    }

    private static void testReproducibilidade() {
        List<Filme> lista1 = GeradorDeFilmes.gerar(1000);
        List<Filme> lista2 = GeradorDeFilmes.gerar(1000);
        boolean igual = lista1.get(0).nome().equals(lista2.get(0).nome())
                && lista1.get(499).nome().equals(lista2.get(499).nome())
                && lista1.get(999).nome().equals(lista2.get(999).nome())
                && lista1.get(0).ano() == lista2.get(0).ano()
                && lista1.get(0).categoria().equals(lista2.get(0).categoria());
        assertTrue("duas chamadas a gerar(1000) produzem resultados idênticos", igual);
    }

    private static void testCamposValidos() {
        List<Filme> filmes = GeradorDeFilmes.gerar(1000);
        boolean todoValidos = true;
        for (Filme f : filmes) {
            if (f.nome() == null || f.nome().isEmpty()) { todoValidos = false; break; }
            if (f.sinopse() == null || f.sinopse().isEmpty()) { todoValidos = false; break; }
            if (f.ano() < 1970 || f.ano() > 2025) { todoValidos = false; break; }
            if (!CATEGORIAS_VALIDAS.contains(f.categoria())) { todoValidos = false; break; }
        }
        assertTrue("todos os filmes têm nome, sinopse, ano 1970..2025 e categoria válida", todoValidos);
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
