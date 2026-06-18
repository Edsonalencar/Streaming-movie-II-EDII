package estruturas.tabelaHash;

import estruturas.ResultadoBusca;
import estruturas.listaLigada.ListaLigada;
import estruturas.listaLigada.NoLista;
import modelo.Filme;

public class TabelaHashTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testBuscarEmTabelaVaziaRetorna1Comparacao();
        testBuscarAposUmaInsercaoRetorna2Comparacoes();
        testBuscarAposInsercaoRetornaRefNoCorreto();
        testMilInsercoesTodosEncontrados();
        testMilInsercoesMaxCadeiaAte3();
        testBuscarInexistenteRetornaNaoEncontrado();
        testColisaoEncadeamentoCorreto();

        System.out.println("\n=== TabelaHashTest: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }

    private static void testBuscarEmTabelaVaziaRetorna1Comparacao() {
        TabelaHash tabela = new TabelaHash();
        ResultadoBusca<NoLista> r = tabela.buscar(99);
        assertTrue("miss em tabela vazia — não encontrado", !r.encontrado());
        assertEquals("miss em tabela vazia — comparacoes = 1", 1, r.comparacoes());
    }

    private static void testBuscarAposUmaInsercaoRetorna2Comparacoes() {
        TabelaHash tabela = new TabelaHash();
        ListaLigada lista = new ListaLigada();
        lista.inserir(filme(42));
        NoLista no = lista.getCauda();
        tabela.inserir(42, no);

        ResultadoBusca<NoLista> r = tabela.buscar(42);
        assertTrue("hit após uma inserção — encontrado", r.encontrado());
        assertEquals("hit após uma inserção — comparacoes = 2", 2, r.comparacoes());
    }

    private static void testBuscarAposInsercaoRetornaRefNoCorreto() {
        TabelaHash tabela = new TabelaHash();
        ListaLigada lista = new ListaLigada();
        lista.inserir(filme(42));
        NoLista no = lista.getCauda();
        tabela.inserir(42, no);

        ResultadoBusca<NoLista> r = tabela.buscar(42);
        assertTrue("refNo é o mesmo nó inserido", r.valor() == no);
    }

    private static void testMilInsercoesTodosEncontrados() {
        TabelaHash tabela = new TabelaHash();
        ListaLigada lista = new ListaLigada();
        for (int i = 1; i <= 1000; i++) {
            lista.inserir(filme(i));
            tabela.inserir(i, lista.getCauda());
        }
        boolean todosEncontrados = true;
        for (int i = 1; i <= 1000; i++) {
            if (!tabela.buscar(i).encontrado()) {
                todosEncontrados = false;
                break;
            }
        }
        assertTrue("todos os 1000 IDs encontrados", todosEncontrados);
    }

    private static void testMilInsercoesMaxCadeiaAte3() {
        TabelaHash tabela = new TabelaHash();
        ListaLigada lista = new ListaLigada();
        for (int i = 1; i <= 1000; i++) {
            lista.inserir(filme(i));
            tabela.inserir(i, lista.getCauda());
        }
        // comparacoes = 1 (bucket) + chain_length; chain_length <= 3 means comparacoes <= 4
        int maxComparacoes = 0;
        for (int i = 1; i <= 1000; i++) {
            int c = tabela.buscar(i).comparacoes();
            if (c > maxComparacoes) maxComparacoes = c;
        }
        assertTrue("max comprimento de cadeia ≤ 3 (comparacoes ≤ 4)", maxComparacoes <= 4);
    }

    private static void testBuscarInexistenteRetornaNaoEncontrado() {
        TabelaHash tabela = new TabelaHash();
        ListaLigada lista = new ListaLigada();
        lista.inserir(filme(1));
        tabela.inserir(1, lista.getCauda());

        ResultadoBusca<NoLista> r = tabela.buscar(9999);
        assertTrue("ID inexistente — não encontrado", !r.encontrado());
        assertTrue("ID inexistente — comparacoes >= 1", r.comparacoes() >= 1);
    }

    private static void testColisaoEncadeamentoCorreto() {
        // IDs that collide: id and id+2003 map to the same bucket
        TabelaHash tabela = new TabelaHash();
        ListaLigada lista = new ListaLigada();
        lista.inserir(filme(1));
        NoLista no1 = lista.getCauda();
        lista.inserir(filme(2004)); // 2004 mod 2003 = 1, collides with id=1
        NoLista no2004 = lista.getCauda();

        tabela.inserir(1, no1);
        tabela.inserir(2004, no2004); // prepended, so chain: [2004] -> [1]

        ResultadoBusca<NoLista> r1 = tabela.buscar(1);
        assertTrue("colisão — id=1 encontrado", r1.encontrado());
        assertEquals("colisão — id=1 comparacoes = 3 (bucket + miss 2004 + hit 1)", 3, r1.comparacoes());

        ResultadoBusca<NoLista> r2004 = tabela.buscar(2004);
        assertTrue("colisão — id=2004 encontrado", r2004.encontrado());
        assertEquals("colisão — id=2004 comparacoes = 2 (bucket + hit 2004)", 2, r2004.comparacoes());
    }

    private static Filme filme(int id) {
        return new Filme(id, "Filme " + id, "Sinopse", 2020, "Ação");
    }

    private static void assertEquals(String label, int expected, int actual) {
        if (expected == actual) {
            System.out.println("  PASS: " + label + " [" + actual + "]");
            passed++;
        } else {
            System.out.println("  FAIL: " + label + " — expected " + expected + " got " + actual);
            failed++;
        }
    }

    private static void assertTrue(String label, boolean condition) {
        if (condition) {
            System.out.println("  PASS: " + label);
            passed++;
        } else {
            System.out.println("  FAIL: " + label);
            failed++;
        }
    }
}
