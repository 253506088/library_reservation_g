<template>
  <div class="my-reservations">
    <van-nav-bar title="我的预约" fixed>
      <template #left>
        <van-icon name="arrow-left" @click="$router.go(-1)" />
      </template>
      <template #right>
        <van-icon name="replay" @click="refreshData" />
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
      
      <!-- 调试信息（开发环境显示） -->
      <div v-if="showDebugInfo" class="debug-info">
        <p>当前页: {{ pagination.current }}</p>
        <p>每页大小: {{ pagination.size }}</p>
        <p>已加载: {{ reservations.length }}</p>
        <p>加载中: {{ loading }}</p>
        <p>已完成: {{ finished }}</p>
        <p>当前标签: {{ activeTab || '全部' }}</p>
        <div style="margin-top: 10px;">
          <button @click="forceLoadMore" style="margin-right: 10px; padding: 5px 10px; background: #1989fa; color: white; border: none; border-radius: 4px;">强制加载更多</button>
          <button @click="resetAndReload" style="padding: 5px 10px; background: #ff4d4f; color: white; border: none; border-radius: 4px;">重置并重新加载</button>
        </div>
      </div>
      
      <!-- 预约列表 -->
      <van-list
        v-model="loading"
        :finished="finished"
        finished-text="没有更多了"
        :offset="50"
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
      },
      showDebugInfo: process.env.NODE_ENV === 'development'
    }
  },
  
  watch: {
    finished(newVal, oldVal) {
      console.log('finished 状态变化:', { from: oldVal, to: newVal })
      if (newVal) {
        console.trace('finished 被设置为 true 的调用栈:')
      }
    },
    
    loading(newVal, oldVal) {
      console.log('loading 状态变化:', { from: oldVal, to: newVal })
    }
  },
  
  created() {
    console.log('MyReservations 组件创建')
  },
  
  mounted() {
    console.log('MyReservations 组件挂载完成', {
      loading: this.loading,
      finished: this.finished,
      reservationsCount: this.reservations.length
    })
    // 让 van-list 自动触发初始加载，不需要手动调用
  },
  
  methods: {
    async loadReservations() {
      console.log('loadReservations 被调用', {
        loading: this.loading,
        finished: this.finished,
        currentPage: this.pagination.current
      })
      
      // 只检查 finished 状态，不检查 loading 状态
      // 因为 van-list 会自动管理 loading 状态
      if (this.finished) {
        console.log('loadReservations 提前返回，原因：finished = true')
        return
      }
      
      // van-list 已经自动设置了 loading = true，这里不需要再设置
      try {
        const params = {
          current: this.pagination.current,
          size: this.pagination.size,
          status: this.activeTab
        }
        
        console.log('加载预约记录，参数：', params)
        
        const res = await getReservationPage(params)
        
        console.log('预约记录响应：', res.data)
        
        // 处理返回的数据
        const newRecords = res.data.records || []
        
        if (this.pagination.current === 1) {
          // 第一页，直接替换
          this.reservations = newRecords
        } else {
          // 后续页，追加数据
          this.reservations.push(...newRecords)
        }
        
        // 判断是否还有更多数据
        const returnedCount = newRecords.length
        const requestedSize = this.pagination.size
        const totalCount = res.data.total || 0
        const loadedCount = this.reservations.length
        
        // 只有当返回的数据少于请求的数量时，才认为没有更多数据
        // 或者已加载的数据达到了总数
        const noMoreData = returnedCount < requestedSize
        const reachedTotal = loadedCount >= totalCount
        
        this.finished = noMoreData || (totalCount > 0 && reachedTotal)
        
        console.log('分页状态：', {
          returnedCount,
          requestedSize,
          totalCount,
          loadedCount,
          noMoreData,
          reachedTotal,
          finished: this.finished,
          currentPage: this.pagination.current
        })
        
        // 准备下一页
        this.pagination.current++
        
      } catch (error) {
        console.error('加载预约记录失败：', error)
        this.$toast.fail('加载预约记录失败')
        this.finished = true // 出错时停止加载
      } finally {
        this.loading = false
      }
    },
    
    onLoad() {
      // van-list 的 onLoad 事件，用于加载更多数据
      console.log('van-list onLoad 触发', {
        loading: this.loading,
        finished: this.finished,
        currentPage: this.pagination.current,
        reservationsCount: this.reservations.length
      })
      this.loadReservations()
    },
    
    onTabChange() {
      this.resetList()
      this.loadReservations()
    },
    
    resetList() {
      console.log('重置列表状态', {
        before: {
          reservationsCount: this.reservations.length,
          currentPage: this.pagination.current,
          finished: this.finished,
          loading: this.loading
        }
      })
      this.reservations = []
      this.pagination.current = 1
      this.finished = false
      this.loading = false
      console.log('重置列表状态完成', {
        after: {
          reservationsCount: this.reservations.length,
          currentPage: this.pagination.current,
          finished: this.finished,
          loading: this.loading
        }
      })
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
    },
    
    refreshData() {
      console.log('手动刷新数据')
      this.resetList()
      this.loadReservations()
    },
    
    forceLoadMore() {
      console.log('强制加载更多')
      this.finished = false
      // 不要手动设置 loading = false，让 van-list 管理
      this.loadReservations()
    },
    
    resetAndReload() {
      console.log('重置并重新加载')
      this.resetList()
      this.$nextTick(() => {
        this.loadReservations()
      })
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

.debug-info {
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 4px;
  padding: 10px;
  margin: 10px;
  font-size: 12px;
  color: #856404;
}

.debug-info p {
  margin: 2px 0;
}
</style>