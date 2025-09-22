-- 迁移脚本：删除 agent_node 表中的 yaml_config 列
-- 执行前请备份数据库

-- 进入cosmos_agent schema
\c cosmos_agent;

-- 删除 yaml_config 列
ALTER TABLE agent_node DROP COLUMN IF EXISTS yaml_config;

-- 验证列已删除
\d agent_node;
