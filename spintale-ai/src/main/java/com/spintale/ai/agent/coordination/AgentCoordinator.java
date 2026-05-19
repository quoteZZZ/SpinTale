package com.spintale.ai.agent.coordination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Lightweight multi-agent coordination service.
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "spintale.ai.agent.coordination", name = "enabled", havingValue = "true")
public class AgentCoordinator {

    private final Map<String, AgentRole> registeredRoles = new ConcurrentHashMap<>();
    private final Blackboard blackboard = new Blackboard();

    @PostConstruct
    public void initializeDefaultRoles() {
        registerRole(new AgentRole("COMMANDER", "Commander", "Breaks down tasks and owns final decisions", true));
        registerRole(new AgentRole("RESEARCHER", "Researcher", "Collects facts and context", true));
        registerRole(new AgentRole("ARCHITECT", "Architect", "Plans technical structure", true));
        registerRole(new AgentRole("DEVELOPER", "Developer", "Implements the solution", true));
        registerRole(new AgentRole("REVIEWER", "Reviewer", "Checks quality and edge cases", true));
        registerRole(new AgentRole("CRITIC", "Critic", "Challenges assumptions", true));
    }

    public void registerRole(AgentRole role) {
        if (role != null) {
            registeredRoles.put(role.name(), role);
        }
    }

    public CollaborationResult executeCollaborativeTask(String task) {
        String safeTask = task == null ? "" : task;
        if (safeTask.isBlank()) {
            return new CollaborationResult(false, "Task is empty", Map.of(), blackboard.getHistory(), Set.of());
        }
        blackboard.addMessage("COMMANDER", "Task accepted: " + safeTask);

        List<String> subtasks = decomposeTask(safeTask);
        Map<String, String> assignments = assignTasks(subtasks);
        Map<String, String> results = new HashMap<>();

        for (Map.Entry<String, String> entry : assignments.entrySet()) {
            String result = executeSubtask(entry.getKey(), entry.getValue());
            results.put(entry.getKey(), result);
            blackboard.addMessage(entry.getKey(), result);
        }

        String debate = conductDebate(results);
        String finalDecision = makeFinalDecision(safeTask, results, debate);
        blackboard.addMessage("COMMANDER", finalDecision);

        return new CollaborationResult(true, finalDecision, results, blackboard.getHistory(), registeredRoles.keySet());
    }

    private List<String> decomposeTask(String task) {
        List<String> subtasks = new ArrayList<>();
        String lower = task.toLowerCase();
        if (lower.contains("develop") || lower.contains("implement") || lower.contains("code")) {
            subtasks.add("Requirements analysis");
            subtasks.add("Technical design");
            subtasks.add("Implementation");
            subtasks.add("Verification");
        } else if (lower.contains("analyze") || lower.contains("research")) {
            subtasks.add("Context collection");
            subtasks.add("Fact organization");
            subtasks.add("Risk analysis");
            subtasks.add("Summary");
        } else {
            subtasks.add("Problem understanding");
            subtasks.add("Solution planning");
            subtasks.add("Execution");
        }
        return subtasks;
    }

    private Map<String, String> assignTasks(List<String> subtasks) {
        Map<String, String> assignments = new HashMap<>();
        for (String subtask : subtasks) {
            String lower = subtask.toLowerCase();
            if (lower.contains("analysis") || lower.contains("collection") || lower.contains("fact")) {
                assignments.put("RESEARCHER", mergeAssignment(assignments.get("RESEARCHER"), subtask));
            } else if (lower.contains("design") || lower.contains("planning")) {
                assignments.put("ARCHITECT", mergeAssignment(assignments.get("ARCHITECT"), subtask));
            } else if (lower.contains("implementation") || lower.contains("execution")) {
                assignments.put("DEVELOPER", mergeAssignment(assignments.get("DEVELOPER"), subtask));
            } else if (lower.contains("verification") || lower.contains("risk")) {
                assignments.put("REVIEWER", mergeAssignment(assignments.get("REVIEWER"), subtask));
            } else {
                assignments.put("COMMANDER", mergeAssignment(assignments.get("COMMANDER"), subtask));
            }
        }
        return assignments;
    }

    private String mergeAssignment(String existing, String subtask) {
        return existing == null ? subtask : existing + "; " + subtask;
    }

    private String executeSubtask(String role, String subtask) {
        return role + " completed: " + subtask;
    }

    private String conductDebate(Map<String, String> results) {
        if (results.containsKey("DEVELOPER")) {
            return "Review the developer result for edge cases before release.";
        }
        return "No debate required for this task shape.";
    }

    private String makeFinalDecision(String originalTask, Map<String, String> results, String debate) {
        return "Task completed: " + originalTask
                + "; participants=" + results.keySet()
                + "; review=" + debate;
    }

    public List<BlackboardMessage> getBlackboardHistory() {
        return blackboard.getHistory();
    }

    public CollaborationStats getStats() {
        return new CollaborationStats(registeredRoles.size(), blackboard.getMessageCount());
    }

    public record AgentRole(String name, String displayName, String description, boolean isDefault) {}

    public record BlackboardMessage(String sender, String content, long timestamp) {}

    public record CollaborationResult(
            boolean success,
            String finalOutput,
            Map<String, String> individualResults,
            List<BlackboardMessage> history,
            Set<String> participatingRoles) {}

    public record CollaborationStats(int registeredRoles, int messages) {}

    public static class Blackboard {
        private final List<BlackboardMessage> history = Collections.synchronizedList(new ArrayList<>());
        private final Map<String, Object> data = new ConcurrentHashMap<>();

        public void addMessage(String sender, String content) {
            history.add(new BlackboardMessage(sender, content, System.currentTimeMillis()));
        }

        public void addData(String key, Object value) {
            data.put(key, value);
        }

        public List<BlackboardMessage> getHistory() {
            return new ArrayList<>(history);
        }

        public int getMessageCount() {
            return history.size();
        }

        public Object getData(String key) {
            return data.get(key);
        }
    }
}
