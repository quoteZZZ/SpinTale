package com.spintale.ai.generation.model;

public enum ContentType
{
    ARTICLE("article", "Article"),
    NOVEL("novel", "Novel"),
    AD_COPY("ad_copy", "Ad copy"),
    MARKETING_COPY("marketing_copy", "Marketing copy"),
    SOCIAL_POST("social_post", "Social post"),
    PRODUCT_DESCRIPTION("product_description", "Product description"),
    EMAIL("email", "Email"),
    BLOG_POST("blog_post", "Blog post"),
    PRESS_RELEASE("press_release", "Press release"),
    VIDEO_SCRIPT("video_script", "Video script"),
    CUSTOM("custom", "Custom");

    private final String code;
    private final String description;

    ContentType(String code, String description)
    {
        this.code = code;
        this.description = description;
    }

    public String getCode()
    {
        return code;
    }

    public String getDescription()
    {
        return description;
    }

    public static ContentType fromCode(String code)
    {
        for (ContentType type : values())
        {
            if (type.code.equalsIgnoreCase(code))
            {
                return type;
            }
        }
        return CUSTOM;
    }
}
