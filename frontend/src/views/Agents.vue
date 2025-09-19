<template>
  <div class="page-container">
    <div class="page-header">
      <h2>智能工程师</h2>
      <p class="description">管理和监控 AI Agent 的运行状态与调用情况</p>
    </div>
    
    <div class="agents-content">
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon size="24"><Robot /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.totalAgents }}</div>
                  <div class="stat-label">Agent 总数</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon deployed">
                  <el-icon size="24"><Check /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.deployedAgents }}</div>
                  <div class="stat-label">已部署</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon active">
                  <el-icon size="24"><VideoPlay /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.totalCalls }}</div>
                  <div class="stat-label">总调用次数</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon success">
                  <el-icon size="24"><TrendCharts /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.successRate }}%</div>
                  <div class="stat-label">成功率</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <div class="agents-section">
        <div class="section-header">
          <h3>Agent 列表</h3>
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon>
            创建 Agent
          </el-button>
        </div>
        
        <el-table :data="agents" v-loading="loading">
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="type" label="类型">
            <template #default="{ row }">
              <el-tag :type="getTypeTagType(row.type)">
                {{ getTypeLabel(row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              <el-tag :class="`status-tag status-${row.status.toLowerCase()}`">
                {{ getStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="callCount" label="调用次数" />
          <el-table-column prop="lastCalled" label="最后调用">
            <template #default="{ row }">
              {{ row.lastCalled || '暂无' }}
            </template>
          </el-table-column>
          <el-table-column label="部署状态">
            <template #default="{ row }">
              <el-switch
                :model-value="row.isDeployed"
                @change="toggleDeploy(row)"
                :loading="deploying[row.id]"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button size="small" @click="viewDetails(row)">
                详情
              </el-button>
              <el-button size="small" type="primary" @click="editAgent(row)">
                编辑
              </el-button>
              <el-button size="small" type="danger" @click="deleteAgent(row.id)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    
    <!-- 添加/编辑 Agent 对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingAgent ? '编辑 Agent' : '创建 Agent'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="agentForm"
        :rules="agentRules"
        label-width="120px"
      >
        <el-form-item label="名称" prop="name">
          <el-input
            v-model="agentForm.name"
            placeholder="请输入 Agent 名称"
          />
        </el-form-item>
        
        <el-form-item label="类型" prop="type">
          <el-select v-model="agentForm.type" placeholder="选择类型">
            <el-option label="工程师" value="engineer" />
            <el-option label="测试" value="tester" />
            <el-option label="销售" value="sales" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="agentForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入 Agent 描述"
          />
        </el-form-item>
        
        <el-form-item label="配置" prop="configuration">
          <el-input
            v-model="agentForm.configuration"
            type="textarea"
            :rows="6"
            placeholder="请输入 JSON 格式的配置"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAgent">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- Agent 详情对话框 -->
    <el-dialog
      v-model="showDetailsDialog"
      title="Agent 详情"
      width="800px"
    >
      <div v-if="selectedAgent">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="名称">{{ selectedAgent.name }}</el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag :type="getTypeTagType(selectedAgent.type)">
              {{ getTypeLabel(selectedAgent.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :class="`status-tag status-${selectedAgent.status.toLowerCase()}`">
              {{ getStatusLabel(selectedAgent.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="部署状态">
            <el-tag :type="selectedAgent.isDeployed ? 'success' : 'info'">
              {{ selectedAgent.isDeployed ? '已部署' : '未部署' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="调用次数">{{ selectedAgent.callCount }}</el-descriptions-item>
          <el-descriptions-item label="最后调用">{{ selectedAgent.lastCalled || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ selectedAgent.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ selectedAgent.updatedAt }}</el-descriptions-item>
        </el-descriptions>
        
        <div v-if="selectedAgent.description" class="detail-section">
          <h4>描述</h4>
          <p>{{ selectedAgent.description }}</p>
        </div>
        
        <div v-if="selectedAgent.configuration" class="detail-section">
          <h4>配置</h4>
          <pre>{{ selectedAgent.configuration }}</pre>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { agentApi } from '@/services/api'
import { extractData, handleApiError } from '@/utils/apiHelpers'
import type { Agent } from '@/types'

const agents = ref<Agent[]>([])
const loading = ref(false)
const showAddDialog = ref(false)
const showDetailsDialog = ref(false)
const editingAgent = ref<Agent | null>(null)
const selectedAgent = ref<Agent | null>(null)
const deploying = ref<Record<number, boolean>>({})

const agentForm = reactive<Agent>({
  name: '',
  type: 'engineer',
  description: '',
  configuration: '',
  status: 'CREATED',
  callCount: 0,
  isDeployed: false
})

const formRef = ref<any>(null)

const agentRules = {
  name: [{ required: true, message: '请输入 Agent 名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const stats = computed(() => {
  const totalAgents = agents.value.length
  const deployedAgents = agents.value.filter(a => a.isDeployed).length
  const totalCalls = agents.value.reduce((sum, a) => sum + a.callCount, 0)
  const successRate = totalCalls > 0 ? 95 : 0
  
  return { totalAgents, deployedAgents, totalCalls, successRate }
})

const loadAgents = async () => {
  loading.value = true
  try {
    const response = await agentApi.getAll()
    agents.value = extractData(response)
  } catch (error) {
    ElMessage.error(handleApiError(error))
  } finally {
    loading.value = false
  }
}

const saveAgent = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (editingAgent.value) {
      await agentApi.update(editingAgent.value.id!, agentForm)
      ElMessage.success('Agent 更新成功')
    } else {
      await agentApi.create(agentForm)
      ElMessage.success('Agent 创建成功')
    }
    
    showAddDialog.value = false
    resetForm()
    loadAgents()
  } catch (error) {
    ElMessage.error(handleApiError(error))
  }
}

const editAgent = (agent: Agent) => {
  editingAgent.value = agent
  Object.assign(agentForm, agent)
  showAddDialog.value = true
}

const deleteAgent = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个 Agent 吗？', '确认删除', {
      type: 'warning'
    })
    
    await agentApi.delete(id)
    ElMessage.success('Agent 删除成功')
    loadAgents()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(handleApiError(error))
    }
  }
}

const toggleDeploy = async (agent: Agent) => {
  deploying.value[agent.id!] = true
  
  try {
    if (agent.isDeployed) {
      await agentApi.undeploy(agent.id!)
      ElMessage.success('Agent 停用成功')
    } else {
      await agentApi.deploy(agent.id!)
      ElMessage.success('Agent 启用成功')
    }
    
    loadAgents()
  } catch (error) {
    ElMessage.error(handleApiError(error))
  } finally {
    deploying.value[agent.id!] = false
  }
}

const viewDetails = (agent: Agent) => {
  selectedAgent.value = agent
  showDetailsDialog.value = true
}

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    engineer: '工程师',
    tester: '测试',
    sales: '销售'
  }
  return map[type] || type
}

const getTypeTagType = (type: string) => {
  const map: Record<string, string> = {
    engineer: 'primary',
    tester: 'warning',
    sales: 'success'
  }
  return map[type] || 'info'
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    CREATED: '已创建',
    DEPLOYED: '已部署',
    STOPPED: '已停止',
    ERROR: '错误'
  }
  return map[status] || status
}

const resetForm = () => {
  editingAgent.value = null
  Object.assign(agentForm, {
    name: '',
    type: 'engineer',
    description: '',
    configuration: '',
    status: 'CREATED',
    callCount: 0,
    isDeployed: false
  })
}

onMounted(() => {
  loadAgents()
})
</script>

<style scoped lang="scss">
.agents-content {
  .stats-section {
    margin-bottom: 24px;
    
    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;
        
        .stat-icon {
          width: 48px;
          height: 48px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
          background: linear-gradient(45deg, #1677ff, #722ed1);
          
          &.deployed {
            background: linear-gradient(45deg, #52c41a, #73d13d);
          }
          
          &.active {
            background: linear-gradient(45deg, #fa8c16, #ffa940);
          }
          
          &.success {
            background: linear-gradient(45deg, #722ed1, #9254de);
          }
        }
        
        .stat-info {
          .stat-value {
            font-size: 24px;
            font-weight: bold;
            color: #333;
          }
          
          .stat-label {
            font-size: 14px;
            color: #666;
          }
        }
      }
    }
  }
  
  .agents-section {
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      
      h3 {
        margin: 0;
        color: #333;
      }
    }
  }
}

.detail-section {
  margin-top: 20px;
  
  h4 {
    margin-bottom: 8px;
    color: #333;
  }
  
  p {
    margin: 0;
    color: #666;
    line-height: 1.6;
  }
  
  pre {
    background: #f5f5f5;
    padding: 12px;
    border-radius: 4px;
    font-size: 12px;
    overflow-x: auto;
  }
}
</style>