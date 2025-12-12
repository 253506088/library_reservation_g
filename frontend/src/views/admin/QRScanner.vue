<template>
  <div class="qr-scanner">
    <div class="page-header">
      <h2>扫码验证</h2>
    </div>
    
    <div class="scanner-container">
      <div class="scanner-area">
        <div class="scanner-box">
          <div class="scanner-line"></div>
          <div class="corner top-left"></div>
          <div class="corner top-right"></div>
          <div class="corner bottom-left"></div>
          <div class="corner bottom-right"></div>
        </div>
        <p class="scanner-tip">请将二维码放入扫描框内</p>
      </div>
      
      <div class="manual-input">
        <h3>手动输入流水号</h3>
        <el-form :inline="true" @submit.native.prevent="handleManualCheck">
          <el-form-item>
            <el-input
              v-model="orderNo"
              placeholder="请输入预约流水号"
              style="width: 300px"
              @keyup.enter.native="handleManualCheck"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleManualCheck" :loading="loading">
              验证
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
    
    <!-- 验证结果 -->
    <div v-if="verificationResult" class="result-container">
      <el-card>
        <div slot="header">
          <span>验证结果</span>
        </div>
        <div class="result-content">
          <div class="result-status" :class="resultStatusClass">
            <i :class="resultIcon"></i>
            <span>{{ resultMessage }}</span>
          </div>
          
          <div v-if="reservationInfo" class="reservation-details">
            <div class="detail-item">
              <span class="label">流水号：</span>
              <span class="value">{{ reservationInfo.orderNo }}</span>
            </div>
            <div class="detail-item">
              <span class="label">用户：</span>
              <span class="value">{{ reservationInfo.realName }}</span>
            </div>
            <div class="detail-item">
              <span class="label">图书馆：</span>
              <span class="value">{{ reservationInfo.libraryName }}</span>
            </div>
            <div class="detail-item">
              <span class="label">座位：</span>
              <span class="value">{{ reservationInfo.seatNumber }}</span>
            </div>
            <div class="detail-item">
              <span class="label">预约时间：</span>
              <span class="value">{{ formatTimeRange(reservationInfo.startTime, reservationInfo.endTime) }}</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { checkIn, getReservationByOrderNo } from '@/api/reservation'
import moment from 'moment'

export default {
  name: 'QRScanner',
  data() {
    return {
      orderNo: '',
      loading: false,
      verificationResult: null,
      reservationInfo: null,
      resultMessage: '',
      resultStatusClass: '',
      resultIcon: ''
    }
  },
  
  methods: {
    async handleManualCheck() {
      if (!this.orderNo.trim()) {
        this.$message.warning('请输入流水号')
        return
      }
      
      this.loading = true
      try {
        // 先获取预约信息
        const reservationRes = await getReservationByOrderNo(this.orderNo)
        this.reservationInfo = reservationRes.data
        
        // 执行签到
        await checkIn(this.orderNo)
        
        this.showSuccessResult('签到成功')
        
        // 清空输入框
        this.orderNo = ''
      } catch (error) {
        this.showErrorResult(error.message || '验证失败')
      } finally {
        this.loading = false
      }
    },
    
    showSuccessResult(message) {
      this.verificationResult = true
      this.resultMessage = message
      this.resultStatusClass = 'success'
      this.resultIcon = 'el-icon-success'
    },
    
    showErrorResult(message) {
      this.verificationResult = true
      this.resultMessage = message
      this.resultStatusClass = 'error'
      this.resultIcon = 'el-icon-error'
      this.reservationInfo = null
    },
    
    formatTimeRange(startTime, endTime) {
      if (!startTime || !endTime) return ''
      const start = moment(startTime).format('MM-DD HH:mm')
      const end = moment(endTime).format('HH:mm')
      return `${start} - ${end}`
    }
  }
}
</script>

<style scoped>
.qr-scanner {
  background: white;
  padding: 20px;
  border-radius: 4px;
}

.page-header h2 {
  margin: 0 0 30px 0;
  color: #333;
  text-align: center;
}

.scanner-container {
  max-width: 600px;
  margin: 0 auto;
}

.scanner-area {
  text-align: center;
  margin-bottom: 40px;
}

.scanner-box {
  position: relative;
  width: 300px;
  height: 300px;
  margin: 0 auto 20px;
  background: #f5f5f5;
  border: 2px dashed #ddd;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.scanner-line {
  width: 200px;
  height: 2px;
  background: #409EFF;
  animation: scan 2s linear infinite;
}

@keyframes scan {
  0% {
    transform: translateY(-100px);
    opacity: 0;
  }
  50% {
    opacity: 1;
  }
  100% {
    transform: translateY(100px);
    opacity: 0;
  }
}

.corner {
  position: absolute;
  width: 20px;
  height: 20px;
  border: 3px solid #409EFF;
}

.corner.top-left {
  top: 10px;
  left: 10px;
  border-right: none;
  border-bottom: none;
}

.corner.top-right {
  top: 10px;
  right: 10px;
  border-left: none;
  border-bottom: none;
}

.corner.bottom-left {
  bottom: 10px;
  left: 10px;
  border-right: none;
  border-top: none;
}

.corner.bottom-right {
  bottom: 10px;
  right: 10px;
  border-left: none;
  border-top: none;
}

.scanner-tip {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.manual-input {
  text-align: center;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
}

.manual-input h3 {
  margin: 0 0 20px 0;
  color: #333;
}

.result-container {
  margin-top: 30px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.result-content {
  text-align: center;
}

.result-status {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 20px;
  padding: 15px;
  border-radius: 8px;
}

.result-status.success {
  background: #f0f9ff;
  color: #52c41a;
}

.result-status.error {
  background: #fff2f0;
  color: #ff4d4f;
}

.result-status i {
  margin-right: 8px;
  font-size: 20px;
}

.reservation-details {
  text-align: left;
  background: #fafafa;
  padding: 20px;
  border-radius: 8px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  padding: 5px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.label {
  color: #666;
  font-weight: 500;
}

.value {
  color: #333;
}
</style>