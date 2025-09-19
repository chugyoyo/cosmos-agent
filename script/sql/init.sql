create schema cosmos_agent;

create database cosmos_agent;

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

-- Agent表
CREATE TABLE IF NOT EXISTS agents
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(200) NOT NULL,
    type          VARCHAR(100) NOT NULL,
    description   TEXT,
    configuration TEXT,
    status        VARCHAR(50) DEFAULT 'CREATED',
    call_count    BIGINT      DEFAULT 0,
    last_called   VARCHAR(100),
    is_deployed   BOOLEAN     DEFAULT false,
    created_at    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_ai_configurations_provider ON ai_configurations (provider);
CREATE INDEX IF NOT EXISTS idx_agents_type ON agents (type);
CREATE INDEX IF NOT EXISTS idx_agents_status ON agents (status);
CREATE INDEX IF NOT EXISTS idx_agents_deployed ON agents (is_deployed);

-- 插入示例数据
INSERT INTO ai_configurations (provider, api_key, base_url, model, is_active)
VALUES ('openai', 'your-openai-api-key', 'https://api.openai.com', 'gpt-4', true)
ON CONFLICT (provider) DO NOTHING;

INSERT INTO agents (name, type, description, configuration, status)
VALUES ('Customer Service Agent', 'CUSTOMER_SERVICE', '智能客服机器人', '{"language": "zh-CN", "temperature": 0.7}',
        'CREATED'),
       ('Sales Assistant', 'SALES', '销售助手机器人', '{"products": ["electronics", "clothing"], "discount": 0.1}',
        'CREATED'),
       ('Technical Support', 'TECHNICAL', '技术支持机器人',
        '{"expertise": ["software", "hardware"], "priority": "high"}', 'CREATED')
ON CONFLICT DO NOTHING;


-- Agent编排表
CREATE TABLE agent_orchestration
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

-- Agent编排节点表
CREATE TABLE agent_orchestration_node
(
    id               BIGSERIAL PRIMARY KEY,
    orchestration_id BIGINT       NOT NULL,
    name             VARCHAR(255) NOT NULL,
    type             VARCHAR(100) NOT NULL,
    position_x       INTEGER      NOT NULL,
    position_y       INTEGER      NOT NULL,
    config           TEXT,
    yaml_config      TEXT,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (orchestration_id) REFERENCES agent_orchestration (id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX idx_agent_orchestration_name ON agent_orchestration (name);
CREATE INDEX idx_agent_orchestration_status ON agent_orchestration (status);
CREATE INDEX idx_agent_orchestration_node_orchestration_id ON agent_orchestration_node (orchestration_id);
CREATE INDEX idx_agent_orchestration_node_type ON agent_orchestration_node (type);

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