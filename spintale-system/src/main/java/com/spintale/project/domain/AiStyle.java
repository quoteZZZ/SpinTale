package com.spintale.project.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spintale.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Date;

public class AiStyle {
    private static final long serialVersionUID = 1L;

    private Long styleId;
    private String styleName;
    private String styleCode;
    private String styleType;
    private String description;
    private String promptPrefix;
    private String promptSuffix;
    private String parameters;
    private String keywords;
    private String examples;
    private String isBuiltin;
    private String isActive;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getStyleId() {
        return styleId;
    }

    public void setStyleId(Long styleId) {
        this.styleId = styleId;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    public String getStyleCode() {
        return styleCode;
    }

    public void setStyleCode(String styleCode) {
        this.styleCode = styleCode;
    }

    public String getStyleType() {
        return styleType;
    }

    public void setStyleType(String styleType) {
        this.styleType = styleType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPromptPrefix() {
        return promptPrefix;
    }

    public void setPromptPrefix(String promptPrefix) {
        this.promptPrefix = promptPrefix;
    }

    public String getPromptSuffix() {
        return promptSuffix;
    }

    public void setPromptSuffix(String promptSuffix) {
        this.promptSuffix = promptSuffix;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getExamples() {
        return examples;
    }

    public void setExamples(String examples) {
        this.examples = examples;
    }

    public String getIsBuiltin() {
        return isBuiltin;
    }

    public void setIsBuiltin(String isBuiltin) {
        this.isBuiltin = isBuiltin;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("styleId", getStyleId())
                .append("styleName", getStyleName())
                .append("styleCode", getStyleCode())
                .append("styleType", getStyleType())
                .toString();
    }
}
