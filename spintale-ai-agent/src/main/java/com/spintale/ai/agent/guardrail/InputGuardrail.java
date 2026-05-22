package com.spintale.ai.agent.guardrail;

import java.util.List;
import java.util.regex.Pattern;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Builder
@Component
public class InputGuardrail
{
    private List<Pattern> blockedPatterns;
    private List<String> blockedKeywords;
    private int maxLength;
    private boolean checkInjection;
    private boolean checkPii;

    public GuardrailResult validate(String input)
    {
        if (input == null || input.isEmpty())
        {
            return GuardrailResult.valid();
        }

        if (maxLength > 0 && input.length() > maxLength)
        {
            return GuardrailResult.invalid(
                    "Input exceeds maximum length of " + maxLength + " characters");
        }

        if (blockedKeywords != null)
        {
            String lowerInput = input.toLowerCase();
            for (String keyword : blockedKeywords)
            {
                if (lowerInput.contains(keyword.toLowerCase()))
                {
                    return GuardrailResult.invalid(
                            "Input contains blocked keyword: " + keyword);
                }
            }
        }

        if (blockedPatterns != null)
        {
            for (Pattern pattern : blockedPatterns)
            {
                if (pattern.matcher(input).find())
                {
                    return GuardrailResult.invalid(
                            "Input matches blocked pattern");
                }
            }
        }

        if (checkInjection)
        {
            String injectionResult = checkForInjection(input);
            if (injectionResult != null)
            {
                return GuardrailResult.invalid(injectionResult);
            }
        }

        return GuardrailResult.valid();
    }

    private String checkForInjection(String input)
    {
        String lower = input.toLowerCase();
        
        String[] injectionPatterns = {
            "ignore previous instructions",
            "ignore all previous",
            "disregard all",
            "forget everything",
            "system:",
            "assistant:",
            "your new instructions",
            "override instructions"
        };

        for (String pattern : injectionPatterns)
        {
            if (lower.contains(pattern))
            {
                return "Potential prompt injection detected";
            }
        }

        return null;
    }

    public static InputGuardrail defaultGuardrail()
    {
        return InputGuardrail.builder()
                .maxLength(100000)
                .checkInjection(true)
                .checkPii(false)
                .build();
    }

    public static InputGuardrail strict()
    {
        return InputGuardrail.builder()
                .maxLength(10000)
                .checkInjection(true)
                .checkPii(true)
                .blockedKeywords(List.of("password", "secret", "api_key", "token"))
                .build();
    }

    @Data
    @Builder
    public static class GuardrailResult
    {
        private final boolean valid;
        private final String reason;
        private final String sanitizedInput;

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
