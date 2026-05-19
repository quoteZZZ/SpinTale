package com.spintale.ai.capability.observability;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A/B 测试管理器
 * 
 * 管理多个A/B测试实验，提供变体选择和结果收集
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "spintale.ai.experiment", name = "enabled", havingValue = "true")
public class ExperimentManager {
    
    private final Map<String, Experiment> experiments = new ConcurrentHashMap<>();
    
    /**
     * 注册实验
     */
    public void registerExperiment(Experiment experiment) {
        experiments.put(experiment.getExperimentId(), experiment);
        log.info("Registered A/B test experiment: {}", experiment.getName());
    }
    
    /**
     * 获取实验
     */
    public Experiment getExperiment(String experimentId) {
        return experiments.get(experimentId);
    }
    
    /**
     * 为用户选择变体
     */
    public String selectVariant(String experimentId, String userId) {
        Experiment experiment = experiments.get(experimentId);
        if (experiment == null) {
            log.warn("Experiment not found: {}", experimentId);
            return null;
        }
        
        return experiment.selectVariant(userId);
    }
    
    /**
     * 记录请求结果
     */
    public void recordResult(String experimentId, String variantId, Map<String, Object> metrics) {
        Experiment experiment = experiments.get(experimentId);
        if (experiment != null) {
            experiment.recordRequest(variantId, metrics);
        }
    }
    
    /**
     * 获取实验结果
     */
    public Experiment.ExperimentResult getResults(String experimentId) {
        Experiment experiment = experiments.get(experimentId);
        if (experiment == null) {
            return null;
        }
        
        return experiment.getResult();
    }
    
    /**
     * 获取所有实验
     */
    public Map<String, Experiment> getAllExperiments() {
        return Map.copyOf(experiments);
    }
    
    /**
     * 激活实验
     */
    public void activateExperiment(String experimentId) {
        Experiment experiment = experiments.get(experimentId);
        if (experiment != null) {
            experiment.setActive(true);
            experiment.setStartTime(System.currentTimeMillis());
            log.info("Activated experiment: {}", experiment.getName());
        }
    }
    
    /**
     * 停止实验
     */
    public void stopExperiment(String experimentId) {
        Experiment experiment = experiments.get(experimentId);
        if (experiment != null) {
            experiment.setActive(false);
            experiment.setEndTime(System.currentTimeMillis());
            log.info("Stopped experiment: {}", experiment.getName());
        }
    }
}
