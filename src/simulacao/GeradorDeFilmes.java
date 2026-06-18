package simulacao;

import modelo.Filme;

import java.util.ArrayList;
import java.util.List;

// Gera o catálogo do servidor a partir de um pool de títulos reais, percorrido ciclicamente
// até completar a quantidade pedida. A geração é determinística (sem aleatoriedade).
public class GeradorDeFilmes {

    private record Titulo(String nome, int ano, String categoria) {}

    private static final Titulo[] CATALOGO = {
            // Ficção Científica
            new Titulo("Matrix", 1999, "Ficção Científica"),
            new Titulo("A Origem", 2010, "Ficção Científica"),
            new Titulo("Interestelar", 2014, "Ficção Científica"),
            new Titulo("Blade Runner", 1982, "Ficção Científica"),
            new Titulo("Jurassic Park", 1993, "Ficção Científica"),
            new Titulo("De Volta para o Futuro", 1985, "Ficção Científica"),
            new Titulo("Alien: O Oitavo Passageiro", 1979, "Ficção Científica"),
            new Titulo("O Exterminador do Futuro", 1984, "Ficção Científica"),
            new Titulo("Avatar", 2009, "Ficção Científica"),
            new Titulo("Duna", 2021, "Ficção Científica"),
            new Titulo("Star Wars: Uma Nova Esperança", 1977, "Ficção Científica"),
            new Titulo("O Império Contra-Ataca", 1980, "Ficção Científica"),
            new Titulo("E.T. - O Extraterrestre", 1982, "Ficção Científica"),
            new Titulo("WALL·E", 2008, "Ficção Científica"),
            new Titulo("A Chegada", 2016, "Ficção Científica"),
            new Titulo("Gravidade", 2013, "Ficção Científica"),
            new Titulo("Perdido em Marte", 2015, "Ficção Científica"),
            new Titulo("Ex Machina: Instinto Artificial", 2014, "Ficção Científica"),
            new Titulo("Contato", 1997, "Ficção Científica"),
            new Titulo("Minority Report: A Nova Lei", 2002, "Ficção Científica"),

            // Ação
            new Titulo("Mad Max: Estrada da Fúria", 2015, "Ação"),
            new Titulo("John Wick: De Volta ao Jogo", 2014, "Ação"),
            new Titulo("Duro de Matar", 1988, "Ação"),
            new Titulo("Gladiador", 2000, "Ação"),
            new Titulo("Pantera Negra", 2018, "Ação"),
            new Titulo("Vingadores: Ultimato", 2019, "Ação"),
            new Titulo("Homem de Ferro", 2008, "Ação"),
            new Titulo("Velozes e Furiosos", 2001, "Ação"),
            new Titulo("Missão: Impossível", 1996, "Ação"),
            new Titulo("Rambo: Programado para Matar", 1982, "Ação"),
            new Titulo("300", 2006, "Ação"),
            new Titulo("Kill Bill: Volume 1", 2003, "Ação"),
            new Titulo("O Senhor dos Anéis: A Sociedade do Anel", 2001, "Ação"),
            new Titulo("Batman: O Cavaleiro das Trevas", 2008, "Ação"),
            new Titulo("Top Gun: Ases Indomáveis", 1986, "Ação"),
            new Titulo("Os Caçadores da Arca Perdida", 1981, "Ação"),
            new Titulo("Capitão América: Guerra Civil", 2016, "Ação"),
            new Titulo("Tropa de Elite", 2007, "Ação"),
            new Titulo("Homem-Aranha no Aranhaverso", 2018, "Ação"),
            new Titulo("O Senhor dos Anéis: O Retorno do Rei", 2003, "Ação"),

            // Drama
            new Titulo("O Poderoso Chefão", 1972, "Drama"),
            new Titulo("Forrest Gump: O Contador de Histórias", 1994, "Drama"),
            new Titulo("Um Sonho de Liberdade", 1994, "Drama"),
            new Titulo("Clube da Luta", 1999, "Drama"),
            new Titulo("A Lista de Schindler", 1993, "Drama"),
            new Titulo("Pulp Fiction: Tempo de Violência", 1994, "Drama"),
            new Titulo("Cidade de Deus", 2002, "Drama"),
            new Titulo("O Resgate do Soldado Ryan", 1998, "Drama"),
            new Titulo("Whiplash: Em Busca da Perfeição", 2014, "Drama"),
            new Titulo("O Lobo de Wall Street", 2013, "Drama"),
            new Titulo("Os Bons Companheiros", 1990, "Drama"),
            new Titulo("Beleza Americana", 1999, "Drama"),
            new Titulo("Náufrago", 2000, "Drama"),
            new Titulo("À Espera de um Milagre", 1999, "Drama"),
            new Titulo("12 Anos de Escravidão", 2013, "Drama"),
            new Titulo("Parasita", 2019, "Drama"),
            new Titulo("Nomadland", 2020, "Drama"),
            new Titulo("A Rede Social", 2010, "Drama"),
            new Titulo("Bohemian Rhapsody", 2018, "Drama"),
            new Titulo("Coringa", 2019, "Drama"),

            // Terror
            new Titulo("O Iluminado", 1980, "Terror"),
            new Titulo("O Exorcista", 1973, "Terror"),
            new Titulo("A Bruxa", 2015, "Terror"),
            new Titulo("Hereditário", 2018, "Terror"),
            new Titulo("It: A Coisa", 2017, "Terror"),
            new Titulo("Invocação do Mal", 2013, "Terror"),
            new Titulo("Corra!", 2017, "Terror"),
            new Titulo("Pânico", 1996, "Terror"),
            new Titulo("A Hora do Pesadelo", 1984, "Terror"),
            new Titulo("O Massacre da Serra Elétrica", 1974, "Terror"),
            new Titulo("Halloween: A Noite do Terror", 1978, "Terror"),
            new Titulo("Sexta-Feira 13", 1980, "Terror"),
            new Titulo("Atividade Paranormal", 2007, "Terror"),
            new Titulo("O Babadook", 2014, "Terror"),
            new Titulo("Midsommar: O Mal Não Espera a Noite", 2019, "Terror"),
            new Titulo("Um Lugar Silencioso", 2018, "Terror"),
            new Titulo("Annabelle", 2014, "Terror"),
            new Titulo("Jogos Mortais", 2004, "Terror"),
            new Titulo("O Chamado", 2002, "Terror"),
            new Titulo("Enigma de Outro Mundo", 1982, "Terror"),

            // Comédia
            new Titulo("Esqueceram de Mim", 1990, "Comédia"),
            new Titulo("Curtindo a Vida Adoidado", 1986, "Comédia"),
            new Titulo("Os Caça-Fantasmas", 1984, "Comédia"),
            new Titulo("A Vida é Bela", 1997, "Comédia"),
            new Titulo("O Grande Lebowski", 1998, "Comédia"),
            new Titulo("Se Beber, Não Case!", 2009, "Comédia"),
            new Titulo("Click", 2006, "Comédia"),
            new Titulo("As Branquelas", 2004, "Comédia"),
            new Titulo("American Pie: A Primeira Vez é Inesquecível", 1999, "Comédia"),
            new Titulo("Toy Story: Um Mundo de Aventuras", 1995, "Comédia"),
            new Titulo("Procurando Nemo", 2003, "Comédia"),
            new Titulo("Shrek", 2001, "Comédia"),
            new Titulo("Divertida Mente", 2015, "Comédia"),
            new Titulo("Todo Poderoso", 2003, "Comédia"),
            new Titulo("Debi & Lóide", 1994, "Comédia"),
            new Titulo("Superbad: É Hoje", 2007, "Comédia"),
            new Titulo("A Proposta", 2009, "Comédia"),
            new Titulo("Ace Ventura: Um Detetive Diferente", 1994, "Comédia"),
            new Titulo("O Máskara", 1994, "Comédia"),
            new Titulo("Hora do Rush", 1998, "Comédia"),

            // Suspense
            new Titulo("O Silêncio dos Inocentes", 1991, "Suspense"),
            new Titulo("Seven: Os Sete Crimes Capitais", 1995, "Suspense"),
            new Titulo("Os Suspeitos", 2013, "Suspense"),
            new Titulo("Garota Exemplar", 2014, "Suspense"),
            new Titulo("O Sexto Sentido", 1999, "Suspense"),
            new Titulo("Ilha do Medo", 2010, "Suspense"),
            new Titulo("Amnésia", 2000, "Suspense"),
            new Titulo("Zodíaco", 2007, "Suspense"),
            new Titulo("O Talentoso Ripley", 1999, "Suspense"),
            new Titulo("Os Infiltrados", 2006, "Suspense"),
            new Titulo("Vidro", 2019, "Suspense"),
            new Titulo("A Garota no Trem", 2016, "Suspense"),
            new Titulo("O Show de Truman: O Show da Vida", 1998, "Suspense"),
            new Titulo("Estrada para Perdição", 2002, "Suspense"),
            new Titulo("Os Suspeitos de Sempre", 1995, "Suspense"),
            new Titulo("Prenda-me Se For Capaz", 2002, "Suspense"),
            new Titulo("Cisne Negro", 2010, "Suspense"),
            new Titulo("O Poço", 2019, "Suspense"),
            new Titulo("Corra que a Polícia Vem Aí!", 1988, "Comédia"),
            new Titulo("A Trama Fantasma", 2017, "Suspense"),
    };

    public static List<Filme> gerar(int quantidade) {
        List<Filme> filmes = new ArrayList<>(quantidade);
        for (int id = 1; id <= quantidade; id++) {
            int indice = (id - 1) % CATALOGO.length;
            int edicao = (id - 1) / CATALOGO.length; // 0 na primeira passagem pelo pool
            Titulo t = CATALOGO[indice];
            // Garante nomes únicos: repetições do pool ganham sufixo de edição.
            String nome = edicao == 0 ? t.nome() : t.nome() + " (ed. " + (edicao + 1) + ")";
            String sinopse = "Filme de " + t.categoria() + " de " + t.ano() + ".";
            filmes.add(new Filme(id, nome, sinopse, t.ano(), t.categoria()));
        }
        return filmes;
    }
}
