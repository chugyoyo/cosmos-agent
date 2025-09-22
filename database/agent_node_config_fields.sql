-- 为 agent_node 表添加 llmConfig 和 startConfig 字段
ALTER TABLE agent_node
    ADD COLUMN llm_config   TEXT,
    ADD COLUMN start_config TEXT;

-- 更新现有记录，为它们设置默认的配置值
UPDATE agent_node
SET llm_config   = '{"model":"gpt-3.5-turbo","systemPrompt":"","userPrompt":"","temperature":0.7,"maxTokens":1000,"inputVariables":[]}',
    start_config = '{"inputVariables":[]}'
WHERE llm_config IS NULL
   OR start_config IS NULL;