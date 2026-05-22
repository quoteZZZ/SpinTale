package com.spintale.ai.console.convert;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import com.spintale.ai.retrieval.knowledge.KnowledgeBase;
import com.spintale.ai.console.vo.KnowledgeBaseVO;

public class KnowledgeBaseConvert
{
    public static KnowledgeBaseVO toVO(KnowledgeBase kb)
    {
        return KnowledgeBaseVO.builder()
                .kbId(extractId(kb.getKbId()))
                .kbName(kb.getName())
                .description(kb.getDescription())
                .embeddingModel(kb.getEmbeddingModel())
                .documentCount(kb.getDocumentCount())
                .chunkCount(kb.getChunkCount())
                .status(kb.isActive() ? 1 : 0)
                .createTime(toLocalDateTime(kb.getCreateTime()))
                .build();
    }

    public static KnowledgeBase toEntity(KnowledgeBaseVO vo)
    {
        return KnowledgeBase.builder()
                .kbId(String.valueOf(vo.getKbId()))
                .name(vo.getKbName())
                .description(vo.getDescription())
                .embeddingModel(vo.getEmbeddingModel())
                .build();
    }

    private static Long extractId(String id)
    {
        if (id == null) return null;
        try
        {
            return Long.parseLong(id.replace("-", "").substring(0, 18));
        }
        catch (Exception e)
        {
            return (long) id.hashCode();
        }
    }

    private static LocalDateTime toLocalDateTime(Instant instant)
    {
        return instant != null ? 
                LocalDateTime.ofInstant(instant, ZoneId.systemDefault()) : null;
    }
}
