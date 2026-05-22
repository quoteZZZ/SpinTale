package com.spintale.ai.console.application;

import java.util.List;
import com.spintale.ai.console.dto.KnowledgeBaseDTO;
import com.spintale.ai.console.vo.KnowledgeBaseVO;

public interface KnowledgeAdminService
{
    List<KnowledgeBaseVO> selectKnowledgeBaseList(KnowledgeBaseDTO kb);

    KnowledgeBaseVO selectKnowledgeBaseById(Long kbId);

    int insertKnowledgeBase(KnowledgeBaseDTO kb);

    int updateKnowledgeBase(KnowledgeBaseDTO kb);

    int deleteKnowledgeBaseByIds(Long[] kbIds);
}
