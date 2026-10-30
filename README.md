# HelpIF API

API REST para gerenciar chamados de suporte do IFRN, desenvolvida para a disciplina de Desenvolvimento de Sistemas Corporativos.

O solicitante abre um chamado com o problema, o local e a categoria. Um técnico do setor responsável assume o atendimento, registra interações e informa a solução ao encerrar.

## Tecnologias

- Java 21 e Spring Boot 4
- Maven, Spring Web, Spring Data JPA e Bean Validation
- PostgreSQL 16 e Flyway
- Docker e Docker Compose
- JUnit 5 e Mockito nos testes

## Executar com Docker

É necessário ter o Docker instalado e em execução, com o Docker Compose disponível. No Windows, pode ser usado o Docker Desktop com containers Linux.

```bash
git clone https://github.com/geigtoria/helpif-api.git
cd helpif-api
docker compose up
```

O Compose constrói a imagem da API e inicia o PostgreSQL. Depois que o banco passa na verificação de disponibilidade, a API inicia, aplica as migrations pendentes e valida o esquema com o Hibernate.

A API atende em `http://localhost:8080`. Para conferir, acesse [a listagem de setores](http://localhost:8080/setores). A rota `/` não possui uma página inicial.

A primeira construção pode demorar para baixar as imagens e dependências. O Dockerfile usa duas etapas: Maven com JDK 21 para compilar e JRE 21 para executar o JAR. Os testes não são executados durante a construção da imagem, pois os testes de integração precisam de um banco em execução; veja a seção de testes abaixo.

Outros comandos úteis:

```bash
# Executar em segundo plano
docker compose up -d

# Reconstruir a imagem após alterar o código
docker compose up --build -d

# Acompanhar os logs da API
docker compose logs -f api

# Consultar os containers
docker compose ps

# Parar e remover os containers, mantendo os dados
docker compose down
```

Os dados ficam no volume `postgres_data` e são mantidos ao parar ou recriar os containers.

## Configuração

O projeto funciona com os valores locais definidos no Compose, sem criar arquivos adicionais. Para personalizar a configuração, copie `.env.example` para `.env` e altere os valores:

```powershell
Copy-Item .env.example .env
```

| Variável | Valor local padrão | Uso |
| --- | --- | --- |
| `DB_NAME` | `helpif` | Nome do banco |
| `DB_USERNAME` | `helpif` | Usuário do banco |
| `DB_PASSWORD` | `helpif_dev` | Senha para desenvolvimento local |
| `DB_PORT` | `5432` | Porta do PostgreSQL na máquina |
| `API_PORT` | `8080` | Porta da API na máquina |

A API recebe `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` por variáveis de ambiente. Dentro do Compose, o endereço do banco usa o serviço `postgres`, em vez de `localhost`. O `application.properties` não contém senha fixa e o arquivo `.env` não é versionado.

As credenciais locais do Compose servem para desenvolvimento. Alterar usuário, senha ou nome do banco no `.env` não altera automaticamente um banco já inicializado no volume: as variáveis de inicialização do PostgreSQL são usadas na criação inicial do banco. Mantenha os valores compatíveis com o volume existente.

## Executar com Java na máquina

Além do Docker para o banco, é necessário ter o JDK 21 instalado. No PowerShell:

```powershell
docker compose up -d postgres
$env:DB_URL = "jdbc:postgresql://localhost:5432/helpif"
$env:DB_USERNAME = "helpif"
$env:DB_PASSWORD = "helpif_dev"
.\mvnw.cmd spring-boot:run
```

Se tiver iniciado a API pelo Compose, pare-a antes com `docker compose stop api` para liberar a porta 8080. Se personalizou as credenciais ou a porta do banco, ajuste também as variáveis acima. O Maven Wrapper baixa a versão necessária do Maven; não é necessário instalá-lo separadamente.

## Testes

Os testes unitários verificam os serviços de usuários, setores e chamados. Os testes de integração verificam a inicialização da aplicação e consultas JPA no PostgreSQL, incluindo filtros opcionais e períodos de abertura. Os dados criados pelos testes de consultas são revertidos ao final da transação.

Para executar toda a suíte no PowerShell, inicie o banco, aguarde ficar disponível e informe as variáveis de conexão:

```powershell
docker compose up -d postgres
$env:DB_URL = "jdbc:postgresql://localhost:5432/helpif"
$env:DB_USERNAME = "helpif"
$env:DB_PASSWORD = "helpif_dev"
.\mvnw.cmd test
```

Somente os testes unitários, sem banco:

```powershell
.\mvnw.cmd "-Dtest=UsuarioServiceTest,SetorServiceTest,ChamadoServiceTest" test
```

## Organização

O código fica em `src/main/java/br/edu/ifrn/helpif_api`:

- `controllers`: recebem as requisições e devolvem as respostas HTTP.
- `services`: concentram as regras de negócio e as transações.
- `repositories`: acessam o banco com Spring Data JPA.
- `entities`: entidades, relacionamentos e enums.
- `dtos`: dados de entrada e saída da API.
- `exceptions`: exceções e tratamento padronizado de erros.

As migrations ficam em `src/main/resources/db/migration`.

## Endpoints

| Método | Rota | Operação |
| --- | --- | --- |
| POST | `/setores` | Criar setor |
| GET | `/setores` | Listar setores |
| GET | `/setores/{id}` | Buscar setor |
| PUT | `/setores/{id}` | Atualizar setor |
| DELETE | `/setores/{id}` | Excluir setor sem chamados vinculados |
| GET | `/setores/{id}/categorias` | Listar categorias de um setor |
| POST | `/categorias` | Criar categoria |
| GET | `/categorias` | Listar categorias |
| GET | `/categorias/{id}` | Buscar categoria |
| PUT | `/categorias/{id}` | Atualizar categoria |
| DELETE | `/categorias/{id}` | Excluir categoria sem chamados vinculados |
| POST | `/usuarios` | Criar usuário |
| GET | `/usuarios` | Listar usuários |
| GET | `/usuarios/{id}` | Buscar usuário |
| PUT | `/usuarios/{id}` | Atualizar usuário |
| GET | `/usuarios/{id}/chamados` | Listar chamados abertos pelo solicitante ou atribuídos ao técnico |
| POST | `/chamados` | Abrir chamado |
| GET | `/chamados` | Listar chamados |
| GET | `/chamados/{id}` | Detalhar chamado com interações |
| PATCH | `/chamados/{id}/assumir` | Assumir atendimento |
| PATCH | `/chamados/{id}/resolver` | Resolver com uma solução |
| PATCH | `/chamados/{id}/cancelar` | Cancelar chamado aberto |
| POST | `/chamados/{id}/interacoes` | Registrar interação |

As listagens são paginadas, com `page` começando em zero e `size` padrão de 10, limitado a 50. A resposta contém `content`, `number`, `size`, `totalElements` e `totalPages`. Use `sort=campo,asc` ou `sort=campo,desc` para ordenar as listagens, inclusive setores. Em chamados, a ordenação padrão é por data de abertura decrescente.

| Listagem | Filtros opcionais |
| --- | --- |
| `/setores` | `nome` |
| `/categorias` | `nome`, `setorId` |
| `/usuarios` | `nome`, `tipo`, `setorId` |
| `/chamados` | `status`, `prioridade`, `setorId`, `categoriaId`, `solicitanteId`, `tecnicoId`, `dataInicio`, `dataFim` |
| `/usuarios/{id}/chamados` | `status` |

Filtros de nome ignoram diferenças entre maiúsculas e minúsculas. Os filtros podem ser combinados. As datas usam o formato ISO com horário, por exemplo `2026-10-30T08:00:00`, com limites inclusivos.

## Exemplos de requisição

A coleção completa está em [`requisicoes.http`](requisicoes.http), com todos os endpoints, fluxos de atendimento e cancelamento e exemplos de erros. Execute os blocos separadamente com um cliente compatível com arquivos `.http`, como a extensão REST Client do VS Code. Após cada cadastro ou abertura de chamado, copie o id retornado para a variável indicada no início do arquivo. As variáveis de novos registros começam em zero para evitar alterações acidentais nos dados iniciais.

Os exemplos abaixo usam os dados de uma instalação nova: solicitante Ana com id 1, técnico Carlos da COTIC com id 3 e categoria Computadores com id 1. Copie o id retornado ao abrir o chamado e substitua `{id}` nos próximos exemplos.

```http
POST http://localhost:8080/chamados
Content-Type: application/json

{
  "titulo": "Projetor sem imagem",
  "descricao": "O projetor liga, mas não exibe a tela do computador.",
  "local": "Laboratório 1",
  "solicitanteId": 1,
  "categoriaId": 1
}
```

A resposta é `201 Created`. O chamado recebe status `ABERTO`, data de abertura preenchida pelo sistema e prioridade `MEDIA` quando não informada.

```http
GET http://localhost:8080/chamados?status=ABERTO&setorId=1&prioridade=MEDIA&page=0&size=10&sort=dataAbertura,desc
```

```http
PATCH http://localhost:8080/chamados/{id}/assumir
Content-Type: application/json

{"tecnicoId": 3}
```

```http
POST http://localhost:8080/chamados/{id}/interacoes
Content-Type: application/json

{"autorId": 3, "texto": "Realizando testes no cabo HDMI."}
```

```http
PATCH http://localhost:8080/chamados/{id}/resolver
Content-Type: application/json

{"tecnicoId": 3, "solucao": "Cabo HDMI substituído e imagem normalizada."}
```

As transições permitidas são `ABERTO -> EM_ATENDIMENTO -> RESOLVIDO` e `ABERTO -> CANCELADO`. Apenas um técnico do setor da categoria pode assumir o chamado; somente o técnico atribuído pode resolver. Apenas o próprio solicitante pode cancelar, enquanto o chamado estiver aberto. Somente os participantes podem registrar interações e chamados encerrados não aceitam novas alterações ou interações.

O projeto não possui autenticação: o usuário que executa uma ação é informado no corpo da requisição.

## Respostas de erro

- `400`: dados inválidos ou campos obrigatórios ausentes.
- `404`: recurso inexistente.
- `409`: conflito de cadastro ou violação de regra de negócio.

O tratamento centralizado retorna erros neste formato:

```json
{
  "timestamp": "2026-10-30T17:00:00",
  "status": 409,
  "mensagem": "Ação não permitida para o status atual do chamado",
  "caminho": "/chamados/1/assumir"
}
```

## Flyway e versionamento do banco

A migration `V1__criar_tabelas.sql` cria as tabelas e seus relacionamentos. A `V2__inserir_dados_iniciais.sql` insere dois setores, quatro categorias e quatro usuários.

O Flyway mantém a tabela `flyway_schema_history`, com informações como versão, descrição, checksum e resultado das migrations aplicadas. Na inicialização, ele compara os arquivos com esse histórico e executa as versões pendentes em ordem.

Se uma migration versionada já aplicada for alterada e seu checksum mudar, a validação falha e impede a inicialização normal da aplicação. Por isso, uma correção deve entrar em uma nova migration, como `V3__descricao_da_mudanca.sql`, preservando as versões anteriores.

O Hibernate usa `spring.jpa.hibernate.ddl-auto=validate`: ele confere se as entidades são compatíveis com as tabelas, mas não altera o esquema. O `update` não é usado porque mudanças automáticas não produzem o mesmo histórico explícito e revisável dos scripts de migração, dificultando o controle das alterações entre ambientes.

Referências: [histórico de migrations](https://documentation.red-gate.com/flyway/flyway-concepts/migrations/flyway-schema-history-table), [validação do Flyway](https://documentation.red-gate.com/fd/validate-277578898.html) e [ordem de inicialização no Compose](https://docs.docker.com/compose/how-tos/startup-order/).
