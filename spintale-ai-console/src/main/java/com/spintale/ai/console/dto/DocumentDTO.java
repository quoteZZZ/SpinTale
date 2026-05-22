package com.spintale.ai.console.dto;

import java.io.Serializable;
import lombok.Data;

@Data
public class DocumentDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long docId;
    private Long kbId;
    private String docName;
    private String docType;
    private String status;
}
