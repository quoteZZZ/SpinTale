package com.spintale.ai.agent.guardrail;

import java.util.List;
import java.util.regex.Pattern;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Builder
@Component
public class OutputGuardrail
{
    private List<Pattern> blockedPatterns;
    private List<String> blockedKeywords;
    private int maxLength;
    private boolean checkPii;
    private boolean checkToxicity;
    private boolean maskSensitiveData;

    public GuardrailResult validate(String output)
    {
        if (output == null || output.isEmpty())
        {
            return GuardrailResult.valid();
        }

        if (maxLength > 0 && output.length() > maxLength)
        {
            String truncated = output.substring(0, maxLength) + "... [truncated]";
            return GuardrailResult.validWithSanitization(truncated);
        }

        if (checkPii)
        {
            String sanitized = maskPii(output);
            if (!sanitized.equals(output))
            {
                return GuardrailResult.validWithSanitization(sanitized);
            }
        }

        if (blockedKeywords != null)
        {
            String lowerOutput = output.toLowerCase();
            for (String keyword : blockedKeywords)
            {
                if (lowerOutput.contains(keyword.toLowerCase()))
                {
                    return GuardrailResult.invalid(
                            "Output contains blocked content");
                }
            }
        }

        return GuardrailResult.valid();
    }

    private String maskPii(String text)
    {
        String result = text;

        Pattern emailPattern = Pattern.compile(
                "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
        result = emailPattern.matcher(result).replaceAll("***@***.***");

        Pattern phonePattern = Pattern.compile(
                "\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b");
        result = phonePattern.matcher(result).replaceAll("***-***-****");

        Pattern ssnPattern = Pattern.compile(
                "\\b\\d{3}-\\d{2}-\\d{4}\\b");
        result = ssnPattern.matcher(result).replaceAll("***-**-****");

        Pattern creditCardPattern = Pattern.compile(
                "\\b\\d{4}[-\\s]?\\d{4}[-\\s]?\\d{4}[-\\s]?\\d{4}\\b");
        result = creditCardPattern.matcher(result).replaceAll("****-****-****-****");

        return result;
    }

    public static OutputGuardrail defaultGuardrail()
    {
        return OutputGuardrail.builder()
                .maxLength(50000)
                .checkPii(true)
                .maskSensitiveData(true)
                .build();
    }

    public static OutputGuardrail strict()
    {
        return OutputGuardrail.builder()
                .maxLength(10000)
                .checkPii(true)
                .checkToxicity(true)
                .maskSensitiveData(true)
                .build();
    }

    @Data
    @Builder
    public static class GuardrailResult
    {
        private final boolean valid;
        private final String reason;
        private final String sanitizedOutput;

        public static GuardrailResult valid()
        {
            return new GuardrailResult(true, null, null);
        }

        public static GuardrailResult invalid(String reason)
        {
            return new GuardrailResult(false, reason, null);
        }

        public static GuardrailResult validWithSanitization(String sanitized)
        {
            return new GuardrailResult(true, null, sanitized);
        }
    }
}
