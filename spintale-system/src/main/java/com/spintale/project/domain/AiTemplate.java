package com.spintale.project.domain;

import com.spintale.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;

public class AiTemplate extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long templateId;
    private String templateName;
    private String templateCode;
    private String templateType;
    private String category;
    private String description;
    private String promptTemplate;
    private String variables;
    private String defaultParams;
    private String styleGuide;
    private String exampleOutput;
    private String isPublic;
    private String isBuiltin;
    private Integer useCount;
    private BigDecimal rating;

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPromptTemplate() {
        return promptTemplate;
    }

    public void setPromptTemplate(String promptTemplate) {
        this.promptTemplate = promptTemplate;
    }

    public String getVariables() {
        return variables;
    }

    public void setVariables(String variables) {
        this.variables = variables;
    }

    public String getDefaultParams() {
        return defaultParams;
    }

    public void setDefaultParams(String defaultParams) {
        this.defaultParams = defaultParams;
    }

    public String getStyleGuide() {
        return styleGuide;
    }

    public void setStyleGuide(String styleGuide) {
        this.styleGuide = styleGuide;
    }

    public String getExampleOutput() {
        return exampleOutput;
    }

    public void setExampleOutput(String exampleOutput) {
        this.exampleOutput = exampleOutput;
    }

    public String getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(String isPublic) {
        this.isPublic = isPublic;
    }

    public String getIsBuiltin() {
        return isBuiltin;
    }

    public void setIsBuiltin(String isBuiltin) {
        this.isBuiltin = isBuiltin;
    }

    public Integer getUseCount() {
        return useCount;
    }

    public void setUseCount(Integer useCount) {
        this.useCount = useCount;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("templateId", getTemplateId())
                .append("templateName", getTemplateName())
                .append("templateCode", getTemplateCode())
                .append("templateType", getTemplateType())
                .append("useCount", getUseCount())
                .toString();
    }
}
