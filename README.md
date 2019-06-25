# larissa_codes

Projetos desenvolvidos em C#

Para rodar é preciso de uma máquina Windows, IDE Visual Studio 2017 ou superior, .Net Framework 4.7 ou superior

Para o projeto 2PC:
Instalar o PostgreSQL

Creates do banco:
CREATE DATABASE mestrado
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'Portuguese_Brazil.1252'
    LC_CTYPE = 'Portuguese_Brazil.1252'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;
    
  
CREATE TABLE public.disciplina
(
    nome character varying(150) COLLATE pg_catalog."default" NOT NULL,
    id integer NOT NULL,
    professor character varying(150) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT disciplina_pkey PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.disciplina
    OWNER to postgres;
   
 CREATE TABLE public.provas
(
    data date,
    materia character varying COLLATE pg_catalog."default",
    id integer NOT NULL,
    CONSTRAINT provas_pkey PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.provas
    OWNER to postgres;  
    
Será preciso trocar a conexão com o banco    
