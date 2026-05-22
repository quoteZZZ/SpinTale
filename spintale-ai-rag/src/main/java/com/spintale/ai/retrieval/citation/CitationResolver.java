package com.spintale.ai.retrieval.citation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class CitationResolver
{
    private Pattern citationPattern = Pattern.compile("\\[(\\d+)\\]");
    private Pattern documentRefPattern = Pattern.compile("\\[([^\\]]+)\\]");

    public ResolvedCitations resolve(String answer, List<Citation> citations)
    {
        List<CitationReference> references = new ArrayList<>();
        List<String> unmatchedReferences = new ArrayList<>();

        Matcher matcher = citationPattern.matcher(answer);
        while (matcher.find())
        {
            int citationNum = Integer.parseInt(matcher.group(1));
            if (citationNum > 0 && citationNum <= citations.size())
            {
                Citation citation = citations.get(citationNum - 1);
                references.add(new CitationReference(
                        matcher.start(),
                        matcher.end(),
                        citationNum,
                        citation
                ));
            }
            else
            {
                unmatchedReferences.add(matcher.group());
            }
        }

        return new ResolvedCitations(answer, references, unmatchedReferences);
    }

    public String enrichAnswerWithCitations(String answer, List<Citation> citations)
    {
        if (citations == null || citations.isEmpty())
        {
            return answer;
        }

        StringBuilder enriched = new StringBuilder(answer);
        enriched.append("\n\n---\n**参考来源：**\n");

        for (int i = 0; i < citations.size(); i++)
        {
            Citation citation = citations.get(i);
            enriched.append("\n[").append(i + 1).append("] ");
            enriched.append(citation.getDisplayReference());

            if (citation.getSnippet() != null && !citation.getSnippet().isEmpty())
            {
                String snippet = citation.getSnippet();
                if (snippet.length() > 100)
                {
                    snippet = snippet.substring(0, 100) + "...";
                }
                enriched.append("\n    > ").append(snippet);
            }
        }

        return enriched.toString();
    }

    public String formatCitations(List<Citation> citations)
    {
        if (citations == null || citations.isEmpty())
        {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < citations.size(); i++)
        {
            Citation c = citations.get(i);
            sb.append("[").append(i + 1).append("] ")
              .append(c.getDocumentName() != null ? c.getDocumentName() : c.getDocumentId());
            
            if (c.getPageNumber() != null)
            {
                sb.append(", p.").append(c.getPageNumber());
            }
            if (c.getRelevanceScore() > 0)
            {
                sb.append(" (相关度: ").append(String.format("%.2f", c.getRelevanceScore())).append(")");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Data
    public static class CitationReference
    {
        private final int startPos;
        private final int endPos;
        private final int citationNumber;
        private final Citation citation;
    }

    @Data
    public static class ResolvedCitations
    {
        private final String originalAnswer;
        private final List<CitationReference> references;
        private final List<String> unmatchedReferences;

        public boolean hasCitations()
        {
            return references != null && !references.isEmpty();
        }

        public int getCitationCount()
        {
            return references != null ? references.size() : 0;
        }

        public List<Citation> getCitations()
        {
            return references.stream()
                    .map(CitationReference::getCitation)
                    .toList();
        }
    }
}
