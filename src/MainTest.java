import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class MainTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        // Executa Main uma única vez (a simulação completa contém pausas) e
        // valida a saída capturada com várias asserções.
        String output = executarMain();

        testCompletouSemExcecao(output);
        testContemSecoes(output);
        testContemRelatorio(output);
        testContemAnalise(output);

        System.out.println("\n=== MainTest: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }

    private static String executarMain() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(buf));
        try {
            Main.main(new String[]{});
        } catch (Exception e) {
            System.setOut(original);
            System.out.println("FAIL: Main.main lançou " + e);
            failed++;
            return "";
        } finally {
            System.setOut(original);
        }
        return buf.toString();
    }

    private static void testCompletouSemExcecao(String output) {
        assertTrue("Main.main completa e produz saída", output != null && !output.isEmpty());
    }

    private static void testContemSecoes(String output) {
        String[] secoes = {"Prática Offline 3", "Bateria de consultas",
                "cache LRU", "preferências", "popularidade",
                "Busca por parte do nome do filme", "Huffman"};
        for (String s : secoes) assertTrue("saída contém seção \"" + s + "\"", output.contains(s));
    }

    private static void testContemRelatorio(String output) {
        assertTrue("saída contém relatório comparativo",
                output.contains("Relatório comparativo") && output.contains("comparações"));
    }

    private static void testContemAnalise(String output) {
        assertTrue("saída contém análise (cache + índice + hash)",
                output.contains("cache") && output.contains("índice") && output.contains("hash"));
    }

    private static void assertTrue(String label, boolean condition) {
        if (condition) { System.out.println("PASS: " + label); passed++; }
        else { System.out.println("FAIL: " + label); failed++; }
    }
}
