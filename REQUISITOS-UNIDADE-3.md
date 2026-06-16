# Prática Offline 3 — Requisitos e Cobertura

Documento que mapeia **cada exigência do enunciado** da Prática Offline 3 (Estrutura de Dados II,
UFERSA) ao que foi implementado neste projeto, com referência aos arquivos e classes
correspondentes. Ao final há uma seção de **lacunas/observações** com pontos opcionais ou
parcialmente cobertos.

> Resumo: simulação single-JVM de uma plataforma de *streaming* onde **3 clientes** compartilham
> um **servidor**, exercitando 5 estruturas de dados implementadas do zero. Vale **70%** da 3ª
> unidade; entrega **18/06/2026**.

---

## Visão geral das camadas

| Camada | Estrutura exigida | Onde foi implementado |
|---|---|---|
| Cache do cliente | Tabela hash + lista autoajustável (LRU) | `cliente/CacheLRU.java` (+ `NoLRU.java`) |
| Preferências do cliente | Árvore splay | `estruturas/ArvoreSplay.java` (+ `NoSplay.java`) |
| Catálogo do servidor | Lista ligada | `estruturas/ListaLigada.java` (+ `NoLista.java`) |
| Índice do servidor | Tabela hash | `estruturas/TabelaHash.java` (+ `EntradaHash.java`) |
| Popularidade do servidor | Árvore splay | `estruturas/ArvoreSplay.java` (reutilizada) |
| Comunicação | Árvore de Huffman | `estruturas/ArvoreHuffman.java` (+ `NoHuffman.java`) |
| Orquestração | — | `simulacao/Simulador.java`, `Main.java` |
| Catálogo de dados | — | `simulacao/GeradorDeFilmes.java`, `modelo/Filme.java` |

A contagem de comparações segue a convenção do projeto (ADR-004): cada estrutura incrementa um
contador por comparação de chave e o devolve em `estruturas/ResultadoBusca.java`.

---

## 1. Lado do cliente (Frontend/App)

### 1.1 Cache local — tabela hash + lista autoajustável
**Exigência:** *"Estruturas: tabela hash + lista autoajustável. A tabela hash permite localizar
rapidamente um item armazenado, com tempo médio O(1), enquanto a lista autoajustável mantém a
ordem de utilização dos elementos."*

**Cobertura — `cliente/CacheLRU.java`:**
- Tabela hash interna (`NoLRU[] baldes`, encadeamento separado por `encadeamentoHash`) dá busca
  em tempo médio **O(1)** — método `buscar(int id)` percorre só o balde e conta as comparações.
- Lista **duplamente encadeada** (campos `cabeca`/`cauda`, `anterior`/`proximo` em `NoLRU`)
  mantém a ordem de uso: cabeça = mais recente (MRU), cauda = menos recente (LRU).
- A cada *hit*, `moverParaFrente(no)` promove o item a MRU (auto-ajuste).

### 1.2 Eviction — política LRU
**Exigência:** *"Quando o cache atinge sua capacidade máxima, a política LRU deve ser aplicada. O
item menos recentemente utilizado é removido."*

**Cobertura:** `CacheLRU.inserir(...)` chama `evictLRU()` quando `tamanho > capacidade`, removendo
o nó da **cauda** (LRU) da lista e do hash, e registrando o id em `removidos` para a análise final.

### 1.3 Preferências e recomendação — árvore splay
**Exigência:** *"Estrutura: árvore de difusão (splay). Registrar o histórico recente de consumo e
inferir preferências. Sempre que um filme é acessado, seu nó é movido para próximo da raiz."*

**Cobertura:**
- `estruturas/ArvoreSplay.java` faz *splay* top-down (zig / zig-zig / zig-zag) levando o último
  acesso à raiz (`acessar`/`inserir`).
- `cliente/CacheCliente.java` compõe o cache e a splay de preferências; `registrarAcesso(filme)`
  alimenta as duas. A raiz representa a **preferência atual**; o sistema de recomendação usa
  `preferenciaAtual()` (raiz) e `cincoMaisAcessados()`.

---

## 2. Lado do servidor (Backend/Database)

### 2.1 Catálogo — lista ligada
**Exigência:** *"Armazenamento físico: lista ligada. Simula a disposição sequencial/encadeada dos
dados. Por si só, a busca seria lenta (O(n))."*

**Cobertura:** `estruturas/ListaLigada.java` — `buscarSemIndice` no `servidor/Servidor.java` faz a
varredura sequencial O(n) e conta as comparações (≈ posição do item na lista).

### 2.2 Indexação — tabela hash
**Exigência:** *"Indexação: tabela hash. Mapa de endereços para a lista ligada. Cada entrada aponta
diretamente para uma referência do nó correspondente na lista ligada, reduzindo o custo para
O(1)."*

**Cobertura:** `estruturas/TabelaHash.java` mapeia `id → NoLista` (referência ao nó na lista).
`Servidor.inserir` registra `indice.inserir(f.id(), catalogo.getCauda())`; `buscarComIndice`
resolve em O(1) (acesso ao balde + cadeia curta).

### 2.3 Popularidade global — árvore splay
**Exigência:** *"Análise de popularidade e acessos: árvore splay. Registrar e reorganizar
dinamicamente a frequência de acesso aos filmes no sistema como um todo."*

**Cobertura:** `Servidor` mantém uma `ArvoreSplay popularidade`; **toda** busca bem-sucedida
(`buscarSemIndice`/`buscarComIndice`) chama `popularidade.inserir(filme)`. Os títulos mais
acessados pela base inteira migram para perto da raiz. Acessores: `filmeMaisPopular()` (raiz) e
`maisProximosDaRaiz(10)`.

---

## 3. Camada de comunicação — árvore de Huffman

**Exigência:** *"Estrutura: árvore de huffman. Comprimir e descomprimir as mensagens transmitidas
pela 'rede'. Compressão sem perdas, atribuindo códigos menores aos caracteres mais frequentes."*

**Cobertura:** `estruturas/ArvoreHuffman.java`:
- `construir(mensagem)` conta frequências e monta a árvore extraindo os dois menores nós de uma
  lista **manualmente** (sem `PriorityQueue`), atendendo ao requisito de implementar do zero.
- `comprimir` / `descomprimir` codificam e reconstroem a mensagem (sem perdas).
- `analisar(mensagem)` devolve tamanho original (bits), tamanho comprimido (bits) e taxa.
- Mensagens demonstradas espelham os exemplos do enunciado: `LOGIN_OK`, `GET /filme/505`,
  `FILME:505|Matrix|1999`, `RECOMENDACAO:202|Interestelar` (em `Simulador.demonstrarHuffman`).

---

## 4. Fluxo da simulação

**Exigência (resumo):** cliente verifica o cache (hash + LRU); em *hit*, exibe e promove a MRU; em
*miss*, requisita ao servidor (hash → lista, atualiza splay de popularidade); a resposta atualiza
cache, LRU e splay de preferências do cliente.

**Cobertura:** `Simulador.executarConsultasDoCliente(...)` implementa exatamente esse fluxo:
1. `cliente.buscar(id)` consulta o cache local (hit promove a MRU, conta comparações).
2. Em miss, `servidor.buscarSemIndice/ComIndice(id)` (atualiza a splay de popularidade).
3. Em retorno positivo, `cliente.registrarAcesso(filme)` insere no cache (com evicção LRU) e
   atualiza a splay de preferências.

---

## 5. Execução da simulação

| Item do enunciado | Cobertura |
|---|---|
| Inserir **1000 filmes** no servidor | `Simulador.TOTAL_FILMES = 1000`; loop `servidor.inserir` |
| Cada filme com **id, título, categoria, ano, sinopse** | `modelo/Filme.java` (`record Filme(id, nome, sinopse, ano, categoria)`) |
| Registros na **lista ligada** do servidor | `Servidor.catalogo` (`ListaLigada`) |
| Tabela hash apontando para os nós da lista | `Servidor.indice` (`TabelaHash` → `NoLista`) |
| Cache do cliente com capacidade **50** | `Simulador.CAPACIDADE_CACHE = 50` |
| **50 filmes pré-carregados** no cache | `inicializar()` pré-aquece 50 por cliente via `cliente.inserir` |
| Árvores splay (cliente e servidor) **iniciam vazias** | pré-carga usa só `inserir` no cache; splays só são alimentadas pelas consultas |
| **3 clientes** cadastrados | `PERFIS` (Ana, Bruno, Carla); `clientes` |
| **20 consultas por cliente** | `executarConsultasDoCliente`: 2 + 6 + 6 + 6 |
| 2 consultas **inválidas** (ids inexistentes) | ids `{0, 9999}` por cliente |
| 6 consultas com **cache hit** | ids dentro do range pré-aquecido (com reacessos a favoritos) |
| 6 consultas **sem indexação** (varredura na lista) | `servidor.buscarSemIndice` |
| 6 consultas **com indexação** (tabela hash) | `servidor.buscarComIndice` |
| **Contabilizar comparações** em cada situação | acumuladores `totalCacheHit/SemIndice/ComIndice/Invalidos` + relatório |

---

## 6. Análises exigidas ao final

| Análise pedida | Cobertura (`Simulador`) | Saída |
|---|---|---|
| **Cache LRU:** 10 mais recentes + removidos | `analisarCacheLRU()` usa `dezMaisRecentes()` e `removidosDoCache()` | lista MRU→LRU e ids evictados, por cliente |
| **Splay do cliente:** raiz + 5 mais acessados | `analisarPreferenciasCliente()` usa `preferenciaAtual()` e `cincoMaisAcessadosDescritos()` | raiz + ranking `id=N (Kx)` por cliente |
| **Splay do servidor:** 10 mais próximos da raiz + mais populares | `analisarPopularidadeServidor()` usa `maisProximosDaRaiz(10)` e `filmeMaisPopular()` | conteúdo mais popular + 10 próximos da raiz |
| **Huffman:** tamanho original, comprimido e taxa | `demonstrarHuffman()` usa `ArvoreHuffman.analisar` + verificação de integridade | tabela por mensagem |
| **Resultados comparativos** | `imprimirRelatorio()` + `imprimirAnalise()` | média de comparações por situação + parágrafo de análise |

Resultado típico (média de comparações por consulta): **cache hit = 1 < com indexação = 2 ≪ sem
indexação ≈ 517 < inválidas = 1000** — exatamente o contraste esperado entre busca local, busca
indexada e varredura linear.

---

## 7. Catálogo com nomes reais

Embora o enunciado não exija, o catálogo usa **títulos de filmes reais** (com ano e gênero
corretos), em `GeradorDeFilmes.java`: um pool curado de 120 filmes mapeados nas 6 categorias
(Ação, Ficção Científica, Drama, Terror, Comédia, Suspense), ciclado de forma determinística até
1000 — repetições recebem sufixo de edição (`Duna (ed. 3)`) para garantir **1000 nomes únicos**.

---

## 8. Avaliação e formato

| Requisito | Situação |
|---|---|
| Linguagem **Java** com orientação a objetos | OK — pacotes `cliente`, `servidor`, `estruturas`, `simulacao`, `modelo` |
| Estruturas de dados **implementadas do zero** | OK — nenhuma `Collection` da JDK como estrutura central (apenas `ArrayList` para listas de saída/análise; Huffman monta a árvore sem `PriorityQueue`) |
| Projeto **individual** | OK |
| Empacotar como `seu-nome-pratica-off-3.zip` | a fazer na entrega (ver `README.md`) |
| Vídeo de até 8 min (apresentação) | a gravar — os delays `Thread.sleep` do `Simulador` ajudam na narração |

### Como executar e testar
Ver `README.md`. Em resumo:
```bash
javac -encoding UTF-8 -d out $(find src -name "*.java") && java -cp out Main
```
A suíte de testes (manual, padrão `passed/failed`) cobre cada estrutura: `CacheLRUTest`,
`ArvoreSplayTest`, `ArvoreHuffmanTest`, `TabelaHashTest`, `ListaLigadaTest`, `CacheClienteTest`,
`ServidorTest`, `GeradorDeFilmesTest`, `SimuladorTest`, `MainTest` — todas verdes.

---

## 9. Lacunas e observações (pontos de atenção)

São itens **opcionais** ou **sugeridos** pelo enunciado, ainda não cobertos integralmente.
Listados para transparência e decisão.

1. **Autoajuste nas listas de colisão da tabela hash (servidor).**
   O enunciado diz: *"Caso opte pelo tratamento de colisões baseado em encadeamento, use algum
   método de autoajuste nas listas de colisão. Justifique."* A `TabelaHash` usa encadeamento
   (com inserção no início), mas **não** aplica *move-to-front* na cadeia ao buscar.
   *Justificativa atual:* a capacidade é um primo grande (2003) para 1000 itens, então as cadeias
   têm comprimento ≈ 1 e o autoajuste seria irrelevante na prática (os testes confirmam cadeia
   máxima ≤ 3). *Opção:* posso adicionar *move-to-front* na cadeia de colisão para atender o
   clause à risca.

2. **Interface de navegação por categorias.**
   O enunciado **sugere** (*"os filmes podem ser organizados por categorias"*) um menu por
   categoria. Hoje cada `Filme` tem `categoria` e ela aparece nas análises, mas não há um menu
   interativo de navegação por categoria. *Opção:* posso adicionar uma listagem por categoria na
   interface do cliente.

3. **Preenchimento do nome do aluno no banner.**
   `Simulador.imprimirBanner()` exibe "Aluno: Edson" — confirmar/ajustar antes de gravar e enviar.

> Se quiser, implemento os itens 1 e 2 — diga quais e eu sigo.
