create schema cosmos_agent;

-- 进入cosmos_agent schema
\c cosmos_agent;

-- AI配置表
CREATE TABLE IF NOT EXISTS ai_configurations
(
    id         BIGSERIAL PRIMARY KEY,
    provider   VARCHAR(100) UNIQUE NOT NULL,
    api_key    VARCHAR(500)        NOT NULL,
    base_url   VARCHAR(500),
    model      VARCHAR(100),
    is_active  BOOLEAN   DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_ai_configurations_provider ON ai_configurations (provider);

-- 插入示例数据
INSERT INTO ai_configurations (provider, api_key, base_url, model, is_active)
VALUES ('openai', 'your-openai-api-key', 'https://api.openai.com', 'gpt-4', true)
ON CONFLICT (provider) DO NOTHING;

-- Agent表
CREATE TABLE agent
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    flow_data   TEXT,
    status      INTEGER   DEFAULT 0,
    config      TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Agent节点表
drop table if exists agent_node;
CREATE TABLE agent_node
(
    id         BIGSERIAL PRIMARY KEY,
    agent_id   BIGINT       NOT NULL,
    name       VARCHAR(255) NOT NULL,
    type       VARCHAR(100) NOT NULL,
    status     INTEGER   DEFAULT 1,
    position_x INTEGER      NOT NULL,
    position_y INTEGER      NOT NULL,
    config     TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_agent_name ON agent (name);
CREATE INDEX idx_agent_status ON agent (status);
CREATE INDEX idx_agent_node_agent_id ON agent_node (agent_id);
CREATE INDEX idx_agent_node_type ON agent_node (type);

-- 创建 Agent Link 表
CREATE TABLE agent_link
(
    id             BIGSERIAL PRIMARY KEY,
    agent_id       BIGINT      NOT NULL,
    source_node_id BIGINT      NOT NULL,
    target_node_id BIGINT      NOT NULL,
    link_type      VARCHAR(50) NOT NULL,
    name           VARCHAR(100) DEFAULT NULL,
    description    TEXT,
    condition      VARCHAR(200) DEFAULT NULL,
    sort_order     INTEGER      DEFAULT 0,
    status         INTEGER      DEFAULT 1,
    config         JSON         DEFAULT NULL,
    created_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_agent_link_agent_id ON agent_link (agent_id);
CREATE INDEX idx_agent_link_source_node_id ON agent_link (source_node_id);
CREATE INDEX idx_agent_link_target_node_id ON agent_link (target_node_id);
CREATE INDEX idx_agent_link_link_type ON agent_link (link_type);
CREATE INDEX idx_agent_link_status ON agent_link (status);

-- 聊天会话表
CREATE TABLE chat_session
(
    id         BIGSERIAL PRIMARY KEY,
    agent_id   BIGINT       NOT NULL,
    name       VARCHAR(255) NOT NULL,
    status     INTEGER   DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 聊天消息表
CREATE TABLE chat_message
(
    id         BIGSERIAL PRIMARY KEY,
    session_id BIGINT      NOT NULL,
    role       VARCHAR(50) NOT NULL,
    content    TEXT        NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_chat_session_agent_id ON chat_session (agent_id);
CREATE INDEX idx_chat_session_status ON chat_session (status);
CREATE INDEX idx_chat_session_created_at ON chat_session (created_at);
CREATE INDEX idx_chat_message_session_id ON chat_message (session_id);
CREATE INDEX idx_chat_message_created_at ON chat_message (created_at);
CREATE INDEX idx_chat_message_role ON chat_message (role);


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