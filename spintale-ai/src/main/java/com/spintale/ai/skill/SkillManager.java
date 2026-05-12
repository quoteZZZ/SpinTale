package com.spintale.ai.skill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * AI 技能管理器
 * 
 * 负责技能的注册、发现、执行和生命周期管理
 * 参考 Gitee 热门项目：skill-manager, langchain4j-spring-boot-starter
 */
@Service
public class SkillManager {
    
    private static final Logger log = LoggerFactory.getLogger(SkillManager.class);
    
    private final Map<String, AiSkill> skillRegistry = new ConcurrentHashMap<>();
    private final Map<String, List<SkillListener>> listeners = new ConcurrentHashMap<>();
    
    /**
     * 注册技能
     */
    public void registerSkill(AiSkill skill) {
        if (skill == null) {
            throw new IllegalArgumentException("Skill cannot be null");
        }
        String skillId = skill.getId();
        if (skillRegistry.containsKey(skillId)) {
            log.warn("Skill {} already registered, replacing with new instance", skillId);
        }
        skillRegistry.put(skillId, skill);
        log.info("Registered skill: {} ({})", skill.getName(), skillId);
        
        // 通知监听器
        notifyListeners(skill, SkillEventType.REGISTERED);
    }
    
    /**
     * 注销技能
     */
    public void unregisterSkill(String skillId) {
        AiSkill removed = skillRegistry.remove(skillId);
        if (removed != null) {
            log.info("Unregistered skill: {} ({})", removed.getName(), skillId);
            notifyListeners(removed, SkillEventType.UNREGISTERED);
        } else {
            log.warn("Attempted to unregister non-existent skill: {}", skillId);
        }
    }
    
    /**
     * 获取技能
     */
    public Optional<AiSkill> getSkill(String skillId) {
        return Optional.ofNullable(skillRegistry.get(skillId));
    }
    
    /**
     * 获取所有已注册技能
     */
    public List<AiSkill> getAllSkills() {
        return new ArrayList<>(skillRegistry.values());
    }
    
    /**
     * 根据标签筛选技能
     */
    public List<AiSkill> getSkillsByTag(String tag) {
        return skillRegistry.values().stream()
                .filter(skill -> {
                    for (String t : skill.getTags()) {
                        if (t.equals(tag)) return true;
                    }
                    return false;
                })
                .toList();
    }
    
    /**
     * 执行技能
     */
    public AiSkill.SkillResult executeSkill(String skillId, Map<String, Object> args) {
        AiSkill skill = skillRegistry.get(skillId);
        if (skill == null) {
            return AiSkill.SkillResult.error("Skill not found: " + skillId);
        }
        
        log.debug("Executing skill: {} with args: {}", skillId, args);
        
        try {
            // 通知监听器 - 执行前
            notifyExecutionStart(skill, args);
            
            AiSkill.SkillResult result = skill.execute(args);
            
            // 通知监听器 - 执行后
            notifyExecutionComplete(skill, result);
            
            return result;
        } catch (Exception e) {
            log.error("Skill execution failed: {}", skillId, e);
            AiSkill.SkillResult errorResult = AiSkill.SkillResult.error(e.getMessage());
            notifyExecutionError(skill, e);
            return errorResult;
        }
    }
    
    /**
     * 流式执行技能
     */
    public void executeSkillStreaming(String skillId, Map<String, Object> args, 
                                      AiSkill.StreamingHandler handler) {
        AiSkill skill = skillRegistry.get(skillId);
        if (skill == null) {
            handler.onError("Skill not found: " + skillId);
            return;
        }
        
        if (!skill.supportsStreaming()) {
            // 降级为同步执行
            AiSkill.SkillResult result = executeSkill(skillId, args);
            if (result.isSuccess()) {
                handler.onComplete(result);
            } else {
                handler.onError(result.getErrorMessage());
            }
            return;
        }
        
        try {
            skill.executeStreaming(args, handler);
        } catch (Exception e) {
            log.error("Streaming skill execution failed: {}", skillId, e);
            handler.onError(e.getMessage());
        }
    }
    
    /**
     * 添加技能监听器
     */
    public void addListener(SkillListener listener) {
        listeners.computeIfAbsent(listener.getEventType().name(), k -> new ArrayList<>()).add(listener);
    }
    
    /**
     * 移除技能监听器
     */
    public void removeListener(SkillListener listener) {
        List<SkillListener> list = listeners.get(listener.getEventType().name());
        if (list != null) {
            list.remove(listener);
        }
    }
    
    private void notifyListeners(AiSkill skill, SkillEventType eventType) {
        List<SkillListener> list = listeners.get(eventType.name());
        if (list != null) {
            for (SkillListener listener : list) {
                try {
                    listener.onEvent(skill);
                } catch (Exception e) {
                    log.error("Listener notification failed", e);
                }
            }
        }
    }
    
    private void notifyExecutionStart(AiSkill skill, Map<String, Object> args) {
        List<SkillListener> list = listeners.get(SkillEventType.EXECUTION_START.name());
        if (list != null) {
            for (SkillListener listener : list) {
                try {
                    listener.onExecutionStart(skill, args);
                } catch (Exception e) {
                    log.error("Execution start listener failed", e);
                }
            }
        }
    }
    
    private void notifyExecutionComplete(AiSkill skill, AiSkill.SkillResult result) {
        List<SkillListener> list = listeners.get(SkillEventType.EXECUTION_COMPLETE.name());
        if (list != null) {
            for (SkillListener listener : list) {
                try {
                    listener.onExecutionComplete(skill, result);
                } catch (Exception e) {
                    log.error("Execution complete listener failed", e);
                }
            }
        }
    }
    
    private void notifyExecutionError(AiSkill skill, Exception error) {
        List<SkillListener> list = listeners.get(SkillEventType.EXECUTION_ERROR.name());
        if (list != null) {
            for (SkillListener listener : list) {
                try {
                    listener.onExecutionError(skill, error);
                } catch (Exception e) {
                    log.error("Execution error listener failed", e);
                }
            }
        }
    }
    
    /**
     * 技能事件类型
     */
    public enum SkillEventType {
        REGISTERED,
        UNREGISTERED,
        EXECUTION_START,
        EXECUTION_COMPLETE,
        EXECUTION_ERROR
    }
    
    /**
     * 技能监听器接口
     */
    public interface SkillListener {
        SkillEventType getEventType();
        default void onEvent(AiSkill skill) {}
        default void onExecutionStart(AiSkill skill, Map<String, Object> args) {}
        default void onExecutionComplete(AiSkill skill, AiSkill.SkillResult result) {}
        default void onExecutionError(AiSkill skill, Exception error) {}
    }
}
