# quarkus-playground

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/quarkus-playground-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)

# Ollama

1. Download Ollama from website for your OS

2. Run the following command to download the model

```
ollama run llama3.2
ollama pull nomic-embed-text
```

## REGISTRY

List of models available in the registry

https://ollama.com/library

you can download a model by running the following command

```
ollama pull xxx
ollama pull nomic-embed-text
```

## EMBEDDING MODELS

```markdown
ollama pull nomic-embed-text
```

## MODERATE MODAL

```bash
ollama pull llama-guard3

```

#CURL

```bash
curl http://localhost:11434/api/chat -d "{\"model\":\"llama3.2\",\"messages\":[{\"role\":\"USER\",\"content\":\"Provide three short bullet points explaining why Java is awesome\"}],\"options\":{},\"stream\":false}"
```

# Embeddings

# INSTALLING PG VECTOR

<!-- LangChain4j PostgreSQL pgvector Integration -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-pgvector</artifactId>
    <version>0.26.1</version> <!-- Use the version compatible with your LangChain4j -->
</dependency>

<!-- PostgreSQL JDBC Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.1</version> <!-- Use latest stable version -->
</dependency>

```markdown
Installing pgvector on macOS with Homebrew

Stop PostgreSQL server if it's running:
bashbrew services stop postgresql@16

brew install make gcc
See https://github.com/pgvector/pgvector

Start PostgreSQL server again:
bashbrew services start postgresql@16
```

```markdown
Only super users can create vectors
CREATE EXTENSION IF NOT EXISTS vector;

SELECT usename, usesuper FROM pg_user;
```

# CREATE EMBEDDINGS TABLE

```sql
CREATE SCHEMA IF NOT EXISTS rag_store;
-- Table for document-based chunks
CREATE TABLE IF NOT EXISTS rag_store.docs_embeddings
(
    embedding_id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    text TEXT NOT NULL,
    embedding vector
(
    768
) NOT NULL,
    metadata JSONB
    );

-- Optional indexes for faster retrieval
CREATE INDEX IF NOT EXISTS docs_embedding_idx
    ON rag_store.docs_embeddings USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

-- Table for property-based semantic data
CREATE TABLE IF NOT EXISTS rag_store.props_embeddings
(
    embedding_id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    building_id INTEGER NOT NULL,
    text TEXT NOT NULL, -- The concatenated text that was embedded
    embedding vector
(
    768
) NOT NULL,
    metadata JSONB, -- Will store building metadata including FKs
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                             );


ALTER TABLE rag_store.props_embeddings
    ADD CONSTRAINT unique_building_id UNIQUE (building_id);

CREATE INDEX IF NOT EXISTS props_embedding_idx
    ON rag_store.props_embeddings USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
```

```sql
-- Add a trigger to update the updated_at timestamp
CREATE
OR REPLACE FUNCTION vector_store.update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at
= CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER update_building_embeddings_timestamp
    BEFORE UPDATE
    ON vector_store.building_embeddings
    FOR EACH ROW EXECUTE FUNCTION vector_store.update_timestamp();
```