# Prática Off-line 2 — EDII: Cache e Indexação em Streaming

Simulação single-JVM de um cliente com cache AVL + servidor com lista ligada e tabela hash,
comparando o número de comparações nas três modalidades de busca (cache hit, varredura linear,
busca com índice). Não há rede real — cliente e servidor são grafos de objetos no mesmo JVM.

## Requisitos

- **Java 16+** (uses `record` for `Filme`, `ResultadoBusca`)
- No external libraries; pure standard JDK

## Compilar e executar

```bash
mkdir -p out
javac -d out $(find src -name "*.java") && java -cp out Main
```

### Alternativa explícita (sem `find`)

```bash
mkdir -p out
javac -d out \
  src/Main.java \
  src/modelo/Filme.java \
  src/estruturas/ResultadoBusca.java \
  src/estruturas/NoLista.java \
  src/estruturas/ListaLigada.java \
  src/estruturas/EntradaHash.java \
  src/estruturas/TabelaHash.java \
  src/estruturas/NoAVL.java \
  src/estruturas/ArvoreAVL.java \
  src/servidor/Servidor.java \
  src/cliente/CacheCliente.java \
  src/simulacao/GeradorDeFilmes.java \
  src/simulacao/Simulador.java \
  src/MainTest.java \
  src/simulacao/SimuladorTest.java
java -cp out Main
```

## Fallback para Java 11

`record` não existe antes do Java 16. Para compilar em Java 11, substitua cada `record` por
uma `final class` equivalente — por exemplo:

```java
// antes (Java 16+)
public record Filme(int id, String nome, String sinopse, int ano, String categoria) {}

// depois (Java 11)
public final class Filme {
    public final int id;
    public final String nome;
    public final String sinopse;
    public final int ano;
    public final String categoria;

    public Filme(int id, String nome, String sinopse, int ano, String categoria) {
        this.id = id; this.nome = nome; this.sinopse = sinopse;
        this.ano = ano; this.categoria = categoria;
    }
}
```

Repita o procedimento para `ResultadoBusca<T>`. Todo o restante do projeto é Java 11 compatível.

## Executar testes

```bash
mkdir -p out
javac -d out $(find src -name "*.java")

java -cp out MainTest
java -cp out simulacao.SimuladorTest
java -cp out simulacao.GeradorDeFilmesTest
java -cp out cliente.CacheClienteTest
java -cp out servidor.ServidorTest
java -cp out estruturas.ArvoreAVLTest
java -cp out estruturas.TabelaHashTest
java -cp out estruturas.ListaLigadaTest
```

## Saída esperada

O programa imprime oito seções em sequência:

1. Banner com nome do estudante e data
2. Estado inicial do cache AVL (50 nós pré-aquecidos)
3. Fase de consultas inválidas (2 queries — IDs ausentes no catálogo)
4. Fase de consultas com cache hit (6 queries)
5. Fase de consultas sem índice (6 queries — varredura linear)
6. Fase de consultas com índice hash (6 queries)
7. Estado final do cache AVL (evicção LRU visível)
8. Relatório comparativo e parágrafo de análise em português

## Antes de submeter

- **Preencher o nome do estudante** no banner dentro de `Simulador.imprimirBanner()`.
- Empacotar o projeto como **`seu-nome-pratica-off-2.zip`** antes de fazer upload no SIGAA.
- O arquivo `out/` é gerado localmente e não deve ser incluído no ZIP (já está no `.gitignore`).
