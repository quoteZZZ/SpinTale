-- ============================================================================
-- SpinTale AI 文本生成工具核心表
-- 功能：支持小说/论文/文案/广告词等智能文本生成
-- 日期：2026-05-20
-- ============================================================================

-- ----------------------------
-- 1、AI作品项目表
-- ----------------------------
DROP TABLE IF EXISTS `ai_project`;
CREATE TABLE `ai_project` (
  `project_id`       bigint(20)      NOT NULL AUTO_INCREMENT  COMMENT '项目ID',
  `user_id`          bigint(20)      NOT NULL                 COMMENT '用户ID',
  `project_name`     varchar(200)    NOT NULL                 COMMENT '项目名称',
  `project_type`     varchar(20)     NOT NULL                 COMMENT '项目类型（novel/thesis/copywriting/advertisement）',
  `description`      varchar(1000)   DEFAULT ''               COMMENT '项目描述',
  `cover_image`      varchar(500)    DEFAULT NULL             COMMENT '封面图片',
  `status`           char(1)         DEFAULT '0'              COMMENT '状态（0草稿 1进行中 2已完成 3已发布）',
  `word_count`       int(11)         DEFAULT 0                COMMENT '总字数',
  `chapter_count`    int(11)         DEFAULT 0                COMMENT '章节数',
  `total_tokens`     int(11)         DEFAULT 0                COMMENT '消耗Token总数',
  `total_cost`       decimal(10,4)   DEFAULT 0.0              COMMENT '总成本（美元）',
  `tags`             varchar(500)    DEFAULT ''               COMMENT '标签（JSON数组）',
  `metadata`         json            DEFAULT NULL             COMMENT '项目元数据',
  `create_by`        varchar(64)     DEFAULT ''               COMMENT '创建者',
  `create_time`      datetime                                 COMMENT '创建时间',
  `update_by`        varchar(64)     DEFAULT ''               COMMENT '更新者',
  `update_time`      datetime                                 COMMENT '更新时间',
  `remark`           varchar(500)    DEFAULT NULL             COMMENT '备注',
  PRIMARY KEY (`project_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_project_type` (`project_type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT='AI作品项目表';

-- ----------------------------
-- 2、AI章节表
-- ----------------------------
DROP TABLE IF EXISTS `ai_chapter`;
CREATE TABLE `ai_chapter` (
  `chapter_id`       bigint(20)      NOT NULL AUTO_INCREMENT  COMMENT '章节ID',
  `project_id`       bigint(20)      NOT NULL                 COMMENT '项目ID',
  `parent_id`        bigint(20)      DEFAULT 0                COMMENT '父章节ID（支持多级目录）',
  `chapter_title`    varchar(200)    NOT NULL                 COMMENT '章节标题',
  `chapter_order`    int(11)         NOT NULL                 COMMENT '章节顺序',
  `content`          longtext        DEFAULT NULL             COMMENT '章节内容',
  `content_summary`  varchar(500)    DEFAULT ''               COMMENT '内容摘要',
  `word_count`       int(11)         DEFAULT 0                COMMENT '字数',
  `status`           char(1)         DEFAULT '0'              COMMENT '状态（0草稿 1已生成 2已审核 3已发布）',
  `generation_strategy` varchar(20)  DEFAULT 'manual'         COMMENT '生成策略（manual/auto/ai_assisted）',
  `generate_params`  json            DEFAULT NULL             COMMENT '生成参数',
  `ai_metadata`      json            DEFAULT NULL             COMMENT 'AI生成元数据',
  `version`          int(11)         DEFAULT 1                COMMENT '版本号',
  `create_by`        varchar(64)     DEFAULT ''               COMMENT '创建者',
  `create_time`      datetime                                 COMMENT '创建时间',
  `update_by`        varchar(64)     DEFAULT ''               COMMENT '更新者',
  `update_time`      datetime                                 COMMENT '更新时间',
  PRIMARY KEY (`chapter_id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_chapter_order` (`chapter_order`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=10000 COMMENT='AI章节表';

-- ----------------------------
-- 3、AI模板表
-- ----------------------------
DROP TABLE IF EXISTS `ai_template`;
CREATE TABLE `ai_template` (
  `template_id`      bigint(20)      NOT NULL AUTO_INCREMENT  COMMENT '模板ID',
  `template_name`    varchar(100)    NOT NULL                 COMMENT '模板名称',
  `template_code`    varchar(50)     NOT NULL                 COMMENT '模板代码（唯一标识）',
  `template_type`    varchar(20)     NOT NULL                 COMMENT '模板类型（novel/thesis/copywriting/advertisement）',
  `category`         varchar(50)     DEFAULT ''               COMMENT '模板分类',
  `description`      varchar(500)    DEFAULT ''               COMMENT '模板描述',
  `prompt_template`  text            NOT NULL                 COMMENT 'Prompt模板',
  `variables`        json            DEFAULT NULL             COMMENT '变量定义（JSON数组）',
  `default_params`   json            DEFAULT NULL             COMMENT '默认参数（temperature、max_tokens等）',
  `style_guide`      text            DEFAULT NULL             COMMENT '风格指南',
  `example_output`   text            DEFAULT NULL             COMMENT '示例输出',
  `is_public`        char(1)         DEFAULT '1'              COMMENT '是否公开（0否 1是）',
  `is_builtin`       char(1)         DEFAULT '0'              COMMENT '是否内置（0否 1是）',
  `use_count`        int(11)         DEFAULT 0                COMMENT '使用次数',
  `rating`           decimal(3,2)    DEFAULT 0.0              COMMENT '评分（0-5）',
  `create_by`        varchar(64)     DEFAULT ''               COMMENT '创建者',
  `create_time`      datetime                                 COMMENT '创建时间',
  `update_by`        varchar(64)     DEFAULT ''               COMMENT '更新者',
  `update_time`      datetime                                 COMMENT '更新时间',
  PRIMARY KEY (`template_id`),
  UNIQUE KEY `uk_template_code` (`template_code`),
  KEY `idx_template_type` (`template_type`),
  KEY `idx_is_public` (`is_public`),
  KEY `idx_use_count` (`use_count`)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='AI模板表';

-- ----------------------------
-- 4、AI风格库表
-- ----------------------------
DROP TABLE IF EXISTS `ai_style`;
CREATE TABLE `ai_style` (
  `style_id`         bigint(20)      NOT NULL AUTO_INCREMENT  COMMENT '风格ID',
  `style_name`       varchar(50)     NOT NULL                 COMMENT '风格名称',
  `style_code`       varchar(30)     NOT NULL                 COMMENT '风格代码',
  `style_type`       varchar(20)     NOT NULL                 COMMENT '风格类型',
  `description`      varchar(200)    DEFAULT ''               COMMENT '风格描述',
  `prompt_prefix`    text            DEFAULT NULL             COMMENT 'Prompt前缀',
  `prompt_suffix`    text            DEFAULT NULL             COMMENT 'Prompt后缀',
  `parameters`       json            DEFAULT NULL             COMMENT '风格参数（语气、用词、句式等）',
  `keywords`         varchar(500)    DEFAULT ''               COMMENT '关键词（JSON数组）',
  `examples`         text            DEFAULT NULL             COMMENT '风格示例',
  `is_builtin`       char(1)         DEFAULT '0'              COMMENT '是否内置',
  `is_active`        char(1)         DEFAULT '1'              COMMENT '是否启用',
  `create_time`      datetime                                 COMMENT '创建时间',
  PRIMARY KEY (`style_id`),
  UNIQUE KEY `uk_style_code` (`style_code`),
  KEY `idx_style_type` (`style_type`)
) ENGINE=InnoDB AUTO_INCREMENT=50 COMMENT='AI风格库表';

-- ----------------------------
-- 5、AI生成历史表
-- ----------------------------
DROP TABLE IF EXISTS `ai_generation_history`;
CREATE TABLE `ai_generation_history` (
  `history_id`       bigint(20)      NOT NULL AUTO_INCREMENT  COMMENT '历史ID',
  `user_id`          bigint(20)      NOT NULL                 COMMENT '用户ID',
  `project_id`       bigint(20)      DEFAULT NULL             COMMENT '项目ID',
  `chapter_id`       bigint(20)      DEFAULT NULL             COMMENT '章节ID',
  `template_id`      bigint(20)      DEFAULT NULL             COMMENT '模板ID',
  `style_id`         bigint(20)      DEFAULT NULL             COMMENT '风格ID',
  `generation_type`  varchar(20)     NOT NULL                 COMMENT '生成类型',
  `input_prompt`     text            NOT NULL                 COMMENT '输入提示',
  `generated_content` longtext       NOT NULL                 COMMENT '生成内容',
  `model_name`       varchar(50)     NOT NULL                 COMMENT '使用模型',
  `provider`         varchar(20)     NOT NULL                 COMMENT '提供商',
  `generation_params` json           DEFAULT NULL             COMMENT '生成参数',
  `tokens_used`      int(11)         DEFAULT 0                COMMENT '消耗Token',
  `cost_usd`         decimal(10,6)   DEFAULT 0.0              COMMENT '成本（美元）',
  `duration_ms`      int(11)         DEFAULT 0                COMMENT '生成耗时（毫秒）',
  `rating`           int(1)          DEFAULT NULL             COMMENT '用户评分（1-5）',
  `feedback`         varchar(500)    DEFAULT NULL             COMMENT '用户反馈',
  `is_accepted`      char(1)         DEFAULT NULL             COMMENT '是否采纳',
  `create_time`      datetime                                 COMMENT '创建时间',
  PRIMARY KEY (`history_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_template_id` (`template_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=100000 COMMENT='AI生成历史表';

-- ----------------------------
-- 6、AI版本快照表
-- ----------------------------
DROP TABLE IF EXISTS `ai_version`;
CREATE TABLE `ai_version` (
  `version_id`       bigint(20)      NOT NULL AUTO_INCREMENT  COMMENT '版本ID',
  `entity_type`      varchar(20)     NOT NULL                 COMMENT '实体类型（project/chapter）',
  `entity_id`        bigint(20)      NOT NULL                 COMMENT '实体ID',
  `version_number`   int(11)         NOT NULL                 COMMENT '版本号',
  `content`          longtext        NOT NULL                 COMMENT '内容快照',
  `change_summary`   varchar(500)    DEFAULT ''               COMMENT '变更摘要',
  `change_type`      varchar(20)     DEFAULT 'update'         COMMENT '变更类型（create/update/delete/merge）',
  `parent_version`   bigint(20)      DEFAULT NULL             COMMENT '父版本ID（支持分支）',
  `is_active`        char(1)         DEFAULT '1'              COMMENT '是否活跃版本',
  `create_by`        varchar(64)     DEFAULT ''               COMMENT '创建者',
  `create_time`      datetime                                 COMMENT '创建时间',
  PRIMARY KEY (`version_id`),
  UNIQUE KEY `uk_entity_version` (`entity_type`, `entity_id`, `version_number`),
  KEY `idx_entity_type_id` (`entity_type`, `entity_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=10000 COMMENT='AI版本快照表';

-- ----------------------------
-- 7、AI导出记录表
-- ----------------------------
DROP TABLE IF EXISTS `ai_export_record`;
CREATE TABLE `ai_export_record` (
  `export_id`        bigint(20)      NOT NULL AUTO_INCREMENT  COMMENT '导出ID',
  `user_id`          bigint(20)      NOT NULL                 COMMENT '用户ID',
  `project_id`       bigint(20)      DEFAULT NULL             COMMENT '项目ID',
  `export_type`      varchar(20)     NOT NULL                 COMMENT '导出类型（project/chapter/range）',
  `export_format`    varchar(20)     NOT NULL                 COMMENT '导出格式（txt/docx/pdf/md）',
  `file_name`        varchar(200)    NOT NULL                 COMMENT '文件名',
  `file_path`        varchar(500)    NOT NULL                 COMMENT '文件路径',
  `file_size`        bigint(20)      DEFAULT 0                COMMENT '文件大小（字节）',
  `chapter_ids`      json            DEFAULT NULL             COMMENT '章节ID列表（JSON数组）',
  `export_params`    json            DEFAULT NULL             COMMENT '导出参数',
  `status`           char(1)         DEFAULT '0'              COMMENT '状态（0进行中 1成功 2失败）',
  `error_message`    varchar(500)    DEFAULT NULL             COMMENT '错误信息',
  `create_time`      datetime                                 COMMENT '创建时间',
  PRIMARY KEY (`export_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT='AI导出记录表';

-- ----------------------------
-- 初始化数据 - 内置模板
-- ----------------------------
INSERT INTO `ai_template` VALUES 
(1, '小说章节生成', 'novel_chapter', 'novel', '小说', '自动生成小说章节内容，支持情节发展、人物对话、场景描写', 
 '根据以下大纲和上下文生成小说章节：\n\n章节标题：{{chapter_title}}\n章节大纲：{{outline}}\n前文摘要：{{prev_summary}}\n人物设定：{{characters}}\n风格要求：{{style}}\n\n请生成约{{word_count}}字的章节内容。', 
 '[{"name": "chapter_title", "type": "string", "required": true}, {"name": "outline", "type": "string", "required": true}, {"name": "prev_summary", "type": "string", "required": false}, {"name": "characters", "type": "string", "required": false}, {"name": "word_count", "type": "integer", "default": 2000}]',
 '{"temperature": 0.8, "max_tokens": 3000}', 
 '注意情节连贯性，人物性格一致性，对话自然流畅', 
 '第一章 初遇\n\n阳光透过树叶的缝隙，洒在校园的林荫道上。李明背着书包，漫无目的地走着...', 
 '1', '1', 0, 0.0, 'admin', NOW(), '', NULL),

(2, '学术论文生成', 'thesis_section', 'thesis', '论文', '生成学术论文的各个章节，包括引言、方法、结果、讨论等',
 '生成论文{{section_type}}章节：\n\n研究主题：{{research_topic}}\n研究问题：{{research_question}}\n已有内容：{{existing_content}}\n参考文献：{{references}}\n\n请生成符合学术规范的{{section_type}}章节，约{{word_count}}字。',
 '[{"name": "section_type", "type": "string", "required": true, "options": ["introduction", "methodology", "results", "discussion", "conclusion"]}, {"name": "research_topic", "type": "string", "required": true}, {"name": "word_count", "type": "integer", "default": 1500}]',
 '{"temperature": 0.3, "max_tokens": 2500}',
 '使用学术语言，逻辑严谨，引用规范',
 '1. 引言\n\n随着人工智能技术的快速发展，深度学习在自然语言处理领域取得了突破性进展...',
 '1', '1', 0, 0.0, 'admin', NOW(), '', NULL),

(3, '营销文案生成', 'marketing_copy', 'copywriting', '营销', '生成产品营销文案、推广软文',
 '为以下产品生成营销文案：\n\n产品名称：{{product_name}}\n产品类型：{{product_type}}\n核心卖点：{{selling_points}}\n目标受众：{{target_audience}}\n营销目标：{{marketing_goal}}\n风格要求：{{style}}\n\n请生成吸引{{target_audience}}的营销文案，约{{word_count}}字。',
 '[{"name": "product_name", "type": "string", "required": true}, {"name": "selling_points", "type": "string", "required": true}, {"name": "target_audience", "type": "string", "required": true}]',
 '{"temperature": 0.7, "max_tokens": 2000}',
 '突出产品优势，语言生动有感染力，符合目标受众喜好',
 '【限时特惠】智能AI写作助手，让创作更简单！\n\n还在为写作发愁？AI写作助手帮你轻松搞定...',
 '1', '1', 0, 0.0, 'admin', NOW(), '', NULL),

(4, '广告词生成', 'advertising_slogan', 'advertisement', '广告', '生成朗朗上口的广告词、slogan',
 '为以下品牌/产品生成广告词：\n\n品牌名称：{{brand_name}}\n产品特点：{{features}}\n品牌定位：{{positioning}}\n情感诉求：{{emotion}}\n风格：{{style}}\n\n请生成{{count}}条广告词，要求简洁有力、易记传播。',
 '[{"name": "brand_name", "type": "string", "required": true}, {"name": "features", "type": "string", "required": true}, {"name": "count", "type": "integer", "default": 5}]',
 '{"temperature": 0.9, "max_tokens": 500}',
 '简洁有力，富有感染力，易于传播记忆',
 '1. 科技改变生活，智能引领未来\n2. 因为专业，所以信赖\n3. 每一天，新可能',
 '1', '1', 0, 0.0, 'admin', NOW(), '', NULL);

-- ----------------------------
-- 初始化数据 - 内置风格
-- ----------------------------
INSERT INTO `ai_style` VALUES 
(1, '正式严谨', 'formal', 'tone', '正式、专业的语言风格', '请使用正式、严谨、专业的语言风格进行写作。',
 '注意使用专业术语，语言正式规范，逻辑严密。', '{"tone": "formal", "vocabulary": "professional", "sentence": "complex"}',
 '["因此", "综上所述", "研究表明", "值得注意的是"]', '根据研究数据显示，该系统在实际应用中表现优异。', '1', '1', NOW()),

(2, '幽默轻松', 'humorous', 'tone', '幽默、轻松的语言风格', '请使用幽默、轻松、风趣的语言风格进行写作。',
 '注意使用生动比喻，适当幽默，让人会心一笑。', '{"tone": "humorous", "vocabulary": "casual", "sentence": "varied"}',
 '["话说", "有趣的是", "你可能不信", "哈哈"]', '说实话，这个功能简直像是给AI装上了小脑，灵得很！', '1', '1', NOW()),

(3, '学术规范', 'academic', 'tone', '符合学术写作规范', '请遵循学术写作规范，使用严谨的学术语言。',
 '使用第三人称，避免主观表述，引用规范，逻辑清晰。', '{"tone": "academic", "vocabulary": "specialized", "sentence": "structured"}',
 '["本文", "研究表明", "实验结果", "数据表明"]', '本文通过实验验证了所提方法的有效性，实验结果表明...', '1', '1', NOW()),

(4, '生动活泼', 'vivid', 'tone', '生动、活泼、富有画面感', '请使用生动、活泼、富有画面感的语言。',
 '使用大量感官描写，营造画面感，让读者身临其境。', '{"tone": "vivid", "vocabulary": "descriptive", "sentence": "flowing"}',
 '["阳光下", "微风中", "只见", "忽然"]', '阳光透过树叶的缝隙，在地面洒下斑驳的光影...', '1', '1', NOW()),

(5, '简洁明快', 'concise', 'tone', '简洁、明了、直击要点', '请使用简洁、明了的语言，直击要点。',
 '避免冗长描述，直接表达核心内容，一句话说清楚。', '{"tone": "concise", "vocabulary": "simple", "sentence": "short"}',
 '["直接", "简单", "就是", "只需"]', '三个步骤，轻松完成。上传、配置、生成，就这么简单。', '1', '1', NOW()),

(6, '文艺唯美', 'literary', 'tone', '文艺、唯美、富有诗意', '请使用文艺、唯美、富有诗意的语言。',
 '使用优美词藻，营造意境美，让文字如诗如画。', '{"tone": "literary", "vocabulary": "elegant", "sentence": "poetic"}',
 '["宛如", "仿佛", "岁月静好", "流年"]', '时光静好，与君语；细水长流，与君同...', '1', '1', NOW());

-- ----------------------------
-- 初始化数据 - 项目类型字典
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (100, '项目类型', 'ai_project_type', '0', 'admin', NOW(), '', NULL, 'AI项目类型');
INSERT INTO `sys_dict_data` VALUES 
(1001, 0, '小说', 'novel', 'ai_project_type', '', 'default', 'N', '0', 'admin', NOW(), '', NULL, '小说创作'),
(1002, 1, '论文', 'thesis', 'ai_project_type', '', 'primary', 'N', '0', 'admin', NOW(), '', NULL, '学术论文'),
(1003, 2, '文案', 'copywriting', 'ai_project_type', '', 'success', 'N', '0', 'admin', NOW(), '', NULL, '营销文案'),
(1004, 3, '广告词', 'advertisement', 'ai_project_type', '', 'info', 'N', '0', 'admin', NOW(), '', NULL, '广告标语');

INSERT INTO `sys_dict_type` VALUES (101, '生成策略', 'ai_generation_strategy', '0', 'admin', NOW(), '', NULL, 'AI生成策略');
INSERT INTO `sys_dict_data` VALUES 
(1011, 0, '手动编写', 'manual', 'ai_generation_strategy', '', 'default', 'N', '0', 'admin', NOW(), '', NULL, '完全手动编写'),
(1012, 1, 'AI自动生成', 'auto', 'ai_generation_strategy', '', 'primary', 'N', '0', 'admin', NOW(), '', NULL, 'AI完全自动生成'),
(1013, 2, 'AI辅助生成', 'ai_assisted', 'ai_generation_strategy', '', 'success', 'N', '0', 'admin', NOW(), '', NULL, 'AI辅助用户编写');

-- ----------------------------
-- 说明
-- ----------------------------
-- 1. ai_project：管理作品项目，支持小说、论文、文案、广告词等类型
-- 2. ai_chapter：支持多级目录结构，灵活管理章节内容
-- 3. ai_template：内置多种模板，支持用户自定义
-- 4. ai_style：丰富的风格库，支持风格混合
-- 5. ai_generation_history：完整的生成历史追溯，支持评分反馈
-- 6. ai_version：版本管理系统，支持分支和回滚
-- 7. ai_export_record：多格式导出记录
