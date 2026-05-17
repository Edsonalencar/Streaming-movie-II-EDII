import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class MainTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testMainCompletesWithoutException();
        testOutputContainsSectionHeaders();
        testOutputContainsSummaryTable();
        testOutputContainsAnalysisParagraph();

        System.out.println("\n=== MainTest: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }

    private static void testMainCompletesWithoutException() {
        PrintStream original = System.out;
        try {
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            Main.main(new String[]{});
            passed++;
            System.setOut(original);
            System.out.println("PASS: Main.main completes without exception");
        } catch (Exception e) {
            System.setOut(original);
            System.out.println("FAIL: Main.main threw " + e);
            failed++;
        }
    }

    private static void testOutputContainsSectionHeaders() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(buf));
        try {
            Main.main(new String[]{});
        } finally {
            System.setOut(original);
        }
        String output = buf.toString();
        String[] headers = {
            "Consultas inválidas",
            "cache hit",
            "sem índice",
            "com índice"
        };
        for (String h : headers) {
            if (output.contains(h)) {
                passed++;
                System.out.println("PASS: output contains header \"" + h + "\"");
            } else {
                failed++;
                System.out.println("FAIL: output missing header \"" + h + "\"");
            }
        }
    }

    private static void testOutputContainsSummaryTable() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(buf));
        try {
            Main.main(new String[]{});
        } finally {
            System.setOut(original);
        }
        String output = buf.toString();
        boolean hasTable = output.contains("Média") || output.contains("media") || output.contains("comparac");
        if (hasTable) {
            passed++;
            System.out.println("PASS: output contains comparative summary table");
        } else {
            failed++;
            System.out.println("FAIL: output missing comparative summary table");
        }
    }

    private static void testOutputContainsAnalysisParagraph() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(buf));
        try {
            Main.main(new String[]{});
        } finally {
            System.setOut(original);
        }
        String output = buf.toString();
        // Analysis paragraph is in Portuguese and mentions the three strategies
        boolean hasAnalysis = output.contains("cache") && output.contains("índice") &&
                              (output.contains("AVL") || output.contains("hash"));
        if (hasAnalysis) {
            passed++;
            System.out.println("PASS: output contains Portuguese analysis paragraph");
        } else {
            failed++;
            System.out.println("FAIL: output missing Portuguese analysis paragraph");
        }
    }
}
