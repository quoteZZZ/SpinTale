package com.spintale.ai.console.vo;

import java.io.Serializable;
import lombok.Data;

@Data
public class DocumentChunkVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long chunkId;
    private Long docId;
    private Integer chunkIndex;
    private String content;
    private Integer tokenCount;
}
