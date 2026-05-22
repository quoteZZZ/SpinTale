package com.spintale.ai.retrieval.ingestion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class DocumentChunker
{
    private int defaultChunkSize = 500;
    private int defaultOverlapSize = 50;
    private ChunkStrategy strategy = ChunkStrategy.SENTENCE_AWARE;

    public enum ChunkStrategy
    {
        FIXED_SIZE,
        SENTENCE_AWARE,
        PARAGRAPH_AWARE,
        SEMANTIC
    }

    public List<String> chunk(String content)
    {
        return chunk(content, defaultChunkSize, defaultOverlapSize);
    }

    public List<String> chunk(String content, int chunkSize)
    {
        return chunk(content, chunkSize, defaultOverlapSize);
    }

    public List<String> chunk(String content, int chunkSize, int overlap)
    {
        if (content == null || content.isEmpty())
        {
            return List.of();
        }

        return switch (strategy)
        {
            case FIXED_SIZE -> chunkFixedSize(content, chunkSize, overlap);
            case SENTENCE_AWARE -> chunkSentenceAware(content, chunkSize, overlap);
            case PARAGRAPH_AWARE -> chunkParagraphAware(content, chunkSize, overlap);
            case SEMANTIC -> chunkSemantic(content, chunkSize, overlap);
        };
    }

    private List<String> chunkFixedSize(String content, int chunkSize, int overlap)
    {
        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < content.length())
        {
            int end = Math.min(start + chunkSize, content.length());
            chunks.add(content.substring(start, end));
            start = end - overlap;
            if (start < 0) start = 0;
        }

        return chunks;
    }

    private List<String> chunkSentenceAware(String content, int chunkSize, int overlap)
    {
        List<String> chunks = new ArrayList<>();
        List<Integer> sentenceBoundaries = findSentenceBoundaries(content);

        int start = 0;
        while (start < content.length())
        {
            int targetEnd = Math.min(start + chunkSize, content.length());
            
            int bestEnd = targetEnd;
            int minDiff = Integer.MAX_VALUE;
            
            for (int boundary : sentenceBoundaries)
            {
                if (boundary > start && boundary <= targetEnd + 100)
                {
                    int diff = Math.abs(boundary - targetEnd);
                    if (diff < minDiff)
                    {
                        minDiff = diff;
                        bestEnd = boundary;
                    }
                }
            }

            if (bestEnd <= start)
            {
                bestEnd = targetEnd;
            }

            chunks.add(content.substring(start, Math.min(bestEnd, content.length())));
            
            int nextStart = bestEnd - overlap;
            start = Math.max(nextStart, start + 1);
        }

        return chunks;
    }

    private List<String> chunkParagraphAware(String content, int chunkSize, int overlap)
    {
        List<String> chunks = new ArrayList<>();
        String[] paragraphs = content.split("\\n\\n+");

        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs)
        {
            if (currentChunk.length() + paragraph.length() > chunkSize 
                    && currentChunk.length() > 0)
            {
                chunks.add(currentChunk.toString().trim());
                
                if (overlap > 0 && currentChunk.length() > overlap)
                {
                    String overlapText = currentChunk.substring(
                            currentChunk.length() - overlap);
                    currentChunk = new StringBuilder(overlapText);
                }
                else
                {
                    currentChunk = new StringBuilder();
                }
            }

            if (currentChunk.length() > 0)
            {
                currentChunk.append("\n\n");
            }
            currentChunk.append(paragraph);
        }

        if (currentChunk.length() > 0)
        {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    private List<String> chunkSemantic(String content, int chunkSize, int overlap)
    {
        return chunkSentenceAware(content, chunkSize, overlap);
    }

    private List<Integer> findSentenceBoundaries(String content)
    {
        List<Integer> boundaries = new ArrayList<>();
        
        for (int i = 0; i < content.length(); i++)
        {
            char c = content.charAt(i);
            if (c == '.' || c == '!' || c == '?' || c == '。' || c == '！' || c == '？')
            {
                boundaries.add(i + 1);
            }
        }

        return boundaries;
    }

    public DocumentChunker withChunkSize(int size)
    {
        this.defaultChunkSize = size;
        return this;
    }

    public DocumentChunker withOverlap(int overlap)
    {
        this.defaultOverlapSize = overlap;
        return this;
    }

    public DocumentChunker withStrategy(ChunkStrategy strategy)
    {
        this.strategy = strategy;
        return this;
    }
}
