<template>
  <div class="mobile-register">
    <van-nav-bar
      title="用户注册"
      left-text="返回"
      left-arrow
      @click-left="$router.go(-1)"
    />
    
    <div class="form-container">
      <van-form @submit="handleRegister">
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
        <van-field
          v-model="form.realName"
          name="realName"
          label="真实姓名"
          placeholder="请输入真实姓名"
          :rules="[{ required: true, message: '请输入真实姓名' }]"
        />
        <van-field
          v-model="form.phone"
          name="phone"
          label="手机号"
          placeholder="请输入手机号"
        />
        <van-field
          v-model="form.email"
          name="email"
          label="邮箱"
          placeholder="请输入邮箱"
        />
        
        <div class="button-group">
          <van-button round block type="info" native-type="submit" :loading="loading">
            注册
          </van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script>
import { register } from '@/api/auth'

export default {
  name: 'MobileRegister',
  data() {
    return {
      form: {
        username: '',
        password: '',
        realName: '',
        phone: '',
        email: ''
      },
      loading: false
    }
  },
  methods: {
    async handleRegister() {
      this.loading = true
      try {
        await register(this.form)
        this.$toast.success('注册成功，请登录')
        this.$router.push('/mobile/login')
      } catch (error) {
        this.$toast.fail(error.message || '注册失败')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.mobile-register {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.form-container {
  padding: 20px;
}

.button-group {
  margin-top: 30px;
}
</style>