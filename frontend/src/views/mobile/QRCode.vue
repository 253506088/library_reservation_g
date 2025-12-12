<template>
  <div class="qrcode-page">
    <van-nav-bar
      title="预约二维码"
      left-text="返回"
      left-arrow
      @click-left="$router.go(-1)"
    />
    
    <div class="content" v-if="reservation">
      <div class="reservation-info">
        <h2>预约成功</h2>
        <div class="info-item">
          <span class="label">流水号：</span>
          <span class="value">{{ reservation.orderNo }}</span>
        </div>
        <div class="info-item">
          <span class="label">图书馆：</span>
          <span class="value">{{ libraryName }}</span>
        </div>
        <div class="info-item">
          <span class="label">座位号：</span>
          <span class="value">{{ seatNumber }}</span>
        </div>
        <div class="info-item">
          <span class="label">预约时间：</span>
          <span class="value">{{ formatTime(reservation.startTime) }} - {{ formatTime(reservation.endTime) }}</span>
        </div>
        <div class="info-item">
          <span class="label">状态：</span>
          <span class="value" :class="getStatusClass(reservation.status)">{{ reservation.status }}</span>
        </div>
      </div>
      
      <div class="qrcode-container">
        <canvas ref="qrcode" class="qrcode"></canvas>
        <p class="qrcode-tip">请向管理员出示此二维码进行签到</p>
      </div>
      
      <div class="button-container">
        <van-button type="info" block @click="goToMyReservations">
          查看我的预约
        </van-button>
      </div>
    </div>
  </div>
</template>

<script>
import { getReservationDetailByOrderNo } from '@/api/reservation'
import QRCode from 'qrcode'
import { formatTime as formatTimeUtil } from '@/utils/time'

export default {
  name: 'MobileQRCode',
  data() {
    return {
      reservation: null,
      libraryName: '',
      seatNumber: ''
    }
  },
  
  async created() {
    const orderNo = this.$route.params.orderNo
    await this.loadReservation(orderNo)
    await this.generateQRCode(orderNo)
  },
  
  methods: {
    async loadReservation(orderNo) {
      try {
        const res = await getReservationDetailByOrderNo(orderNo)
        this.reservation = res.data
        
        // 现在返回的数据包含完整的图书馆和座位信息
        this.libraryName = this.reservation.libraryName || '未知图书馆'
        this.seatNumber = this.reservation.seatNumber || '未知座位'
      } catch (error) {
        this.$toast.fail('加载预约信息失败')
        this.$router.go(-1)
      }
    },
    
    async generateQRCode(orderNo) {
      try {
        await QRCode.toCanvas(this.$refs.qrcode, orderNo, {
          width: 200,
          margin: 2,
          color: {
            dark: '#000000',
            light: '#FFFFFF'
          }
        })
      } catch (error) {
        console.error('生成二维码失败:', error)
      }
    },
    
    formatTime(timeStr) {
      return formatTimeUtil(timeStr)
    },
    
    getStatusClass(status) {
      const statusMap = {
        '已预约': 'status-reserved',
        '已使用': 'status-used',
        '爽约': 'status-missed',
        '已取消': 'status-cancelled'
      }
      return statusMap[status] || ''
    },
    
    goToMyReservations() {
      this.$router.push('/mobile/my-reservations')
    }
  }
}
</script>

<style scoped>
.qrcode-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.content {
  padding: 20px;
}

.reservation-info {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.reservation-info h2 {
  text-align: center;
  color: #1989fa;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.label {
  color: #666;
  font-size: 14px;
}

.value {
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.status-reserved {
  color: #1989fa;
}

.status-used {
  color: #07c160;
}

.status-missed {
  color: #ee0a24;
}

.status-cancelled {
  color: #969799;
}

.qrcode-container {
  background: white;
  border-radius: 8px;
  padding: 30px;
  text-align: center;
  margin-bottom: 20px;
}

.qrcode {
  display: block;
  margin: 0 auto 15px;
}

.qrcode-tip {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.button-container {
  margin-top: 20px;
}
</style>