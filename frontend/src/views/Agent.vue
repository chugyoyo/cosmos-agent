<template>
  <div class="agent-page">
    <!-- 顶部操作栏 -->
    <div class="top-bar">
      <div class="agent-selector">
        <el-select v-model="currentAgent" placeholder="选择 Agent" @change="selectAgent" size="large">
          <el-option
              v-for="agent in agents"
              :key="agent.id"
              :label="agent.name"
              :value="agent"
          >
            <div class="agent-option">
              <span class="agent-initial">{{ getAgentInitial(agent.name) }}</span>
              <span class="agent-name">{{ agent.name }}</span>
            </div>
          </el-option>
        </el-select>
      </div>
      <div class="top-actions">
        <el-button type="primary" size="large" @click="createNewAgent">
          <el-icon>
            <Plus/>
          </el-icon>
          新建 Agent
        </el-button>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="agent-layout">
      <!-- 右侧工作流编辑器 -->
      <main class="workflow-editor full-width">
        <div v-if="currentAgent" class="editor-container">
          <!-- 工具栏 -->
          <div class="toolbar">
            <div class="toolbar-left">
              <div class="agent-info-header">
                <h2>{{ currentAgent.name }}</h2>
                <p>{{ currentAgent.description || '暂无描述' }}</p>
              </div>
            </div>
            <div class="toolbar-right">
              <div class="toolbar-actions">
                <el-button-group>
                  <el-button @click="addNode" type="primary">
                    <el-icon>
                      <Plus/>
                    </el-icon>
                    添加节点
                  </el-button>
                  <el-button @click="startLinkCreation" :type="linkCreationMode ? 'success' : 'default'">
                    <el-icon>
                      <Link/>
                    </el-icon>
                    {{ linkCreationMode ? '取消连线' : '添加连线' }}
                  </el-button>
                </el-button-group>
                <el-button @click="clearWorkspace" type="warning">
                  <el-icon>
                    <Delete/>
                  </el-icon>
                  清空
                </el-button>
                <el-button @click="runWorkflow" type="success" :disabled="!canRunWorkflow">
                  <el-icon>
                    <VideoPlay/>
                  </el-icon>
                  运行工作流
                </el-button>
              </div>
            </div>
          </div>

          <!-- 画布区域 -->
          <div class="canvas-wrapper">
            <div ref="canvas" class="workflow-canvas">
              <!-- D3 将在这里动态创建 SVG -->
            </div>

            <!-- 画布工具栏 -->
            <div class="canvas-toolbar">
              <div class="zoom-controls">
                <el-button size="small" @click="zoomIn">
                  <el-icon>
                    <ZoomIn/>
                  </el-icon>
                </el-button>
                <el-button size="small" @click="zoomOut">
                  <el-icon>
                    <ZoomOut/>
                  </el-icon>
                </el-button>
                <el-button size="small" @click="resetZoom">
                  <el-icon>
                    <Refresh/>
                  </el-icon>
                </el-button>
              </div>
              <div class="view-controls">
                <el-button size="small" @click="fitToView">
                  <el-icon>
                    <FullScreen/>
                  </el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-workspace">
          <div class="empty-content">
            <div class="empty-icon">
              <el-icon size="64">
                <Connection/>
              </el-icon>
            </div>
            <h3>选择一个 Agent 开始编辑</h3>
            <p>从左侧列表中选择一个 Agent，或者创建一个新的 Agent 来开始构建您的工作流</p>
            <el-button type="primary" size="large" @click="createNewAgent">
              <el-icon>
                <Plus/>
              </el-icon>
              创建新 Agent
            </el-button>
          </div>
        </div>
      </main>
    </div>

    <!-- 节点编辑模态框 -->
    <div v-if="showNodeModal" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <h3>编辑节点 - {{ selectedNode?.name }}</h3>
          <button @click="closeNodeModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>节点名称:</label>
            <input v-model="editingNode.name" class="form-control"/>
          </div>
          <div class="form-group">
            <label>节点类型:</label>
            <select v-model="editingNode.type" class="form-control" @change="checkStartNodeLimit">
              <option value="START">START (开始节点)</option>
              <option value="LLM">LLM (大语言模型)</option>
            </select>
            <small class="text-muted" v-if="editingNode.type === 'START'">
              注意：一个画布只能有一个 START 节点
            </small>
          </div>

          <!-- START节点配置 -->
          <div v-if="editingNode.type === 'START'" class="start-config">
            <div class="form-group">
              <label>输入变量:</label>
              <div class="input-variables">
                <div v-for="(variable, index) in (editingNode.startConfig?.inputVariables || [])"
                     :key="index" class="variable-item">
                  <input v-model="variable.name" placeholder="变量名" class="form-control variable-name">
                  <input v-model="variable.defaultValue" placeholder="默认值" class="form-control variable-value">
                  <input v-model="variable.description" placeholder="描述" class="form-control variable-desc">
                  <button @click="removeStartVariable(index)" class="btn btn-sm btn-danger">删除</button>
                </div>
                <button @click="addStartVariable" class="btn btn-sm btn-secondary">添加变量</button>
              </div>
            </div>
          </div>

          <!-- LLM节点特有配置 -->
          <div v-if="editingNode.type === 'LLM'" class="llm-config">
            <div class="form-group">
              <label>模型选择:</label>
              <select v-model="editingNode.llmConfig.model" class="form-control"
                      style="color: #ffffff !important; background: #2a2a2a !important;">
                <option v-for="config in aiConfigurations"
                        :key="config.id"
                        :value="config.model"
                        style="color: #ffffff !important; background: #1a1a1a !important;">
                  {{ config.provider }} - {{ config.model }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label>系统提示词 (System Prompt):</label>
              <textarea v-model="editingNode.llmConfig.systemPrompt" class="form-control" rows="4"
                        placeholder="设置系统角色和指示..."></textarea>
            </div>

            <div class="form-group">
              <label>用户提示词 (User Prompt):</label>
              <textarea v-model="editingNode.llmConfig.userPrompt" class="form-control" rows="4"
                        placeholder="用户输入的提示词模板..."></textarea>
            </div>

            <div class="form-group">
              <label>温度 (Temperature):</label>
              <input type="range" v-model="editingNode.llmConfig.temperature"
                     min="0" max="2" step="0.1" class="form-range">
              <div class="temperature-value">{{ editingNode.llmConfig.temperature }}</div>
            </div>

            <div class="form-group">
              <label>最大令牌数 (Max Tokens):</label>
              <input type="number" v-model="editingNode.llmConfig.maxTokens"
                     class="form-control" min="1" max="8000">
            </div>

            <div class="form-group">
              <label>输入变量:</label>
              <div class="input-variables">
                <div v-for="(variable, index) in (editingNode.llmConfig?.inputVariables || [])"
                     :key="index" class="variable-item">
                  <input v-model="variable.name" placeholder="变量名" class="form-control variable-name">
                  <input v-model="variable.defaultValue" placeholder="默认值" class="form-control variable-value">
                  <button @click="removeInputVariable(index)" class="btn btn-sm btn-danger">删除</button>
                </div>
                <button @click="addInputVariable" class="btn btn-sm btn-secondary">添加变量</button>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="saveNode(selectedNode)" class="btn btn-primary">保存</button>
          <button @click="deleteNode(selectedNode)" class="btn btn-danger">删除</button>
          <button @click="closeNodeModal" class="btn btn-secondary">取消</button>
        </div>
      </div>
    </div>


    <!-- 连线编辑模态框 -->
    <div v-if="showLinkModal" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <h3>编辑连线</h3>
          <button @click="closeLinkModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>连线类型:</label>
            <select v-model="editingLink.type" class="form-control">
              <option value="flow">数据流</option>
              <option value="condition">条件判断</option>
              <option value="async">异步调用</option>
              <option value="error">错误处理</option>
            </select>
          </div>
          <div class="form-group">
            <label>连线名称:</label>
            <input v-model="editingLink.name" class="form-control" placeholder="输入连线名称"/>
          </div>
          <div class="form-group">
            <label>描述:</label>
            <textarea v-model="editingLink.description" class="form-control" rows="3"
                      placeholder="输入连线描述"></textarea>
          </div>
          <div class="form-group">
            <label>条件表达式:</label>
            <input v-model="editingLink.condition" class="form-control"
                   placeholder="如: success == true 或 result.code == 200"/>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="saveLink" class="btn btn-primary">保存</button>
          <button @click="deleteLink" class="btn btn-danger">删除</button>
          <button @click="closeLinkModal" class="btn btn-secondary">取消</button>
        </div>
      </div>
    </div>

    <!-- 连线创建模式提示 -->
    <div v-if="linkCreationMode" class="link-creation-hint">
      <div class="hint-content">
        <p>点击源节点，然后点击目标节点来创建连线</p>
        <button @click="cancelLinkCreation" class="btn btn-sm btn-secondary">取消</button>
      </div>
    </div>

    <!-- 新建代理模态框 -->
    <div v-if="showCreateModal" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <h3>新建代理</h3>
          <button @click="closeCreateModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>代理名称:</label>
            <input v-model="newAgent.name" class="form-control" placeholder="输入代理名称"/>
          </div>
          <div class="form-group">
            <label>描述:</label>
            <textarea v-model="newAgent.description" class="form-control" rows="3"
                      placeholder="输入代理描述"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="createAgent" class="btn btn-primary">创建</button>
          <button @click="closeCreateModal" class="btn btn-secondary">取消</button>
        </div>
      </div>
    </div>

    <!-- 编辑 Agent 模态框 -->
    <div v-if="showEditModal" class="modal-overlay" @click="closeEditModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>编辑代理</h3>
          <button @click="closeEditModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>代理名称:</label>
            <input v-model="editingAgent.name" class="form-control" placeholder="输入代理名称"/>
          </div>
          <div class="form-group">
            <label>描述:</label>
            <textarea v-model="editingAgent.description" class="form-control" rows="3"
                      placeholder="输入代理描述"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="saveAgent" class="btn btn-primary">保存</button>
          <button @click="closeEditModal" class="btn btn-secondary">取消</button>
        </div>
      </div>
    </div>

    <!-- 工作流运行抽屉 -->
    <div v-if="showRunDrawer" class="run-drawer-overlay">
      <div class="run-drawer">
        <div class="drawer-header">
          <h3>工作流运行</h3>
          <button @click="closeRunDrawer" class="close-btn">&times;</button>
        </div>
        <div class="drawer-body">
          <div v-if="!isRunning && !executionResult">
            <div class="form-group">
              <label>工作流参数:</label>
              <div v-if="startNode && startNode.startConfig" class="workflow-params">
                <div v-for="(variable, index) in startNode.startConfig.inputVariables"
                     :key="index" class="param-item">
                  <label>{{ variable.name || `参数${index + 1}` }}:</label>
                  <input v-model="workflowParams[variable.name]"
                         :placeholder="variable.description || `请输入${variable.name}`"
                         class="form-control">
                </div>
              </div>
              <div v-else class="no-params">
                <p>此工作流没有需要配置的参数</p>
              </div>
            </div>
            <div class="drawer-actions">
              <button @click="executeWorkflow" class="btn btn-success">
                <i class="fas fa-play"></i> 开始执行
              </button>
              <button @click="closeRunDrawer" class="btn btn-secondary">取消</button>
            </div>
          </div>

          <div v-else-if="isRunning" class="running-status">
            <div class="spinner"></div>
            <p>工作流正在执行中...</p>
            <div class="progress-container">
              <div class="progress-bar"></div>
            </div>
          </div>

          <div v-else class="execution-result">
            <h4>执行结果</h4>
            <div class="result-content">
              <pre>{{ JSON.stringify(executionResult, null, 2) }}</pre>
            </div>
            <div class="drawer-actions">
              <button @click="resetExecution" class="btn btn-primary">重新运行</button>
              <button @click="closeRunDrawer" class="btn btn-secondary">关闭</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as d3 from 'd3';
import {agentApi} from '@/services/api';
import {
  Plus,
  Search,
  MoreFilled,
  Link,
  Delete,
  VideoPlay,
  ZoomIn,
  ZoomOut,
  Refresh,
  FullScreen,
  Connection,
  ArrowLeft,
  ArrowRight
} from '@element-plus/icons-vue';

export default {
  name: 'Agent',
  components: {
    Plus,
    Search,
    MoreFilled,
    Link,
    Delete,
    VideoPlay,
    ZoomIn,
    ZoomOut,
    Refresh,
    FullScreen,
    Connection,
    ArrowLeft,
    ArrowRight
  },
  props: {
    id: {
      type: [String, Number],
      default: null
    }
  },
  data() {
    return {
      agents: [],
      currentAgent: null,
      aiConfigurations: [],
      graphNodes: [],
      graphLinks: [],
      selectedNode: null,
      selectedLink: null,
      editingNode: {
        name: '',
        type: 'START',
        x: 0,
        y: 0,
        config: {},
        startConfig: {
          inputVariables: []
        },
        llmConfig: {
          model: 'gpt-3.5-turbo',
          systemPrompt: '',
          userPrompt: '',
          temperature: 0.7,
          maxTokens: 1000,
          inputVariables: []
        }
      },
      editingLink: {},
      showNodeModal: false,
      showLinkModal: false,
      showCreateModal: false,
      showEditModal: false,
      editingAgent: null,
      agentListCollapsed: false,
      linkCreationMode: false,
      tempLinkSource: null,
      newAgent: {
        name: '',
        description: ''
      },
      simulation: null,
      zoom: null,
      svg: null, // 保存SVG元素引用
      resizeTimeout: null, // 防抖定时器
      d3Data: null,
      nextNodeId: 1,
      showRunDrawer: false,
      workflowParams: {},
      isRunning: false,
      executionResult: null,
      startNode: null,
      searchQuery: '',
      zoomLevel: 1
    };
  },
  mounted() {
    this.loadAgents();
    this.loadAIConfigurations();
    window.addEventListener('resize', this.handleResize);
    
    // 延迟检查工具栏位置，确保DOM已渲染
    setTimeout(() => {
      this.adjustToolbarPosition();
    }, 100);
  },
  watch: {
    id: {
      immediate: true,
      handler(newId) {
        if (newId) {
          this.loadAgentById(newId);
        }
      }
    }
  },
  beforeUnmount() {
    this.destroyGraph();
    window.removeEventListener('resize', this.handleResize);
    
    // 清理防抖定时器
    if (this.resizeTimeout) {
      clearTimeout(this.resizeTimeout);
    }
  },
  methods: {
    async loadAgentById(agentId) {
      try {
        // First load all agents to populate the sidebar
        await this.loadAgents();

        // Then find and select the specific agent
        const agent = this.agents.find(a => a.id == agentId);
        if (agent) {
          this.currentAgent = agent;
          await this.loadAgentNodes(agentId);
        }
      } catch (error) {
        console.error('加载代理失败:', error);
      }
    },

    async loadAgents() {
      try {
        const response = await agentApi.getAll();
        this.agents = response.data.data;
        console.log("loadAgents done, this.agents", this.agents);
      } catch (error) {
        console.error('加载代理列表失败:', error);
      }
    },

    async loadAIConfigurations() {
      try {
        const response = await agentApi.getAIConfigurations();
        this.aiConfigurations = response.data.data;
        console.log("loadAIConfigurations done, this.aiConfigurations", this.aiConfigurations);
      } catch (error) {
        console.error('加载AI配置失败:', error);
      }
    },

    async selectAgent(agent) {
      this.currentAgent = agent;
      console.log("agents", this.agents);
      console.log("selectAgent, agent", agent);
      // router
      this.$router.push({name: 'AgentDetail', params: {id: agent.id}});
      await this.loadAgentNodes(agent.id);
      
      // Agent切换后延迟更新SVG尺寸，确保DOM已渲染
      setTimeout(() => {
        this.updateSvgSize();
      }, 50);
    },

    async deleteAgent(agent) {
      if (confirm(`确定要删除代理 "${agent.name}" 吗？此操作不可恢复。`)) {
        try {
          await agentApi.deleteAgent(agent.id);
          // 从列表中移除
          this.agents = this.agents.filter(a => a.id !== agent.id);

          // 如果删除的是当前选中的代理，清空工作区
          if (this.currentAgent?.id === agent.id) {
            this.currentAgent = null;
            this.graphNodes = [];
            this.graphLinks = [];
            this.$router.push({name: 'Agent'});
          }

          console.log(`代理 "${agent.name}" 删除成功`);
        } catch (error) {
          console.error('删除代理失败:', error);
          alert('删除代理失败，请重试');
        }
      }
    },

    async loadAgentNodes(agentId) {
      try {
        // 加载节点
        const nodesResponse = await agentApi.getNodes(agentId);
        const nodes = nodesResponse.data.data.map(node => ({
          ...node,
          id: node.id,
          name: node.name,
          type: node.type,
          x: node.positionX,
          y: node.positionY,
          config: node.config ? JSON.parse(node.config) : null,
          llmConfig: node.llmConfig ? JSON.parse(node.llmConfig) : null,
          startConfig: node.startConfig ? JSON.parse(node.startConfig) : null
        }));

        console.log("loadAgentNodes nodes", nodes)

        // 加载连线数据
        const linksResponse = await agentApi.getLinks(agentId);
        const links = linksResponse.data.data.map(link => ({
          id: link.id,
          source: link.sourceNodeId,
          target: link.targetNodeId,
          type: link.linkType,
          name: link.name || link.linkType,
          description: link.description || '',
          condition: link.condition || '',
          agentId: link.agentId
        }));

        this.graphNodes = nodes;
        this.graphLinks = links;
        this.initGraph();
      } catch (error) {
        console.error('加载节点失败:', error);
      }
    },

    // 获取节点颜色
    getNodeColor(type) {
      const colors = {
        START: '#4CAF50',
        LLM: '#673AB7'
      };
      return colors[type] || '#607D8B';
    },

    // 获取节点半径
    getNodeRadius(node) {
      const name = node.name || '';
      const baseRadius = 35;

      // 根据文本长度计算所需半径
      const textLength = name.length;
      let radius = baseRadius;

      if (textLength <= 4) {
        radius = baseRadius;
      } else if (textLength <= 8) {
        radius = baseRadius + 10;
      } else if (textLength <= 12) {
        radius = baseRadius + 20;
      } else {
        radius = baseRadius + 25;
      }

      const weight = node.weight || 1;
      return radius * Math.sqrt(weight);
    },

    // 文本换行函数
    wrapText(text, maxCharsPerLine = 4) {
      if (!text) return [''];

      const lines = [];
      let currentLine = '';

      for (let i = 0; i < text.length; i++) {
        currentLine += text[i];

        if (currentLine.length >= maxCharsPerLine || i === text.length - 1) {
          lines.push(currentLine);
          currentLine = '';
        }
      }

      return lines.length > 0 ? lines : [''];
    },

    // 计算连线路径
    calculateLinkPath(source, target) {
      // 检查节点位置是否有效
      if (!source || !target ||
          typeof source.x !== 'number' || typeof source.y !== 'number' ||
          typeof target.x !== 'number' || typeof target.y !== 'number' ||
          isNaN(source.x) || isNaN(source.y) || isNaN(target.x) || isNaN(target.y)) {
        return 'M0,0 L0,0'; // 返回一个有效的空路径
      }

      const dx = target.x - source.x;
      const dy = target.y - source.y;
      const distance = Math.sqrt(dx * dx + dy * dy);

      // 如果距离为0或非常小，返回一个简单的路径
      if (distance < 1) {
        return `M${source.x},${source.y} L${target.x},${target.y}`;
      }

      // 获取节点半径
      const sourceRadius = this.getNodeRadius(source);
      const targetRadius = this.getNodeRadius(target);

      // 计算连线在圆圈边缘的起点和终点
      const sourceX = source.x + (dx / distance) * sourceRadius;
      const sourceY = source.y + (dy / distance) * sourceRadius;
      const targetX = target.x - (dx / distance) * targetRadius;
      const targetY = target.y - (dy / distance) * targetRadius;

      // 创建一个偏移量，根据连线方向来确定曲线的方向
      const offset = 30 * (dy > 0 ? 1 : -1);

      // 使用二次贝塞尔曲线，从圆圈边缘到圆圈边缘
      const midX = (sourceX + targetX) / 2;
      const midY = (sourceY + targetY) / 2 + offset;

      return `M${sourceX},${sourceY} Q${midX},${midY} ${targetX},${targetY}`;
    },

    // 拖拽开始
    dragstarted(event, d) {
      if (!event.active) this.simulation.alphaTarget(0.3).restart();
      d.fx = d.x;
      d.fy = d.y;
    },

    // 拖拽中
    dragged(event, d) {
      d.fx = event.x;
      d.fy = event.y;
    },

    // 拖拽结束
    dragended(event, d) {
      if (!event.active) this.simulation.alphaTarget(0);
      // 默认固定节点位置
      d.isFixed = true;
      const targetElement = event.sourceEvent.target.closest('g');
      if (targetElement) {
        d3.select(targetElement)
            .select("circle")
            .classed("fixed", true);
      }

      // 保存节点位置到后端
      this.saveNodePosition(d);
    },

    // 保存节点位置到后端
    async saveNodePosition(node) {
      try {
        const nodeData = {
          ...node,
          positionX: Math.round(node.x || 0),
          positionY: Math.round(node.y || 0),
          llmConfig: node.llmConfig? JSON.stringify(node.llmConfig) : null,
          startConfig: node.startConfig? JSON.stringify(node.startConfig): null,
        };

        await agentApi.saveUpdateNode(nodeData);
        console.log('节点位置已保存:', nodeData);
      } catch (error) {
        console.error('保存节点位置失败:', error);
      }
    },

    checkStartNodeLimit() {
      if (this.editingNode.type === 'START') {
        const existingStartNode = this.graphNodes.find(node =>
            node.type === 'START' && node.id !== this.selectedNode?.id
        );
        if (existingStartNode) {
          alert('一个画布只能有一个 START 节点。\n\n当前已存在 START 节点：' + existingStartNode.name + '\n\n请先删除现有的 START 节点，或选择其他节点类型。');
          this.editingNode.type = 'LLM';
        }
      }
    },

    // 节点点击事件
    handleNodeClick(node) {
      if (this.linkCreationMode) {
        // 在连线创建模式下，处理连线创建
        this.handleLinkCreationNodeClick(node);
      } else {
        // 正常模式下，编辑节点
        this.selectedNode = node;
        this.editingNode = {
          ...node,
          startConfig: node.startConfig || {
            inputVariables: []
          },
          llmConfig: node.llmConfig || {
            model: 'gpt-3.5-turbo',
            systemPrompt: '',
            userPrompt: '',
            temperature: 0.7,
            maxTokens: 1000,
            inputVariables: []
          }
        };
        this.showNodeModal = true;
      }
    },

    // 初始化图表
    initGraph() {
      const container = this.$refs.canvas;
      if (!container) return;

      // 先清理现有元素
      try {
        d3.select(container).selectAll("*").remove();
      } catch (error) {
        console.warn('清理图表元素时出错:', error);
        return;
      }

      if (!Array.isArray(this.graphNodes) || !this.graphNodes.length) {
        console.warn('没有可用的图表数据');
        return;
      }

      // 获取容器尺寸
      const width = container.clientWidth;
      const height = container.clientHeight;

      if (!width || !height) {
        console.warn('图表容器尺寸无效');
        return;
      }

      // 创建SVG容器
      this.svg = d3.select(container)
          .append("svg")
          .attr("width", width)
          .attr("height", height);

      // 创建箭头标记
      this.svg.append("defs").append("marker")
          .attr("id", "arrow")
          .attr("viewBox", "0 -5 10 10")
          .attr("refX", 8)
          .attr("refY", 0)
          .attr("markerWidth", 8)
          .attr("markerHeight", 8)
          .attr("orient", "auto")
          .append("path")
          .attr("d", "M0,-5L10,0L0,5")
          .attr("fill", "#666");

      // 创建容器组
      const g = this.svg.append("g");

      // 添加画布拖拽功能
      const zoom = d3.zoom()
          .scaleExtent([0.1, 4])
          .on("zoom", (event) => {
            g.attr("transform", event.transform);
          });

      this.svg.call(zoom);

      // 保存zoom实例以便后续使用
      this.zoom = zoom;

      // 创建力导向图布局 - 先设置节点的初始位置
      this.graphNodes.forEach(node => {
        // 如果节点有保存的位置，则固定该位置
        if (node.x !== undefined && node.y !== undefined) {
          node.fx = node.x;
          node.fy = node.y;
          node.isFixed = true;
        }
      });

      this.simulation = d3.forceSimulation(this.graphNodes)
          .force("link", d3.forceLink(this.graphLinks)
              .id(d => d.id)
              .distance(180)
              .strength(0.6))
          .force("charge", d3.forceManyBody()
              .strength(-800)
              .distanceMax(400))
          .force("center", d3.forceCenter(width / 2, height / 2))
          .force("collision", d3.forceCollide()
              .radius(d => this.getNodeRadius(d) + 25)
              .strength(1.0))
          .force("x", d3.forceX(d => d.fx || width / 2).strength(d => d.fx ? 0 : 0.05))
          .force("y", d3.forceY(d => d.fy || height / 2).strength(d => d.fy ? 0 : 0.05));

      // 创建连线组（先渲染连线，让节点在连线上面）
      const linkGroup = g.append("g")
          .selectAll("g")
          .data(this.graphLinks)
          .enter()
          .append("g");

      // 添加连线
      linkGroup.append("path")
          .attr("stroke", d => this.getLinkColor(d.type))
          .attr("stroke-opacity", 0.8)
          .attr("stroke-width", 2)
          .attr("fill", "transparent")
          .attr("marker-end", "url(#arrow)")
          .attr("class", "link-line")
          .attr("d", d => this.calculateLinkPath(d.source, d.target))
          .on("click", (event, d) => {
            event.stopPropagation();
            vm.handleLinkClick(d);
          });

      // 添加关系文本（在连线上）
      const linkLabels = linkGroup.append("text")
          .attr("text-anchor", "middle")
          .attr("dy", -5)
          .attr("fill", "#303133")
          .attr("font-size", "12px")
          .attr("class", "relation-label clickable")
          .text(d => d.name || d.type)
          .on("click", (event, d) => {
            event.stopPropagation();
            vm.handleLinkClick(d);
          });

      // 为关系文本添加背景
      linkLabels.each(function (d) {
        const bbox = this.getBBox();
        const padding = 2;
        const parent = this.parentElement;
        if (parent) {
          d3.select(parent)
              .insert("rect", "text")
              .attr("x", bbox.x - padding)
              .attr("y", bbox.y - padding)
              .attr("width", bbox.width + 2 * padding)
              .attr("height", bbox.height + 2 * padding)
              .attr("fill", "white")
              .attr("fill-opacity", 0.7)
              .attr("rx", 3)
              .attr("ry", 3)
              .attr("class", "relation-label-bg clickable")
              .style("cursor", "pointer")
              .style("pointer-events", "all")
              .on("click", (event) => {
                event.stopPropagation();
                vm.handleLinkClick(d);
              });
        }
      });

      // 创建节点组（后渲染节点，让节点在连线上面）
      const nodes = g.append("g")
          .selectAll("g")
          .data(this.graphNodes)
          .enter()
          .append("g")
          .call(d3.drag()
              .on("start", this.dragstarted)
              .on("drag", this.dragged)
              .on("end", this.dragended))
          .on("dblclick", (event, d) => {
            event.stopPropagation();
            // 切换固定状态
            d.isFixed = !d.isFixed;
            if (!d.isFixed) {
              d.fx = null;
              d.fy = null;
            } else {
              d.fx = d.x;
              d.fy = d.y;
            }
            // 更新节点样式
            const targetElement = event.target.closest('g');
            if (targetElement) {
              d3.select(targetElement)
                  .select("circle")
                  .classed("fixed", d.isFixed);
            }
          });

      // 添加节点圆形
      nodes.append("circle")
          .attr("r", d => this.getNodeRadius(d))
          .attr("fill", d => this.getNodeColor(d.type))
          .attr("stroke", "none")
          .attr("stroke-width", 0)
          .style("filter", "drop-shadow(1px 1px 3px rgba(0,0,0,0.15))")
          .classed("fixed", d => d.isFixed)
          .classed("node-circle", true)
          .on("click", (event, d) => {
            event.stopPropagation();
            this.handleNodeClick(d);
          });

      // 添加节点名称（在圆圈内，支持换行）
      const vm = this; // 保存 Vue 组件实例的引用
      nodes.each(function (d) {
        const nodeGroup = d3.select(this);
        const lines = vm.wrapText(d.name, 4);
        const lineHeight = 16;
        const totalHeight = lines.length * lineHeight;
        const startY = -totalHeight / 2 + lineHeight / 2;

        lines.forEach((line, index) => {
          nodeGroup.append("text")
              .text(line)
              .attr("text-anchor", "middle")
              .attr("dy", startY + index * lineHeight)
              .attr("fill", "#fff")
              .attr("font-size", lines.length > 2 ? "12px" : lines.length > 1 ? "14px" : "16px")
              .attr("font-weight", "bold")
              .attr("class", "node-name clickable")
              .style("pointer-events", "all")
              .on("click", (event) => {
                event.stopPropagation();
                vm.handleNodeClick(d);
              });
        });
      });

      // 添加类型标签（在圆圈下方）
      nodes.append("text")
          .text(d => d.type || '')
          .attr("text-anchor", "middle")
          .attr("dy", d => this.getNodeRadius(d) + 20)
          .attr("fill", "#666")
          .attr("font-size", "12px")
          .attr("font-weight", "600")
          .attr("class", "type-label clickable")
          .on("click", (event, d) => {
            event.stopPropagation();
            vm.handleNodeClick(d);
          });

  
      // 更新力导向图的tick函数
      this.simulation.on("tick", () => {
        // 更新连线位置
        linkGroup.selectAll("path")
            .attr("d", d => this.calculateLinkPath(d.source, d.target));

        // 更新节点组位置
        nodes.attr("transform", d => {
          const x = isNaN(d.x) ? 0 : d.x;
          const y = isNaN(d.y) ? 0 : d.y;
          return `translate(${x},${y})`;
        });

        // 更新关系文本和背景位置 - 基于曲线中点
        linkGroup.each(function (d) {
          const g = d3.select(this);

          // 检查节点位置是否有效
          if (!d.source || !d.target ||
              typeof d.source.x !== 'number' || typeof d.source.y !== 'number' ||
              typeof d.target.x !== 'number' || typeof d.target.y !== 'number' ||
              isNaN(d.source.x) || isNaN(d.source.y) || isNaN(d.target.x) || isNaN(d.target.y)) {
            return;
          }

          // 计算曲线的中点位置
          const dx = d.target.x - d.source.x;
          const dy = d.target.y - d.source.y;
          const distance = Math.sqrt(dx * dx + dy * dy);

          let midX, midY;

          // 如果距离为0或非常小，使用节点位置作为中点
          if (distance < 1) {
            midX = (d.source.x + d.target.x) / 2;
            midY = (d.source.y + d.target.y) / 2;
          } else {
            // 获取节点半径
            const sourceRadius = vm.getNodeRadius(d.source);
            const targetRadius = vm.getNodeRadius(d.target);

            // 计算连线在圆圈边缘的起点和终点
            const sourceX = d.source.x + (dx / distance) * sourceRadius;
            const sourceY = d.source.y + (dy / distance) * sourceRadius;
            const targetX = d.target.x - (dx / distance) * targetRadius;
            const targetY = d.target.y - (dy / distance) * targetRadius;

            // 计算曲线中点（考虑偏移）
            const offset = 30 * (dy > 0 ? 1 : -1);
            midX = (sourceX + targetX) / 2;
            midY = (sourceY + targetY) / 2 + offset;
          }

          const text = g.select("text");
          const rect = g.select("rect");
          const textElement = text.node();
          const bbox = textElement ? textElement.getBBox() : null;

          if (bbox) {
            text.attr("x", midX).attr("y", midY);
            rect
                .attr("x", midX - bbox.width / 2 - 2)
                .attr("y", midY - bbox.height - 2);
          }
        });
      });
      
      // 延迟调整工具栏位置，确保DOM已完全渲染
      setTimeout(() => {
        this.adjustToolbarPosition();
      }, 50);
    },

    // 处理窗口大小变化
    handleResize() {
      // 防抖处理，避免频繁更新
      if (this.resizeTimeout) {
        clearTimeout(this.resizeTimeout);
      }
      
      this.resizeTimeout = setTimeout(() => {
        // 优先更新SVG尺寸，避免重新初始化整个图表
        if (this.svg && this.$refs.canvas) {
          this.updateSvgSize();
        } else if (this.$refs.canvas && this.graphNodes && this.graphNodes.length > 0) {
          // 如果SVG不存在但有节点数据，重新初始化
          this.initGraph();
        }
        this.adjustToolbarPosition();
      }, 100); // 100ms 防抖延迟
    },

    // 调整工具栏位置以避免被遮挡
    adjustToolbarPosition() {
      const toolbar = document.querySelector('.canvas-toolbar');
      if (!toolbar) return;

      const canvas = this.$refs.canvas;
      if (!canvas) return;

      const canvasRect = canvas.getBoundingClientRect();
      const toolbarRect = toolbar.getBoundingClientRect();
      const viewportHeight = window.innerHeight;
      
      // 计算工具栏需要的空间（工具栏高度 + 安全边距）
      const toolbarHeight = toolbarRect.height + 32; // 32px 是安全边距
      const availableSpaceAtBottom = viewportHeight - canvasRect.bottom;
      
      // 检查底部是否有足够空间
      if (availableSpaceAtBottom < toolbarHeight) {
        // 底部空间不足，移动到顶部
        toolbar.style.bottom = 'auto';
        toolbar.style.top = '16px';
        toolbar.style.left = '16px';
        toolbar.style.transform = 'none';
      } else {
        // 底部空间充足，恢复到中央底部位置
        toolbar.style.bottom = '16px';
        toolbar.style.top = 'auto';
        toolbar.style.left = '50%';
        toolbar.style.transform = 'translateX(-50%)';
      }
    },

    // 更新SVG尺寸
    updateSvgSize() {
      if (!this.svg || !this.$refs.canvas) return;
      
      const container = this.$refs.canvas;
      const width = container.clientWidth;
      const height = container.clientHeight;
      
      if (!width || !height) {
        console.warn('图表容器尺寸无效');
        return;
      }
      
      // 动态更新SVG尺寸
      this.svg
        .attr("width", width)
        .attr("height", height);
        
      console.log(`SVG尺寸已更新: ${width}x${height}`);
    },

    // 销毁图表
    destroyGraph() {
      if (this.simulation) {
        this.simulation.stop();
        this.simulation = null;
      }
      if (this.$refs.canvas) {
        try {
          d3.select(this.$refs.canvas).selectAll("*").remove();
        } catch (error) {
          console.warn('销毁图表时出错:', error);
        }
      }
      this.svg = null;
    },

    createNewAgent() {
      this.showCreateModal = true;
      this.newAgent = {name: '', description: ''};
    },

    closeCreateModal() {
      this.showCreateModal = false;
    },
    closeEditModal() {
      this.showEditModal = false;
      this.editingAgent = null;
    },
    async saveAgent() {
      try {
        const response = await agentApi.updateAgent(this.editingAgent.id, this.editingAgent);
        const data = response.data.data || response.data;

        // 更新本地数据
        const index = this.agents.findIndex(a => a.id === this.editingAgent.id);
        if (index !== -1) {
          this.agents[index] = data;
        }

        // 如果编辑的是当前选中的 Agent，也要更新
        if (this.currentAgent && this.currentAgent.id === this.editingAgent.id) {
          this.currentAgent = data;
        }

        this.showEditModal = false;
        this.editingAgent = null;
      } catch (error) {
        console.error('保存 Agent 失败:', error);
        alert('保存 Agent 失败，请重试');
      }
    },

    async createAgent() {
      try {
        const response = await agentApi.create(this.newAgent);
        const data = response.data.data || response.data;
        this.agents.push(data);
        this.currentAgent = data;
        this.showCreateModal = false;
        this.graphNodes = [];
        this.graphLinks = [];
        this.linkCreationMode = false;
        this.tempLinkSource = null;
        this.destroyGraph();
      } catch (error) {
        console.error('创建代理失败:', error);
      }
    },

    addNode() {
      this.editingNode = {
        name: `LLM节点${this.graphNodes.length + 1}`,
        type: 'llm',
        x: 400 + Math.random() * 200 - 100,
        y: 300 + Math.random() * 200 - 100,
        config: {},
        startConfig: {
          inputVariables: []
        },
        llmConfig: {
          model: this.aiConfigurations.length > 0 ? this.aiConfigurations[0].model : 'gpt-3.5-turbo',
          systemPrompt: '',
          userPrompt: '',
          temperature: 0.7,
          maxTokens: 1000,
          inputVariables: []
        },
        isFixed: false,
        agentId: this.currentAgent?.id
      };
      this.selectedNode = null;
      this.showNodeModal = true;
    },

    // LLM配置相关方法
    addInputVariable() {
      if (!this.editingNode.llmConfig.inputVariables) {
        this.editingNode.llmConfig.inputVariables = [];
      }
      this.editingNode.llmConfig.inputVariables.push({
        name: '',
        defaultValue: ''
      });
    },

    removeInputVariable(index) {
      if (this.editingNode.llmConfig.inputVariables) {
        this.editingNode.llmConfig.inputVariables.splice(index, 1);
      }
    },

    // START节点配置相关方法
    addStartVariable() {
      if (!this.editingNode.startConfig) {
        this.editingNode.startConfig = {};
      }
      if (!this.editingNode.startConfig.inputVariables) {
        this.editingNode.startConfig.inputVariables = [];
      }
      this.editingNode.startConfig.inputVariables.push({
        name: '',
        defaultValue: '',
        description: ''
      });
    },

    removeStartVariable(index) {
      if (this.editingNode.startConfig && this.editingNode.startConfig.inputVariables) {
        this.editingNode.startConfig.inputVariables.splice(index, 1);
      }
    },

    // 工作流运行相关方法
    runWorkflow() {
      this.startNode = this.graphNodes.find(node => node.type === 'START');
      if (!this.startNode) {
        alert('请先添加开始节点');
        return;
      }

      this.showRunDrawer = true;
      this.workflowParams = {};
      this.executionResult = null;
      this.isRunning = false;
    },

    closeRunDrawer() {
      this.showRunDrawer = false;
      this.isRunning = false;
      this.executionResult = null;
    },

    async executeWorkflow() {
      if (!this.startNode) {
        alert('开始节点不存在');
        return;
      }

      this.isRunning = true;

      try {
        const workflowData = {
          agentId: this.currentAgent.id,
          params: this.workflowParams
        };

        const response = await agentApi.executeWorkflow(workflowData);
        this.executionResult = response.data.data;
      } catch (error) {
        console.error('工作流执行失败:', error);
        this.executionResult = {
          error: '工作流执行失败: ' + (error.response?.data?.message || error.message)
        };
      } finally {
        this.isRunning = false;
      }
    },

    resetExecution() {
      this.executionResult = null;
      this.isRunning = false;
    },

    editNode(node) {
      this.editingNode = {
        ...node,
        llmConfig: node.llmConfig || {
          model: 'gpt-3.5-turbo',
          systemPrompt: '',
          userPrompt: '',
          temperature: 0.7,
          maxTokens: 1000,
          inputVariables: []
        }
      };
      this.selectedNode = node;
      this.showNodeModal = true;
    },

    closeNodeModal() {
      this.showNodeModal = false;
      this.selectedNode = null;
      this.editingNode = {};
    },

    async saveNode() {
      if (!this.editingNode.name) {
        alert('请输入节点名称');
        return;
      }

      try {
        const nodeData = {
          name: this.editingNode.name,
          type: this.editingNode.type,
          agentId: this.currentAgent?.id,
          positionX: Math.round(this.editingNode.x || 0),
          positionY: Math.round(this.editingNode.y || 0),
          config: JSON.stringify(this.editingNode.config || {}),
          llmConfig: JSON.stringify(this.editingNode.llmConfig || {
            model: 'gpt-3.5-turbo',
            systemPrompt: '',
            userPrompt: '',
            temperature: 0.7,
            maxTokens: 1000,
            inputVariables: []
          }),
          startConfig: JSON.stringify(this.editingNode.startConfig || {
            inputVariables: []
          }),
          isFixed: this.editingNode.isFixed || false
        };

        if (this.selectedNode) {
          // 编辑现有节点
          nodeData.id = this.selectedNode.id;
          await agentApi.saveUpdateNode(nodeData);

          // 更新本地数据
          const nodeIndex = this.graphNodes.findIndex(n => n.id === this.selectedNode.id);
          if (nodeIndex !== -1) {
            this.graphNodes[nodeIndex] = {...this.editingNode, id: this.selectedNode.id};
          }
        } else {
          // 创建新节点
          const response = await agentApi.saveUpdateNode(nodeData);
          const newNode = {
            ...this.editingNode,
            id: response.data || Date.now().toString()
          };
          this.graphNodes.push(newNode);
        }

        this.closeNodeModal();
        this.initGraph();
      } catch (error) {
        console.error('保存节点失败:', error);
        if (error.response?.data?.message?.includes('START 节点')) {
          alert('一个画布只能有一个 START 节点，请先删除现有的 START 节点');
        } else {
          alert('保存节点失败: ' + (error.response?.data?.message || error.message));
        }
      }
    },

    async deleteNode() {
      if (!this.selectedNode) return;

      if (confirm('确定要删除这个节点吗？')) {
        // 从前端数组中移除
        this.graphNodes = this.graphNodes.filter(n => n.id !== this.selectedNode.id);

        // 如果是现有节点，从后端删除
        if (this.selectedNode.id < 10000) {
          try {
            await agentApi.deleteNode(this.selectedNode.id);
          } catch (error) {
            console.error('删除节点失败:', error);
          }
        }

        this.closeNodeModal();
        this.simulation.nodes(this.graphNodes);
        this.simulation.alpha(1).restart();
      }
    },

    clearWorkspace() {
      if (confirm('确定要清空所有节点和连线吗？')) {
        this.graphNodes = [];
        this.graphLinks = [];
        this.linkCreationMode = false;
        this.tempLinkSource = null;
        this.destroyGraph();
      }
    },

    // ===== 连线相关方法 =====

    // 开始创建连线
    startLinkCreation() {
      this.linkCreationMode = true;
      this.tempLinkSource = null;
    },

    // 取消连线创建
    cancelLinkCreation() {
      this.linkCreationMode = false;
      this.tempLinkSource = null;
    },

    // 处理连线创建模式下的节点点击
    handleLinkCreationNodeClick(node) {
      if (!this.linkCreationMode) return;

      if (!this.tempLinkSource) {
        // 第一次点击，选择源节点
        this.tempLinkSource = node;
        console.log('选择了源节点:', node.name);
      } else {
        // 第二次点击，选择目标节点，创建连线
        if (this.tempLinkSource.id !== node.id) {
          this.createLink(this.tempLinkSource, node);
        }
        this.linkCreationMode = false;
        this.tempLinkSource = null;
      }
    },

    // 创建连线
    createLink(sourceNode, targetNode) {
      this.editingLink = {
        source: sourceNode.id,
        target: targetNode.id,
        type: 'flow',
        name: `${sourceNode.name} → ${targetNode.name}`,
        description: '',
        condition: '',
        agentId: this.currentAgent?.id
      };
      this.selectedLink = null;
      this.showLinkModal = true;
    },

    // 连线点击事件
    handleLinkClick(link) {
      this.selectedLink = link;
      this.editingLink = {
        id: link.id,
        source: link.source,
        target: link.target,
        type: link.type,
        name: link.name,
        description: link.description || '',
        condition: link.condition || '',
        agentId: link.agentId
      };
      // 确保 source 和 target 是 ID 值，不是对象
      if (typeof this.editingLink.source === 'object' && this.editingLink.source !== null) {
        this.editingLink.source = this.editingLink.source.id;
      }
      if (typeof this.editingLink.target === 'object' && this.editingLink.target !== null) {
        this.editingLink.target = this.editingLink.target.id;
      }
      this.showLinkModal = true;
    },

    // 关闭连线模态框
    closeLinkModal() {
      this.showLinkModal = false;
      this.selectedLink = null;
      this.editingLink = {};
    },

    // 保存连线（统一处理新增和编辑）
    async saveLink() {
      if (!this.editingLink.name) {
        alert('请输入连线名称');
        return;
      }

      try {
        const linkData = {
          sourceNodeId: this.editingLink.source,
          targetNodeId: this.editingLink.target,
          linkType: this.editingLink.type,
          name: this.editingLink.name,
          description: this.editingLink.description || '',
          condition: this.editingLink.condition || '',
          agentId: this.currentAgent?.id
        };

        if (this.selectedLink) {
          // 编辑现有连线
          linkData.id = this.selectedLink.id;
          const response = await agentApi.saveUpdateLink(linkData);

          // 更新本地数据
          const updatedLink = response.data.data;
          const linkIndex = this.graphLinks.findIndex(l => l.id === this.selectedLink.id);
          if (linkIndex !== -1) {
            this.graphLinks[linkIndex] = {
              id: updatedLink.id,
              source: updatedLink.sourceNodeId,
              target: updatedLink.targetNodeId,
              type: updatedLink.linkType,
              name: updatedLink.name || updatedLink.linkType,
              description: updatedLink.description || '',
              condition: updatedLink.condition || '',
              agentId: updatedLink.agentId
            };
          }
        } else {
          // 创建新连线
          const response = await agentApi.saveUpdateLink(linkData);
          const newLinkData = response.data.data;
          const newLink = {
            id: newLinkData.id,
            source: newLinkData.sourceNodeId,
            target: newLinkData.targetNodeId,
            type: newLinkData.linkType,
            name: newLinkData.name || newLinkData.linkType,
            description: newLinkData.description || '',
            condition: newLinkData.condition || '',
            agentId: newLinkData.agentId
          };
          this.graphLinks.push(newLink);
        }

        this.closeLinkModal();
        this.initGraph();
      } catch (error) {
        console.error('保存连线失败:', error);
        alert('保存连线失败: ' + (error.response?.data?.message || error.message));
      }
    },

    // 删除连线
    async deleteLink() {
      if (!this.selectedLink) return;

      if (confirm('确定要删除这条连线吗？')) {
        try {
          await agentApi.deleteLink(this.selectedLink.id);
          this.graphLinks = this.graphLinks.filter(l => l.id !== this.selectedLink.id);
          this.closeLinkModal();
          this.initGraph(); // 重新渲染图表
        } catch (error) {
          console.error('删除连线失败:', error);
          alert('删除连线失败: ' + (error.response?.data?.message || error.message));
        }
      }
    },

    // 获取连线颜色
    getLinkColor(type) {
      const colors = {
        flow: '#1890ff',
        condition: '#52c41a',
        async: '#722ed1',
        error: '#f5222d'
      };
      return colors[type] || '#666';
    },

    // 新增方法
    getAgentNodeCount(agentId) {
      return this.graphNodes.filter(node => node.agentId === agentId).length;
    },

    getAgentStatus(agent) {
      // 这里可以根据实际业务逻辑返回状态
      return '活跃';
    },

    getAgentStatusType(agent) {
      // 这里可以根据实际业务逻辑返回状态类型
      return 'success';
    },

    getAgentInitial(name) {
      return name ? name.charAt(0).toUpperCase() : '?';
    },

    getAgentLinkCount(agentId) {
      // 获取 Agent 的连线数量
      return this.graphLinks.filter(link =>
          this.graphNodes.some(node => node.agentId === agentId &&
              (link.source === node.id || link.target === node.id))
      ).length;
    },

    toggleAgentList() {
      this.agentListCollapsed = !this.agentListCollapsed;
    },

    editAgent(agent) {
      // 编辑 Agent 信息
      this.editingAgent = {...agent};
      this.showEditModal = true;
    },

    duplicateAgent(agent) {
      // 复制 Agent
      console.log('复制 Agent:', agent);
    },

    // 缩放控制方法
    zoomIn() {
      this.zoomLevel = Math.min(this.zoomLevel * 1.2, 3);
      this.updateZoom();
    },

    zoomOut() {
      this.zoomLevel = Math.max(this.zoomLevel / 1.2, 0.1);
      this.updateZoom();
    },

    resetZoom() {
      this.zoomLevel = 1;
      this.updateZoom();
    },

    updateZoom() {
      if (this.svg) {
        this.svg.transition().duration(300).attr('transform', `scale(${this.zoomLevel})`);
      }
    },

    fitToView() {
      // 适应视图
      if (this.svg && this.graphNodes.length > 0) {
        const container = this.$refs.canvas;
        const width = container.clientWidth;
        const height = container.clientHeight;

        // 计算所有节点的边界
        let minX = Infinity, minY = Infinity, maxX = -Infinity, maxY = -Infinity;
        this.graphNodes.forEach(node => {
          minX = Math.min(minX, node.x);
          minY = Math.min(minY, node.y);
          maxX = Math.max(maxX, node.x);
          maxY = Math.max(maxY, node.y);
        });

        const nodeWidth = 100; // 节点宽度
        const nodeHeight = 100; // 节点高度
        const padding = 50;

        const scaleX = (width - padding * 2) / (maxX - minX + nodeWidth);
        const scaleY = (height - padding * 2) / (maxY - minY + nodeHeight);
        const scale = Math.min(scaleX, scaleY, 1);

        const centerX = (minX + maxX) / 2;
        const centerY = (minY + maxY) / 2;

        this.zoomLevel = scale;
        this.svg.transition().duration(500)
            .attr('transform', `translate(${width / 2 - centerX * scale}, ${height / 2 - centerY * scale}) scale(${scale})`);
      }
    },
  },

  computed: {
    canRunWorkflow() {
      return this.graphNodes.length > 0 && this.graphNodes.some(node => node.type === 'START');
    },

    filteredAgents() {
      if (!this.searchQuery) {
        return this.agents;
      }
      return this.agents.filter(agent =>
          agent.name.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
          (agent.description && agent.description.toLowerCase().includes(this.searchQuery.toLowerCase()))
      );
    }
  },
}
</script>

<style scoped lang="scss">
// 主页面容器
.agent-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--background-color);
  overflow: hidden;
}

// 顶部操作栏
.top-bar {
  background: var(--surface-color);
  border-bottom: 1px solid var(--border-color);
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

  .agent-selector {
    flex: 1;
    max-width: 400px;
    
    .el-select {
      width: 100%;
    }
  }

  .top-actions {
    .el-button {
      border-radius: 12px;
      font-weight: 600;
      padding: 12px 24px;
      height: auto;
    }
  }
}

// Agent 下拉选项样式
.agent-option {
  display: flex;
  align-items: center;
  gap: 12px;

  .agent-initial {
    width: 32px;
    height: 32px;
    background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 14px;
    font-weight: 600;
    flex-shrink: 0;
  }

  .agent-name {
    font-size: 14px;
    font-weight: 500;
    color: var(--text-color);
  }
}

// 主内容区域
.agent-layout {
  display: flex;
  flex: 1;
  overflow: hidden;
}

// 页面头部
.page-header {
  background: var(--surface-color);
  border-bottom: 1px solid var(--border-color);
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }

  .header-left {
    .page-title {
      font-size: 28px;
      font-weight: 700;
      color: var(--text-color);
      margin: 0 0 8px 0;
      background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .page-description {
      font-size: 16px;
      color: var(--text-secondary);
      margin: 0;
    }
  }

  .header-actions {
    .el-button {
      border-radius: 12px;
      font-weight: 600;
      padding: 12px 24px;
      height: auto;
    }
  }
}

// 主布局
.agent-layout {
  flex: 1;
  display: flex;
  overflow: hidden;
  width: 100%;
}

// 左侧 Agent 列表
.agent-sidebar {
  width: 320px;
  background: var(--surface-color);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: width var(--transition-normal);

  &.collapsed {
    width: 80px;

    .sidebar-header {
      padding: 16px 12px;
      justify-content: center;

      .collapse-btn .el-icon {
        transform: rotate(180deg);
      }
    }

    .agent-list {
      padding: 8px 4px;
    }
  }
}

.sidebar-header {
  padding: 24px;
  border-bottom: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;

  .collapse-btn {
    background: var(--background-color);
    border: 1px solid var(--border-color);
    border-radius: 6px;
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all var(--transition-fast);
    color: var(--text-secondary);

    &:hover {
      background: var(--primary-color);
      color: white;
      border-color: var(--primary-color);
    }

    .el-icon {
      font-size: 14px;
      transition: transform var(--transition-fast);
    }
  }
}

h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-color);
  margin: 0 0 16px 0;
}

.search-box {
  .el-input {
    .el-input__wrapper {
      border-radius: 8px;
      border: 1px solid var(--border-color);
      transition: all var(--transition-fast);

      &:hover {
        border-color: var(--primary-color);
      }

      &.is-focus {
        border-color: var(--primary-color);
        box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.1);
      }
    }
  }
}

.agent-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

// Agent 卡片
.agent-card {
  background: var(--surface-color);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all var(--transition-normal);
  position: relative;

  &.collapsed {
    padding: 12px 8px;
    text-align: center;
    margin-bottom: 8px;

    .agent-avatar-collapsed {
      width: 48px;
      height: 48px;
      background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 20px;
      font-weight: 600;
      margin: 0 auto;
    }

    &:hover {
      transform: scale(1.05);
    }
  }

  &:hover {
    border-color: var(--primary-color);
    box-shadow: 0 4px 12px rgba(22, 119, 255, 0.15);
    transform: translateY(-2px);
  }

  &.active {
    border-color: var(--primary-color);
    background: linear-gradient(135deg, rgba(22, 119, 255, 0.05), rgba(114, 46, 209, 0.05));
    box-shadow: 0 4px 12px rgba(22, 119, 255, 0.2);

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 4px;
      background: linear-gradient(180deg, var(--primary-color), var(--secondary-color));
      border-radius: 0 4px 4px 0;
    }
  }

  .agent-card-header {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 12px;

    .agent-icon {
      width: 40px;
      height: 40px;
      background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 18px;
      flex-shrink: 0;
    }

    .agent-info {
      flex: 1;
      min-width: 0;

      .agent-name {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-color);
        margin: 0 0 4px 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .agent-desc {
        font-size: 13px;
        color: var(--text-secondary);
        margin: 0;
        line-height: 1.4;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
    }

    .agent-actions {
      flex-shrink: 0;
      opacity: 0;
      transition: opacity var(--transition-fast);
    }
  }

  &:hover .agent-actions {
    opacity: 1;
  }

  .agent-stats {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 12px;
    border-top: 1px solid var(--border-light);

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;

      .stat-label {
        font-size: 11px;
        color: var(--text-tertiary);
        font-weight: 500;
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }

      .stat-value {
        font-size: 14px;
        font-weight: 600;
        color: var(--text-color);
      }
    }
  }
}

// 工作流编辑器
.workflow-editor {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--background-color);
  overflow: hidden;

  &.full-width {
    width: 100%;
  }
}

// 编辑器容器
.editor-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

// 工具栏
.toolbar {
  background: var(--surface-color);
  border-bottom: 1px solid var(--border-color);
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 73px;
  flex-shrink: 0;

  .toolbar-left {
    .agent-info-header {
      h2 {
        font-size: 20px;
        font-weight: 600;
        color: var(--text-color);
        margin: 0 0 4px 0;
      }

      p {
        font-size: 14px;
        color: var(--text-secondary);
        margin: 0;
      }
    }
  }

  .toolbar-right {
    .toolbar-actions {
      display: flex;
      align-items: center;
      gap: 12px;

      .el-button-group {
        .el-button {
          border-radius: 8px;
          font-weight: 500;
        }
      }

      .el-button {
        border-radius: 8px;
        font-weight: 500;
        height: 36px;
        padding: 0 16px;
      }
    }
  }
}

// 画布包装器
.canvas-wrapper {
  flex: 1;
  position: relative;
  background: var(--surface-color);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
}

// 工作流画布
.workflow-canvas {
  flex: 1;
  width: 100%;
  height: 100%;
  position: relative;
  background: radial-gradient(circle at 20px 20px, var(--border-light) 1px, transparent 1px),
  radial-gradient(circle at 60px 60px, var(--border-light) 1px, transparent 1px);
  background-size: 40px 40px;
  background-position: 0 0, 20px 20px;
  min-height: 400px;
}

// 画布工具栏
.canvas-toolbar {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  padding: 8px;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 100;
  transition: all 0.3s ease;
  
  // 当屏幕高度不足时，自动调整位置
  @media (max-height: 600px) {
    bottom: auto;
    top: 80px;
  }
  
  @media (max-height: 500px) {
    bottom: auto;
    top: 16px;
  }

  .zoom-controls,
  .view-controls {
    display: flex;
    gap: 4px;
  }

  .el-button {
    width: 32px;
    height: 32px;
    padding: 0;
    border-radius: 6px;
    font-size: 14px;
  }
}

// 空状态
.empty-workspace {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--surface-color);
  margin: 16px;
  border-radius: 12px;
  border: 1px solid var(--border-color);

  .empty-content {
    text-align: center;
    max-width: 400px;

    .empty-icon {
      color: var(--text-tertiary);
      margin-bottom: 24px;
    }

    h3 {
      font-size: 24px;
      font-weight: 600;
      color: var(--text-color);
      margin: 0 0 12px 0;
    }

    p {
      font-size: 16px;
      color: var(--text-secondary);
      margin: 0 0 24px 0;
      line-height: 1.6;
    }

    .el-button {
      border-radius: 12px;
      font-weight: 600;
      padding: 12px 24px;
      height: auto;
    }
  }
}

// 响应式设计
@media (max-width: 1024px) {
  .top-bar {
    flex-direction: column;
    gap: 12px;
    padding: 16px;

    .agent-selector {
      max-width: 100%;
      width: 100%;
    }
  }
}

@media (max-width: 768px) {
  .top-bar {
    padding: 12px 16px;

    .agent-selector {
      max-width: 100%;
      width: 100%;
    }
  }

  .toolbar {
    padding: 12px 16px;
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;

    .toolbar-right {
      width: 100%;

      .toolbar-actions {
        flex-wrap: wrap;
        justify-content: flex-start;
      }
    }
  }

  .canvas-wrapper {
    margin: 8px;
  }
}


/* D3图表样式 */
:deep(svg) {
  width: 100%;
  height: 100%;

  .node-circle {
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      filter: drop-shadow(2px 2px 6px rgba(0, 0, 0, 0.3)) !important;
      transform: scale(1.05);
    }

    &.highlighted {
      filter: drop-shadow(0 0 10px rgba(64, 158, 255, 0.8)) !important;
      transform: scale(1.1);
    }

    &.fixed {
      filter: drop-shadow(0 0 8px rgba(255, 193, 7, 0.6)) !important;
    }
  }

  .node-name {
    cursor: pointer;
    pointer-events: all;
    user-select: none;
    text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);

    &.highlighted {
      fill: #409eff !important;
      font-weight: bold;
      text-shadow: 0 0 4px rgba(64, 158, 255, 0.8);
    }
  }

  .type-label {
    cursor: pointer;
    pointer-events: all;
    user-select: none;

    &.highlighted {
      fill: #409eff !important;
      font-weight: bold;
    }
  }

  .link-line {
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      stroke-width: 3px !important;
      stroke-opacity: 1 !important;
    }

    &.highlighted {
      stroke: #409eff !important;
      stroke-width: 4px !important;
      stroke-opacity: 1 !important;
    }
  }
}


.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 600px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #ddd;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #ddd;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.form-control {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.yaml-editor {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.4;
  resize: vertical;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-primary {
  background: #2196F3;
  color: white;
}

.btn-primary:hover {
  background: #1976D2;
}

.btn-success {
  background: #4CAF50;
  color: white;
}

.btn-success:hover {
  background: #45a049;
}

.btn-danger {
  background: #f44336;
  color: white;
}

.btn-danger:hover {
  background: #da190b;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #5a6268;
}

.btn-sm {
  padding: 4px 8px;
  font-size: 12px;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}

.close-btn:hover {
  color: #333;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.workspace-actions {
  display: flex;
  gap: 10px;
}

/* 连线创建模式提示 */
.link-creation-hint {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: #1890ff;
  color: white;
  padding: 12px 24px;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;

  .hint-content {
    display: flex;
    align-items: center;
    gap: 12px;

    p {
      margin: 0;
      font-weight: 500;
    }
  }
}

/* 连线相关样式 */
.btn-info {
  background: #17a2b8;
  color: white;

  &:hover {
    background: #138496;
  }
}

.btn-warning {
  background: #ffc107;
  color: #212529;

  &:hover {
    background: #e0a800;
  }
}

/* D3连线样式增强 */
:deep(svg) {
  .relation-label {
    cursor: pointer;
    pointer-events: all;
    user-select: none;
    font-weight: 500;

    &.highlighted {
      fill: #409eff !important;
      font-weight: bold;
    }
  }

  .relation-label-bg {
    cursor: pointer;
    pointer-events: all;
    mix-blend-mode: normal;

    &:hover {
      fill-opacity: 0.9 !important;
    }

    &.highlighted {
      fill: #409eff !important;
      fill-opacity: 0.3 !important;
    }
  }
}

/* 智宙主题样式 */
.agent-container {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
}

.agent-header {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.agent-header h2 {
  color: #ffffff;
  font-weight: 600;
  margin: 0;
}

.sidebar {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border-right: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar h3 {
  color: #ffffff;
  font-weight: 500;
  margin-top: 0;
}

.agent-item {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #ffffff;
}

.agent-item:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.3);
}

.agent-item.active {
  background: rgba(255, 255, 255, 0.2);
  border-color: #64b5f6;
}

.agent-info {
  flex: 1;
  cursor: pointer;
}

.delete-agent-btn {
  padding: 4px 8px;
  font-size: 12px;
  opacity: 0.7;
  transition: opacity 0.3s;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #ffffff;
}

.delete-agent-btn:hover {
  opacity: 1;
  background: rgba(244, 67, 54, 0.2);
  border-color: #f44336;
}

.workspace-header {
  background: rgba(255, 255, 255, 0.05);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.workspace-header h3 {
  color: #ffffff;
  font-weight: 500;
  margin: 0;
}

.canvas-container {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
}

.btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #ffffff;
  backdrop-filter: blur(10px);
}

.btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
}

.btn-primary {
  background: linear-gradient(45deg, #42a5f5, #1e88e5);
  border: none;
}

.btn-success {
  background: linear-gradient(45deg, #66bb6a, #43a047);
  border: none;
}

.btn-info {
  background: linear-gradient(45deg, #26c6da, #00acc1);
  border: none;
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.modal-content {
  background: linear-gradient(135deg, #2c3e50, #34495e);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-header h3 {
  color: #ffffff;
  font-weight: 500;
}

.form-control {
  background: rgba(30, 30, 30, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #ffffff;
  border-radius: 8px;
}

.form-control:focus {
  background: rgba(40, 40, 40, 0.95);
  border-color: #64b5f6;
  color: #ffffff;
  box-shadow: 0 0 0 2px rgba(100, 181, 246, 0.2);
}

/* 下拉框选项样式 */
select.form-control option {
  background: #1a1a1a !important;
  color: #ffffff !important;
  padding: 8px;
}

select.form-control option:hover {
  background: #333333 !important;
  color: #ffffff !important;
}

/* 确保下拉框文字可见 */
select.form-control {
  color: #ffffff !important;
  background: #2a2a2a !important;
}

/* 下拉框展开时的背景色 */
select.form-control:focus {
  color: #ffffff !important;
  background: #2a2a2a !important;
}

.form-control::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

/* START节点配置样式 */
.start-config {
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.1), rgba(56, 142, 60, 0.1));
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  border: 1px solid rgba(76, 175, 80, 0.2);
}

/* LLM配置样式 */
.llm-config {
  background: linear-gradient(135deg, rgba(103, 58, 183, 0.1), rgba(81, 45, 168, 0.1));
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  border: 1px solid rgba(103, 58, 183, 0.2);
}

.llm-config .form-group {
  margin-bottom: 16px;
}

.llm-config .form-group:last-child {
  margin-bottom: 0;
}

.temperature-value {
  text-align: center;
  font-weight: 600;
  color: #673AB7;
  margin-top: 8px;
}

.form-range {
  width: 100%;
  height: 6px;
  background: #ddd;
  border-radius: 3px;
  outline: none;
}

.form-range::-webkit-slider-thumb {
  appearance: none;
  width: 20px;
  height: 20px;
  background: #673AB7;
  border-radius: 50%;
  cursor: pointer;
}

.input-variables {
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
}

.variable-item {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  align-items: center;
}

.variable-item:last-child {
  margin-bottom: 0;
}

.variable-name {
  flex: 1;
  min-width: 120px;
}

.variable-value {
  flex: 1;
  min-width: 120px;
}

.variable-desc {
  flex: 2;
  min-width: 150px;
}

.variable-item .btn {
  flex-shrink: 0;
  padding: 4px 8px;
  font-size: 12px;
}

/* 工作流运行抽屉样式 */
.run-drawer-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  z-index: 2000;
  display: flex;
  justify-content: flex-end;
}

.run-drawer {
  width: 400px;
  max-width: 90vw;
  height: 100%;
  background: linear-gradient(135deg, #2c3e50, #34495e);
  box-shadow: -5px 0 20px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
  }
  to {
    transform: translateX(0);
  }
}

.drawer-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.05);
}

.drawer-header h3 {
  color: #ffffff;
  font-weight: 500;
  margin: 0;
}

.drawer-body {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.workflow-params {
  margin-bottom: 20px;
}

.param-item {
  margin-bottom: 16px;
}

.param-item label {
  display: block;
  color: #ffffff;
  margin-bottom: 8px;
  font-weight: 500;
}

.no-params {
  text-align: center;
  color: rgba(255, 255, 255, 0.7);
  padding: 20px;
}

.drawer-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 20px;
}

.running-status {
  text-align: center;
  color: #ffffff;
  padding: 40px 20px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-top: 4px solid #64b5f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.progress-container {
  width: 100%;
  height: 4px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
  overflow: hidden;
  margin-top: 20px;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #64b5f6, #42a5f5);
  width: 60%;
  animation: progress 2s ease-in-out infinite;
}

@keyframes progress {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

.execution-result {
  color: #ffffff;
}

.execution-result h4 {
  color: #64b5f6;
  margin-top: 0;
  margin-bottom: 16px;
}

.result-content {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 8px;
  padding: 16px;
  max-height: 400px;
  overflow-y: auto;
}

.result-content pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  color: #64b5f6;
  font-size: 14px;
  line-height: 1.5;
}
</style>