package simulacao;

import modelo.Filme;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeradorDeFilmes {

    private static final String[] CATEGORIAS = {"Ação", "Drama", "Comédia", "Terror", "Ficção"};

    public static List<Filme> gerar(int quantidade) {
        Random rng = new Random(42L);
        List<Filme> filmes = new ArrayList<>(quantidade);
        for (int id = 1; id <= quantidade; id++) {
            String nome = "Filme #" + id;
            String sinopse = "Sinopse do filme " + id;
            int ano = 1970 + rng.nextInt(56);
            String categoria = CATEGORIAS[rng.nextInt(CATEGORIAS.length)];
            filmes.add(new Filme(id, nome, sinopse, ano, categoria));
        }
        return filmes;
    }
}
