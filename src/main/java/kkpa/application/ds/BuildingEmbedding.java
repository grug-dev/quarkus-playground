package kkpa.application.ds;

import java.util.UUID;

public record BuildingEmbedding(
    UUID embeddingId,
    Long buildingId,
    String text,
    float[] embedding,
    String metadata,
    java.time.OffsetDateTime createdAt,
    java.time.OffsetDateTime updatedAt) {}
