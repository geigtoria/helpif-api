
CREATE TABLE setores (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    sigla VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE categorias (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT NOT NULL,
    setor_id BIGINT NOT NULL,
    CONSTRAINT fk_categoria_setor
        FOREIGN KEY (setor_id) REFERENCES setores(id)
);

CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    matricula VARCHAR(255) NOT NULL,
    tipo VARCHAR(255) NOT NULL,
    setor_id BIGINT,
    CONSTRAINT fk_usuario_setor
        FOREIGN KEY (setor_id) REFERENCES setores(id)
);

CREATE TABLE chamados (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT NOT NULL,
    local VARCHAR(255) NOT NULL,
    patrimonio VARCHAR(50),
    prioridade VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    data_abertura TIMESTAMP NOT NULL,
    data_fechamento TIMESTAMP,
    solucao TEXT,
    solicitante_id BIGINT NOT NULL,
    categoria_id BIGINT NOT NULL,
    tecnico_id BIGINT,
    CONSTRAINT fk_chamado_solicitante
        FOREIGN KEY (solicitante_id) REFERENCES usuarios(id),
    CONSTRAINT fk_chamado_categoria
        FOREIGN KEY (categoria_id) REFERENCES categorias(id),
    CONSTRAINT fk_chamado_tecnico
        FOREIGN KEY (tecnico_id) REFERENCES usuarios(id)
);

CREATE TABLE interacoes (
    id BIGSERIAL PRIMARY KEY,
    texto TEXT NOT NULL,
    data_hora TIMESTAMP NOT NULL,
    chamado_id BIGINT NOT NULL,
    autor_id BIGINT NOT NULL,
    CONSTRAINT fk_interacao_chamado
        FOREIGN KEY (chamado_id) REFERENCES chamados(id),
    CONSTRAINT fk_interacao_autor
        FOREIGN KEY (autor_id) REFERENCES usuarios(id)
);
