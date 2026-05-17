package estruturas;

import modelo.Filme;

class NoAVL {
    Filme filme;
    int altura;
    long ultimoAcesso;
    NoAVL esquerda;
    NoAVL direita;

    NoAVL(Filme filme, long ultimoAcesso) {
        this.filme = filme;
        this.altura = 1;
        this.ultimoAcesso = ultimoAcesso;
    }
}
