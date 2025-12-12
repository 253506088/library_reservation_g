<template>
  <div class="my-reservations">
    <van-nav-bar title="我的预约" fixed>
      <template #left>
        <van-icon name="arrow-left" @click="$router.go(-1)" />
      </template>
    </van-nav-bar>
    
    <div class="content">
      <!-- 状态筛选 -->
      <van-tabs v-model="activeTab" @change="onTabChange">
        <van-tab title="全部" name=""></van-tab>
        <van-tab title="已预约" name="已预约"></van-tab>
        <van-tab title="已使用" name="已使用"></van-tab>
        <van-tab title="已取消" name="已取消"></van-tab>
        <van-tab title="爽约" name="爽约"></van-tab>
      </van-tabs>
      
      <!-- 预约列表 -->
      <van-list
        v-model="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <div
          v-for="item in reservations"
          :key="item.id"
          class="reservation-item"
        >
          <div class="item-header">
            <span class="order-no">{{ item.orderNo }}</span>
            <span class="status" :class="getStatusClass(item.status)">
              {{ item.status }}
            </span>
          </div>
          
          <div class="item-content">
            <div class="info-row">
              <span class="label">图书馆：</span>
              <span class="value">{{ item.libraryName }}</span>
            </div>
            <div class="info-row">
              <span class="label">座位号：</span>
              <span class="value">{{ item.seatNumber }}</span>
            </div>
            <div class="info-row">
              <span class="label">预约时间：</span>
              <span class="value">{{ formatTimeRange(item.startTime, item.endTime) }}</span>
            </div>
            <div class="info-row">
              <span class="label">预约时间：</span>
              <span class="value">{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
          
          <div class="item-actions">
            <van-button
              v-if="item.status === '已预约'"
              size="small"
              type="primary"
              @click="showQRCode(item)"
            >
              查看二维码
            </van-button>
            <van-button
              v-if="item.status === '已预约'"
              size="small"
              type="danger"
              @click="cancelReservation(item)"
            >
              取消预约
            </van-button>
          </div>
        </div>
      </van-list>
    </div>
    
    <!-- 底部导航 -->
    <van-tabbar v-model="bottomTab" @change="onBottomTabChange">
      <van-tabbar-item icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item icon="orders-o">我的预约</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script>
import { getReservationPage, cancelReservation } from '@/api/reservation'
import { formatTime as formatTimeUtil, formatTimeRange as formatTimeRangeUtil } from '@/utils/time'

export default {
  name: 'MyReservations',
  data() {
    return {
      activeTab: '',
      bottomTab: 1,
      loading: false,
      finished: false,
      reservations: [],
      pagination: {
        current: 1,
        size: 10
      }
    }
  },
  
  created() {
    this.loadReservations()
  },
  
  methods: {
    async loadReservations() {
      if (this.loading) return
      
      this.loading = true
      try {
        const params = {
          current: this.pagination.current,
          size: this.pagination.size,
          status: this.activeTab
        }
        
        const res = await getReservationPage(params)
        
        if (this.pagination.current === 1) {
          this.reservations = res.data.records
        } else {
          this.reservations.push(...res.data.records)
        }
        
        // 检查是否还有更多数据
        this.finished = this.reservations.length >= res.data.total
        
        this.pagination.current++
      } catch (error) {
        this.$toast.fail('加载预约记录失败')
      } finally {
        this.loading = false
      }
    },
    
    onLoad() {
      this.loadReservations()
    },
    
    onTabChange() {
      this.resetList()
      this.loadReservations()
    },
    
    resetList() {
      this.reservations = []
      this.pagination.current = 1
      this.finished = false
    },
    
    async cancelReservation(item) {
      try {
        await this.$dialog.confirm({
          title: '确认取消',
          message: '确定要取消这个预约吗？'
        })
        
        await cancelReservation(item.id)
        this.$toast.success('取消成功')
        
        // 更新列表中的状态
        item.status = '已取消'
      } catch (error) {
        if (error !== 'cancel') {
          this.$toast.fail('取消失败')
        }
      }
    },
    
    showQRCode(item) {
      this.$router.push(`/mobile/qrcode/${item.orderNo}`)
    },
    
    formatTime(timeStr) {
      return formatTimeUtil(timeStr)
    },
    
    formatTimeRange(startTime, endTime) {
      return formatTimeRangeUtil(startTime, endTime)
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
    
    onBottomTabChange(index) {
      if (index === 0) {
        this.$router.push('/mobile/home')
      }
    }
  }
}
</script>

<style scoped>
.my-reservations {
  padding-top: 46px;
  padding-bottom: 50px;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.content {
  padding: 0;
}

.reservation-item {
  background: white;
  margin: 10px;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.order-no {
  font-weight: 500;
  color: #333;
}

.status {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-reserved {
  background: #e6f7ff;
  color: #1890ff;
}

.status-used {
  background: #f6ffed;
  color: #52c41a;
}

.status-missed {
  background: #fff2f0;
  color: #ff4d4f;
}

.status-cancelled {
  background: #f5f5f5;
  color: #999;
}

.item-content {
  margin-bottom: 15px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.label {
  color: #666;
  font-size: 14px;
}

.value {
  color: #333;
  font-size: 14px;
}

.item-actions {
  display: flex;
  gap: 10px;
}

.item-actions .van-button {
  flex: 1;
}
</style>