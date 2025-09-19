<template>
  <div class="agent-container">
    <div class="agent-header">
      <h2>Agent 管理</h2>
      <div class="header-actions">
        <button @click="createNewAgent" class="btn btn-primary">新建代理</button>
        <button @click="saveAgent" class="btn btn-success" :disabled="!currentAgent">保存</button>
      </div>
    </div>

    <div class="agent-content">
      <div class="sidebar">
        <h3>代理列表</h3>
        <div class="agent-list">
          <div v-for="agent in agents" :key="agent.id" 
               class="agent-item" 
               :class="{ active: currentAgent?.id === agent.id }"
               @click="selectAgent(agent)">
            <div class="agent-name">{{ agent.name }}</div>
            <div class="agent-desc">{{ agent.description }}</div>
          </div>
        </div>
      </div>

      <div class="main-content">
        <div v-if="currentAgent" class="agent-workspace">
          <div class="workspace-header">
            <h3>{{ currentAgent.name }}</h3>
            <div class="workspace-actions">
              <button @click="addNode" class="btn btn-sm btn-primary">添加节点</button>
              <button @click="startLinkCreation" class="btn btn-sm btn-info">添加连线</button>
              <button @click="clearWorkspace" class="btn btn-sm btn-secondary">清空</button>
            </div>
          </div>
          
          <div class="canvas-container">
            <div ref="canvas" class="agent-canvas">
              <!-- D3将在这里动态创建SVG -->
            </div>
          </div>
        </div>
        
        <div v-else class="empty-state">
          <p>请选择或创建一个代理</p>
        </div>
      </div>
    </div>

    <!-- 节点编辑模态框 -->
    <div v-if="showNodeModal" class="modal-overlay" @click="closeNodeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>编辑节点 - {{ selectedNode?.name }}</h3>
          <button @click="closeNodeModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>节点名称:</label>
            <input v-model="editingNode.name" class="form-control" />
          </div>
          <div class="form-group">
            <label>节点类型:</label>
            <select v-model="editingNode.type" class="form-control">
              <option value="input">输入节点</option>
              <option value="process">处理节点</option>
              <option value="output">输出节点</option>
              <option value="condition">条件节点</option>
            </select>
          </div>
          <div class="form-group">
            <label>YAML 配置:</label>
            <textarea v-model="editingNode.yamlConfig" class="form-control yaml-editor" rows="10" 
                      placeholder="输入 YAML 配置..."></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="saveNode" class="btn btn-primary">保存</button>
          <button @click="deleteNode" class="btn btn-danger">删除</button>
          <button @click="closeNodeModal" class="btn btn-secondary">取消</button>
        </div>
      </div>
    </div>

    <!-- 节点编辑模态框 -->
    <div v-if="showNodeModal" class="modal-overlay" @click="closeNodeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>编辑节点 - {{ selectedNode?.name }}</h3>
          <button @click="closeNodeModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>节点名称:</label>
            <input v-model="editingNode.name" class="form-control" />
          </div>
          <div class="form-group">
            <label>节点类型:</label>
            <select v-model="editingNode.type" class="form-control">
              <option value="input">输入节点</option>
              <option value="process">处理节点</option>
              <option value="output">输出节点</option>
              <option value="condition">条件节点</option>
            </select>
          </div>
          <div class="form-group">
            <label>YAML 配置:</label>
            <textarea v-model="editingNode.yamlConfig" class="form-control yaml-editor" rows="10" 
                      placeholder="输入 YAML 配置..."></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="saveNode" class="btn btn-primary">保存</button>
          <button @click="deleteNode" class="btn btn-danger">删除</button>
          <button @click="closeNodeModal" class="btn btn-secondary">取消</button>
        </div>
      </div>
    </div>

    <!-- 连线编辑模态框 -->
    <div v-if="showLinkModal" class="modal-overlay" @click="closeLinkModal">
      <div class="modal-content" @click.stop>
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
            <input v-model="editingLink.name" class="form-control" placeholder="输入连线名称" />
          </div>
          <div class="form-group">
            <label>描述:</label>
            <textarea v-model="editingLink.description" class="form-control" rows="3" 
                      placeholder="输入连线描述"></textarea>
          </div>
          <div class="form-group">
            <label>条件表达式:</label>
            <input v-model="editingLink.condition" class="form-control" 
                   placeholder="如: success == true 或 result.code == 200" />
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
    <div v-if="showCreateModal" class="modal-overlay" @click="closeCreateModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>新建代理</h3>
          <button @click="closeCreateModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>代理名称:</label>
            <input v-model="newAgent.name" class="form-control" placeholder="输入代理名称" />
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
  </div>
</template>

<script>
import * as d3 from 'd3';
import { agentOrchestrationApi } from '@/services/api';

export default {
  name: 'Agent',
  data() {
    return {
      agents: [],
      currentAgent: null,
      graphNodes: [],
      graphLinks: [],
      selectedNode: null,
      selectedLink: null,
      editingNode: {},
      editingLink: {},
      showNodeModal: false,
      showLinkModal: false,
      showCreateModal: false,
      linkCreationMode: false,
      tempLinkSource: null,
      newAgent: {
        name: '',
        description: ''
      },
      simulation: null,
      d3Data: null,
      nextNodeId: 1,
      nextLinkId: 1
    };
  },
  mounted() {
    this.loadAgents();
    window.addEventListener('resize', this.handleResize);
  },
  beforeUnmount() {
    this.destroyGraph();
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    async loadAgents() {
      try {
        const response = await agentOrchestrationApi.getAll();
        this.agents = response.data.data;
        console.log("loadAgents done, this.agents", this.agents);
      } catch (error) {
        console.error('加载代理列表失败:', error);
      }
    },
    
    async selectAgent(agent) {
      this.currentAgent = agent;
      console.log("agents", this.agents);
      console.log("selectAgent, agent", agent);
      await this.loadAgentNodes(agent.id);
    },
    
    async loadAgentNodes(agentId) {
      try {
        // 加载节点
        const nodesResponse = await agentOrchestrationApi.getNodes(agentId);
        const nodes = nodesResponse.data.data.map(node => ({
          id: node.id,
          name: node.name,
          type: node.type,
          x: node.positionX,
          y: node.positionY,
          yamlConfig: node.yamlConfig || '',
          config: node.config || {}
        }));
        
        // 加载连线数据（这里需要根据实际API调整）
        const links = []; // 暂时为空，后续可以从后端获取
        
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
        input: '#4CAF50',
        process: '#2196F3',
        output: '#FF9800',
        condition: '#9C27B0'
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
    },
    
    // 节点点击事件
    handleNodeClick(node) {
      if (this.linkCreationMode) {
        // 在连线创建模式下，处理连线创建
        this.handleLinkCreationNodeClick(node);
      } else {
        // 正常模式下，编辑节点
        this.selectedNode = node;
        this.editingNode = { ...node };
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
      const svg = d3.select(container)
        .append("svg")
        .attr("width", width)
        .attr("height", height);
      
      // 创建箭头标记
      svg.append("defs").append("marker")
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
      const g = svg.append("g");
      
      // 创建力导向图布局
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
        .force("x", d3.forceX(width / 2).strength(0.05))
        .force("y", d3.forceY(height / 2).strength(0.05));
      
      // 创建节点组
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
      
      // 创建连线组
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
            .attr("fill-opacity", 0.8)
            .attr("rx", 2)
            .attr("ry", 2)
            .attr("class", "relation-label-bg clickable")
            .style("cursor", "pointer")
            .on("click", (event) => {
              event.stopPropagation();
              vm.handleLinkClick(d);
            });
        }
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
    },
    
    // 处理窗口大小变化
    handleResize() {
      if (this.$refs.canvas && this.graphNodes && this.graphNodes.length > 0) {
        this.initGraph();
      }
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
    },
    
    createNewAgent() {
      this.showCreateModal = true;
      this.newAgent = { name: '', description: '' };
    },
    
    closeCreateModal() {
      this.showCreateModal = false;
    },
    
    async createAgent() {
      try {
        const response = await agentOrchestrationApi.create(this.newAgent);
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
      const newNode = {
        id: this.nextNodeId++,
        name: `节点${this.nextNodeId - 1}`,
        type: 'process',
        x: 400 + Math.random() * 200 - 100,
        y: 300 + Math.random() * 200 - 100,
        yamlConfig: '',
        config: {},
        isFixed: false
      };
      
      this.graphNodes.push(newNode);
      console.log("addNode, this.graphNodes", this.graphNodes);
      
      // 重新初始化图表以显示新节点
      this.initGraph();
    },
    
    closeNodeModal() {
      this.showNodeModal = false;
      this.selectedNode = null;
      this.editingNode = {};
    },
    
    async saveNode() {
      if (!this.selectedNode) return;
      
      // 更新节点数据
      const nodeIndex = this.graphNodes.findIndex(n => n.id === this.selectedNode.id);
      if (nodeIndex !== -1) {
        this.graphNodes[nodeIndex] = { ...this.editingNode };
      }
      
      // 如果是现有节点，更新到后端
      if (this.selectedNode.id < 10000) { // 假设ID小于10000的是已存在的节点
        try {
          await agentOrchestrationApi.updateNode(this.selectedNode.id, {
            name: this.editingNode.name,
            type: this.editingNode.type,
            positionX: Math.round(this.editingNode.x || 0),
            positionY: Math.round(this.editingNode.y || 0),
            yamlConfig: this.editingNode.yamlConfig
          });
        } catch (error) {
          console.error('更新节点失败:', error);
        }
      }
      
      this.closeNodeModal();
      this.initGraph(); // 重新初始化图表以显示更新
    },
    
    async deleteNode() {
      if (!this.selectedNode) return;
      
      if (confirm('确定要删除这个节点吗？')) {
        // 从前端数组中移除
        this.graphNodes = this.graphNodes.filter(n => n.id !== this.selectedNode.id);
        
        // 如果是现有节点，从后端删除
        if (this.selectedNode.id < 10000) {
          try {
            await agentOrchestrationApi.deleteNode(this.selectedNode.id);
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
    
    async saveAgent() {
      if (!this.currentAgent) return;
      
      try {
        // 保存节点位置和配置
        const flowData = {
          nodes: this.graphNodes,
          links: this.graphLinks
        };
        
        await agentOrchestrationApi.updateFlow(this.currentAgent.id, JSON.stringify(flowData));
        
        // 保存每个节点的配置
        for (const node of this.graphNodes) {
          if (node.id < 10000) {
            await orchestrationApi.updateNode(node.id, {
              name: node.name,
              type: node.type,
              positionX: Math.round(node.x || 0),
              positionY: Math.round(node.y || 0),
              yamlConfig: node.yamlConfig
            });
          }
        }
        
        alert('代理保存成功！');
      } catch (error) {
        console.error('保存代理失败:', error);
        alert('保存失败，请重试');
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
      const newLink = {
        id: this.nextLinkId++,
        source: sourceNode.id,
        target: targetNode.id,
        type: 'flow',
        name: `${sourceNode.name} → ${targetNode.name}`,
        description: '',
        condition: ''
      };
      
      this.graphLinks.push(newLink);
      this.initGraph(); // 重新渲染图表
    },
    
    // 连线点击事件
    handleLinkClick(link) {
      this.selectedLink = link;
      this.editingLink = { ...link };
      this.showLinkModal = true;
    },
    
    // 关闭连线模态框
    closeLinkModal() {
      this.showLinkModal = false;
      this.selectedLink = null;
      this.editingLink = {};
    },
    
    // 保存连线
    saveLink() {
      if (!this.selectedLink) return;
      
      // 更新连线数据
      const linkIndex = this.graphLinks.findIndex(l => l.id === this.selectedLink.id);
      if (linkIndex !== -1) {
        this.graphLinks[linkIndex] = { ...this.editingLink };
      }
      
      this.closeLinkModal();
      this.initGraph(); // 重新渲染图表
    },
    
    // 删除连线
    deleteLink() {
      if (!this.selectedLink) return;
      
      if (confirm('确定要删除这条连线吗？')) {
        this.graphLinks = this.graphLinks.filter(l => l.id !== this.selectedLink.id);
        this.closeLinkModal();
        this.initGraph(); // 重新渲染图表
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
    }
  }
};
</script>

<style scoped>
.agent-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

.agent-header {
  padding: 20px;
  background: white;
  border-bottom: 1px solid #ddd;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.agent-content {
  flex: 1;
  display: flex;
}

.sidebar {
  width: 250px;
  background: white;
  border-right: 1px solid #ddd;
  padding: 20px;
  overflow-y: auto;
}

.main-content {
  flex: 1;
  padding: 20px;
}

.agent-list {
  margin-top: 15px;
}

.agent-item {
  padding: 12px;
  margin-bottom: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.agent-item:hover {
  background-color: #f0f0f0;
}

.agent-item.active {
  background-color: #e3f2fd;
  border-color: #2196F3;
}

.agent-name {
  font-weight: bold;
  margin-bottom: 4px;
}

.agent-desc {
  font-size: 12px;
  color: #666;
}

.workspace-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.canvas-container {
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
}

.agent-canvas {
  width: 100%;
  height: 600px;
  min-height: 400px;
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
    
    &:hover {
      fill-opacity: 1 !important;
    }
    
    &.highlighted {
      fill: #409eff !important;
      fill-opacity: 0.3 !important;
    }
  }
}
</style>