<template>
  <div class="mobile-login">
    <div class="header">
      <h1>图书馆座位预约</h1>
    </div>
    
    <div class="form-container">
      <van-form @submit="handleLogin">
        <van-field
          v-model="form.username"
          name="username"
          label="用户名"
          placeholder="请输入用户名"
          :rules="[{ required: true, message: '请输入用户名' }]"
        />
        <van-field
          v-model="form.password"
          type="password"
          name="password"
          label="密码"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请输入密码' }]"
        />
        <div class="button-group">
          <van-button round block type="info" native-type="submit" :loading="loading">
            登录
          </van-button>
          <van-button round block plain type="info" @click="goRegister">
            注册
          </van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script>
import { login } from '@/api/auth'

export default {
  name: 'MobileLogin',
  data() {
    return {
      form: {
        username: '',
        password: ''
      },
      loading: false
    }
  },
  methods: {
    async handleLogin() {
      this.loading = true
      try {
        const res = await login(this.form)
        this.$store.dispatch('setUser', res.data)
        this.$toast.success('登录成功')
        this.$router.push('/mobile/home')
      } catch (error) {
        this.$toast.fail(error.message || '登录失败')
      } finally {
        this.loading = false
      }
    },
    
    goRegister() {
      this.$router.push('/mobile/register')
    }
  }
}
</script>

<style scoped>
.mobile-login {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.header {
  text-align: center;
  padding: 60px 0 40px;
}

.header h1 {
  color: white;
  font-size: 28px;
  font-weight: 300;
}

.form-container {
  background: white;
  border-radius: 10px;
  padding: 30px 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.button-group {
  margin-top: 30px;
}

.button-group .van-button {
  margin-bottom: 15px;
}
</style>