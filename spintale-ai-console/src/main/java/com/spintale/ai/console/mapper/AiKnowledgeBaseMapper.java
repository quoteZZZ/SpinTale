package com.spintale.ai.console.mapper;

import java.util.List;
import org.apache.ibatis.annotations.*;
import com.spintale.ai.console.domain.AiKnowledgeBase;

@Mapper
public interface AiKnowledgeBaseMapper
{
    @Select("SELECT * FROM ai_knowledge_base WHERE kb_id = #{kbId}")
    AiKnowledgeBase selectByKbId(@Param("kbId") String kbId);

    @Select("SELECT * FROM ai_knowledge_base WHERE create_by = #{createBy}")
    List<AiKnowledgeBase> selectByCreator(@Param("createBy") Long createBy);

    @Select("SELECT * FROM ai_knowledge_base WHERE status = 'ACTIVE'")
    List<AiKnowledgeBase> selectActive();

    @Insert("INSERT INTO ai_knowledge_base(kb_id, kb_name, description, embedding_model, " +
            "vector_dimension, chunk_size, chunk_overlap, document_count, chunk_count, " +
            "status, create_by, create_time) " +
            "VALUES(#{kbId}, #{kbName}, #{description}, #{embeddingModel}, " +
            "#{vectorDimension}, #{chunkSize}, #{chunkOverlap}, #{documentCount}, #{chunkCount}, " +
            "#{status}, #{createBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiKnowledgeBase kb);

    @Update("UPDATE ai_knowledge_base SET kb_name = #{kbName}, " +
            "description = #{description}, embedding_model = #{embeddingModel}, " +
            "vector_dimension = #{vectorDimension}, chunk_size = #{chunkSize}, " +
            "chunk_overlap = #{chunkOverlap}, document_count = #{documentCount}, " +
            "chunk_count = #{chunkCount}, status = #{status}, update_time = NOW() " +
            "WHERE kb_id = #{kbId}")
    int update(AiKnowledgeBase kb);

    @Delete("DELETE FROM ai_knowledge_base WHERE kb_id = #{kbId}")
    int deleteByKbId(@Param("kbId") String kbId);

    @Update("UPDATE ai_knowledge_base SET document_count = document_count + 1, " +
            "update_time = NOW() WHERE kb_id = #{kbId}")
    int incrementDocumentCount(@Param("kbId") String kbId);

    @Update("UPDATE ai_knowledge_base SET chunk_count = chunk_count + #{count}, " +
            "update_time = NOW() WHERE kb_id = #{kbId}")
    int incrementChunkCount(@Param("kbId") String kbId, @Param("count") int count);
}
