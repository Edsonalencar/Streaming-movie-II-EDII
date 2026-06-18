package estruturas;

import modelo.Filme;

import java.util.List;

// Resultado de uma busca por nome: pode casar com vários títulos de uma vez.
public record ResultadoBuscaNome(List<Filme> resultados, int comparacoes) {
    public boolean vazio() { return resultados.isEmpty(); }
    public int quantidade() { return resultados.size(); }
}