package simulacao;

import cliente.CacheCliente;
import estruturas.ArvoreHuffman;
import estruturas.ResultadoBusca;
import modelo.Filme;
import servidor.Servidor;

import java.util.ArrayList;
import java.util.List;

/**
 * Orquestra a simulação de streaming da Prática Offline 3.
 *
 * <p>Três clientes compartilham um servidor. Cada cliente possui cache local
 * (tabela hash + lista autoajustável LRU) e árvore splay de preferências; o
 * servidor mantém lista ligada + tabela hash + árvore splay de popularidade
 * global. A camada de comunicação é demonstrada com compressão de Huffman.</p>
 */
public class Simulador {

    private static final int TOTAL_FILMES     = 1000;
    private static final int CAPACIDADE_CACHE = 50;

    private static final long PAUSA_SECAO = 7 * 1000L;
    private static final long PAUSA_FASE  = 1 * 1000L;

    /** Desligado pelos testes para execução instantânea. */
    boolean comDelays = true;

    Servidor servidor;
    List<CacheCliente> clientes;
    private List<Filme> filmes;

    // Acumuladores globais de comparações por situação
    long totalInvalidos;
    long totalCacheHit;
    long totalSemIndice;
    long totalComIndice;
    int qtdInvalidos;
    int qtdCacheHit;
    int qtdSemIndice;
    int qtdComIndice;

    // Configuração das consultas de cada cliente.
    // As 6 consultas de cache hit reacessam "favoritos" (filmes re-assistidos),
    // de modo que a árvore splay de preferências acumule frequências distintas.
    private static final Cliente[] PERFIS = {
            new Cliente("Ana",   1,   new int[]{0, 9999}, new int[]{12, 3, 12, 25, 12, 3},
                    new int[]{120, 250, 410, 560, 730, 900}, new int[]{130, 260, 420, 570, 740, 910}),
            new Cliente("Bruno", 100, new int[]{0, 9999}, new int[]{135, 110, 135, 122, 135, 110},
                    new int[]{160, 305, 455, 605, 805, 955}, new int[]{170, 315, 465, 615, 815, 965}),
            new Cliente("Carla", 300, new int[]{0, 9999}, new int[]{333, 311, 333, 322, 333, 333},
                    new int[]{60, 220, 480, 640, 770, 880}, new int[]{70, 230, 490, 650, 780, 890}),
    };

    private record Cliente(String nome, int inicioPreCarga, int[] invalidos,
                           int[] hits, int[] semIndice, int[] comIndice) {}

    public void executar() {
        imprimirBanner();
        inicializar();

        pausa(PAUSA_SECAO);
        System.out.println("=== Bateria de consultas (3 clientes x 20 = 60 consultas) ===\n");

        for (int i = 0; i < clientes.size(); i++) {
            executarConsultasDoCliente(clientes.get(i), PERFIS[i]);
            pausa(PAUSA_FASE);
        }

        pausa(PAUSA_SECAO);
        analisarCacheLRU();
        analisarPreferenciasCliente();
        analisarPopularidadeServidor();
        demonstrarHuffman();
        imprimirRelatorio();
    }

    void imprimirBanner() {
        System.out.println("============================================================");
        System.out.println(" Prática Offline 3 — Simulação de Streaming Cliente-Servidor");
        System.out.println(" Estrutura de Dados II — UFERSA");
        System.out.println(" Aluno: Edson");
        System.out.println("============================================================\n");
    }

    void inicializar() {
        servidor = new Servidor();
        filmes = GeradorDeFilmes.gerar(TOTAL_FILMES);

        System.out.println("Inicializando servidor (lista ligada + tabela hash)...");
        for (Filme f : filmes) servidor.inserir(f);
        System.out.println("  " + servidor.tamanho() + " filmes inseridos no catálogo do servidor.");

        System.out.println("Cadastrando 3 clientes e pré-aquecendo caches (50 filmes cada)...");
        clientes = new ArrayList<>();
        for (Cliente perfil : PERFIS) {
            CacheCliente cliente = new CacheCliente(perfil.nome(), CAPACIDADE_CACHE);
            for (int k = 0; k < CAPACIDADE_CACHE; k++) {
                cliente.inserir(filmePorId(perfil.inicioPreCarga() + k));
            }
            clientes.add(cliente);
            System.out.printf("  Cliente %-6s — %d filmes pré-carregados (ids %d..%d).%n",
                    perfil.nome(), cliente.tamanho(), perfil.inicioPreCarga(),
                    perfil.inicioPreCarga() + CAPACIDADE_CACHE - 1);
        }
        System.out.println("  Árvores splay (preferências e popularidade) iniciam vazias.\n");
    }

    void executarConsultasDoCliente(CacheCliente cliente, Cliente perfil) {
        System.out.println("------------------------------------------------------------");
        System.out.println(" Cliente " + perfil.nome() + " — 20 consultas");
        System.out.println("------------------------------------------------------------");

        System.out.println("--- 2 consultas inválidas (ids inexistentes) ---");
        for (int id : perfil.invalidos()) {
            ResultadoBusca<Filme> rCache = cliente.buscar(id);
            ResultadoBusca<Filme> rServidor = servidor.buscarSemIndice(id);
            int comp = rCache.comparacoes() + rServidor.comparacoes();
            totalInvalidos += comp; qtdInvalidos++;
            imprimirLinha("[inválida ]", id, comp, null);
        }

        System.out.println("--- 6 consultas com cache hit ---");
        for (int id : perfil.hits()) {
            ResultadoBusca<Filme> r = cliente.buscar(id);
            totalCacheHit += r.comparacoes(); qtdCacheHit++;
            if (r.encontrado()) cliente.registrarAcesso(r.valor());
            imprimirLinha("[hit      ]", id, r.comparacoes(), r.valor());
        }

        System.out.println("--- 6 consultas SEM indexação (busca sequencial na lista) ---");
        for (int id : perfil.semIndice()) {
            cliente.buscar(id); // cache miss
            ResultadoBusca<Filme> r = servidor.buscarSemIndice(id);
            totalSemIndice += r.comparacoes(); qtdSemIndice++;
            if (r.encontrado()) cliente.registrarAcesso(r.valor());
            imprimirLinha("[sem-índice]", id, r.comparacoes(), r.valor());
        }

        System.out.println("--- 6 consultas COM indexação (tabela hash do servidor) ---");
        for (int id : perfil.comIndice()) {
            cliente.buscar(id); // cache miss
            ResultadoBusca<Filme> r = servidor.buscarComIndice(id);
            totalComIndice += r.comparacoes(); qtdComIndice++;
            if (r.encontrado()) cliente.registrarAcesso(r.valor());
            imprimirLinha("[com-índice]", id, r.comparacoes(), r.valor());
        }
        System.out.println();
    }

    // ---- análises ------------------------------------------------------------

    void analisarCacheLRU() {
        System.out.println("=== Análise: cache LRU (cliente) ===");
        for (CacheCliente cliente : clientes) {
            System.out.println("Cliente " + cliente.nome() + ":");
            System.out.print("  10 mais recentes (MRU→LRU): ");
            System.out.println(idsDe(cliente.dezMaisRecentes()));
            System.out.println("  removidos pela política LRU: " + cliente.removidosDoCache());
        }
        System.out.println();
    }

    void analisarPreferenciasCliente() {
        System.out.println("=== Análise: árvore splay de preferências (cliente) ===");
        for (CacheCliente cliente : clientes) {
            Filme raiz = cliente.preferenciaAtual();
            System.out.printf("Cliente %s — raiz (preferência atual): %s%n",
                    cliente.nome(), raiz == null ? "(vazia)" : descrever(raiz));
            System.out.println("  5 mais acessados (por frequência): "
                    + cliente.cincoMaisAcessadosDescritos());
        }
        System.out.println();
    }

    void analisarPopularidadeServidor() {
        System.out.println("=== Análise: árvore splay de popularidade (servidor) ===");
        Filme popular = servidor.filmeMaisPopular();
        System.out.println("Conteúdo mais popular globalmente (raiz): "
                + (popular == null ? "(vazia)" : descrever(popular)));
        System.out.println("10 filmes mais próximos da raiz: "
                + idsDe(servidor.maisProximosDaRaiz(10)));
        System.out.println();
    }

    void demonstrarHuffman() {
        System.out.println("=== Análise: compressão de Huffman (comunicação) ===");
        String[] mensagens = {
                "LOGIN_OK",
                "GET /filme/505",
                "FILME:505|Matrix|1999",
                "RECOMENDACAO:202|Interestelar",
        };
        System.out.printf("%-32s %8s %8s %8s%n", "Mensagem", "orig(b)", "comp(b)", "taxa");
        System.out.println("-".repeat(60));
        for (String msg : mensagens) {
            ArvoreHuffman.ResultadoCompressao r = ArvoreHuffman.analisar(msg);
            // verificação de integridade (sem perdas)
            ArvoreHuffman arvore = ArvoreHuffman.construir(msg);
            boolean ok = arvore.descomprimir(arvore.comprimir(msg)).equals(msg);
            System.out.printf("%-32s %8d %8d %7.1f%% %s%n",
                    "\"" + msg + "\"", r.bitsOriginais(), r.bitsComprimidos(), r.taxa(),
                    ok ? "" : "[FALHA INTEGRIDADE]");
        }
        System.out.println();
    }

    void imprimirRelatorio() {
        System.out.println("=== Relatório comparativo (média de comparações por consulta) ===");
        System.out.printf("%-28s %18s%n", "Situação", "Média comparações");
        System.out.println("-".repeat(48));
        System.out.printf("%-28s %18.2f%n", "Inválidas", media(totalInvalidos, qtdInvalidos));
        System.out.printf("%-28s %18.2f%n", "Cache hit", media(totalCacheHit, qtdCacheHit));
        System.out.printf("%-28s %18.2f%n", "Sem indexação (lista)", media(totalSemIndice, qtdSemIndice));
        System.out.printf("%-28s %18.2f%n", "Com indexação (hash)", media(totalComIndice, qtdComIndice));
        System.out.println();
        imprimirAnalise();
    }

    private void imprimirAnalise() {
        System.out.println("=== Análise dos resultados ===");
        System.out.println(
            "A busca no cache local (tabela hash + LRU) e a busca indexada no servidor (tabela\n" +
            "hash) resolvem em poucas comparações — tempo médio O(1) — enquanto a busca sem\n" +
            "indexação percorre a lista ligada elemento a elemento (O(n)), exigindo centenas de\n" +
            "comparações em um catálogo de " + TOTAL_FILMES + " filmes. A política LRU mantém no cache os\n" +
            "itens mais recentemente usados e descarta os mais antigos, reduzindo requisições à\n" +
            "rede. A árvore splay de preferências leva o último consumo de cada cliente à raiz,\n" +
            "base para recomendações personalizadas; já a splay de popularidade do servidor\n" +
            "concentra perto da raiz os títulos mais acessados pela base inteira, revelando\n" +
            "tendências globais. Por fim, a árvore de Huffman comprime as mensagens trocadas na\n" +
            "rede sem perdas, reduzindo o volume de dados trafegados.");
        System.out.println();
    }

    // ---- utilitários ---------------------------------------------------------

    private Filme filmePorId(int id) {
        return filmes.get(id - 1); // ids são 1..TOTAL_FILMES, contíguos
    }

    private static double media(long total, int qtd) {
        return qtd == 0 ? 0.0 : total / (double) qtd;
    }

    private static List<Integer> idsDe(List<Filme> lista) {
        List<Integer> ids = new ArrayList<>();
        for (Filme f : lista) ids.add(f.id());
        return ids;
    }

    private static String descrever(Filme f) {
        return "id=" + f.id() + " \"" + f.nome() + "\" [" + f.categoria() + "]";
    }

    private void imprimirLinha(String tag, int id, int comparacoes, Filme filme) {
        String nome = filme != null ? "\"" + filme.nome() + "\"" : "NÃO ENCONTRADO";
        System.out.printf("  %s id=%-6d comparacoes=%-4d %s%n", tag, id, comparacoes, nome);
    }

    private void pausa(long ms) {
        if (!comDelays) return;
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
