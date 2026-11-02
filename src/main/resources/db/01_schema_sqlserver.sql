
CREATE TABLE endereco (
    codigo_endereco INT IDENTITY(1,1) PRIMARY KEY,
    uf VARCHAR(2) NOT NULL, cep VARCHAR(9) NOT NULL,
    cidade VARCHAR(50) NOT NULL, bairro VARCHAR(50) NOT NULL,
    logradouro VARCHAR(50) NOT NULL, numero VARCHAR(5), complemento VARCHAR(10)
);

CREATE TABLE cliente (
    cpf_pessoa VARCHAR(14) NOT NULL PRIMARY KEY,
    nome_pessoa VARCHAR(50) NOT NULL, tel_pessoa VARCHAR(11),
    end_pessoa_codigo_endereco INT NULL UNIQUE REFERENCES endereco(codigo_endereco)
);

CREATE TABLE funcionario (
    cpf_pessoa VARCHAR(14) NOT NULL PRIMARY KEY,
    nome_pessoa VARCHAR(50) NOT NULL, tel_pessoa VARCHAR(11),
    end_pessoa_codigo_endereco INT NULL UNIQUE REFERENCES endereco(codigo_endereco),
    salario_funcionario FLOAT NOT NULL CONSTRAINT df_func_salario DEFAULT 0 CONSTRAINT ck_func_salario CHECK (salario_funcionario >= 0),
    senha VARCHAR(255)
);

CREATE TABLE marca (
    cod_marca INT IDENTITY(1,1) PRIMARY KEY,
    nome_marca VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE categoria (
    cod_cat INT IDENTITY(1,1) PRIMARY KEY,
    nome_categoria VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE metodo_pagamento (
    codigo_metodo_pagamento INT IDENTITY(1,1) PRIMARY KEY,
    descricao_metodo_pagamento VARCHAR(255) NOT NULL UNIQUE,
    is_active_metodo_pagamento BIT NOT NULL CONSTRAINT df_metodo_ativo DEFAULT 1
);

CREATE TABLE produto (
    codigo_produto INT IDENTITY(1,1) PRIMARY KEY,
    descricao_produto VARCHAR(255) NOT NULL, tamanho_produto VARCHAR(10) NOT NULL,
    marca_id INT NOT NULL REFERENCES marca(cod_marca),
    categoria_id INT NOT NULL REFERENCES categoria(cod_cat),
    is_selled BIT NOT NULL CONSTRAINT df_produto_vendido DEFAULT 0,
    preco_produto FLOAT NOT NULL CONSTRAINT ck_produto_preco CHECK (preco_produto >= 0)
);

CREATE TABLE venda (
    codigo_venda INT IDENTITY(1,1) PRIMARY KEY,
    data_venda VARCHAR(20) NOT NULL,
    cliente_id VARCHAR(14) NOT NULL REFERENCES cliente(cpf_pessoa),
    funcionario_id VARCHAR(14) NOT NULL REFERENCES funcionario(cpf_pessoa),
    valor_total FLOAT NOT NULL CONSTRAINT df_venda_total DEFAULT 0 CONSTRAINT ck_venda_total CHECK (valor_total >= 0),
    status INT NOT NULL CONSTRAINT df_venda_status DEFAULT 0 CONSTRAINT ck_venda_status CHECK (status IN (0,1,2))
);

CREATE TABLE venda_produtos (
    venda_codigo_venda INT NOT NULL REFERENCES venda(codigo_venda) ON DELETE CASCADE,
    produtos_codigo_produto INT NOT NULL UNIQUE REFERENCES produto(codigo_produto),
    CONSTRAINT pk_venda_produtos PRIMARY KEY (venda_codigo_venda, produtos_codigo_produto)
);

CREATE TABLE venda_pagamento (
    cod_venda_pagamento INT IDENTITY(1,1) PRIMARY KEY,
    venda_id INT NOT NULL REFERENCES venda(codigo_venda) ON DELETE CASCADE,
    metodo_pagamento_id INT NOT NULL REFERENCES metodo_pagamento(codigo_metodo_pagamento),
    valor_pago FLOAT NOT NULL CONSTRAINT ck_pagamento_valor CHECK (valor_pago > 0)
);