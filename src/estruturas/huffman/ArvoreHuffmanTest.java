package estruturas.huffman;

public class ArvoreHuffmanTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testRoundTripSimples();
        testRoundTripMensagensReais();
        testUnicoSimbolo();
        testComprimeReduzTamanho();
        testTaxaCoerente();

        System.out.println("\n=== ArvoreHuffmanTest: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }

    private static void testRoundTripSimples() {
        verificarRoundTrip("ABRACADABRA");
    }

    private static void testRoundTripMensagensReais() {
        verificarRoundTrip("LOGIN_OK");
        verificarRoundTrip("GET /filme/505");
        verificarRoundTrip("FILME:505|Matrix|1999");
        verificarRoundTrip("RECOMENDACAO:202|Interestelar");
    }

    private static void verificarRoundTrip(String msg) {
        ArvoreHuffman a = ArvoreHuffman.construir(msg);
        String descomprimido = a.descomprimir(a.comprimir(msg));
        assertTrue("round-trip preserva \"" + msg + "\"", msg.equals(descomprimido));
    }

    private static void testUnicoSimbolo() {
        ArvoreHuffman a = ArvoreHuffman.construir("AAAA");
        String descomprimido = a.descomprimir(a.comprimir("AAAA"));
        assertTrue("único símbolo round-trip", "AAAA".equals(descomprimido));
    }

    private static void testComprimeReduzTamanho() {
        // mensagem com frequências bem desbalanceadas → boa compressão
        String msg = "AAAAAAAAAAAAAAAABCD";
        ArvoreHuffman.ResultadoCompressao r = ArvoreHuffman.analisar(msg);
        assertTrue("compressão reduz bits (comp < orig)", r.bitsComprimidos() < r.bitsOriginais());
    }

    private static void testTaxaCoerente() {
        ArvoreHuffman.ResultadoCompressao r = ArvoreHuffman.analisar("FILME:505|Matrix|1999");
        assertTrue("bits originais = 8 * comprimento", r.bitsOriginais() == "FILME:505|Matrix|1999".length() * 8);
        assertTrue("taxa entre 0 e 100", r.taxa() >= 0 && r.taxa() <= 100);
    }

    private static void assertTrue(String label, boolean condition) {
        if (condition) { System.out.println("  PASS: " + label); passed++; }
        else { System.out.println("  FAIL: " + label); failed++; }
    }
}
