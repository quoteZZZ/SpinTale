package com.spintale.ai.agent.tool;

import lombok.Getter;

@Getter
public enum ToolRiskLevel
{
    SAFE(0, "安全", false, false,
            "只读操作，无副作用，如查询、搜索"),
    
    LOW(1, "低风险", false, false,
            "低影响操作，如发送通知、创建草稿"),
    
    MEDIUM(2, "中等风险", false, true,
            "可能影响外部系统，如发送邮件、更新配置"),
    
    HIGH(3, "高风险", true, true,
            "重要业务操作，如创建订单、修改关键数据"),
    
    CRITICAL(4, "极高风险", true, true,
            "不可逆操作，如删除数据、支付、权限变更");

    private final int level;
    private final String description;
    private final boolean requiresApproval;
    private final boolean requiresAudit;
    private final String guidance;

    ToolRiskLevel(int level, String description, 
            boolean requiresApproval, boolean requiresAudit, String guidance)
    {
        this.level = level;
        this.description = description;
        this.requiresApproval = requiresApproval;
        this.requiresAudit = requiresAudit;
        this.guidance = guidance;
    }

    public boolean isHigherThan(ToolRiskLevel other)
    {
        return this.level > other.level;
    }

    public boolean isLowerThan(ToolRiskLevel other)
    {
        return this.level < other.level;
    }

    public boolean requiresHumanApproval()
    {
        return requiresApproval;
    }

    public static ToolRiskLevel fromLevel(int level)
    {
        for (ToolRiskLevel risk : values())
        {
            if (risk.level == level)
            {
                return risk;
            }
        }
        return CRITICAL;
    }
}
