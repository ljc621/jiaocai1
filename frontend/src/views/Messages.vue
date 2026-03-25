<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Promotion, UserFilled } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const currentUser = ref<any>(null)
const conversations = ref<any[]>([])
const activeChat = ref<any>(null)
const messages = ref<any[]>([])
const newMessage = ref('')
const messageContainer = ref<HTMLElement | null>(null)

// Initialize
onMounted(async () => {
    const userStr = localStorage.getItem('user')
    if (!userStr) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
    }
    currentUser.value = JSON.parse(userStr)
    
    await fetchConversations()
    
    // Check if we need to open a specific chat
    const targetUserId = route.query.userId as string
    if (targetUserId) {
        // Find existing conversation
        const existing = conversations.value.find(c => c.userId === targetUserId)
        if (existing) {
            selectChat(existing)
        } else {
            // Create temporary conversation object if not exists
            await initNewChat(targetUserId)
        }
    }
})

const fetchConversations = async () => {
    try {
        const res = await axios.get('/api/messages/conversations', {
            params: { userId: currentUser.value.id }
        })
        if (res.data.code === 200) {
            conversations.value = res.data.data
        }
    } catch (e) {
        console.error(e)
    }
}

const initNewChat = async (targetUserId: string) => {
    // Fetch user info
    try {
        const res = await axios.get(`/api/users/${targetUserId}`)
        if (res.data.code === 200) {
            const targetUser = res.data.data
            const newConv = {
                userId: targetUser.id,
                userName: targetUser.name,
                userAvatar: targetUser.avatar,
                lastMessage: '',
                lastTime: new Date().toISOString()
            }
            conversations.value.unshift(newConv)
            selectChat(newConv)
        }
    } catch (e) {
        ElMessage.error('无法获取用户信息的')
    }
}

const selectChat = async (chat: any) => {
    activeChat.value = chat
    
    // Mark messages as read
    if (chat.unreadCount > 0) {
        try {
            await axios.post('/api/messages/read', {
                userId: currentUser.value.id,
                otherUserId: chat.userId
            })
            // Update local state
            chat.unreadCount = 0
            // Notify Layout to update global unread count
            window.dispatchEvent(new Event('message-update'))
        } catch (e) {
            console.error('Failed to mark as read')
        }
    }
    
    await fetchMessages()
    scrollToBottom()
}

const fetchMessages = async () => {
    if (!activeChat.value) return
    try {
        const res = await axios.get('/api/messages', {
            params: {
                user1: currentUser.value.id,
                user2: activeChat.value.userId
            }
        })
        if (res.data.code === 200) {
            messages.value = res.data.data
        }
    } catch (e) {
        console.error(e)
    }
}

const sendMessage = async () => {
    if (!newMessage.value.trim() || !activeChat.value) return
    
    const content = newMessage.value
    newMessage.value = '' // Optimistic clear
    
    try {
        const res = await axios.post('/api/messages', {
            senderId: currentUser.value.id,
            receiverId: activeChat.value.userId,
            content: content
        })
        
        if (res.data.code === 200) {
            messages.value.push(res.data.data)
            // Update conversation list
            activeChat.value.lastMessage = content
            activeChat.value.lastTime = new Date().toISOString()
            scrollToBottom()
        }
    } catch (e) {
        ElMessage.error('发送失败')
        newMessage.value = content // Restore if failed
    }
}

const scrollToBottom = () => {
    nextTick(() => {
        if (messageContainer.value) {
            messageContainer.value.scrollTop = messageContainer.value.scrollHeight
        }
    })
}
</script>

<template>
  <div class="messages-container">
    <div class="messages-sidebar">
      <div class="sidebar-header">
        <h3>消息列表</h3>
      </div>
      <div class="message-list">
        <el-empty v-if="conversations.length === 0" description="暂无消息" :image-size="100" />
        <div 
            v-for="chat in conversations" 
            :key="chat.userId" 
            class="conversation-item"
            :class="{ active: activeChat?.userId === chat.userId }"
            @click="selectChat(chat)"
        >
            <el-badge :value="chat.unreadCount" :hidden="!chat.unreadCount || chat.unreadCount === 0" class="avatar-badge">
                <el-avatar :src="chat.userAvatar" :icon="UserFilled" />
            </el-badge>
            <div class="chat-info">
                <div class="chat-name">{{ chat.userName }}</div>
                <div class="chat-last-msg">{{ chat.lastMessage || '开始聊天吧' }}</div>
            </div>
            <div class="chat-time">{{ new Date(chat.lastTime).toLocaleDateString() }}</div>
        </div>
      </div>
    </div>
    
    <div class="chat-area">
      <div v-if="!activeChat" class="empty-chat">
        <div class="empty-content">
          <div class="icon-circle">
            <el-icon><Promotion /></el-icon>
          </div>
          <p>选择一个对话开始聊天</p>
        </div>
      </div>
      <div v-else class="chat-content">
        <div class="chat-header">
            <span>{{ activeChat.userName }}</span>
        </div>
        <div class="message-display" ref="messageContainer">
            <div 
                v-for="msg in messages" 
                :key="msg.id" 
                class="message-bubble"
                :class="{ 'mine': msg.senderId === currentUser.id }"
            >
                <div class="bubble-content">{{ msg.content }}</div>
            </div>
        </div>
        <div class="input-area">
            <el-input 
                v-model="newMessage" 
                type="textarea" 
                :rows="3" 
                placeholder="输入消息..." 
                @keydown.enter.prevent="sendMessage"
            />
            <div class="send-btn-wrapper">
                <el-button type="primary" @click="sendMessage">发送</el-button>
            </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.messages-container {
  display: flex;
  height: calc(100vh - 64px - 40px);
  max-width: 1200px;
  margin: 20px auto;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
  overflow: hidden;
}

.messages-sidebar {
  width: 300px;
  border-right: 1px solid #f0f2f5;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #f0f2f5;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

.chat-area {
  flex: 1;
  background-color: #f9fafc;
  display: flex;
  flex-direction: column;
}

.empty-chat {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.empty-content {
  text-align: center;
}

.icon-circle {
  width: 64px;
  height: 64px;
  background-color: #e6e8eb;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.icon-circle .el-icon {
  font-size: 32px;
  color: #909399;
}

.avatar-badge :deep(.el-badge__content) {
    top: 5px;
    right: 5px;
}

.conversation-item {
    display: flex;
    padding: 15px;
    border-bottom: 1px solid #f0f2f5;
    cursor: pointer;
    transition: background 0.2s;
}
.conversation-item:hover, .conversation-item.active {
    background: #e6f7ff;
}
.chat-info {
    flex: 1;
    margin-left: 10px;
    overflow: hidden;
}
.chat-name {
    font-weight: bold;
    color: #333;
}
.chat-last-msg {
    color: #999;
    font-size: 12px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
.chat-time {
    font-size: 12px;
    color: #ccc;
}
.chat-content {
    display: flex;
    flex-direction: column;
    height: 100%;
}
.chat-header {
    padding: 15px 20px;
    border-bottom: 1px solid #eee;
    font-weight: bold;
    background: white;
}
.message-display {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
    background: #f5f7fa;
}
.message-bubble {
    display: flex;
    margin-bottom: 15px;
}
.message-bubble.mine {
    justify-content: flex-end;
}
.bubble-content {
    max-width: 70%;
    padding: 10px 15px;
    border-radius: 8px;
    background: white;
    box-shadow: 0 1px 2px rgba(0,0,0,0.1);
}
.message-bubble.mine .bubble-content {
    background: #409eff;
    color: white;
}
.input-area {
    padding: 20px;
    background: white;
    border-top: 1px solid #eee;
}
.send-btn-wrapper {
    margin-top: 10px;
    text-align: right;
}
</style>
