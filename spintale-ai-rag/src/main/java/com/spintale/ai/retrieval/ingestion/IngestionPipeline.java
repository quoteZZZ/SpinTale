package com.spintale.ai.retrieval.ingestion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Data;
import org.springframework.stereotype.Component;
import com.spintale.ai.retrieval.chunk.DocumentChunk;
import com.spintale.ai.retrieval.document.Document;

@Data
@Component
public class IngestionPipeline
{
    private final List<IngestionStage> stages = new ArrayList<>();

    public interface IngestionStage
    {
        String getName();
        IngestionResult process(IngestionContext context);
    }

    @Data
    public static class IngestionContext
    {
        private Document document;
        private String rawContent;
        private String cleanedContent;
        private List<String> chunks;
        private List<DocumentChunk> documentChunks;
        private Map<String, Object> metadata;
        private int currentStage;
        private String error;

        public static IngestionContext create(Document document, String content)
        {
            IngestionContext ctx = new IngestionContext();
            ctx.setDocument(document);
            ctx.setRawContent(content);
            ctx.setCleanedContent(content);
            ctx.setChunks(new ArrayList<>());
            ctx.setDocumentChunks(new ArrayList<>());
            ctx.setCurrentStage(0);
            return ctx;
        }
    }

    @Data
    public static class IngestionResult
    {
        private boolean success;
        private String stageName;
        private String error;
        private List<DocumentChunk> chunks;

        public static IngestionResult success()
        {
            return new IngestionResult(true, null, null, null);
        }

        public static IngestionResult success(List<DocumentChunk> chunks)
        {
            return new IngestionResult(true, null, null, chunks);
        }

        public static IngestionResult failure(String stage, String error)
        {
            return new IngestionResult(false, stage, error, null);
        }
    }

    public IngestionPipeline addStage(IngestionStage stage)
    {
        stages.add(stage);
        return this;
    }

    public IngestionPipeline addParsingStage(Function<String, String> parser)
    {
        return addStage(new IngestionStage()
        {
            @Override
            public String getName()
            {
                return "PARSE";
            }

            @Override
            public IngestionResult process(IngestionContext ctx)
            {
                try
                {
                    String parsed = parser.apply(ctx.getRawContent());
                    ctx.setCleanedContent(parsed);
                    return IngestionResult.success();
                }
                catch (Exception e)
                {
                    return IngestionResult.failure("PARSE", e.getMessage());
                }
            }
        });
    }

    public IngestionPipeline addChunkingStage(DocumentChunker chunker)
    {
        return addStage(new IngestionStage()
        {
            @Override
            public String getName()
            {
                return "CHUNK";
            }

            @Override
            public IngestionResult process(IngestionContext ctx)
            {
                try
                {
                    List<String> chunks = chunker.chunk(ctx.getCleanedContent());
                    ctx.setChunks(chunks);

                    List<DocumentChunk> docChunks = new ArrayList<>();
                    for (int i = 0; i < chunks.size(); i++)
                    {
                        DocumentChunk chunk = DocumentChunk.of(
                                ctx.getDocument().getDocumentId(),
                                ctx.getDocument().getKnowledgeBaseId(),
                                i,
                                chunks.get(i)
                        );
                        docChunks.add(chunk);
                    }
                    ctx.setDocumentChunks(docChunks);

                    return IngestionResult.success(docChunks);
                }
                catch (Exception e)
                {
                    return IngestionResult.failure("CHUNK", e.getMessage());
                }
            }
        });
    }

    public IngestionPipeline addCleaningStage(Function<String, String> cleaner)
    {
        return addStage(new IngestionStage()
        {
            @Override
            public String getName()
            {
                return "CLEAN";
            }

            @Override
            public IngestionResult process(IngestionContext ctx)
            {
                try
                {
                    String cleaned = cleaner.apply(ctx.getCleanedContent());
                    ctx.setCleanedContent(cleaned);
                    return IngestionResult.success();
                }
                catch (Exception e)
                {
                    return IngestionResult.failure("CLEAN", e.getMessage());
                }
            }
        });
    }

    public IngestionResult execute(Document document, String content)
    {
        IngestionContext context = IngestionContext.create(document, content);

        for (int i = 0; i < stages.size(); i++)
        {
            IngestionStage stage = stages.get(i);
            context.setCurrentStage(i);

            IngestionResult result = stage.process(context);
            if (!result.isSuccess())
            {
                context.setError(result.getError());
                return result;
            }
        }

        return IngestionResult.success(context.getDocumentChunks());
    }

    public static IngestionPipeline defaultPipeline()
    {
        IngestionPipeline pipeline = new IngestionPipeline();

        pipeline.addCleaningStage(content -> content
                .replaceAll("\\s+", " ")
                .trim());

        DocumentChunker chunker = new DocumentChunker();
        chunker.setStrategy(DocumentChunker.ChunkStrategy.SENTENCE_AWARE);
        pipeline.addChunkingStage(chunker);

        return pipeline;
    }
}
