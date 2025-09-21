<template>
  <div class="home-container">
    <!-- Hero Section -->
    <div class="hero-section">
      <div class="hero-content">
        <div class="hero-text">
          <h1 class="hero-title">
            <span class="gradient-text">cosmos-agent</span>
            <span class="subtitle">智宙</span>
          </h1>
          <p class="hero-description">
            AI Agent 智能机器人运行与编排平台
          </p>
          <div class="hero-actions">
            <el-button type="primary" size="large" @click="router.push('/agent')">
              <el-icon><Robot /></el-icon>
              开始使用
            </el-button>
            <el-button size="large" @click="router.push('/configurations/getAllConfigurations')">
              <el-icon><Setting /></el-icon>
              配置模型
            </el-button>
          </div>
        </div>
        <div class="hero-visual">
          <div class="floating-cards">
            <div class="card tech-card">
              <el-icon><Setting /></el-icon>
              <span>模型配置</span>
            </div>
            <div class="card tech-card">
              <el-icon><Robot /></el-icon>
              <span>智能工程师</span>
            </div>
            <div class="card tech-card">
              <el-icon><ChatDotRound /></el-icon>
              <span>智能对话</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Features Section -->
    <div class="features-section">
      <div class="section-header">
        <h2>核心功能</h2>
        <p>强大的 AI Agent 管理与编排能力</p>
      </div>
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon><Setting /></el-icon>
          </div>
          <h3>模型配置</h3>
          <p>支持多种 AI 大模型接入，包括 OpenAI、Azure、阿里云通义千问、智谱 AI、百度千帆等</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon><Robot /></el-icon>
          </div>
          <h3>智能工程师</h3>
          <p>管理和部署 AI Agent，支持运行状态监控、调用次数统计、日志信息展示</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <h3>智能对话</h3>
          <p>与 AI Agent 进行实时对话交互，支持流式响应</p>
        </div>
      </div>
    </div>

    <!-- Tech Stack Section -->
    <div class="tech-stack-section">
      <div class="section-header">
        <h2>技术栈</h2>
        <p>现代化的技术架构，确保高性能和可扩展性</p>
      </div>
      
      <div class="tech-stack-container">
        <div class="tech-column">
          <h3>后端技术</h3>
          <div class="tech-items">
            <div class="tech-item">
              <div class="tech-icon spring">Spring</div>
              <div class="tech-info">
                <h4>Spring Boot 3.x</h4>
                <p>核心后端框架</p>
              </div>
            </div>
            <div class="tech-item">
              <div class="tech-icon ai">Spring AI</div>
              <div class="tech-info">
                <h4>Spring AI</h4>
                <p>AI 模型调用与封装</p>
              </div>
            </div>
            <div class="tech-item">
              <div class="tech-icon db">PostgreSQL</div>
              <div class="tech-info">
                <h4>PostgreSQL 16</h4>
                <p>主数据库</p>
              </div>
            </div>
            <div class="tech-item">
              <div class="tech-icon mybatis">MyBatis</div>
              <div class="tech-info">
                <h4>MyBatis-Plus</h4>
                <p>数据库操作框架</p>
              </div>
            </div>
          </div>
        </div>

        <div class="tech-column">
          <h3>前端技术</h3>
          <div class="tech-items">
            <div class="tech-item">
              <div class="tech-icon vue">Vue</div>
              <div class="tech-info">
                <h4>Vue 3</h4>
                <p>现代化前端框架</p>
              </div>
            </div>
            <div class="tech-item">
              <div class="tech-icon vite">Vite</div>
              <div class="tech-info">
                <h4>Vite</h4>
                <p>快速构建工具</p>
              </div>
            </div>
            <div class="tech-item">
              <div class="tech-icon ts">TypeScript</div>
              <div class="tech-info">
                <h4>TypeScript</h4>
                <p>类型安全</p>
              </div>
            </div>
            <div class="tech-item">
              <div class="tech-icon element">Element</div>
              <div class="tech-info">
                <h4>Element Plus</h4>
                <p>UI 组件库</p>
              </div>
            </div>
            <div class="tech-item">
              <div class="tech-icon d3">D3.js</div>
              <div class="tech-info">
                <h4>D3.js</h4>
                <p>可视化编排</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Quick Start Section -->
    <div class="quick-start-section">
      <div class="section-header">
        <h2>快速开始</h2>
        <p>简单的步骤，快速搭建您的 AI Agent 平台</p>
      </div>
      <div class="steps-container">
        <div class="step">
          <div class="step-number">1</div>
          <div class="step-content">
            <h3>配置 AI 模型</h3>
            <p>在设置页面配置您的 AI 大模型 API Key</p>
          </div>
        </div>
        <div class="step">
          <div class="step-number">2</div>
          <div class="step-content">
            <h3>创建 Agent</h3>
            <p>通过可视化界面创建和编排您的 AI Agent</p>
          </div>
        </div>
        <div class="step">
          <div class="step-number">3</div>
          <div class="step-content">
            <h3>开始对话</h3>
            <p>与您的 Agent 进行智能对话交互</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Footer -->
    <div class="home-footer">
      <div class="footer-content">
        <div class="footer-section">
          <h3>cosmos-agent 智宙</h3>
          <p>AI Agent 智能机器人运行与编排平台</p>
        </div>
        <div class="footer-section">
          <h3>快速链接</h3>
          <div class="footer-links">
            <a href="#" @click="router.push('/agent')">智能工程师</a>
            <a href="#" @click="router.push('/configurations/getAllConfigurations')">配置设置</a>
            <a href="#" @click="router.push('/chat')">对话界面</a>
          </div>
        </div>
        <div class="footer-section">
          <h3>技术支持</h3>
          <p>基于现代化的技术栈构建</p>
          <div class="tech-tags">
            <span class="tech-tag">Vue 3</span>
            <span class="tech-tag">Spring Boot</span>
            <span class="tech-tag">PostgreSQL</span>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <p>&copy; 2024 cosmos-agent 智宙. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped lang="scss">
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.hero-section {
  padding: 80px 20px;
  text-align: center;
  color: white;
}

.hero-content {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
}

.hero-title {
  font-size: 4rem;
  font-weight: 700;
  margin-bottom: 20px;
  line-height: 1.2;
}

.gradient-text {
  background: linear-gradient(45deg, #fff, #e0e7ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  display: block;
  font-size: 2rem;
  color: #e0e7ff;
  margin-top: 10px;
}

.hero-description {
  font-size: 1.5rem;
  margin-bottom: 40px;
  opacity: 0.9;
}

.hero-actions {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.hero-visual {
  display: flex;
  justify-content: center;
  align-items: center;
}

.floating-cards {
  position: relative;
  width: 300px;
  height: 300px;
}

.card {
  position: absolute;
  width: 120px;
  height: 120px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  transition: transform 0.3s ease;
  
  .el-icon {
    font-size: 2rem;
    margin-bottom: 8px;
  }
}

.card:nth-child(1) {
  top: 0;
  left: 0;
  animation: float 3s ease-in-out infinite;
}

.card:nth-child(2) {
  top: 60px;
  right: 0;
  animation: float 3s ease-in-out infinite 1s;
}

.card:nth-child(3) {
  bottom: 0;
  left: 90px;
  animation: float 3s ease-in-out infinite 2s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}

.features-section {
  background: white;
  padding: 80px 20px;
}

.section-header {
  text-align: center;
  margin-bottom: 60px;
  
  h2 {
    font-size: 2.5rem;
    color: #1a202c;
    margin-bottom: 16px;
  }
  
  p {
    font-size: 1.2rem;
    color: #718096;
  }
}

.features-grid {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 40px;
}

.feature-card {
  text-align: center;
  padding: 40px 20px;
  border-radius: 16px;
  background: #f7fafc;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  
  &:hover {
    transform: translateY(-10px);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  }
}

.feature-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(45deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  
  .el-icon {
    font-size: 2rem;
    color: white;
  }
}

.feature-card h3 {
  font-size: 1.5rem;
  color: #1a202c;
  margin-bottom: 16px;
}

.feature-card p {
  color: #718096;
  line-height: 1.6;
}

.tech-stack-section {
  background: #1a202c;
  color: white;
  padding: 80px 20px;
}

.section-header h2 {
  color: white;
}

.tech-stack-container {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
}

.tech-column h3 {
  font-size: 1.8rem;
  margin-bottom: 30px;
  color: #e2e8f0;
}

.tech-items {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.tech-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  transition: background 0.3s ease;
  
  &:hover {
    background: rgba(255, 255, 255, 0.1);
  }
}

.tech-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  color: white;
  
  &.spring { background: #6db33f; }
  &.ai { background: #007396; }
  &.db { background: #336791; }
  &.mybatis { background: #ff6b35; }
  &.vue { background: #4fc08d; }
  &.vite { background: #646cff; }
  &.ts { background: #3178c6; }
  &.element { background: #409eff; }
  &.d3 { background: #ff6b6b; }
}

.tech-info h4 {
  font-size: 1.2rem;
  margin-bottom: 4px;
}

.tech-info p {
  color: #a0aec0;
  font-size: 0.9rem;
}

.quick-start-section {
  background: white;
  padding: 80px 20px;
}

.steps-container {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  gap: 40px;
}

.step {
  flex: 1;
  text-align: center;
  position: relative;
}

.step-number {
  width: 60px;
  height: 60px;
  background: linear-gradient(45deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: bold;
  color: white;
  margin: 0 auto 20px;
}

.step-content h3 {
  font-size: 1.3rem;
  color: #1a202c;
  margin-bottom: 12px;
}

.step-content p {
  color: #718096;
  line-height: 1.6;
}

.home-footer {
  background: #1a202c;
  color: white;
  padding: 60px 20px 20px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 40px;
  margin-bottom: 40px;
}

.footer-section h3 {
  font-size: 1.3rem;
  margin-bottom: 16px;
  color: #e2e8f0;
}

.footer-section p {
  color: #a0aec0;
  line-height: 1.6;
  margin-bottom: 16px;
}

.footer-links {
  display: flex;
  flex-direction: column;
  gap: 8px;
  
  a {
    color: #a0aec0;
    text-decoration: none;
    transition: color 0.3s ease;
    
    &:hover {
      color: #667eea;
    }
  }
}

.tech-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tech-tag {
  background: rgba(102, 126, 234, 0.2);
  color: #a0aec0;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
}

.footer-bottom {
  border-top: 1px solid #2d3748;
  padding-top: 20px;
  text-align: center;
  color: #a0aec0;
}

@media (max-width: 768px) {
  .hero-content {
    grid-template-columns: 1fr;
    gap: 40px;
  }
  
  .hero-title {
    font-size: 3rem;
  }
  
  .tech-stack-container {
    grid-template-columns: 1fr;
    gap: 40px;
  }
  
  .steps-container {
    flex-direction: column;
    gap: 30px;
  }
  
  .footer-content {
    grid-template-columns: 1fr;
    gap: 30px;
  }
}
</style>