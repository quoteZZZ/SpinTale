-- ============================================================================
-- SpinTale AI 模块菜单权限配置
-- 功能：为AI功能添加菜单和权限控制
-- 日期：2026-05-20
-- ============================================================================

-- ----------------------------
-- AI模块菜单配置
-- ----------------------------
-- 菜单 SQL
INSERT INTO `sys_menu` VALUES (2000, 'AI管理', 0, 5, 'ai', null, null, 1, 0, 'M', '0', '0', '', 'robot', 'admin', NOW(), '', NULL, 'AI功能管理目录');

-- RAG文档管理菜单
INSERT INTO `sys_menu` VALUES (2001, 'RAG文档', 2000, 1, 'rag', 'ai/rag/index', null, 1, 0, 'C', '0', '0', 'ai:rag:list', 'documentation', 'admin', NOW(), '', NULL, 'RAG文档管理菜单');

-- RAG文档操作权限
INSERT INTO `sys_menu` VALUES (2002, '文档上传', 2001, 1, '', null, null, 1, 0, 'F', '0', '0', 'ai:rag:upload', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2003, '文档搜索', 2001, 2, '', null, null, 1, 0, 'F', '0', '0', 'ai:rag:search', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2004, '文档删除', 2001, 3, '', null, null, 1, 0, 'F', '0', '0', 'ai:rag:delete', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2005, '文档列表', 2001, 4, '', null, null, 1, 0, 'F', '0', '0', 'ai:rag:list', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2006, '清空索引', 2001, 5, '', null, null, 1, 0, 'F', '0', '0', 'ai:rag:clear', '#', 'admin', NOW(), '', NULL, '');

-- AI会话管理菜单
INSERT INTO `sys_menu` VALUES (2010, 'AI会话', 2000, 2, 'conversation', 'ai/conversation/index', null, 1, 0, 'C', '0', '0', 'ai:conversation:list', 'message', 'admin', NOW(), '', NULL, 'AI对话会话管理');

-- AI会话操作权限
INSERT INTO `sys_menu` VALUES (2011, '会话查询', 2010, 1, '', null, null, 1, 0, 'F', '0', '0', 'ai:conversation:query', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2012, '会话删除', 2010, 2, '', null, null, 1, 0, 'F', '0', '0', 'ai:conversation:delete', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2013, '会话导出', 2010, 3, '', null, null, 1, 0, 'F', '0', '0', 'ai:conversation:export', '#', 'admin', NOW(), '', NULL, '');

-- AI记忆管理菜单
INSERT INTO `sys_menu` VALUES (2020, 'AI记忆', 2000, 3, 'memory', 'ai/memory/index', null, 1, 0, 'C', '0', '0', 'ai:memory:list', 'memory', 'admin', NOW(), '', NULL, 'AI长期记忆管理');

-- AI记忆操作权限
INSERT INTO `sys_menu` VALUES (2021, '记忆查询', 2020, 1, '', null, null, 1, 0, 'F', '0', '0', 'ai:memory:query', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2022, '记忆删除', 2020, 2, '', null, null, 1, 0, 'F', '0', '0', 'ai:memory:delete', '#', 'admin', NOW(), '', NULL, '');

-- AI技能管理菜单
INSERT INTO `sys_menu` VALUES (2030, 'AI技能', 2000, 4, 'skill', 'ai/skill/index', null, 1, 0, 'C', '0', '0', 'ai:skill:list', 'skill', 'admin', NOW(), '', NULL, 'AI技能注册管理');

-- AI技能操作权限
INSERT INTO `sys_menu` VALUES (2031, '技能查询', 2030, 1, '', null, null, 1, 0, 'F', '0', '0', 'ai:skill:query', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2032, '技能新增', 2030, 2, '', null, null, 1, 0, 'F', '0', '0', 'ai:skill:add', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2033, '技能修改', 2030, 3, '', null, null, 1, 0, 'F', '0', '0', 'ai:skill:edit', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2034, '技能删除', 2030, 4, '', null, null, 1, 0, 'F', '0', '0', 'ai:skill:delete', '#', 'admin', NOW(), '', NULL, '');

-- AI统计监控菜单
INSERT INTO `sys_menu` VALUES (2040, 'AI统计', 2000, 5, 'statistics', 'ai/statistics/index', null, 1, 0, 'C', '0', '0', 'ai:statistics:list', 'chart', 'admin', NOW(), '', NULL, 'AI使用统计分析');

-- AI统计操作权限
INSERT INTO `sys_menu` VALUES (2041, 'Token统计', 2040, 1, '', null, null, 1, 0, 'F', '0', '0', 'ai:statistics:token', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2042, '使用统计', 2040, 2, '', null, null, 1, 0, 'F', '0', '0', 'ai:statistics:usage', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2043, '成本统计', 2040, 3, '', null, null, 1, 0, 'F', '0', '0', 'ai:statistics:cost', '#', 'admin', NOW(), '', NULL, '');

-- ----------------------------
-- 说明
-- ----------------------------
-- 1. 菜单ID从2000开始，避免与若依系统菜单冲突
-- 2. 菜单类型：M-目录，C-菜单，F-按钮
-- 3. 权限标识格式：模块:子模块:操作
-- 4. 所有菜单默认对admin开放，需在角色管理中分配给其他角色
