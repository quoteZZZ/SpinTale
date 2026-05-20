package com.spintale.ai.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaContent {
    
    private ContentType type;
    
    private String mediaType;
    
    private String data;
    
    private String url;
    
    private String text;
    
    private Map<String, Object> metadata;
    
    public enum ContentType {
        TEXT,
        IMAGE,
        AUDIO,
        VIDEO,
        FILE
    }
    
    public static MediaContent text(String text) {
        return MediaContent.builder()
                .type(ContentType.TEXT)
                .text(text)
                .build();
    }
    
    public static MediaContent image(String url) {
        return MediaContent.builder()
                .type(ContentType.IMAGE)
                .url(url)
                .build();
    }
    
    public static MediaContent imageFromBase64(String base64Data, String mimeType) {
        return MediaContent.builder()
                .type(ContentType.IMAGE)
                .data(base64Data)
                .mediaType(mimeType)
                .build();
    }
    
    public static MediaContent audio(String url) {
        return MediaContent.builder()
                .type(ContentType.AUDIO)
                .url(url)
                .build();
    }
    
    public static MediaContent audioFromBase64(String base64Data, String mimeType) {
        return MediaContent.builder()
                .type(ContentType.AUDIO)
                .data(base64Data)
                .mediaType(mimeType)
                .build();
    }
    
    public static MediaContent video(String url) {
        return MediaContent.builder()
                .type(ContentType.VIDEO)
                .url(url)
                .build();
    }
    
    public static MediaContent file(String url, String mimeType) {
        return MediaContent.builder()
                .type(ContentType.FILE)
                .url(url)
                .mediaType(mimeType)
                .build();
    }
    
    public boolean isTextOnly() {
        return type == ContentType.TEXT || (text != null && !text.isEmpty() && data == null && url == null);
    }
    
    public boolean isMultimodal() {
        return type != ContentType.TEXT && (data != null || url != null);
    }
}
