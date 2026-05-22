-- SpinTale AI 模块数据库表结构
-- 根据AI_PROPOSAL.md第8节数据库规划创建
-- 版本: 3.9.2
-- 日期: 2026-05-22

-- =====================================================
-- 1. Runtime / 观测表
-- =====================================================

-- AI运行记录表
CREATE TABLE IF NOT EXISTS ai_run (
    run_id VARCHAR(64) PRIMARY KEY COMMENT '运行ID',
    trace_id VARCHAR(64) COMMENT '追踪ID',
    parent_run_id VARCHAR(64) COMMENT '父运行ID',
    run_type VARCHAR(32) COMMENT '运行类型(CHAT/RAG/AGENT/WORKFLOW)',
    model VARCHAR(128) COMMENT '使用的模型',
    provider VARCHAR(64) COMMENT '供应商',
    user_id BIGINT COMMENT '用户ID',
    session_id VARCHAR(64) COMMENT '会话ID',
    status VARCHAR(32) COMMENT '状态(PENDING/RUNNING/SUCCEEDED/FAILED/TIMEOUT)',
    input_text TEXT COMMENT '输入内容',
    output_text TEXT COMMENT '输出内容',
    input_tokens BIGINT DEFAULT 0 COMMENT '输入Token数',
    output_tokens BIGINT DEFAULT 0 COMMENT '输出Token数',
    total_cost DECIMAL(10,6) DEFAULT 0 COMMENT '总成本',
    duration_ms BIGINT DEFAULT 0 COMMENT '耗时(毫秒)',
    error_message TEXT COMMENT '错误信息',
    error_code VARCHAR(64) COMMENT '错误码',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_trace_id (trace_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI运行记录表';

-- AI运行Span表
CREATE TABLE IF NOT EXISTS ai_run_span (
    span_id VARCHAR(64) PRIMARY KEY COMMENT 'SpanID',
    run_id VARCHAR(64) NOT NULL COMMENT '运行ID',
    parent_span_id VARCHAR(64) COMMENT '父SpanID',
    span_name VARCHAR(128) COMMENT 'Span名称',
    span_type VARCHAR(32) COMMENT 'Span类型',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration_ms BIGINT DEFAULT 0 COMMENT '耗时(毫秒)',
    status VARCHAR(32) COMMENT '状态',
    attributes JSON COMMENT '属性',
    INDEX idx_run_id (run_id),
    INDEX idx_parent_span_id (parent_span_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI运行Span表';

-- AI成本使用统计表
CREATE TABLE IF NOT EXISTS ai_cost_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT COMMENT '用户ID',
    run_id VARCHAR(64) COMMENT '运行ID',
    model VARCHAR(128) COMMENT '模型',
    provider VARCHAR(64) COMMENT '供应商',
    input_tokens BIGINT DEFAULT 0 COMMENT '输入Token',
    output_tokens BIGINT DEFAULT 0 COMMENT '输出Token',
    total_tokens BIGINT DEFAULT 0 COMMENT '总Token',
    cost DECIMAL(10,6) DEFAULT 0 COMMENT '成本',
    currency VARCHAR(16) DEFAULT 'USD' COMMENT '货币',
    usage_date DATE COMMENT '使用日期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_model (model),
    INDEX idx_usage_date (usage_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI成本使用统计表';

-- =====================================================
-- 2. Provider / 模型配置表
-- =====================================================

-- AI供应商配置表
CREATE TABLE IF NOT EXISTS ai_provider_config (
    provider_id VARCHAR(64) PRIMARY KEY COMMENT '供应商ID',
    provider_name VARCHAR(128) NOT NULL COMMENT '供应商名称',
    provider_type VARCHAR(32) NOT NULL COMMENT '供应商类型(OPENAI/OLLAMA/AZURE/etc)',
    base_url VARCHAR(512) COMMENT 'API地址',
    api_key_ref VARCHAR(256) COMMENT 'API密钥引用',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    health_status VARCHAR(32) DEFAULT 'UNKNOWN' COMMENT '健康状态',
    last_health_check DATETIME COMMENT '最后健康检查时间',
    config_json JSON COMMENT '配置JSON',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_enabled (enabled),
    INDEX idx_health_status (health_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI供应商配置表';

-- AI模型配置表
CREATE TABLE IF NOT EXISTS ai_model_config (
    model_id VARCHAR(64) PRIMARY KEY COMMENT '模型ID',
    model_name VARCHAR(128) NOT NULL COMMENT '模型名称',
    provider_id VARCHAR(64) NOT NULL COMMENT '供应商ID',
    model_type VARCHAR(32) NOT NULL COMMENT '模型类型(CHAT/EMBEDDING/RERANK)',
    max_context_tokens INT COMMENT '最大上下文Token',
    max_output_tokens INT COMMENT '最大输出Token',
    input_price_per_1k DECIMAL(10,6) COMMENT '输入每1k Token价格',
    output_price_per_1k DECIMAL(10,6) COMMENT '输出每1k Token价格',
    supports_streaming TINYINT DEFAULT 0 COMMENT '是否支持流式',
    supports_function_calling TINYINT DEFAULT 0 COMMENT '是否支持函数调用',
    supports_vision TINYINT DEFAULT 0 COMMENT '是否支持视觉',
    supports_json_mode TINYINT DEFAULT 0 COMMENT '是否支持JSON模式',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    capabilities JSON COMMENT '能力列表',
    metadata JSON COMMENT '元数据',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_provider_id (provider_id),
    INDEX idx_model_type (model_type),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型配置表';

-- AI路由策略表
CREATE TABLE IF NOT EXISTS ai_routing_policy (
    policy_id VARCHAR(64) PRIMARY KEY COMMENT '策略ID',
    policy_name VARCHAR(128) NOT NULL COMMENT '策略名称',
    strategy VARCHAR(32) NOT NULL COMMENT '策略类型',
    model_type VARCHAR(32) COMMENT '模型类型',
    rules_json JSON COMMENT '路由规则JSON',
    priority INT DEFAULT 0 COMMENT '优先级',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI路由策略表';

-- =====================================================
-- 3. RAG 知识库表
-- =====================================================

-- 知识库表
CREATE TABLE IF NOT EXISTS ai_knowledge_base (
    kb_id VARCHAR(64) PRIMARY KEY COMMENT '知识库ID',
    kb_name VARCHAR(256) NOT NULL COMMENT '知识库名称',
    description TEXT COMMENT '描述',
    embedding_model VARCHAR(128) COMMENT 'Embedding模型',
    vector_dimension INT COMMENT '向量维度',
    chunk_size INT DEFAULT 500 COMMENT '分块大小',
    chunk_overlap INT DEFAULT 50 COMMENT '分块重叠',
    document_count INT DEFAULT 0 COMMENT '文档数量',
    chunk_count INT DEFAULT 0 COMMENT '分块数量',
    status VARCHAR(32) DEFAULT 'ACTIVE' COMMENT '状态',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_create_by (create_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';

-- 文档表
CREATE TABLE IF NOT EXISTS ai_document (
    document_id VARCHAR(64) PRIMARY KEY COMMENT '文档ID',
    kb_id VARCHAR(64) NOT NULL COMMENT '知识库ID',
    name VARCHAR(512) NOT NULL COMMENT '文档名称',
    source VARCHAR(1024) COMMENT '来源路径',
    source_type VARCHAR(32) COMMENT '来源类型',
    mime_type VARCHAR(128) COMMENT 'MIME类型',
    file_size BIGINT COMMENT '文件大小',
    status VARCHAR(32) DEFAULT 'UPLOADED' COMMENT '状态',
    version VARCHAR(64) COMMENT '版本',
    checksum VARCHAR(128) COMMENT '校验和',
    chunk_count INT DEFAULT 0 COMMENT '分块数量',
    total_tokens BIGINT DEFAULT 0 COMMENT '总Token数',
    error_message TEXT COMMENT '错误信息',
    processed_time DATETIME COMMENT '处理完成时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_kb_id (kb_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档表';

-- 文档分块表
CREATE TABLE IF NOT EXISTS ai_document_chunk (
    chunk_id VARCHAR(64) PRIMARY KEY COMMENT '分块ID',
    document_id VARCHAR(64) NOT NULL COMMENT '文档ID',
    kb_id VARCHAR(64) NOT NULL COMMENT '知识库ID',
    chunk_index INT NOT NULL COMMENT '分块索引',
    content TEXT NOT NULL COMMENT '内容',
    token_count INT DEFAULT 0 COMMENT 'Token数',
    char_start INT COMMENT '起始字符位置',
    char_end INT COMMENT '结束字符位置',
    page_number INT COMMENT '页码',
    section_path VARCHAR(512) COMMENT '章节路径',
    metadata JSON COMMENT '元数据',
    checksum VARCHAR(128) COMMENT '校验和',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_document_id (document_id),
    INDEX idx_kb_id (kb_id),
    INDEX idx_chunk_index (chunk_index),
    FULLTEXT INDEX ft_content (content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档分块表';

-- 文档索引任务表
CREATE TABLE IF NOT EXISTS ai_document_index_job (
    job_id VARCHAR(64) PRIMARY KEY COMMENT '任务ID',
    document_id VARCHAR(64) NOT NULL COMMENT '文档ID',
    kb_id VARCHAR(64) NOT NULL COMMENT '知识库ID',
    job_type VARCHAR(32) COMMENT '任务类型(INDEX/REINDEX/DELETE)',
    status VARCHAR(32) DEFAULT 'PENDING' COMMENT '状态',
    progress INT DEFAULT 0 COMMENT '进度',
    total_chunks INT DEFAULT 0 COMMENT '总分块数',
    processed_chunks INT DEFAULT 0 COMMENT '已处理分块数',
    error_message TEXT COMMENT '错误信息',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_document_id (document_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档索引任务表';

-- 检索追踪表
CREATE TABLE IF NOT EXISTS ai_retrieval_trace (
    trace_id VARCHAR(64) PRIMARY KEY COMMENT '追踪ID',
    run_id VARCHAR(64) COMMENT '运行ID',
    kb_id VARCHAR(64) COMMENT '知识库ID',
    query_text TEXT COMMENT '查询文本',
    query_rewrite TEXT COMMENT '重写查询',
    retrieved_count INT DEFAULT 0 COMMENT '检索数量',
    reranked_count INT DEFAULT 0 COMMENT '重排数量',
    top_k INT COMMENT 'TopK',
    retrieval_time_ms BIGINT COMMENT '检索耗时',
    rerank_time_ms BIGINT COMMENT '重排耗时',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_run_id (run_id),
    INDEX idx_kb_id (kb_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检索追踪表';

-- =====================================================
-- 4. Agent / Tool / Memory表
-- =====================================================

-- Agent配置表
CREATE TABLE IF NOT EXISTS ai_agent (
    agent_id VARCHAR(64) PRIMARY KEY COMMENT 'AgentID',
    agent_name VARCHAR(256) NOT NULL COMMENT 'Agent名称',
    agent_type VARCHAR(32) NOT NULL COMMENT 'Agent类型',
    description TEXT COMMENT '描述',
    model VARCHAR(128) COMMENT '使用的模型',
    system_prompt TEXT COMMENT '系统提示词',
    max_steps INT DEFAULT 10 COMMENT '最大步骤数',
    timeout_ms BIGINT DEFAULT 60000 COMMENT '超时时间',
    tool_ids JSON COMMENT '工具ID列表',
    memory_config JSON COMMENT '记忆配置',
    status VARCHAR(32) DEFAULT 'ACTIVE' COMMENT '状态',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent配置表';

-- Agent运行历史表
CREATE TABLE IF NOT EXISTS ai_agent_run (
    run_id VARCHAR(64) PRIMARY KEY COMMENT '运行ID',
    agent_id VARCHAR(64) NOT NULL COMMENT 'AgentID',
    user_id BIGINT COMMENT '用户ID',
    session_id VARCHAR(64) COMMENT '会话ID',
    model VARCHAR(128) COMMENT '模型',
    status VARCHAR(32) COMMENT '状态',
    input_text TEXT COMMENT '输入',
    output_text TEXT COMMENT '输出',
    step_count INT DEFAULT 0 COMMENT '步骤数',
    total_input_tokens BIGINT DEFAULT 0 COMMENT '总输入Token',
    total_output_tokens BIGINT DEFAULT 0 COMMENT '总输出Token',
    total_cost DECIMAL(10,6) DEFAULT 0 COMMENT '总成本',
    duration_ms BIGINT DEFAULT 0 COMMENT '耗时',
    error_message TEXT COMMENT '错误信息',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_agent_id (agent_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent运行历史表';

-- Agent步骤表
CREATE TABLE IF NOT EXISTS ai_agent_step (
    step_id VARCHAR(64) PRIMARY KEY COMMENT '步骤ID',
    run_id VARCHAR(64) NOT NULL COMMENT '运行ID',
    step_number INT NOT NULL COMMENT '步骤编号',
    step_type VARCHAR(32) COMMENT '步骤类型',
    name VARCHAR(256) COMMENT '名称',
    thought TEXT COMMENT '思考内容',
    action VARCHAR(256) COMMENT '动作',
    action_input TEXT COMMENT '动作输入',
    action_output TEXT COMMENT '动作输出',
    tool_name VARCHAR(128) COMMENT '工具名称',
    tool_args JSON COMMENT '工具参数',
    tool_result TEXT COMMENT '工具结果',
    input_tokens BIGINT DEFAULT 0 COMMENT '输入Token',
    output_tokens BIGINT DEFAULT 0 COMMENT '输出Token',
    cost DECIMAL(10,6) DEFAULT 0 COMMENT '成本',
    status VARCHAR(32) COMMENT '状态',
    error_message TEXT COMMENT '错误信息',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration_ms BIGINT DEFAULT 0 COMMENT '耗时',
    INDEX idx_run_id (run_id),
    INDEX idx_step_number (step_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent步骤表';

-- 工具表
CREATE TABLE IF NOT EXISTS ai_tool (
    tool_id VARCHAR(64) PRIMARY KEY COMMENT '工具ID',
    tool_name VARCHAR(128) NOT NULL COMMENT '工具名称',
    description TEXT COMMENT '描述',
    category VARCHAR(64) COMMENT '分类',
    risk_level VARCHAR(32) DEFAULT 'LOW' COMMENT '风险等级',
    requires_approval TINYINT DEFAULT 0 COMMENT '是否需要审批',
    permission_code VARCHAR(128) COMMENT '权限码',
    input_schema JSON COMMENT '输入Schema',
    output_schema JSON COMMENT '输出Schema',
    implementation_class VARCHAR(512) COMMENT '实现类',
    config_json JSON COMMENT '配置',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category (category),
    INDEX idx_risk_level (risk_level),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工具表';

-- 工具调用记录表
CREATE TABLE IF NOT EXISTS ai_tool_call (
    call_id VARCHAR(64) PRIMARY KEY COMMENT '调用ID',
    run_id VARCHAR(64) COMMENT '运行ID',
    step_id VARCHAR(64) COMMENT '步骤ID',
    tool_id VARCHAR(64) COMMENT '工具ID',
    tool_name VARCHAR(128) COMMENT '工具名称',
    args JSON COMMENT '参数',
    result TEXT COMMENT '结果',
    status VARCHAR(32) COMMENT '状态',
    risk_level VARCHAR(32) COMMENT '风险等级',
    approval_status VARCHAR(32) COMMENT '审批状态',
    approved_by BIGINT COMMENT '审批人',
    approval_time DATETIME COMMENT '审批时间',
    duration_ms BIGINT DEFAULT 0 COMMENT '耗时',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_run_id (run_id),
    INDEX idx_tool_id (tool_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工具调用记录表';

-- 记忆条目表
CREATE TABLE IF NOT EXISTS ai_memory_entry (
    entry_id VARCHAR(64) PRIMARY KEY COMMENT '条目ID',
    session_id VARCHAR(64) COMMENT '会话ID',
    user_id BIGINT COMMENT '用户ID',
    agent_id VARCHAR(64) COMMENT 'AgentID',
    memory_type VARCHAR(32) COMMENT '记忆类型',
    role VARCHAR(32) COMMENT '角色',
    content TEXT COMMENT '内容',
    metadata JSON COMMENT '元数据',
    importance DECIMAL(3,2) DEFAULT 0.5 COMMENT '重要性',
    expires_at DATETIME COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_agent_id (agent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='记忆条目表';

-- Agent检查点表
CREATE TABLE IF NOT EXISTS ai_agent_checkpoint (
    checkpoint_id VARCHAR(64) PRIMARY KEY COMMENT '检查点ID',
    run_id VARCHAR(64) NOT NULL COMMENT '运行ID',
    agent_id VARCHAR(64) COMMENT 'AgentID',
    step_number INT COMMENT '步骤编号',
    checkpoint_type VARCHAR(32) COMMENT '检查点类型',
    status VARCHAR(32) DEFAULT 'ACTIVE' COMMENT '状态',
    state_json JSON COMMENT '状态JSON',
    current_thought TEXT COMMENT '当前思考',
    next_action VARCHAR(256) COMMENT '下一个动作',
    action_args JSON COMMENT '动作参数',
    reason TEXT COMMENT '原因',
    user_id BIGINT COMMENT '用户ID',
    expires_at DATETIME COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_run_id (run_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent检查点表';

-- =====================================================
-- 5. 审批表
-- =====================================================

-- 审批请求表
CREATE TABLE IF NOT EXISTS ai_approval_request (
    request_id VARCHAR(64) PRIMARY KEY COMMENT '请求ID',
    run_id VARCHAR(64) COMMENT '运行ID',
    step_id VARCHAR(64) COMMENT '步骤ID',
    tool_name VARCHAR(128) COMMENT '工具名称',
    tool_args JSON COMMENT '工具参数',
    tool_description TEXT COMMENT '工具描述',
    risk_level VARCHAR(32) COMMENT '风险等级',
    requester_id BIGINT COMMENT '请求人ID',
    requester_name VARCHAR(128) COMMENT '请求人名称',
    status VARCHAR(32) DEFAULT 'PENDING' COMMENT '状态',
    approver_id BIGINT COMMENT '审批人ID',
    approver_name VARCHAR(128) COMMENT '审批人名称',
    approval_reason TEXT COMMENT '审批原因',
    approval_time DATETIME COMMENT '审批时间',
    timeout_seconds INT DEFAULT 3600 COMMENT '超时秒数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_status (status),
    INDEX idx_requester_id (requester_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批请求表';

-- =====================================================
-- 6. 评估表
-- =====================================================

-- 评估数据集表
CREATE TABLE IF NOT EXISTS ai_eval_dataset (
    dataset_id VARCHAR(64) PRIMARY KEY COMMENT '数据集ID',
    dataset_name VARCHAR(256) NOT NULL COMMENT '数据集名称',
    description TEXT COMMENT '描述',
    case_count INT DEFAULT 0 COMMENT '用例数量',
    status VARCHAR(32) DEFAULT 'ACTIVE' COMMENT '状态',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估数据集表';

-- 评估用例表
CREATE TABLE IF NOT EXISTS ai_eval_case (
    case_id VARCHAR(64) PRIMARY KEY COMMENT '用例ID',
    dataset_id VARCHAR(64) NOT NULL COMMENT '数据集ID',
    case_name VARCHAR(256) COMMENT '用例名称',
    input_text TEXT COMMENT '输入',
    expected_output TEXT COMMENT '期望输出',
    context TEXT COMMENT '上下文',
    metadata JSON COMMENT '元数据',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_dataset_id (dataset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估用例表';

-- 评估结果表
CREATE TABLE IF NOT EXISTS ai_eval_result (
    result_id VARCHAR(64) PRIMARY KEY COMMENT '结果ID',
    dataset_id VARCHAR(64) COMMENT '数据集ID',
    case_id VARCHAR(64) COMMENT '用例ID',
    run_id VARCHAR(64) COMMENT '运行ID',
    actual_output TEXT COMMENT '实际输出',
    metrics JSON COMMENT '评估指标',
    overall_score DECIMAL(5,2) COMMENT '总体分数',
    feedback TEXT COMMENT '反馈',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_dataset_id (dataset_id),
    INDEX idx_case_id (case_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估结果表';

-- =====================================================
-- 7. 用户反馈表
-- =====================================================

CREATE TABLE IF NOT EXISTS ai_user_feedback (
    feedback_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '反馈ID',
    run_id VARCHAR(64) COMMENT '运行ID',
    user_id BIGINT COMMENT '用户ID',
    feedback_type VARCHAR(32) COMMENT '反馈类型',
    rating INT COMMENT '评分(1-5)',
    comment TEXT COMMENT '评论',
    is_positive TINYINT COMMENT '是否正面',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_run_id (run_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户反馈表';
