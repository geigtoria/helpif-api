
INSERT INTO setores (nome, sigla) VALUES
('Coordenação de Tecnologia da Informação', 'COTIC'),
('Setor de Manutenção', 'MANUT');

INSERT INTO categorias (nome, descricao, setor_id) VALUES
('Computadores', 'Problemas em computadores e periféricos', 1),
('Rede e Internet', 'Problemas de conectividade e Wi-Fi', 1),
('Instalações Elétricas', 'Problemas na rede elétrica', 2),
('Infraestrutura', 'Problemas estruturais e prediais', 2);

INSERT INTO usuarios (nome, email, matricula, tipo, setor_id) VALUES
('Ana Silva', 'ana.silva@example.com', '20260001', 'SOLICITANTE', NULL),
('Pedro Santos', 'pedro.santos@example.com', '20260002', 'SOLICITANTE', NULL),
('Carlos Oliveira', 'carlos.oliveira@example.com', '20260003', 'TECNICO', 1),
('Mariana Costa', 'mariana.costa@example.com', '20260004', 'TECNICO', 2);
