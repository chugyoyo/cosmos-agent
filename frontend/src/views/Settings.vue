<template>
  <div class="page-container">
    <div class="page-header">
      <h2>AI 模型配置</h2>
      <p class="description">配置和管理 AI 大模型的 API Key 和相关参数</p>
    </div>
    
    <div class="settings-content">
      <div class="configurations-section">
        <div class="section-header">
          <h3>已配置的模型</h3>
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon>
            添加配置
          </el-button>
        </div>
        
        <el-table :data="configurations" v-loading="loading">
          <el-table-column prop="provider" label="提供商" />
          <el-table-column prop="model" label="模型" />
          <el-table-column prop="baseUrl" label="基础 URL" />
          <el-table-column label="状态">
            <template #default="{ row }">
              <el-tag :type="row.isActive ? 'success' : 'info'">
                {{ row.isActive ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="300">
            <template #default="{ row }">
              <el-button 
                size="small" 
                @click="testConnection(row.provider)"
                :loading="testingConnection === row.provider"
              >
                <el-icon><Connection /></el-icon>
                测试连接
              </el-button>
              <el-button size="small" type="primary" @click="editConfiguration(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button size="small" type="danger" @click="deleteConfiguration(row.id)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    
    <!-- 添加/编辑配置对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingConfig ? '编辑配置' : '添加配置'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="configForm"
        :rules="configRules"
        label-width="120px"
      >
        <el-form-item label="提供商" prop="provider">
          <el-select v-model="configForm.provider" placeholder="选择提供商">
<!--            <el-option label="OpenAI" value="openai" />-->
<!--            <el-option label="Azure" value="azure" />-->
<!--            <el-option label="阿里云通义千问" value="qwen" />-->
            <el-option label="智谱 AI" value="zhipuai" />
<!--            <el-option label="百度千帆" value="qianfan" />-->
          </el-select>
        </el-form-item>
        
        <el-form-item label="API Key" prop="apiKey">
          <el-input
            v-model="configForm.apiKey"
            type="password"
            placeholder="请输入 API Key"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="基础 URL" prop="baseUrl">
          <el-input
            v-model="configForm.baseUrl"
            placeholder="请输入基础 URL（可选）"
          />
        </el-form-item>
        
        <el-form-item label="模型" prop="model">
          <el-input
            v-model="configForm.model"
            placeholder="请输入模型名称（可选）"
          />
        </el-form-item>
        
        <el-form-item label="启用状态" prop="isActive">
          <el-switch v-model="configForm.isActive" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveConfiguration">保存</el-button>
      </template>
    </el-dialog>

    <!-- 测试结果对话框 -->
    <TestResultDialog
      v-model:visible="showTestDialog"
      :provider="testProvider"
      :test-result="testResult"
      @retry="() => testConnection(testProvider)"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { configurationApi } from '@/services/api'
import { extractData, handleApiError } from '@/utils/apiHelpers'
import type { AIConfiguration } from '@/types'

const configurations = ref<AIConfiguration[]>([])
const loading = ref(false)
const testingConnection = ref<string | null>(null)
const showAddDialog = ref(false)
const editingConfig = ref<AIConfiguration | null>(null)
const showTestDialog = ref(false)
const testResult = ref<any>(null)
const testProvider = ref('')

const configForm = reactive<AIConfiguration>({
  provider: '',
  apiKey: '',
  baseUrl: '',
  model: '',
  isActive: true
})

const formRef = ref<any>(null)

const configRules = {
  provider: [{ required: true, message: '请选择提供商', trigger: 'change' }],
  apiKey: [{ required: true, message: '请输入 API Key', trigger: 'blur' }]
}

const loadConfigurations = async () => {
  loading.value = true
  try {
    const response = await configurationApi.getAll()
    configurations.value = extractData(response)
  } catch (error) {
    ElMessage.error(handleApiError(error))
  } finally {
    loading.value = false
  }
}

const saveConfiguration = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (editingConfig.value) {
      await configurationApi.saveUpdateConfiguration({...configForm, id: editingConfig.value.id!})
      ElMessage.success('配置更新成功')
    } else {
      await configurationApi.create(configForm)
      ElMessage.success('配置创建成功')
    }
    
    showAddDialog.value = false
    resetForm()
    loadConfigurations()
  } catch (error) {
    ElMessage.error(handleApiError(error))
  }
}

const editConfiguration = (config: AIConfiguration) => {
  editingConfig.value = config
  Object.assign(configForm, config)
  showAddDialog.value = true
}

const deleteConfiguration = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个配置吗？', '确认删除', {
      type: 'warning'
    })
    
    await configurationApi.deleteConfiguration(id)
    ElMessage.success('配置删除成功')
    loadConfigurations()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(handleApiError(error))
    }
  }
}

const testConnection = async (provider: string) => {
  testingConnection.value = provider
  testProvider.value = provider
  
  try {
    const response = await configurationApi.testConnection(provider)
    const result = extractData(response)
    
    testResult.value = result
    showTestDialog.value = true
    
    if (result.success) {
      ElMessage({
        message: `连接测试成功！${result.message}`,
        type: 'success',
        duration: 3000
      })
    } else {
      ElMessage({
        message: `连接测试失败: ${result.message}`,
        type: 'error',
        duration: 3000
      })
    }
  } catch (error) {
    ElMessage.error(handleApiError(error))
  } finally {
    testingConnection.value = null
  }
}

const resetForm = () => {
  editingConfig.value = null
  Object.assign(configForm, {
    provider: '',
    apiKey: '',
    baseUrl: '',
    model: '',
    isActive: true
  })
}

onMounted(() => {
  loadConfigurations()
})
</script>

<style scoped lang="scss">
.settings-content {
  .configurations-section {
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
</style>