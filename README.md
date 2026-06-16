# Prática Off-line 3 — EDII: Streaming Cliente-Servidor

Simulação single-JVM de uma plataforma de streaming. **Três clientes** compartilham um
**servidor**, exercitando cinco estruturas de dados implementadas do zero e comparando o número
de comparações em cada modalidade de busca. Não há rede real — cliente e servidor são grafos de
objetos no mesmo JVM.

## Arquitetura

| Camada | Estrutura | Papel |
|---|---|---|
| Cache do cliente | **Tabela hash + lista autoajustável (LRU)** (`cliente/CacheLRU`) | Busca local O(1); promove o item acessado a MRU; evicta o LRU quando cheio |
| Preferências do cliente | **Árvore splay** (`estruturas/ArvoreSplay`) | Último consumo vai à raiz → base para recomendações |
| Catálogo do servidor | **Lista ligada** (`estruturas/ListaLigada`) | Persistência simulada (busca sequencial O(n)) |
| Índice do servidor | **Tabela hash** (`estruturas/TabelaHash`) | Mapa id → nó da lista (acesso O(1)) |
| Popularidade do servidor | **Árvore splay** (`estruturas/ArvoreSplay`) | Títulos mais acessados pela base inteira migram para perto da raiz |
| Comunicação | **Árvore de Huffman** (`estruturas/ArvoreHuffman`) | Compressão sem perdas das mensagens trafegadas |

A contagem de comparações segue a convenção do projeto (ADR-004): cada estrutura incrementa um
contador por comparação de chave e o devolve em `ResultadoBusca<T>`.

## Requisitos

- **Java 16+** (usa `record` em `Filme`, `ResultadoBusca`, `ResultadoCompressao`)
- Sem bibliotecas externas; apenas o JDK padrão

## Compilar e executar

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java") && java -cp out Main
```

## Executar testes

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")

java -cp out estruturas.ListaLigadaTest
java -cp out estruturas.TabelaHashTest
java -cp out estruturas.ArvoreSplayTest
java -cp out estruturas.ArvoreHuffmanTest
java -cp out cliente.CacheLRUTest
java -cp out cliente.CacheClienteTest
java -cp out servidor.ServidorTest
java -cp out simulacao.GeradorDeFilmesTest
java -cp out simulacao.SimuladorTest
java -cp out MainTest
```

Cada teste imprime `N passed, 0 failed` e sai com código ≠ 0 em caso de falha.

## Cenário de execução

- **1000** filmes inseridos no servidor (lista ligada + tabela hash).
- **3 clientes** (Ana, Bruno, Carla), cada um com cache de **50** filmes pré-aquecido.
- As árvores splay (preferências do cliente e popularidade do servidor) iniciam **vazias**.
- Cada cliente faz **20 consultas**: 2 inválidas, 6 com cache hit, 6 sem indexação (varredura
  sequencial na lista), 6 com indexação (tabela hash).

## Saída esperada

O programa imprime em sequência:

1. Banner com nome do aluno
2. Inicialização (servidor + 3 caches pré-aquecidos)
3. Bateria de consultas por cliente (60 consultas, com contagem de comparações por linha)
4. Análise do cache LRU — 10 mais recentes (MRU→LRU) e ids removidos por cliente
5. Análise da árvore splay de preferências — raiz + 5 mais acessados por cliente
6. Análise da árvore splay de popularidade do servidor — mais popular + 10 mais próximos da raiz
7. Compressão de Huffman — tamanho original/comprimido e taxa por mensagem
8. Relatório comparativo (média de comparações por situação) + parágrafo de análise

A média de comparações evidencia: **cache hit (1) < com indexação (2) ≪ sem indexação (~500) <
inválidas (1000)**.

## Antes de submeter

- Conferir o nome do aluno no banner em `Simulador.imprimirBanner()`.
- Empacotar como **`seu-nome-pratica-off-3.zip`** antes do upload no SIGAA.
- O diretório `out/` é gerado localmente e não deve ir no ZIP (já está no `.gitignore`).
