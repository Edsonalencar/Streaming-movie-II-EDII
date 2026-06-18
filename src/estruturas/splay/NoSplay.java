package estruturas.splay;

import modelo.Filme;

class NoSplay {
    Filme filme;
    int contadorAcessos;
    NoSplay esquerda;
    NoSplay direita;

    NoSplay(Filme filme) {
        this.filme = filme;
        this.contadorAcessos = 1;
    }
}
