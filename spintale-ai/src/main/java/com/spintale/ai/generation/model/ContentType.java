package com.spintale.ai.generation.model;

import java.io.Serializable;

/**
 * AI 生成内容类型枚举
 */
public enum ContentType {
    /**
     * 文章生成
     */
    ARTICLE("article", "文章"),
    
    /**
     * 小说/故事创作
     */
    NOVEL("novel", "小说"),
    
    /**
     * 广告词/文案
     */
    AD_COPY("ad_copy", "广告词"),
    
    /**
     * 营销文案
     */
    MARKETING_COPY("marketing_copy", "营销文案"),
    
    /**
     * 社交媒体帖子
     */
    SOCIAL_POST("social_post", "社交媒体帖子"),
    
    /**
     * 产品描述
     */
    PRODUCT_DESCRIPTION("product_description", "产品描述"),
    
    /**
     * 邮件内容
     */
    EMAIL("email", "邮件"),
    
    /**
     * 博客文章
     */
    BLOG_POST("blog_post", "博客文章"),
    
    /**
     * 新闻稿
     */
    PRESS_RELEASE("press_release", "新闻稿"),
    
    /**
     * 视频脚本
     */
    VIDEO_SCRIPT("video_script", "视频脚本"),
    
    /**
     * 自定义类型
     */
    CUSTOM("custom", "自定义");

    private final String code;
    private final String description;

    ContentType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ContentType fromCode(String code) {
        for (ContentType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return CUSTOM;
    }
}
