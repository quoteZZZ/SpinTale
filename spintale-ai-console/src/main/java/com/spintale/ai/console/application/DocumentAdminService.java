package com.spintale.ai.console.application;

import java.util.List;
import com.spintale.ai.console.dto.DocumentDTO;
import com.spintale.ai.console.vo.DocumentChunkVO;
import com.spintale.ai.console.vo.DocumentVO;

public interface DocumentAdminService
{
    List<DocumentVO> selectDocumentList(DocumentDTO doc);

    DocumentVO selectDocumentById(Long docId);

    int insertDocument(DocumentDTO doc);

    int updateDocument(DocumentDTO doc);

    int deleteDocumentByIds(Long[] docIds);

    int startIndexJob(Long docId);

    List<DocumentChunkVO> listDocumentChunks(Long docId);
}
