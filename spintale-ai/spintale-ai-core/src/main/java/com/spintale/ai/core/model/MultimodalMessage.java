package com.spintale.ai.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultimodalMessage {
    
    private String role;
    
    private List<MediaContent> contents;
    
    private String name;
    
    public static MultimodalMessage userText(String text) {
        return MultimodalMessage.builder()
                .role("user")
                .contents(List.of(MediaContent.text(text)))
                .build();
    }
    
    public static MultimodalMessage userImage(String text, String imageUrl) {
        List<MediaContent> contents = new ArrayList<>();
        if (text != null && !text.isEmpty()) {
            contents.add(MediaContent.text(text));
        }
        contents.add(MediaContent.image(imageUrl));
        
        return MultimodalMessage.builder()
                .role("user")
                .contents(contents)
                .build();
    }
    
    public static MultimodalMessage userImages(String text, List<String> imageUrls) {
        List<MediaContent> contents = new ArrayList<>();
        if (text != null && !text.isEmpty()) {
            contents.add(MediaContent.text(text));
        }
        for (String url : imageUrls) {
            contents.add(MediaContent.image(url));
        }
        
        return MultimodalMessage.builder()
                .role("user")
                .contents(contents)
                .build();
    }
    
    public static MultimodalMessage userAudio(String text, String audioUrl) {
        List<MediaContent> contents = new ArrayList<>();
        if (text != null && !text.isEmpty()) {
            contents.add(MediaContent.text(text));
        }
        contents.add(MediaContent.audio(audioUrl));
        
        return MultimodalMessage.builder()
                .role("user")
                .contents(contents)
                .build();
    }
    
    public static MultimodalMessage assistantText(String text) {
        return MultimodalMessage.builder()
                .role("assistant")
                .contents(List.of(MediaContent.text(text)))
                .build();
    }
    
    public static MultimodalMessage systemText(String text) {
        return MultimodalMessage.builder()
                .role("system")
                .contents(List.of(MediaContent.text(text)))
                .build();
    }
    
    public String getTextContent() {
        if (contents == null || contents.isEmpty()) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        for (MediaContent content : contents) {
            if (content.getType() == MediaContent.ContentType.TEXT && content.getText() != null) {
                sb.append(content.getText());
            }
        }
        return sb.toString();
    }
    
    public List<MediaContent> getImages() {
        if (contents == null) {
            return List.of();
        }
        
        return contents.stream()
                .filter(c -> c.getType() == MediaContent.ContentType.IMAGE)
                .toList();
    }
    
    public List<MediaContent> getAudio() {
        if (contents == null) {
            return List.of();
        }
        
        return contents.stream()
                .filter(c -> c.getType() == MediaContent.ContentType.AUDIO)
                .toList();
    }
    
    public boolean isMultimodal() {
        if (contents == null || contents.isEmpty()) {
            return false;
        }
        
        return contents.stream().anyMatch(MediaContent::isMultimodal);
    }
    
    public boolean isTextOnly() {
        if (contents == null || contents.isEmpty()) {
            return true;
        }
        
        return contents.stream().allMatch(MediaContent::isTextOnly);
    }
}
