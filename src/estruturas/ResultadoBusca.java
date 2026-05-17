package estruturas;

public record ResultadoBusca<T>(T valor, int comparacoes) {
    public boolean encontrado() { return valor != null; }

    public static <T> ResultadoBusca<T> vazio(int comparacoes) {
        return new ResultadoBusca<>(null, comparacoes);
    }
}
