package estruturas;

class NoHuffman {
    char caractere;     // significativo apenas em folhas
    int frequencia;
    NoHuffman esquerda;
    NoHuffman direita;

    NoHuffman(char caractere, int frequencia) {
        this.caractere = caractere;
        this.frequencia = frequencia;
    }

    NoHuffman(NoHuffman esquerda, NoHuffman direita) {
        this.frequencia = esquerda.frequencia + direita.frequencia;
        this.esquerda = esquerda;
        this.direita = direita;
    }

    boolean folha() {
        return esquerda == null && direita == null;
    }
}
