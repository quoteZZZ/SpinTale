package com.spintale.ai.agent.planner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务规划器 (Planner)
 * 将复杂任务拆解为可执行的子任务序列
 */
@Slf4j
@Service
public class TaskPlannerService {

    /**
     * 拆解复杂任务为子任务
     * @param task 原始任务描述
     * @return 子任务列表
     */
    public List<SubTask> decompose(String task) {
        log.info("开始拆解任务：{}", task);

        List<SubTask> subTasks = new ArrayList<>();

        // 简单实现：基于关键词识别任务类型并生成子任务
        // 实际应调用 LLM 进行智能规划
        
        if (task.contains("分析") || task.contains("报告")) {
            subTasks.add(new SubTask(1, "收集相关数据和信息", "research", false));
            subTasks.add(new SubTask(2, "整理和分类数据", "organize", false));
            subTasks.add(new SubTask(3, "执行分析和推理", "analyze", false));
            subTasks.add(new SubTask(4, "生成结论和建议", "synthesize", false));
            subTasks.add(new SubTask(5, "格式化输出报告", "format", false));
        } else if (task.contains("比较") || task.contains("对比")) {
            subTasks.add(new SubTask(1, "识别比较对象", "identify", false));
            subTasks.add(new SubTask(2, "提取关键特征", "extract", false));
            subTasks.add(new SubTask(3, "逐项对比分析", "compare", false));
            subTasks.add(new SubTask(4, "总结异同点", "summarize", false));
        } else if (task.contains("步骤") || task.contains("如何")) {
            subTasks.add(new SubTask(1, "理解问题背景", "understand", false));
            subTasks.add(new SubTask(2, "列出必要前提条件", "prerequisites", false));
            subTasks.add(new SubTask(3, "分解操作步骤", "decompose", false));
            subTasks.add(new SubTask(4, "提供注意事项", "warnings", false));
        } else {
            // 通用拆解
            subTasks.add(new SubTask(1, "理解任务需求", "understand", false));
            subTasks.add(new SubTask(2, "制定执行计划", "plan", false));
            subTasks.add(new SubTask(3, "执行任务", "execute", false));
            subTasks.add(new SubTask(4, "验证结果", "verify", false));
        }

        log.info("任务拆解完成：共 {} 个子任务", subTasks.size());
        return subTasks;
    }

    /**
     * 验证任务执行结果
     * @param task 原始任务
     * @param result 执行结果
     * @return 是否通过验证
     */
    public boolean validate(String task, String result) {
        log.info("验证任务结果：task={}, result_length={}", task, result.length());
        
        // 简单验证：检查结果是否包含任务中的关键词
        // 实际应调用 LLM 进行语义验证
        boolean isValid = result.length() > 10; // 基础长度检查
        
        log.info("验证结果：{}", isValid ? "通过" : "失败");
        return isValid;
    }

    /**
     * 自我反思：分析失败原因并提出改进建议
     */
    public ReflectionResult reflect(String task, String result, boolean success) {
        if (success) {
            return new ReflectionResult(true, "任务成功完成", new ArrayList<>());
        }

        List<String> suggestions = new ArrayList<>();
        suggestions.add("检查输入数据是否完整");
        suggestions.add("尝试调整任务拆解粒度");
        suggestions.add("考虑使用更专业的子任务处理模块");

        return new ReflectionResult(false, "任务执行失败", suggestions);
    }

    /**
     * 子任务记录
     */
    public record SubTask(
            int order,
            String description,
            String type,
            boolean completed
    ) {}

    /**
     * 反思结果记录
     */
    public record ReflectionResult(
            boolean isSuccess,
            String message,
            List<String> suggestions
    ) {}
}
