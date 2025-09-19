create schema cosmos_agent;

create database cosmos_agent;

-- 进入cosmos_agent schema
\c cosmos_agent;

-- AI配置表
CREATE TABLE IF NOT EXISTS ai_configurations (
    id BIGSERIAL PRIMARY KEY,
    provider VARCHAR(100) UNIQUE NOT NULL,
    api_key VARCHAR(500) NOT NULL,
    base_url VARCHAR(500),
    model VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Agent表
CREATE TABLE IF NOT EXISTS agents (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(100) NOT NULL,
    description TEXT,
    configuration TEXT,
    status VARCHAR(50) DEFAULT 'CREATED',
    call_count BIGINT DEFAULT 0,
    last_called VARCHAR(100),
    is_deployed BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_ai_configurations_provider ON ai_configurations(provider);
CREATE INDEX IF NOT EXISTS idx_agents_type ON agents(type);
CREATE INDEX IF NOT EXISTS idx_agents_status ON agents(status);
CREATE INDEX IF NOT EXISTS idx_agents_deployed ON agents(is_deployed);

-- 插入示例数据
INSERT INTO ai_configurations (provider, api_key, base_url, model, is_active) VALUES
('openai', 'your-openai-api-key', 'https://api.openai.com', 'gpt-4', true)
ON CONFLICT (provider) DO NOTHING;

INSERT INTO agents (name, type, description, configuration, status) VALUES
('Customer Service Agent', 'CUSTOMER_SERVICE', '智能客服机器人', '{"language": "zh-CN", "temperature": 0.7}', 'CREATED'),
('Sales Assistant', 'SALES', '销售助手机器人', '{"products": ["electronics", "clothing"], "discount": 0.1}', 'CREATED'),
('Technical Support', 'TECHNICAL', '技术支持机器人', '{"expertise": ["software", "hardware"], "priority": "high"}', 'CREATED')
ON CONFLICT DO NOTHING;