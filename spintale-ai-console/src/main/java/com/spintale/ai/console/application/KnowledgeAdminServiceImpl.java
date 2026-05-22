package com.spintale.ai.console.application;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.spintale.ai.retrieval.knowledge.KnowledgeBase;
import com.spintale.ai.retrieval.knowledge.KnowledgeBaseService;
import com.spintale.ai.console.dto.KnowledgeBaseDTO;
import com.spintale.ai.console.vo.KnowledgeBaseVO;
import com.spintale.ai.console.convert.KnowledgeBaseConvert;

@Service
public class KnowledgeAdminServiceImpl implements KnowledgeAdminService
{
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Override
    public List<KnowledgeBaseVO> selectKnowledgeBaseList(KnowledgeBaseDTO kb)
    {
        List<KnowledgeBase> knowledgeBases = knowledgeBaseService.listAll();
        
        return knowledgeBases.stream()
                .filter(k -> kb.getKbName() == null || 
                        k.getName().contains(kb.getKbName()))
                .filter(k -> kb.getStatus() == null || 
                        (kb.getStatus() == 1) == k.isActive())
                .map(KnowledgeBaseConvert::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public KnowledgeBaseVO selectKnowledgeBaseById(Long kbId)
    {
        KnowledgeBase kb = knowledgeBaseService.get(String.valueOf(kbId))
                .orElse(null);
        return kb != null ? KnowledgeBaseConvert.toVO(kb) : null;
    }

    @Override
    public int insertKnowledgeBase(KnowledgeBaseDTO kb)
    {
        KnowledgeBase created = knowledgeBaseService.create(
                kb.getKbName(),
                kb.getDescription(),
                kb.getEmbeddingModel()
        );
        return created != null ? 1 : 0;
    }

    @Override
    public int updateKnowledgeBase(KnowledgeBaseDTO kb)
    {
        KnowledgeBase existing = knowledgeBaseService.get(String.valueOf(kb.getKbId()))
                .orElse(null);
        if (existing == null) return 0;

        existing.setName(kb.getKbName() != null ? kb.getKbName() : existing.getName());
        existing.setDescription(kb.getDescription() != null ? kb.getDescription() : existing.getDescription());
        existing.setEmbeddingModel(kb.getEmbeddingModel() != null ? kb.getEmbeddingModel() : existing.getEmbeddingModel());
        
        knowledgeBaseService.update(existing);
        return 1;
    }

    @Override
    public int deleteKnowledgeBaseByIds(Long[] kbIds)
    {
        int count = 0;
        for (Long kbId : kbIds)
        {
            knowledgeBaseService.delete(String.valueOf(kbId));
            count++;
        }
        return count;
    }
}
