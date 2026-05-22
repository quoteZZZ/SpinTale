package com.spintale.ai.console.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DocumentVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long docId;
    private Long kbId;
    private String docName;
    private String docType;
    private Long fileSize;
    private String status;
    private Integer chunkCount;
    private LocalDateTime createTime;
}
