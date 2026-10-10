package com.dn.dnaiagent.rag;

import com.dn.dnaiagent.app.LoveApp;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.Resource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

/**
 * @author Lin_jd
 * @version V1.0.0
 * @since 2026/10/10 16:48
 **/
@Configuration
@Profile("local")
public class PgVectorVectorStoreConfig {
    @Value("${spring.postgres.datasource.url}")
    private String pgUrl;

    @Value("${spring.postgres.datasource.username}")
    private String pgUsername;

    @Value("${spring.postgres.datasource.password}")
    private String pgPassword;

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Bean
    public VectorStore pgVectorVectorStore(EmbeddingModel dashscopeEmbeddingModel, LoveApp loveApp) {
        HikariDataSource pgDataSource = new HikariDataSource();
        pgDataSource.setDriverClassName("org.postgresql.Driver");
        pgDataSource.setJdbcUrl(pgUrl);
        pgDataSource.setUsername(pgUsername);
        pgDataSource.setPassword(pgPassword);
        pgDataSource.setMaximumPoolSize(5);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(pgDataSource);

        PgVectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .distanceType(COSINE_DISTANCE)
                .indexType(HNSW)
                .initializeSchema(true)
                .schemaName("public")
                .vectorTableName("vector_store")
                .maxDocumentBatchSize(10000)
                .build();

        vectorStore.add(loveAppDocumentLoader.loadMarkDowns());
        return vectorStore;
    }
}

