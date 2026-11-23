USE brecho;
GO

CREATE TABLE pessoas (
    cpf VARCHAR(11) NOT NULL,
    nome VARCHAR(50) NOT NULL,
    telefone VARCHAR(11) NULL,
    CONSTRAINT pk_pessoas PRIMARY KEY (cpf)
);
GO

CREATE TABLE clientes (
    pessoa_cpf VARCHAR(11) NOT NULL,
    ativo BIT NOT NULL CONSTRAINT df_clientes_ativo DEFAULT 1,
    CONSTRAINT pk_clientes PRIMARY KEY (pessoa_cpf),
    CONSTRAINT fk_pessoas_clientes FOREIGN KEY (pessoa_cpf) REFERENCES pessoas(cpf)
);
GO

CREATE TABLE funcionarios (
    pessoa_cpf VARCHAR(11) NOT NULL,
    salario DECIMAL(8,2) NOT NULL CONSTRAINT df_funcionarios_salario DEFAULT 0,
    perfil INT NOT NULL CONSTRAINT df_funcionarios_perfil DEFAULT 1,
    email VARCHAR(254) NULL,
    senha VARCHAR(255) NOT NULL,
    ativo BIT NOT NULL CONSTRAINT df_funcionarios_ativo DEFAULT 1,
    CONSTRAINT pk_funcionarios PRIMARY KEY (pessoa_cpf),
    CONSTRAINT fk_pessoas_funcionarios FOREIGN KEY (pessoa_cpf) REFERENCES pessoas(cpf),
    CONSTRAINT ck_funcionarios_salario CHECK (salario >= 0),
    CONSTRAINT ck_funcionarios_perfil CHECK (perfil IN (0,1)),
    CONSTRAINT ck_funcionarios_senha CHECK (LEN(LTRIM(RTRIM(senha))) > 0),
    CONSTRAINT uq_funcionarios_email UNIQUE (email)
);
GO

CREATE TABLE marcas (
    cod_marca INT IDENTITY(1,1) NOT NULL,
    nome VARCHAR(50) NOT NULL,
    ativo BIT NOT NULL CONSTRAINT df_marcas_ativo DEFAULT 1,
    CONSTRAINT pk_marcas PRIMARY KEY (cod_marca),
    CONSTRAINT uq_marcas_nome UNIQUE (nome)
);
GO

CREATE TABLE categorias (
    cod_categorias INT IDENTITY(1,1) NOT NULL,
    nome VARCHAR(50) NOT NULL,
    ativo BIT NOT NULL CONSTRAINT df_categorias_ativo DEFAULT 1,
    CONSTRAINT pk_categorias PRIMARY KEY (cod_categorias),
    CONSTRAINT uq_categorias_nome UNIQUE (nome)
);
GO

CREATE TABLE metodo_pagamento (
    cod_pagamento INT IDENTITY(1,1) NOT NULL,
    desc_pagamento VARCHAR(50) NOT NULL,
    is_active BIT NOT NULL CONSTRAINT df_metodo_pagamento_active DEFAULT 1,
    CONSTRAINT pk_pagamento PRIMARY KEY (cod_pagamento),
    CONSTRAINT uq_metodo_pagamento_desc UNIQUE (desc_pagamento)
);
GO

CREATE TABLE endereco (
    cod_end INT IDENTITY(1,1) NOT NULL,
    cpf_pessoa VARCHAR(11) NULL,
    logradouro VARCHAR(50) NULL,
    complemento VARCHAR(50) NULL,
    numero VARCHAR(50) NULL,
    bairro VARCHAR(50) NULL,
    cidade VARCHAR(50) NULL,
    cep VARCHAR(9) NULL,
    uf VARCHAR(2) NULL,
    ativo BIT NOT NULL CONSTRAINT df_endereco_ativo DEFAULT 1,
    CONSTRAINT pk_endereco PRIMARY KEY (cod_end),
    CONSTRAINT fk_endereco_pessoa FOREIGN KEY (cpf_pessoa) REFERENCES pessoas(cpf)
);
GO

CREATE TABLE vendas (
    cod_venda INT IDENTITY(1,1) NOT NULL,
    data_venda DATETIME NOT NULL CONSTRAINT df_vendas_data DEFAULT GETDATE(),
    cpf_cliente VARCHAR(11) NOT NULL,
    cpf_funcionario VARCHAR(11) NOT NULL,
    endereco_entrega INT NULL,
    valor_total DECIMAL(10,2) NULL CONSTRAINT df_vendas_total DEFAULT 0,
    status INT NOT NULL CONSTRAINT df_vendas_status DEFAULT 0,
    CONSTRAINT pk_venda PRIMARY KEY (cod_venda),
    CONSTRAINT fk_venda_cliente FOREIGN KEY (cpf_cliente) REFERENCES clientes(pessoa_cpf),
    CONSTRAINT fk_venda_funcionario FOREIGN KEY (cpf_funcionario) REFERENCES funcionarios(pessoa_cpf),
    CONSTRAINT fk_venda_endereco FOREIGN KEY (endereco_entrega) REFERENCES endereco(cod_end),
    CONSTRAINT ck_vendas_status CHECK (status IN (0,1,2)),
    CONSTRAINT ck_vendas_total CHECK (valor_total IS NULL OR valor_total >= 0)
);
GO

CREATE TABLE produtos (
    cod_prod INT IDENTITY(1,1) NOT NULL,
    descricao VARCHAR(50) NOT NULL,
    tamanho VARCHAR(3) NOT NULL,
    cod_marca INT NULL,
    cod_categoria INT NOT NULL,
    cod_venda INT NULL,
    preco DECIMAL(10,2) NOT NULL CONSTRAINT df_produtos_preco DEFAULT 0,
    is_selled BIT NOT NULL CONSTRAINT df_produtos_vendido DEFAULT 0,
    ativo BIT NOT NULL CONSTRAINT df_produtos_ativo DEFAULT 1,
    CONSTRAINT pk_produtos PRIMARY KEY (cod_prod),
    CONSTRAINT fk_prod_marca FOREIGN KEY (cod_marca) REFERENCES marcas(cod_marca),
    CONSTRAINT fk_prod_categoria FOREIGN KEY (cod_categoria) REFERENCES categorias(cod_categorias),
    CONSTRAINT fk_prod_venda FOREIGN KEY (cod_venda) REFERENCES vendas(cod_venda),
    CONSTRAINT ck_produtos_preco CHECK (preco >= 0)
);
GO

CREATE TABLE venda_pagamento (
    cod_pagamento INT NOT NULL,
    cod_venda INT NOT NULL,
    valor_pago DECIMAL(10,2) NOT NULL CONSTRAINT df_venda_pagamento_valor DEFAULT 0,
    CONSTRAINT pk_venda_pagamento PRIMARY KEY (cod_pagamento, cod_venda),
    CONSTRAINT fk_venda_pagamento_pagamento FOREIGN KEY (cod_pagamento) REFERENCES metodo_pagamento(cod_pagamento),
    CONSTRAINT fk_venda_pagamento_venda FOREIGN KEY (cod_venda) REFERENCES vendas(cod_venda),
    CONSTRAINT ck_venda_pagamento_valor CHECK (valor_pago >= 0)
);
GO

CREATE TABLE configuracao_email (
    cod_configuracao INT NOT NULL PRIMARY KEY,
    email_remetente VARCHAR(254) NOT NULL,
    senha_app VARCHAR(100) NOT NULL
);
GO
