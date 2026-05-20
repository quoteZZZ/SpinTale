package com.spintale.project.domain;

import com.spintale.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class AiChapter extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long chapterId;
    private Long projectId;
    private Long parentId;
    private String chapterTitle;
    private Integer chapterOrder;
    private String content;
    private String contentSummary;
    private Integer wordCount;
    private String status;
    private String generationStrategy;
    private String generateParams;
    private String aiMetadata;
    private Integer version;

    public Long getChapterId() {
        return chapterId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getChapterTitle() {
        return chapterTitle;
    }

    public void setChapterTitle(String chapterTitle) {
        this.chapterTitle = chapterTitle;
    }

    public Integer getChapterOrder() {
        return chapterOrder;
    }

    public void setChapterOrder(Integer chapterOrder) {
        this.chapterOrder = chapterOrder;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentSummary() {
        return contentSummary;
    }

    public void setContentSummary(String contentSummary) {
        this.contentSummary = contentSummary;
    }

    public Integer getWordCount() {
        return wordCount;
    }

    public void setWordCount(Integer wordCount) {
        this.wordCount = wordCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGenerationStrategy() {
        return generationStrategy;
    }

    public void setGenerationStrategy(String generationStrategy) {
        this.generationStrategy = generationStrategy;
    }

    public String getGenerateParams() {
        return generateParams;
    }

    public void setGenerateParams(String generateParams) {
        this.generateParams = generateParams;
    }

    public String getAiMetadata() {
        return aiMetadata;
    }

    public void setAiMetadata(String aiMetadata) {
        this.aiMetadata = aiMetadata;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("chapterId", getChapterId())
                .append("projectId", getProjectId())
                .append("chapterTitle", getChapterTitle())
                .append("chapterOrder", getChapterOrder())
                .append("wordCount", getWordCount())
                .append("status", getStatus())
                .toString();
    }
}
