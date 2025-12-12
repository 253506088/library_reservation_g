<template>
  <div class="admin-dashboard">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="200px">
        <div class="logo">
          <h3>管理系统</h3>
        </div>
        <el-menu
          :default-active="$route.path"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/admin/dashboard/libraries">
            <i class="el-icon-office-building"></i>
            <span>图书馆管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/dashboard/seats">
            <i class="el-icon-chair"></i>
            <span>座位管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/dashboard/reservations">
            <i class="el-icon-tickets"></i>
            <span>预约管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/dashboard/scanner">
            <i class="el-icon-camera"></i>
            <span>扫码验证</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部导航 -->
        <el-header>
          <div class="header-content">
            <span class="title">图书馆座位预约管理系统</span>
            <div class="user-info">
              <el-dropdown @command="handleCommand">
                <span class="el-dropdown-link">
                  {{ user.realName }}<i class="el-icon-arrow-down el-icon--right"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </div>
        </el-header>
        
        <!-- 主体内容 -->
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { logout } from '@/api/auth'

export default {
  name: 'AdminDashboard',
  computed: {
    user() {
      return this.$store.state.user
    }
  },
  methods: {
    async handleCommand(command) {
      if (command === 'logout') {
        try {
          await logout()
          this.$store.dispatch('clearUser')
          this.$message.success('退出登录成功')
          this.$router.push('/admin/login')
        } catch (error) {
          this.$message.error('退出登录失败')
        }
      }
    }
  }
}
</script>

<style scoped>
.admin-dashboard {
  height: 100vh;
}

.el-aside {
  background-color: #304156;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b2f3a;
}

.logo h3 {
  color: white;
  margin: 0;
  font-weight: 300;
}

.el-header {
  background-color: white;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
}

.header-content {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.user-info {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  cursor: pointer;
  color: #409EFF;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>