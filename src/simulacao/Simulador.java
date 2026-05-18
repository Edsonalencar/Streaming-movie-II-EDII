package simulacao;

import cliente.CacheCliente;
import estruturas.ResultadoBusca;
import modelo.Filme;
import servidor.Servidor;

import java.util.List;

public class Simulador {

    private static final long SLEEP_MS           = 7*1000;
    private static final int[] IDS_INVALIDOS     = {0, 9999};
    private static final int[] IDS_CACHE_HIT     = {1, 5, 10, 20, 35, 50};
    private static final int[] IDS_SEM_INDICE    = {51, 120, 250, 400, 600, 850};
    private static final int[] IDS_COM_INDICE    = {55, 130, 260, 410, 610, 860, 2058};

    Servidor servidor;
    CacheCliente cache;
    private List<Filme> filmes;

    // Per-phase accumulators
    long totalInvalidos;
    long totalCacheHit;
    long totalSemIndice;
    long totalComIndice;

    public void executar() {
        inicializar();

        cache.imprimirEstado("=== Estado inicial do cache AVL ===");
        System.out.println();

        sleep();
        System.out.println("=== Bateria de 20 consultas ===");
        System.out.println();

        int sleepTime = 1*1000;

        consultasInvalidas();
        sleep(sleepTime);
        consultasCacheHit();
        sleep(sleepTime);
        consultasSemIndice();
        sleep(sleepTime);
        consultasComIndice();

        sleep();
        System.out.println();
        cache.imprimirEstado("=== Estado final do cache AVL ===");
        System.out.println();

        imprimirRelatorio();
    }

    void inicializar() {
        servidor = new Servidor();
        cache = new CacheCliente();
        filmes = GeradorDeFilmes.gerar(2100);

        System.out.println("Inicializando servidor...");
        for (Filme f : filmes) servidor.inserir(f);
        System.out.println("  " + servidor.tamanho() + " filmes inseridos no servidor.");

        System.out.println("Pré-carregando cache...");
        for (int i = 0; i < 50; i++) cache.inserir(filmes.get(i));
        System.out.println("  " + cache.tamanho() + " filmes pré-carregados no cache.");
        System.out.println();
    }

    void consultasInvalidas() {
        System.out.println("--- Consultas inválidas (2) ---");
        for (int id : IDS_INVALIDOS) {
            ResultadoBusca<Filme> rCache = cache.buscar(id);
            ResultadoBusca<Filme> rServidor = servidor.buscarSemIndice(id);
            ResultadoBusca<Filme> total = somar(rCache, rServidor);
            totalInvalidos += total.comparacoes();
            imprimirLinha("[inválida]", id, total);
        }
        System.out.println();
    }

    void consultasCacheHit() {
        System.out.println("--- Consultas com cache hit (6) ---");
        for (int id : IDS_CACHE_HIT) {
            ResultadoBusca<Filme> r = cache.buscar(id);
            totalCacheHit += r.comparacoes();
            imprimirLinha("[hit     ]", id, r);
        }
        System.out.println();
    }

    void consultasSemIndice() {
        System.out.println("--- Consultas sem indexação (6) ---");
        for (int id : IDS_SEM_INDICE) {
            ResultadoBusca<Filme> rCache = cache.buscar(id);
            ResultadoBusca<Filme> rServidor = servidor.buscarSemIndice(id);
            ResultadoBusca<Filme> total = somar(rCache, rServidor);
            totalSemIndice += total.comparacoes();
            imprimirLinha("[sem-idx ]", id, total);
        }
        System.out.println();
    }

    void consultasComIndice() {
        System.out.println("--- Consultas com indexação (7) ---");
        for (int id : IDS_COM_INDICE) {
            ResultadoBusca<Filme> rCache = cache.buscar(id);
            ResultadoBusca<Filme> rServidor = servidor.buscarComIndice(id);
            ResultadoBusca<Filme> total = somar(rCache, rServidor);
            totalComIndice += total.comparacoes();
            imprimirLinha("[com-idx ]", id, total);
            if (total.encontrado()) cache.inserir(total.valor());
        }
        System.out.println();
    }

    static ResultadoBusca<Filme> somar(ResultadoBusca<?> a, ResultadoBusca<Filme> b) {
        return new ResultadoBusca<>(b.valor(), a.comparacoes() + b.comparacoes());
    }

    private void imprimirLinha(String tag, int id, ResultadoBusca<Filme> r) {
        String nome = r.encontrado() ? r.valor().nome() : "NÃO ENCONTRADO";
        System.out.printf("%s id=%-6d comparacoes=%-5d nome=\"%s\"%n",
                tag, id, r.comparacoes(), nome);
    }

    private void sleep(long sleep) {
        try { Thread.sleep(sleep); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private void sleep() {
        try { Thread.sleep(SLEEP_MS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private void imprimirRelatorio() {
        System.out.println("=== Relatório comparativo ===");
        System.out.printf("%-30s %8s%n", "Fase", "Média comparações");
        System.out.println("-".repeat(42));
        System.out.printf("%-30s %8.2f%n", "Inválidas (2)",
                totalInvalidos / (double) IDS_INVALIDOS.length);
        System.out.printf("%-30s %8.2f%n", "Cache hit (6)",
                totalCacheHit / (double) IDS_CACHE_HIT.length);
        System.out.printf("%-30s %8.2f%n", "Sem indexação (6)",
                totalSemIndice / (double) IDS_SEM_INDICE.length);
        System.out.printf("%-30s %8.2f%n", "Com indexação (7)",
                totalComIndice / (double) IDS_COM_INDICE.length);
        System.out.println();
    }
}
