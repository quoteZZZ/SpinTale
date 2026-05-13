-- ============================================================================
-- SpinTale AI Module Database Schema Upgrade Script
-- Target: MySQL 8.0+ / MariaDB 10.5+
-- Description: Creates all necessary tables for AI features (Memory, RAG, Agent, etc.)
-- Usage: mysql -u root -p spin_tale < scripts/upgrade_ai_schema.sql
-- ============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------------------
-- 1. Conversation & Message Management (对话与会话管理)
-- ----------------------------------------------------------------------------

-- AI 会话主表
CREATE TABLE IF NOT EXISTS `ai_conversation` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `session_id` VARCHAR(64) NOT NULL COMMENT '会话唯一标识 (UUID)',
    `user_id` BIGINT(20) DEFAULT NULL COMMENT '关联用户 ID (sys_user.id)',
    `title` VARCHAR(255) DEFAULT NULL COMMENT '会话标题 (由 AI 自动生成)',
    `model_name` VARCHAR(64) DEFAULT 'gpt-4' COMMENT '使用的模型名称',
    `mode` VARCHAR(32) DEFAULT 'chat' COMMENT '模式：chat, agent, rag',
    `context_window` INT DEFAULT 4096 COMMENT '上下文窗口大小',
    `temperature` DECIMAL(3,2) DEFAULT 0.7 COMMENT '温度参数',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：1-活跃，0-归档，-1-删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `metadata` JSON DEFAULT NULL COMMENT '扩展元数据 (如：主题标签，偏好设置)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_session_id` (`session_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status_created` (`status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 会话主表';

-- AI 消息记录表
CREATE TABLE IF NOT EXISTS `ai_conversation_message` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `session_id` VARCHAR(64) NOT NULL COMMENT '会话 ID',
    `message_id` VARCHAR(64) NOT NULL COMMENT '消息唯一标识',
    `role` VARCHAR(16) NOT NULL COMMENT '角色：user, assistant, system, tool',
    `content` LONGTEXT NOT NULL COMMENT '消息内容',
    `token_count` INT DEFAULT 0 COMMENT 'Token 消耗数量',
    `finish_reason` VARCHAR(32) DEFAULT NULL COMMENT '结束原因：stop, length, tool_calls',
    `tool_calls` JSON DEFAULT NULL COMMENT '工具调用请求 (JSON)',
    `tool_results` JSON DEFAULT NULL COMMENT '工具执行结果 (JSON)',
    `latency_ms` INT DEFAULT 0 COMMENT '响应延迟 (毫秒)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_message_id` (`message_id`),
    KEY `idx_role` (`role`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 对话消息记录表';

-- ----------------------------------------------------------------------------
-- 2. Long-Term Memory System (长期记忆系统)
-- ----------------------------------------------------------------------------

-- 长期记忆存储表 (配合 Redis 缓存使用)
CREATE TABLE IF NOT EXISTS `ai_long_term_memory` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `memory_id` VARCHAR(64) NOT NULL COMMENT '记忆唯一标识',
    `user_id` BIGINT(20) DEFAULT NULL COMMENT '所属用户 ID',
    `session_id` VARCHAR(64) DEFAULT NULL COMMENT '来源会话 ID',
    `content` TEXT NOT NULL COMMENT '记忆内容摘要',
    `original_text` LONGTEXT COMMENT '原始文本片段',
    `memory_type` VARCHAR(32) DEFAULT 'episodic' COMMENT '类型：episodic(事件), semantic(知识), procedural(技能)',
    `importance_score` DECIMAL(3,2) DEFAULT 0.5 COMMENT '重要性评分 (0.0-1.0)',
    `access_count` INT DEFAULT 0 COMMENT '被访问次数',
    `last_accessed_at` DATETIME DEFAULT NULL COMMENT '最后访问时间',
    `expires_at` DATETIME DEFAULT NULL COMMENT '过期时间 (NULL 为永久)',
    `tags` JSON DEFAULT NULL COMMENT '标签列表',
    `vector_id` VARCHAR(64) DEFAULT NULL COMMENT 'Milvus 中的向量 ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_memory_id` (`memory_id`),
    KEY `idx_user_type` (`user_id`, `memory_type`),
    KEY `idx_importance` (`importance_score` DESC),
    KEY `idx_vector_id` (`vector_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 长期记忆存储表';

-- ----------------------------------------------------------------------------
-- 3. RAG Knowledge Base (RAG 知识库)
-- ----------------------------------------------------------------------------

-- RAG 文档元数据表
CREATE TABLE IF NOT EXISTS `ai_rag_document` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `doc_id` VARCHAR(64) NOT NULL COMMENT '文档唯一标识',
    `title` VARCHAR(255) NOT NULL COMMENT '文档标题',
    `file_name` VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
    `file_path` VARCHAR(512) DEFAULT NULL COMMENT '文件存储路径 (OSS/S3/Local)',
    `file_type` VARCHAR(16) DEFAULT 'txt' COMMENT '文件类型：pdf, md, docx, txt',
    `file_size` BIGINT(20) DEFAULT 0 COMMENT '文件大小 (字节)',
    `content_hash` VARCHAR(64) DEFAULT NULL COMMENT '内容哈希 (用于去重)',
    `chunk_count` INT DEFAULT 0 COMMENT '分片数量',
    `status` VARCHAR(16) DEFAULT 'pending' COMMENT '状态：pending, processing, completed, failed',
    `error_msg` TEXT DEFAULT NULL COMMENT '处理错误信息',
    `metadata` JSON DEFAULT NULL COMMENT '自定义元数据 (作者，日期，分类)',
    `created_by` BIGINT(20) DEFAULT NULL COMMENT '上传用户 ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_doc_id` (`doc_id`),
    KEY `idx_status` (`status`),
    KEY `idx_content_hash` (`content_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RAG 文档元数据表';

-- RAG 文档分片表 (Chunk)
CREATE TABLE IF NOT EXISTS `ai_rag_chunk` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `chunk_id` VARCHAR(64) NOT NULL COMMENT '分片唯一标识',
    `doc_id` VARCHAR(64) NOT NULL COMMENT '关联文档 ID',
    `chunk_index` INT NOT NULL COMMENT '分片索引顺序',
    `content` TEXT NOT NULL COMMENT '分片文本内容',
    `token_count` INT DEFAULT 0 COMMENT 'Token 数量',
    `vector_id` VARCHAR(64) DEFAULT NULL COMMENT 'Milvus 中的向量 ID',
    `keywords` JSON DEFAULT NULL COMMENT '提取的关键词',
    `metadata` JSON DEFAULT NULL COMMENT '分片级元数据 (页码，段落号)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_chunk_id` (`chunk_id`),
    KEY `idx_doc_id` (`doc_id`),
    KEY `idx_vector_id` (`vector_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RAG 文档分片表';

-- ----------------------------------------------------------------------------
-- 4. Agent & Workflow Engine (Agent 与工作流引擎)
-- ----------------------------------------------------------------------------

-- Agent 工作流实例表 (配合 Temporal)
CREATE TABLE IF NOT EXISTS `ai_agent_workflow` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `workflow_id` VARCHAR(64) NOT NULL COMMENT 'Temporal 工作流 ID',
    `run_id` VARCHAR(64) DEFAULT NULL COMMENT 'Temporal 运行 ID',
    `agent_name` VARCHAR(64) NOT NULL COMMENT 'Agent 名称',
    `task_type` VARCHAR(32) DEFAULT 'general' COMMENT '任务类型',
    `input_data` JSON DEFAULT NULL COMMENT '输入参数',
    `current_state` VARCHAR(32) DEFAULT 'running' COMMENT '当前状态：running, completed, failed, canceled',
    `output_data` JSON DEFAULT NULL COMMENT '输出结果',
    `error_info` TEXT DEFAULT NULL COMMENT '错误堆栈信息',
    `started_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    `completed_at` DATETIME DEFAULT NULL COMMENT '完成时间',
    `duration_ms` BIGINT(20) DEFAULT 0 COMMENT '总耗时 (毫秒)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_workflow_id` (`workflow_id`),
    KEY `idx_agent_state` (`agent_name`, `current_state`),
    KEY `idx_started_at` (`started_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent 工作流实例表';

-- 技能注册中心表
CREATE TABLE IF NOT EXISTS `ai_skill_registry` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `skill_code` VARCHAR(64) NOT NULL COMMENT '技能代码 (唯一标识)',
    `skill_name` VARCHAR(128) NOT NULL COMMENT '技能名称',
    `description` TEXT DEFAULT NULL COMMENT '技能描述',
    `version` VARCHAR(16) DEFAULT '1.0.0' COMMENT '版本号',
    `config_schema` JSON DEFAULT NULL COMMENT '配置参数 Schema (JSON Schema)',
    `endpoint_url` VARCHAR(512) DEFAULT NULL COMMENT '远程调用地址 (如果是外部技能)',
    `is_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `usage_count` BIGINT(20) DEFAULT 0 COMMENT '累计调用次数',
    `avg_latency_ms` INT DEFAULT 0 COMMENT '平均延迟',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_skill_code` (`skill_code`),
    KEY `idx_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 技能注册中心表';

-- ----------------------------------------------------------------------------
-- 5. Quality & Monitoring (质量监控与统计)
-- ----------------------------------------------------------------------------

-- 幻觉检测记录表
CREATE TABLE IF NOT EXISTS `ai_hallucination_check` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `check_id` VARCHAR(64) NOT NULL COMMENT '检测记录 ID',
    `message_id` VARCHAR(64) DEFAULT NULL COMMENT '关联消息 ID',
    `input_text` TEXT NOT NULL COMMENT '待检测文本',
    `reference_context` TEXT DEFAULT NULL COMMENT '参考上下文 (RAG 内容)',
    `hallucination_score` DECIMAL(3,2) DEFAULT 0.0 COMMENT '幻觉概率 (0.0-1.0)',
    `risk_level` VARCHAR(16) DEFAULT 'low' COMMENT '风险等级：low, medium, high, critical',
    `evidence` JSON DEFAULT NULL COMMENT '检测证据 (引用的片段)',
    `action_taken` VARCHAR(32) DEFAULT 'none' COMMENT '采取动作：none, warn, block, rewrite',
    `checked_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_check_id` (`check_id`),
    KEY `idx_risk_level` (`risk_level`),
    KEY `idx_checked_at` (`checked_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 幻觉检测记录表';

-- Token 使用统计表 (按天聚合)
CREATE TABLE IF NOT EXISTS `ai_token_usage` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `user_id` BIGINT(20) DEFAULT NULL COMMENT '用户 ID (NULL 表示系统总计)',
    `model_name` VARCHAR(64) NOT NULL COMMENT '模型名称',
    `prompt_tokens` BIGINT(20) DEFAULT 0 COMMENT '输入 Token 数',
    `completion_tokens` BIGINT(20) DEFAULT 0 COMMENT '输出 Token 数',
    `total_tokens` BIGINT(20) DEFAULT 0 COMMENT '总 Token 数',
    `estimated_cost` DECIMAL(10,4) DEFAULT 0.0000 COMMENT '预估成本 (元)',
    `request_count` INT DEFAULT 0 COMMENT '请求次数',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_date_user_model` (`stat_date`, `user_id`, `model_name`),
    KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Token 使用统计表';

-- MCP Server 配置表
CREATE TABLE IF NOT EXISTS `ai_mcp_server` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `server_name` VARCHAR(64) NOT NULL COMMENT '服务器名称',
    `transport_type` VARCHAR(16) DEFAULT 'stdio' COMMENT '传输类型：stdio, sse, streamable-http',
    `command` VARCHAR(255) DEFAULT NULL COMMENT '启动命令 (stdio 模式)',
    `args` JSON DEFAULT NULL COMMENT '启动参数',
    `url` VARCHAR(512) DEFAULT NULL COMMENT '远程 URL (sse/http 模式)',
    `env_vars` JSON DEFAULT NULL COMMENT '环境变量',
    `status` VARCHAR(16) DEFAULT 'inactive' COMMENT '状态：inactive, active, error',
    `last_heartbeat` DATETIME DEFAULT NULL COMMENT '最后心跳时间',
    `capabilities` JSON DEFAULT NULL COMMENT '支持的能力 (tools, resources, prompts)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_server_name` (`server_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MCP Server 配置表';

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------------------------------------------------------
-- Initial Data Seeding (初始数据种子)
-- ----------------------------------------------------------------------------

-- 插入默认技能示例
INSERT INTO `ai_skill_registry` (`skill_code`, `skill_name`, `description`, `version`, `is_enabled`) VALUES
('web_search', 'Web Search', '搜索互联网获取最新信息', '1.0.0', 1),
('calculator', 'Calculator', '执行复杂的数学计算', '1.0.0', 1),
('weather_tool', 'Weather Tool', '查询全球天气信息', '1.0.0', 1),
('code_interpreter', 'Code Interpreter', '执行 Python 代码沙箱', '1.0.0', 0);

-- 插入默认 MCP 服务器示例
INSERT INTO `ai_mcp_server` (`server_name`, `transport_type`, `command`, `args`, `status`, `capabilities`) VALUES
('filesystem', 'stdio', 'npx', '["-y", "@modelcontextprotocol/server-filesystem", "/data"]', 'inactive', '{"tools": ["read_file", "write_file"], "resources": []}'),
('github', 'sse', NULL, NULL, 'inactive', '{"tools": ["search_repos", "get_issue"], "resources": []}');

SELECT '✅ SpinTale AI Schema Upgrade Completed Successfully!' AS status;
