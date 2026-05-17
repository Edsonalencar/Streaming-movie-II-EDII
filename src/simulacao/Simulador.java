package simulacao;

import cliente.CacheCliente;
import estruturas.ResultadoBusca;
import modelo.Filme;
import servidor.Servidor;

import java.util.List;

public class Simulador {

    private static final int[] IDS_INVALIDOS     = {0, 9999};
    private static final int[] IDS_CACHE_HIT     = {1, 5, 10, 20, 35, 50};
    private static final int[] IDS_SEM_INDICE    = {51, 120, 250, 400, 600, 850};
    private static final int[] IDS_COM_INDICE    = {55, 130, 260, 410, 610, 860};

    Servidor servidor;
    CacheCliente cache;
    private List<Filme> filmes;

    // Per-phase accumulators
    long totalInvalidos;
    long totalCacheHit;
    long totalSemIndice;
    long totalComIndice;

    public void executar() {
        imprimirBanner();
        inicializar();
        cache.imprimirEstado("=== Estado inicial do cache AVL ===");
        System.out.println();

        System.out.println("=== Bateria de 20 consultas ===");
        System.out.println();

        consultasInvalidas();
        consultasCacheHit();
        consultasSemIndice();
        consultasComIndice();

        System.out.println();
        cache.imprimirEstado("=== Estado final do cache AVL ===");
        System.out.println();

        imprimirRelatorio();
        imprimirAnalise();
    }

    private void imprimirBanner() {
        System.out.println("============================================================");
        System.out.println("  Projeto: Simulação de Streaming com Cache e Indexação");
        System.out.println("  Disciplina: Estruturas de Dados II — UFERSA 2026.1");
        System.out.println("  Aluno: [SEU NOME AQUI]");
        System.out.println("============================================================");
        System.out.println();
    }

    void inicializar() {
        servidor = new Servidor();
        cache = new CacheCliente();
        filmes = GeradorDeFilmes.gerar(1000);

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
        System.out.println("--- Consultas com indexação (6) ---");
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
        System.out.printf("%-30s %8.2f%n", "Com indexação (6)",
                totalComIndice / (double) IDS_COM_INDICE.length);
        System.out.println();
    }

    private void imprimirAnalise() {
        System.out.println("=== Análise ===");
        System.out.println(
            "A simulação evidencia três classes de complexidade distintas. O cache AVL (árvore\n" +
            "balanceada com no máximo 50 nós) localiza cada filme em O(log n) comparações —\n" +
            "tipicamente ≤ 7 inspeções de chave —, pois a busca percorre no máximo a altura da\n" +
            "árvore. A busca sem índice percorre a lista ligada sequencialmente: a cada consulta,\n" +
            "o número de comparações varia de 1 até N (1000), com média em torno de N/2 ≈ 500,\n" +
            "confirmando o comportamento O(n). A busca com índice usa a tabela hash (encadeamento\n" +
            "separado, capacidade 2003): o probe do bucket mais o percurso da cadeia resultam em\n" +
            "poucas comparações — tipicamente ≤ 5 —, aproximando-se de O(1) amortizado. O estado\n" +
            "final do cache AVL mostra IDs distintos do estado inicial, evidenciando que o\n" +
            "mecanismo de eviction LRU substituiu as entradas menos recentemente acessadas por\n" +
            "filmes buscados via índice durante a bateria. Conta-se uma comparação por inspeção\n" +
            "de chave: cada nó visitado na AVL, cada nó na lista ligada, e cada elo da cadeia do\n" +
            "bucket; o probe inicial da tabela hash também conta."
        );
        System.out.println();
    }
}
