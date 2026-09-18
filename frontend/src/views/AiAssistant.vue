<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">AI 智能助手</span>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <div class="stat-card chat-box">
          <div class="chat-messages" ref="messagesRef">
            <div v-for="(msg, i) in messages" :key="i" class="chat-msg" :class="msg.role">
              <div class="msg-avatar">
                <el-icon v-if="msg.role === 'user'"><User /></el-icon>
                <el-icon v-else><MagicStick /></el-icon>
              </div>
              <div class="msg-content">
                <pre>{{ msg.content }}</pre>
              </div>
            </div>
          </div>
          <div class="chat-input">
            <el-input v-model="inputText" type="textarea" :rows="3" placeholder="输入您的问题..." @keydown.ctrl.enter="sendMessage" />
            <el-button type="primary" :loading="loading" style="margin-top:8px" @click="sendMessage">发送 (Ctrl+Enter)</el-button>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <h3 style="margin-bottom:16px">快捷功能</h3>
          <el-card shadow="hover" style="margin-bottom:12px;cursor:pointer" @click="fillPrompt('case')">
            <div style="font-weight:600">📝 生成测试用例</div>
            <div style="font-size:12px;color:#909399;margin-top:4px">根据需求描述自动生成测试用例</div>
          </el-card>
          <el-card shadow="hover" style="margin-bottom:12px;cursor:pointer" @click="fillPrompt('bug')">
            <div style="font-weight:600">🐛 缺陷分析</div>
            <div style="font-size:12px;color:#909399;margin-top:4px">分析缺陷根因并给出修复建议</div>
          </el-card>
          <el-card shadow="hover" style="margin-bottom:12px;cursor:pointer" @click="fillPrompt('api')">
            <div style="font-weight:600">🔌 生成接口用例</div>
            <div style="font-size:12px;color:#909399;margin-top:4px">根据API文档生成接口测试用例</div>
          </el-card>
          <el-card shadow="hover" style="cursor:pointer" @click="fillPrompt('report')">
            <div style="font-weight:600">📊 报告总结</div>
            <div style="font-size:12px;color:#909399;margin-top:4px">总结测试报告并给出质量评估</div>
          </el-card>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { User, MagicStick } from '@element-plus/icons-vue'
import { aiChat } from '@/api'

const messages = ref([
  { role: 'assistant', content: '你好！我是TestHub AI助手，可以帮你：\n1. 生成测试用例\n2. 分析缺陷根因\n3. 生成接口测试用例\n4. 总结测试报告\n\n请输入你的问题...' }
])
const inputText = ref('')
const loading = ref(false)
const messagesRef = ref()

const fillPrompt = (type) => {
  const prompts = {
    case: '请帮我生成一个"用户登录"功能的测试用例，覆盖正常、异常、边界场景。',
    bug: '请分析这个缺陷：登录页面密码框未做长度校验，可以输入超过100个字符。',
    api: '请为这个API生成接口测试用例：POST /api/auth/login，参数：username, password',
    report: '请总结：本次测试共执行100个用例，通过92个，失败8个，主要失败在订单模块。'
  }
  inputText.value = prompts[type]
}

const sendMessage = async () => {
  if (!inputText.value.trim() || loading.value) return
  const text = inputText.value
  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  loading.value = true
  scrollToBottom()
  try {
    const res = await aiChat({ message: text })
    messages.value.push({ role: 'assistant', content: res.data })
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '抱歉，处理请求时出错了。' })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}
</script>

<style scoped>
.chat-box { display: flex; flex-direction: column; height: calc(100vh - 160px); }
.chat-messages { flex: 1; overflow-y: auto; padding: 16px; background: #f5f7fa; border-radius: 8px; }
.chat-msg { display: flex; gap: 12px; margin-bottom: 16px; }
.chat-msg.user { flex-direction: row-reverse; }
.msg-avatar { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.chat-msg.user .msg-avatar { background: #409eff; color: #fff; }
.chat-msg.assistant .msg-avatar { background: #67c23a; color: #fff; }
.msg-content { max-width: 70%; background: #fff; padding: 12px 16px; border-radius: 8px; }
.msg-content pre { white-space: pre-wrap; word-break: break-word; font-family: inherit; margin: 0; }
.chat-input { padding: 12px 0; }
</style>
