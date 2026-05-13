-- ============================================================================
-- SpinTale AI 模块数据库扩展脚本
-- 目标：支持长期记忆、RAG 检索、Agent 工作流、技能系统、幻觉检测
-- 日期：2026-04-17
-- ============================================================================

-- ----------------------------
-- 21、AI 对话会话表
-- ----------------------------
DROP TABLE IF EXISTS `ai_conversation`;
CREATE TABLE `ai_conversation` (
  `conversation_id`   bigint(20)      NOT NULL AUTO_INCREMENT  COMMENT '会话 ID',
  `user_id`           bigint(20)      NOT NULL                 COMMENT '用户 ID',
  `session_id`        varchar(64)     NOT NULL                 COMMENT '会话标识（UUID）',
  `title`             varchar(200)    DEFAULT ''               COMMENT '会话标题',
  `model_name`        varchar(50)     DEFAULT 'gpt-3.5-turbo'  COMMENT '使用的 AI 模型',
  `temperature`       decimal(3,2)    DEFAULT 0.7              COMMENT '温度参数',
  `max_tokens`        int(11)         DEFAULT 2048             COMMENT '最大 token 数',
  `status`            char(1)         DEFAULT '0'              COMMENT '状态（0 进行中 1 已结束 2 已归档）',
  `total_messages`    int(11)         DEFAULT 0                COMMENT '消息总数',
  `total_tokens`      int(11)         DEFAULT 0                COMMENT '消耗 token 总数',
  `start_time`        datetime                                   COMMENT '开始时间',
  `end_time`          datetime                                   COMMENT '结束时间',
  `create_by`         varchar(64)     DEFAULT ''               COMMENT '创建者',
  `create_time`       datetime                                   COMMENT '创建时间',
  `update_by`         varchar(64)     DEFAULT ''               COMMENT '更新者',
  `update_time`       datetime                                   COMMENT '更新时间',
  `remark`            varchar(500)    DEFAULT NULL             COMMENT '备注',
  PRIMARY KEY (`conversation_id`),
  UNIQUE KEY `uk_session_id` (`session_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT='AI 对话会话表';

-- ----------------------------
-- 22、AI 对话消息表
-- ----------------------------
DROP TABLE IF EXISTS `ai_conversation_message`;
CREATE TABLE `ai_conversation_message` (
  `message_id`        bigint(20)      NOT NULL AUTO_INCREMENT  COMMENT '消息 ID',
  `conversation_id`   bigint(20)      NOT NULL                 COMMENT '会话 ID',
  `parent_id`         bigint(20)      DEFAULT NULL             COMMENT '父消息 ID（用于分支对话）',
  `role`              varchar(20)     NOT NULL                 COMMENT '角色（system/user/assistant/tool）',
  `content`           longtext        NOT NULL                 COMMENT '消息内容',
  `content_type`      varchar(20)     DEFAULT 'text'           COMMENT '内容类型（text/markdown/json/image）',
  `tool_calls`        json            DEFAULT NULL             COMMENT '工具调用信息（JSON 数组）',
  `tool_call_id`      varchar(64)     DEFAULT NULL             COMMENT '工具调用 ID',
  `tokens_used`       int(11)         DEFAULT 0                COMMENT '消耗 token 数',
  `latency_ms`        int(11)         DEFAULT 0                COMMENT '响应延迟（毫秒）',
  `model_name`        varchar(50)     DEFAULT NULL             COMMENT '使用的 AI 模型',
  `temperature`       decimal(3,2)    DEFAULT NULL             COMMENT '温度参数',
  `metadata`          json            DEFAULT NULL             COMMENT '元数据（JSON）',
  `status`            char(1)         DEFAULT '0'              COMMENT '状态（0 成功 1 失败 2 重试中）',
  `error_message`     varchar(1000)   DEFAULT NULL             COMMENT '错误信息',
  `create_time`       datetime                                   COMMENT '创建时间',
  PRIMARY KEY (`message_id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_role` (`role`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=10000 COMMENT='AI 对话消息表';

-- ----------------------------
-- 23、AI 长期记忆表
-- ----------------------------
DROP TABLE IF EXISTS `ai_long_term_memory`;
CREATE TABLE `ai_long_term_memory` (
  `memory_id`         bigint(20)      NOT NULL AUTO_INCREMENT  COMMENT '记忆 ID',
  `user_id`           bigint(20)      NOT NULL                 COMMENT '用户 ID',
  `conversation_id`   bigint(20)      DEFAULT NULL             COMMENT '关联会话 ID',
  `memory_type`       varchar(20)     NOT NULL                 COMMENT '记忆类型（fact/preference/context/skill）',
  `category`          varchar(50)     DEFAULT ''               COMMENT '记忆分类',
  `content`           longtext        NOT NULL                 COMMENT '记忆内容',
  `summary`           varchar(1000)   DEFAULT ''               COMMENT '记忆摘要',
  `importance`        int(11)         DEFAULT 5                COMMENT '重要性评分（1-10）',
  `confidence`        decimal(3,2)    DEFAULT 0.8              COMMENT '置信度（0-1）',
  `access_count`      int(11)         DEFAULT 0                COMMENT '访问次数',
  `last_accessed`     datetime        DEFAULT NULL             COMMENT '最后访问时间',
  `expiration_time`   datetime        DEFAULT NULL             COMMENT '过期时间（NULL 表示永久）',
  `is_active`         char(1)         DEFAULT '1'              COMMENT '是否激活（0 否 1 是）',
  `tags`              json            DEFAULT NULL             COMMENT '标签（JSON 数组）',
  `metadata`          json            DEFAULT NULL             COMMENT '元数据（JSON）',
  `create_by`         varchar(64)     DEFAULT ''               COMMENT '创建者',
  `create_time`       datetime                                   COMMENT '创建时间',
  `update_by`         varchar(64)     DEFAULT ''               COMMENT '更新者',
  `update_time`       datetime                                   COMMENT '更新时间',
  PRIMARY KEY (`memory_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_memory_type` (`memory_type`),
  KEY `idx_category` (`category`),
  KEY `idx_is_active` (`is_active`),
  KEY `idx_importance` (`importance`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT='AI 长期记忆表';

-- ----------------------------
-- 24、RAG 文档索引表
-- ----------------------------
DROP TABLE IF EXISTS `ai_rag_document`;
CREATE TABLE `ai_rag_document` (
  `document_id`       bigint(20)      NOT NULL AUTO_INCREMENT  COMMENT '文档 ID',
  `doc_uuid`          varchar(64)     NOT NULL                 COMMENT '文档唯一标识（UUID）',
  `filename`          varchar(255)    NOT NULL                 COMMENT '文件名',
  `original_path`     varchar(500)    DEFAULT NULL             COMMENT '原始路径',
  `file_type`         varchar(20)     NOT NULL                 COMMENT '文件类型（pdf/md/docx/txt）',
  `file_size`         bigint(20)      DEFAULT 0                COMMENT '文件大小（字节）',
  `content_hash`      varchar(64)     DEFAULT NULL             COMMENT '内容哈希（SHA256）',
  `chunk_count`       int(11)         DEFAULT 0                COMMENT '分块数量',
  `embedding_model`   varchar(50)     DEFAULT 'text-embedding-ada-002' COMMENT '嵌入模型',
  `vector_collection` varchar(50)     DEFAULT 'spintale_rag'   COMMENT '向量集合名称',
  `status`            char(1)         DEFAULT '0'              COMMENT '状态（0 待处理 1 处理中 2 已完成 3 失败）',
  `error_message`     varchar(1000)   DEFAULT NULL             COMMENT '错误信息',
  `metadata`          json            DEFAULT NULL             COMMENT '元数据（JSON）',
  `indexed_time`      datetime        DEFAULT NULL             COMMENT '索引完成时间',
  `create_by`         varchar(64)     DEFAULT ''               COMMENT '创建者',
  `create_time`       datetime                                   COMMENT '创建时间',
  `update_by`         varchar(64)     DEFAULT ''               COMMENT '更新者',
  `update_time`       datetime                                   COMMENT '更新时间',
  PRIMARY KEY (`document_id`),
  UNIQUE KEY `uk_doc_uuid` (`doc_uuid`),
  KEY `idx_file_type` (`file_type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='RAG 文档索引表';

-- ----------------------------
-- 25、RAG 文档分块表
-- ----------------------------
DROP TABLE IF EXISTS `ai_rag_chunk`;
CREATE TABLE `ai_rag_chunk` (
  `chunk_id`          bigint(20)      NOT NULL AUTO_INCREMENT  COMMENT '分块 ID',
  `document_id`       bigint(20)      NOT NULL                 COMMENT '文档 ID',
  `chunk_index`       int(11)         NOT NULL                 COMMENT '分块索引',
  `content`           longtext        NOT NULL                 COMMENT '分块内容',
  `token_count`       int(11)         DEFAULT 0                COMMENT 'token 数量',
  `embedding_vector`  blob            DEFAULT NULL             COMMENT '嵌入向量（二进制）',
  `vector_id`         varchar(64)     DEFAULT NULL             COMMENT '向量数据库中的 ID',
  `metadata`          json            DEFAULT NULL             COMMENT '元数据（JSON）',
  `create_time`       datetime                                   COMMENT '创建时间',
  PRIMARY KEY (`chunk_id`),
  KEY `idx_document_id` (`document_id`),
  KEY `idx_chunk_index` (`chunk_index`),
  KEY `idx_vector_id` (`vector_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10000 COMMENT='RAG 文档分块表';

-- ----------------------------
-- 26、AI Agent 工作流实例表
-- ----------------------------
DROP TABLE IF EXISTS `ai_agent_workflow`;
CREATE TABLE `ai_agent_workflow` (
  `workflow_id`       bigint(20)      NOT NULL AUTO_INCREMENT  COMMENT '工作流 ID',
  `workflow_uuid`     varchar(64)     NOT NULL                 COMMENT '工作流唯一标识（UUID）',
  `user_id`           bigint(20)      NOT NULL                 COMMENT '用户 ID',
  `conversation_id`   bigint(20)      DEFAULT NULL             COMMENT '关联会话 ID',
  `workflow_type`     varchar(50)     NOT NULL                 COMMENT '工作流类型（research/coding/analysis/custom）',
  `agent_type`        varchar(50)     NOT NULL                 COMMENT 'Agent 类型（react/planning/collaborative）',
  `status`            varchar(20)     NOT NULL                 COMMENT '状态（RUNNING/COMPLETED/FAILED/CANCELLED）',
  `current_step`      int(11)         DEFAULT 0                COMMENT '当前步骤',
  `total_steps`       int(11)         DEFAULT 0                COMMENT '总步骤数',
  `input_data`        json            DEFAULT NULL             COMMENT '输入数据（JSON）',
  `output_data`       json            DEFAULT NULL             COMMENT '输出数据（JSON）',
  `execution_log`     json            DEFAULT NULL             COMMENT '执行日志（JSON 数组）',
  `error_message`     varchar(1000)   DEFAULT NULL             COMMENT '错误信息',
  `started_at`        datetime        DEFAULT NULL             COMMENT '开始时间',
  `completed_at`      datetime        DEFAULT NULL             COMMENT '完成时间',
  `duration_ms`       bigint(20)      DEFAULT 0                COMMENT '执行时长（毫秒）',
  `create_by`         varchar(64)     DEFAULT ''               COMMENT '创建者',
  `create_time`       datetime                                   COMMENT '创建时间',
  `update_by`         varchar(64)     DEFAULT ''               COMMENT '更新者',
  `update_time`       datetime                                   COMMENT '更新时间',
  PRIMARY KEY (`workflow_id`),
  UNIQUE KEY `uk_workflow_uuid` (`workflow_uuid`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_workflow_type` (`workflow_type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='AI Agent 工作流实例表';

-- ----------------------------
-- 27、AI 技能注册表
-- ----------------------------
DROP TABLE IF EXISTS `ai_skill_registry`;
CREATE TABLE `ai_skill_registry` (
  `skill_id`          bigint(20)      NOT NULL AUTO_INCREMENT  COMMENT '技能 ID',
  `skill_key`         varchar(50)     NOT NULL                 COMMENT '技能标识（唯一）',
  `skill_name`        varchar(100)    NOT NULL                 COMMENT '技能名称',
  `description`       varchar(500)    DEFAULT ''               COMMENT '技能描述',
  `skill_type`        varchar(20)     NOT NULL                 COMMENT '技能类型（builtin/custom/plugin）',
  `class_name`        varchar(255)    DEFAULT NULL             COMMENT '实现类名',
  `method_name`       varchar(100)    DEFAULT NULL             COMMENT '方法名',
  `input_schema`      json            DEFAULT NULL             COMMENT '输入 Schema（JSON Schema）',
  `output_schema`     json            DEFAULT NULL             COMMENT '输出 Schema（JSON Schema）',
  `parameters`        json            DEFAULT NULL             COMMENT '参数定义（JSON）',
  `enabled`           char(1)         DEFAULT '1'              COMMENT '是否启用（0 否 1 是）',
  `version`           varchar(20)     DEFAULT '1.0.0'          COMMENT '版本号',
  `usage_count`       int(11)         DEFAULT 0                COMMENT '使用次数',
  `avg_latency_ms`    int(11)         DEFAULT 0                COMMENT '平均延迟（毫秒）',
  `success_rate`      decimal(5,4)    DEFAULT 1.0              COMMENT '成功率',
  `metadata`          json            DEFAULT NULL             COMMENT '元数据（JSON）',
  `create_by`         varchar(64)     DEFAULT ''               COMMENT '创建者',
  `create_time`       datetime                                   COMMENT '创建时间',
  `update_by`         varchar(64)     DEFAULT ''               COMMENT '更新者',
  `update_time`       datetime                                   COMMENT '更新时间',
  PRIMARY KEY (`skill_id`),
  UNIQUE KEY `uk_skill_key` (`skill_key`),
  KEY `idx_skill_type` (`skill_type`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='AI 技能注册表';

-- ----------------------------
-- 28、AI 幻觉检测记录表
-- ----------------------------
DROP TABLE IF EXISTS `ai_hallucination_check`;
CREATE TABLE `ai_hallucination_check` (
  `check_id`          bigint(20)      NOT NULL AUTO_INCREMENT  COMMENT '检测 ID',
  `message_id`        bigint(20)      NOT NULL                 COMMENT '关联消息 ID',
  `conversation_id`   bigint(20)      NOT NULL                 COMMENT '会话 ID',
  `user_id`           bigint(20)      NOT NULL                 COMMENT '用户 ID',
  `response_content`  longtext        NOT NULL                 COMMENT 'AI 响应内容',
  `check_strategy`    varchar(50)     NOT NULL                 COMMENT '检测策略（consistency/factuality/source/logic/confidence）',
  `confidence_score`  decimal(3,2)    DEFAULT 0.0              COMMENT '置信度评分（0-1）',
  `risk_level`        varchar(20)     DEFAULT 'low'            COMMENT '风险等级（low/medium/high/critical）',
  `detected_issues`   json            DEFAULT NULL             COMMENT '检测到的问题（JSON 数组）',
  `evidence_sources`  json            DEFAULT NULL             COMMENT '证据来源（JSON 数组）',
  `suggested_action`  varchar(100)    DEFAULT NULL             COMMENT '建议操作（accept/review/reject/regenerate）',
  `reviewer_id`       bigint(20)      DEFAULT NULL             COMMENT '审核人 ID',
  `review_comment`    varchar(1000)   DEFAULT NULL             COMMENT '审核意见',
  `review_status`     char(1)         DEFAULT '0'              COMMENT '审核状态（0 待审核 1 已通过 2 已拒绝）',
  `review_time`       datetime        DEFAULT NULL             COMMENT '审核时间',
  `create_time`       datetime                                   COMMENT '创建时间',
  PRIMARY KEY (`check_id`),
  KEY `idx_message_id` (`message_id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_risk_level` (`risk_level`),
  KEY `idx_review_status` (`review_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT='AI 幻觉检测记录表';

-- ----------------------------
-- 29、AI Token 使用统计表
-- ----------------------------
DROP TABLE IF EXISTS `ai_token_usage`;
CREATE TABLE `ai_token_usage` (
  `usage_id`          bigint(20)      NOT NULL AUTO_INCREMENT  COMMENT '使用 ID',
  `user_id`           bigint(20)      NOT NULL                 COMMENT '用户 ID',
  `conversation_id`   bigint(20)      DEFAULT NULL             COMMENT '会话 ID',
  `model_name`        varchar(50)     NOT NULL                 COMMENT '模型名称',
  `date`              date            NOT NULL                 COMMENT '日期',
  `prompt_tokens`     int(11)         DEFAULT 0                COMMENT '提示 token 数',
  `completion_tokens` int(11)         DEFAULT 0                COMMENT '完成 token 数',
  `total_tokens`      int(11)         DEFAULT 0                COMMENT '总 token 数',
  `cost_usd`          decimal(10,6)   DEFAULT 0.0              COMMENT '成本（美元）',
  `request_count`     int(11)         DEFAULT 0                COMMENT '请求次数',
  `create_time`       datetime                                   COMMENT '创建时间',
  `update_time`       datetime                                   COMMENT '更新时间',
  PRIMARY KEY (`usage_id`),
  UNIQUE KEY `uk_user_model_date` (`user_id`, `model_name`, `date`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_model_name` (`model_name`),
  KEY `idx_date` (`date`)
) ENGINE=InnoDB AUTO_INCREMENT=10000 COMMENT='AI Token 使用统计表';

-- ----------------------------
-- 30、AI MCP 服务器配置表
-- ----------------------------
DROP TABLE IF EXISTS `ai_mcp_server`;
CREATE TABLE `ai_mcp_server` (
  `server_id`         bigint(20)      NOT NULL AUTO_INCREMENT  COMMENT '服务器 ID',
  `server_name`       varchar(100)    NOT NULL                 COMMENT '服务器名称',
  `server_url`        varchar(500)    NOT NULL                 COMMENT '服务器 URL',
  `server_type`       varchar(20)     NOT NULL                 COMMENT '服务器类型（sse/stdio）',
  `capabilities`      json            DEFAULT NULL             COMMENT '能力列表（JSON 数组）',
  `auth_type`         varchar(20)     DEFAULT 'none'           COMMENT '认证类型（none/api_key/oauth2）',
  `auth_config`       json            DEFAULT NULL             COMMENT '认证配置（JSON）',
  `enabled`           char(1)         DEFAULT '1'              COMMENT '是否启用（0 否 1 是）',
  `health_status`     varchar(20)     DEFAULT 'unknown'        COMMENT '健康状态（healthy/unhealthy/unknown）',
  `last_health_check` datetime        DEFAULT NULL             COMMENT '最后健康检查时间',
  `connection_count`  int(11)         DEFAULT 0                COMMENT '连接次数',
  `avg_latency_ms`    int(11)         DEFAULT 0                COMMENT '平均延迟（毫秒）',
  `metadata`          json            DEFAULT NULL             COMMENT '元数据（JSON）',
  `create_by`         varchar(64)     DEFAULT ''               COMMENT '创建者',
  `create_time`       datetime                                   COMMENT '创建时间',
  `update_by`         varchar(64)     DEFAULT ''               COMMENT '更新者',
  `update_time`       datetime                                   COMMENT '更新时间',
  PRIMARY KEY (`server_id`),
  UNIQUE KEY `uk_server_name` (`server_name`),
  KEY `idx_server_type` (`server_type`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_health_status` (`health_status`)
) ENGINE=InnoDB AUTO_INCREMENT=10 COMMENT='AI MCP 服务器配置表';

-- ----------------------------
-- 初始化数据 - AI 技能注册表
-- ----------------------------
INSERT INTO `ai_skill_registry` VALUES 
(1, 'code_explanation', '代码解释技能', '解释和分析代码片段的功能', 'builtin', 
 'com.spintale.ai.skill.CodeExplanationSkill', 'execute', 
 '{"type": "object", "properties": {"code": {"type": "string"}, "language": {"type": "string"}}}',
 '{"type": "object", "properties": {"explanation": {"type": "string"}, "complexity": {"type": "string"}}}',
 '[{"name": "code", "type": "string", "required": true}, {"name": "language", "type": "string", "required": false}]',
 '1', '1.0.0', 0, 0, 1.0, '{}', 'admin', NOW(), '', NULL),
(2, 'web_search', '网络搜索技能', '执行网络搜索获取最新信息', 'builtin',
 'com.spintale.ai.skill.WebSearchSkill', 'execute',
 '{"type": "object", "properties": {"query": {"type": "string"}, "num_results": {"type": "integer"}}}',
 '{"type": "object", "properties": {"results": {"type": "array"}}}',
 '[{"name": "query", "type": "string", "required": true}, {"name": "num_results", "type": "integer", "required": false}]',
 '1', '1.0.0', 0, 0, 1.0, '{}', 'admin', NOW(), '', NULL),
(3, 'data_analysis', '数据分析技能', '分析数据并生成洞察报告', 'builtin',
 'com.spintale.ai.skill.DataAnalysisSkill', 'execute',
 '{"type": "object", "properties": {"data": {"type": "array"}, "analysis_type": {"type": "string"}}}',
 '{"type": "object", "properties": {"insights": {"type": "array"}, "summary": {"type": "string"}}}',
 '[{"name": "data", "type": "array", "required": true}, {"name": "analysis_type", "type": "string", "required": false}]',
 '1', '1.0.0', 0, 0, 1.0, '{}', 'admin', NOW(), '', NULL);

-- ----------------------------
-- 初始化数据 - AI MCP 服务器配置表
-- ----------------------------
INSERT INTO `ai_mcp_server` VALUES 
(1, 'Local File System', '本地文件系统 MCP', 'stdio', 
 '["resources", "tools"]', 'none', '{}', 
 '1', 'unknown', NULL, 0, 0, '{}', 'admin', NOW(), '', NULL);

-- ----------------------------
-- 注释说明
-- ----------------------------
-- 1. ai_conversation 和 ai_conversation_message：存储对话历史，支持多轮对话和分支对话
-- 2. ai_long_term_memory：实现跨会话的长期记忆，支持记忆的重要性评分和过期机制
-- 3. ai_rag_document 和 ai_rag_chunk：RAG 系统的文档管理，与 Milvus 向量数据库配合使用
-- 4. ai_agent_workflow：Temporal 工作流引擎的状态持久化
-- 5. ai_skill_registry：技能系统的注册中心，支持动态加载和管理技能
-- 6. ai_hallucination_check：幻觉检测的记录和审核流程
-- 7. ai_token_usage：Token 使用统计和成本核算
-- 8. ai_mcp_server：MCP 协议的服务器配置管理

-- ----------------------------
-- 性能优化建议
-- ----------------------------
-- 1. 对于 ai_conversation_message 和 ai_rag_chunk 等大表，建议定期归档历史数据
-- 2. ai_long_term_memory 表可考虑按 user_id 进行分区
-- 3. ai_token_usage 表可按月进行分区，便于统计分析
-- 4. 所有 JSON 字段在 MySQL 5.7+ 中有良好的性能支持
-- 5. 向量数据存储在 Milvus 中，MySQL 仅存储元数据和引用
