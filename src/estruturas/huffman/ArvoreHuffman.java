package estruturas.huffman;

import java.util.ArrayList;
import java.util.List;

public final class ArvoreHuffman {

    private static final int ALFABETO = 256; // ASCII estendido

    private final NoHuffman raiz;
    private final String[] codigos = new String[ALFABETO];

    private ArvoreHuffman(NoHuffman raiz) {
        this.raiz = raiz;
        gerarCodigos(raiz, "");
    }

    // Constrói a árvore a partir das frequências dos caracteres da mensagem.
    public static ArvoreHuffman construir(String mensagem) {
        int[] freq = new int[ALFABETO];
        for (int i = 0; i < mensagem.length(); i++) freq[mensagem.charAt(i)]++;

        List<NoHuffman> nos = new ArrayList<>();

        for (int c = 0; c < ALFABETO; c++) {
            if (freq[c] > 0) nos.add(new NoHuffman((char) c, freq[c]));
        }

        if (nos.isEmpty()) return new ArvoreHuffman(null);

        if (nos.size() == 1) {
            // único símbolo: cria um pai para que a folha receba o código "0"
            return new ArvoreHuffman(new NoHuffman(nos.get(0), new NoHuffman('\0', 0)));
        }

        while (nos.size() > 1) {
            NoHuffman a = removerMenor(nos);
            NoHuffman b = removerMenor(nos);
            nos.add(new NoHuffman(a, b));
        }

        return new ArvoreHuffman(nos.get(0));
    }

    private static NoHuffman removerMenor(List<NoHuffman> nos) {
        int idxMin = 0;
        for (int i = 1; i < nos.size(); i++) {
            if (nos.get(i).frequencia < nos.get(idxMin).frequencia) idxMin = i;
        }
        return nos.remove(idxMin);
    }

    private void gerarCodigos(NoHuffman no, String prefixo) {
        if (no == null) return;
        if (no.folha()) {
            codigos[no.caractere] = prefixo.isEmpty() ? "0" : prefixo;
            return;
        }
        gerarCodigos(no.esquerda, prefixo + "0");
        gerarCodigos(no.direita, prefixo + "1");
    }

    // Codifica a mensagem como uma cadeia de bits.
    public String comprimir(String mensagem) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mensagem.length(); i++) sb.append(codigos[mensagem.charAt(i)]);
        return sb.toString();
    }

    // Reconstrói a mensagem original a partir da cadeia de bits.
    public String descomprimir(String bits) {
        if (raiz == null) return "";
        StringBuilder sb = new StringBuilder();
        NoHuffman no = raiz;
        for (int i = 0; i < bits.length(); i++) {
            no = bits.charAt(i) == '0' ? no.esquerda : no.direita;
            if (no.folha()) {
                sb.append(no.caractere);
                no = raiz;
            }
        }
        return sb.toString();
    }

    public record ResultadoCompressao(String original, int bitsOriginais,
                                       int bitsComprimidos, double taxa) {}

    // Comprime a mensagem e devolve as métricas. O tamanho original assume 8 bits por caractere.
    public static ResultadoCompressao analisar(String mensagem) {
        ArvoreHuffman arvore = construir(mensagem);
        String bits = arvore.comprimir(mensagem);
        int bitsOriginais = mensagem.length() * 8;
        int bitsComprimidos = bits.length();
        double taxa = bitsOriginais == 0 ? 0.0
                : 100.0 * (1.0 - (double) bitsComprimidos / bitsOriginais);
        return new ResultadoCompressao(mensagem, bitsOriginais, bitsComprimidos, taxa);
    }
}
