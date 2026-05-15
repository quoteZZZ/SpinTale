package com.spintale.ai.agent.multi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多 Agent 协作编排器
 * 支持角色分工、黑板模式、辩论机制
 */
@Slf4j
@Service
public class MultiAgentOrchestrator {

    // 注册的专业 Agent 角色
    private final Map<String, AgentRole> registeredRoles = new ConcurrentHashMap<>();
    
    // 共享黑板（上下文）
    private final Blackboard blackboard = new Blackboard();

    /**
     * 注册专业 Agent 角色
     */
    public void registerRole(AgentRole role) {
        registeredRoles.put(role.name(), role);
        log.info("注册 Agent 角色：{} - {}", role.name(), role.description());
    }

    /**
     * 初始化默认角色
     */
    public void initializeDefaultRoles() {
        registerRole(new AgentRole("COMMANDER", "指挥官", "负责任务分解、分配和最终决策", true));
        registerRole(new AgentRole("RESEARCHER", "研究员", "负责信息检索、资料收集和事实核查", true));
        registerRole(new AgentRole("ARCHITECT", "架构师", "负责系统设计、技术方案规划", true));
        registerRole(new AgentRole("DEVELOPER", "开发者", "负责代码编写、调试和优化", true));
        registerRole(new AgentRole("REVIEWER", "审计员", "负责质量检查、错误发现和合规审查", true));
        registerRole(new AgentRole("CRITIC", "评论家", "负责提出批评意见、挑战假设", true));
        
        log.info("初始化 {} 个默认 Agent 角色", registeredRoles.size());
    }

    /**
     * 执行多 Agent 协作任务
     * @param task 任务描述
     * @return 协作结果
     */
    public CollaborationResult executeCollaborativeTask(String task) {
        log.info("启动多 Agent 协作任务：{}", task);
        
        // 1. 指挥官分解任务
        blackboard.addMessage("COMMANDER", "任务已接收，开始分解：" + task);
        List<String> subtasks = decomposeTask(task);
        blackboard.addData("subtasks", subtasks);
        
        log.info("任务分解为 {} 个子任务：{}", subtasks.size(), subtasks);
        
        // 2. 分配子任务给不同角色
        Map<String, String> assignments = assignTasks(subtasks);
        blackboard.addData("assignments", assignments);
        
        // 3. 各角色并行执行（简化为串行模拟）
        Map<String, String> results = new HashMap<>();
        for (Map.Entry<String, String> entry : assignments.entrySet()) {
            String role = entry.getKey();
            String subtask = entry.getValue();
            
            log.info("分配任务给 {}: {}", role, subtask);
            String result = executeSubtask(role, subtask);
            results.put(role, result);
            blackboard.addMessage(role, "完成任务：" + subtask + " -> " + result);
        }
        
        // 4. 辩论与评审阶段
        log.info("进入辩论评审阶段");
        String debateResult = conductDebate(results);
        blackboard.addMessage("DEBATE", "辩论结论：" + debateResult);
        
        // 5. 指挥官做最终决策
        String finalDecision = makeFinalDecision(task, results, debateResult);
        blackboard.addMessage("COMMANDER", "最终决策：" + finalDecision);
        
        return new CollaborationResult(
            true, 
            finalDecision, 
            results, 
            blackboard.getHistory(),
            registeredRoles.keySet()
        );
    }

    /**
     * 任务分解（简化实现，实际应由 LLM 完成）
     */
    private List<String> decomposeTask(String task) {
        // 示例分解逻辑
        List<String> subtasks = new ArrayList<>();
        
        if (task.contains("开发") || task.contains("实现")) {
            subtasks.add("需求分析");
            subtasks.add("技术选型");
            subtasks.add("代码实现");
            subtasks.add("测试验证");
        } else if (task.contains("分析") || task.contains("研究")) {
            subtasks.add("资料收集");
            subtasks.add("数据整理");
            subtasks.add("趋势分析");
            subtasks.add("报告撰写");
        } else {
            subtasks.add("问题理解");
            subtasks.add("方案制定");
            subtasks.add("执行计划");
        }
        
        return subtasks;
    }

    /**
     * 任务分配策略
     */
    private Map<String, String> assignTasks(List<String> subtasks) {
        Map<String, String> assignments = new HashMap<>();
        
        for (String subtask : subtasks) {
            if (subtask.contains("需求") || subtask.contains("分析")) {
                assignments.put("RESEARCHER", subtask);
            } else if (subtask.contains("技术") || subtask.contains("设计")) {
                assignments.put("ARCHITECT", subtask);
            } else if (subtask.contains("代码") || subtask.contains("实现")) {
                assignments.put("DEVELOPER", subtask);
            } else if (subtask.contains("测试") || subtask.contains("验证")) {
                assignments.put("REVIEWER", subtask);
            } else {
                assignments.put("COMMANDER", subtask);
            }
        }
        
        return assignments;
    }

    /**
     * 执行子任务（模拟，实际应调用对应 Agent）
     */
    private String executeSubtask(String role, String subtask) {
        log.info("{} 正在执行：{}", role, subtask);
        
        // 模拟执行延迟
        try {
            Thread.sleep(100); // 100ms 模拟
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return "已完成：" + subtask + " (by " + role + ")";
    }

    /**
     * 开展辩论（简化实现）
     */
    private String conductDebate(Map<String, String> results) {
        StringBuilder debate = new StringBuilder();
        
        // 评论家提出质疑
        if (results.containsKey("DEVELOPER")) {
            debate.append("评论家指出：").append(results.get("DEVELOPER")).append(" 可能存在边界情况未处理。");
        }
        
        // 审计员进行检查
        if (results.containsKey("REVIEWER")) {
            debate.append(" 审计员确认：质量检查通过。");
        }
        
        return debate.toString();
    }

    /**
     * 最终决策
     */
    private String makeFinalDecision(String originalTask, Map<String, String> results, String debate) {
        StringBuilder decision = new StringBuilder();
        decision.append("任务 '").append(originalTask).append("' 已完成。\n");
        decision.append("各角色贡献：\n");
        
        for (Map.Entry<String, String> entry : results.entrySet()) {
            decision.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        decision.append("辩论结论：").append(debate).append("\n");
        decision.append("最终输出：任务成功完成，所有子任务已验证。");
        
        return decision.toString();
    }

    /**
     * 获取黑板历史
     */
    public List<BlackboardMessage> getBlackboardHistory() {
        return blackboard.getHistory();
    }

    /**
     * 获取统计信息
     */
    public CollaborationStats getStats() {
        return new CollaborationStats(registeredRoles.size(), blackboard.getMessageCount());
    }

    // ==================== 数据模型 ====================

    public record AgentRole(
        String name,
        String displayName,
        String description,
        boolean isDefault
    ) {}

    public record BlackboardMessage(
        String sender,
        String content,
        long timestamp
    ) {}

    public record CollaborationResult(
        boolean success,
        String finalOutput,
        Map<String, String> individualResults,
        List<BlackboardMessage> history,
        Set<String> participatingRoles
    ) {}

    public record CollaborationStats(int registeredRoles, int messages) {}

    /**
     * 共享黑板类
     */
    public static class Blackboard {
        private final List<BlackboardMessage> history = Collections.synchronizedList(new ArrayList<>());
        private final Map<String, Object> data = new ConcurrentHashMap<>();

        public void addMessage(String sender, String content) {
            history.add(new BlackboardMessage(sender, content, System.currentTimeMillis()));
            log.debug("黑板消息 [{}]: {}", sender, content);
        }

        public void addData(String key, Object value) {
            data.put(key, value);
            log.debug("黑板数据 [{}]: {}", key, value);
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
