package com.spintale.ai.runtime.skill;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Registry and execution facade for AI skills.
 */
@Service
public class SkillManager {

    private static final Logger log = LoggerFactory.getLogger(SkillManager.class);

    private final Map<String, AiSkill> skillRegistry = new ConcurrentHashMap<>();
    private final Map<String, List<SkillListener>> listeners = new ConcurrentHashMap<>();

    public SkillManager(List<AiSkill> skills) {
        if (skills != null) {
            skills.forEach(this::registerSkill);
        }
    }

    public void registerSkill(AiSkill skill) {
        if (skill == null) {
            throw new IllegalArgumentException("Skill cannot be null");
        }
        String skillId = skill.getId();
        if (skillRegistry.containsKey(skillId)) {
            log.warn("Skill {} already registered, replacing existing instance", skillId);
        }
        skillRegistry.put(skillId, skill);
        notifyListeners(skill, SkillEventType.REGISTERED);
    }

    public void unregisterSkill(String skillId) {
        AiSkill removed = skillRegistry.remove(skillId);
        if (removed != null) {
            notifyListeners(removed, SkillEventType.UNREGISTERED);
        }
    }

    public Optional<AiSkill> getSkill(String skillId) {
        return Optional.ofNullable(skillRegistry.get(skillId));
    }

    public List<AiSkill> getAllSkills() {
        return new ArrayList<>(skillRegistry.values());
    }

    public List<AiSkill> getSkillsByTag(String tag) {
        return skillRegistry.values().stream()
                .filter(skill -> {
                    for (String value : skill.getTags()) {
                        if (value.equals(tag)) {
                            return true;
                        }
                    }
                    return false;
                })
                .toList();
    }

    public AiSkill.SkillResult executeSkill(String skillId, Map<String, Object> args) {
        AiSkill skill = skillRegistry.get(skillId);
        if (skill == null) {
            return AiSkill.SkillResult.error("Skill not found: " + skillId);
        }

        try {
            notifyExecutionStart(skill, args);
            AiSkill.SkillResult result = skill.execute(args == null ? Map.of() : args);
            notifyExecutionComplete(skill, result);
            return result;
        } catch (Exception e) {
            log.error("Skill execution failed: {}", skillId, e);
            notifyExecutionError(skill, e);
            return AiSkill.SkillResult.error(e.getMessage());
        }
    }

    public void executeSkillStreaming(String skillId, Map<String, Object> args, AiSkill.StreamingHandler handler) {
        AiSkill skill = skillRegistry.get(skillId);
        if (skill == null) {
            handler.onError("Skill not found: " + skillId);
            return;
        }
        if (!skill.supportsStreaming()) {
            AiSkill.SkillResult result = executeSkill(skillId, args);
            if (result.isSuccess()) {
                handler.onComplete(result);
            } else {
                handler.onError(result.getErrorMessage());
            }
            return;
        }
        try {
            skill.executeStreaming(args == null ? Map.of() : args, handler);
        } catch (Exception e) {
            log.error("Streaming skill execution failed: {}", skillId, e);
            handler.onError(e.getMessage());
        }
    }

    public void addListener(SkillListener listener) {
        listeners.computeIfAbsent(listener.getEventType().name(), key -> new ArrayList<>()).add(listener);
    }

    public void removeListener(SkillListener listener) {
        List<SkillListener> list = listeners.get(listener.getEventType().name());
        if (list != null) {
            list.remove(listener);
        }
    }

    private void notifyListeners(AiSkill skill, SkillEventType eventType) {
        List<SkillListener> list = listeners.get(eventType.name());
        if (list != null) {
            list.forEach(listener -> listener.onEvent(skill));
        }
    }

    private void notifyExecutionStart(AiSkill skill, Map<String, Object> args) {
        List<SkillListener> list = listeners.get(SkillEventType.EXECUTION_START.name());
        if (list != null) {
            list.forEach(listener -> listener.onExecutionStart(skill, args));
        }
    }

    private void notifyExecutionComplete(AiSkill skill, AiSkill.SkillResult result) {
        List<SkillListener> list = listeners.get(SkillEventType.EXECUTION_COMPLETE.name());
        if (list != null) {
            list.forEach(listener -> listener.onExecutionComplete(skill, result));
        }
    }

    private void notifyExecutionError(AiSkill skill, Exception error) {
        List<SkillListener> list = listeners.get(SkillEventType.EXECUTION_ERROR.name());
        if (list != null) {
            list.forEach(listener -> listener.onExecutionError(skill, error));
        }
    }

    public enum SkillEventType {
        REGISTERED,
        UNREGISTERED,
        EXECUTION_START,
        EXECUTION_COMPLETE,
        EXECUTION_ERROR
    }

    public interface SkillListener {
        SkillEventType getEventType();

        default void onEvent(AiSkill skill) {}

        default void onExecutionStart(AiSkill skill, Map<String, Object> args) {}

        default void onExecutionComplete(AiSkill skill, AiSkill.SkillResult result) {}

        default void onExecutionError(AiSkill skill, Exception error) {}
    }
}
