<template>
  <div class="mobile-home">
    <van-nav-bar title="座位预约" fixed>
      <template #right>
        <van-icon name="user-o" @click="showUserMenu = true" />
      </template>
    </van-nav-bar>
    
    <div class="content">
      <!-- 预约步骤 -->
      <van-steps :active="currentStep" active-color="#1989fa">
        <van-step>选择时间</van-step>
        <van-step>选择图书馆</van-step>
        <van-step>选择座位</van-step>
      </van-steps>
      
      <!-- 步骤1：选择时间 -->
      <div v-if="currentStep === 0" class="step-container">
        <van-cell-group>
          <van-field
            v-model="reservationDate"
            readonly
            label="预约日期"
            placeholder="选择预约日期"
            @click="showDatePicker = true"
          />
          <van-field
            v-model="startTime"
            readonly
            label="开始时间"
            placeholder="选择开始时间"
            @click="showStartTimePicker = true"
          />
          <van-field
            v-model="endTime"
            readonly
            label="结束时间"
            placeholder="选择结束时间"
            @click="showEndTimePicker = true"
          />
        </van-cell-group>
        
        <div class="button-container">
          <van-button type="info" block :disabled="!isTimeValid" @click="nextStep">
            下一步
          </van-button>
        </div>
      </div>
      
      <!-- 步骤2：选择图书馆 -->
      <div v-if="currentStep === 1" class="step-container">
        <van-list>
          <van-cell
            v-for="library in libraries"
            :key="library.id"
            :title="library.name"
            :label="library.address"
            is-link
            @click="selectLibrary(library)"
          />
        </van-list>
        
        <div class="button-container">
          <van-button plain @click="prevStep">上一步</van-button>
        </div>
      </div>
      
      <!-- 步骤3：选择座位 -->
      <div v-if="currentStep === 2" class="step-container">
        <div class="library-info">
          <h3>{{ selectedLibrary.name }}</h3>
          <p>{{ getTimeRange() }}</p>
        </div>
        
        <div class="seat-grid">
          <div
            v-for="seat in availableSeats"
            :key="seat.id"
            class="seat-item"
            :class="{ selected: selectedSeat && selectedSeat.id === seat.id }"
            @click="selectSeat(seat)"
          >
            {{ seat.seatNumber }}
          </div>
        </div>
        
        <div class="button-container">
          <van-button plain @click="prevStep">上一步</van-button>
          <van-button type="info" :disabled="!selectedSeat" @click="confirmReservation">
            确认预约
          </van-button>
        </div>
      </div>
    </div>
    
    <!-- 底部导航 -->
    <van-tabbar v-model="activeTab" @change="onTabChange">
      <van-tabbar-item icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item icon="orders-o">我的预约</van-tabbar-item>
    </van-tabbar>
    
    <!-- 日期选择器 -->
    <van-popup v-model="showDatePicker" position="bottom">
      <van-datetime-picker
        v-model="currentDate"
        type="date"
        :min-date="minDate"
        @confirm="onDateConfirm"
        @cancel="showDatePicker = false"
      />
    </van-popup>
    
    <!-- 开始时间选择器 -->
    <van-popup v-model="showStartTimePicker" position="bottom">
      <van-picker
        :columns="timeColumns"
        @confirm="onStartTimeConfirm"
        @cancel="showStartTimePicker = false"
      />
    </van-popup>
    
    <!-- 结束时间选择器 -->
    <van-popup v-model="showEndTimePicker" position="bottom">
      <van-picker
        :columns="timeColumns"
        @confirm="onEndTimeConfirm"
        @cancel="showEndTimePicker = false"
      />
    </van-popup>
    
    <!-- 用户菜单 -->
    <van-action-sheet v-model="showUserMenu" :actions="userActions" @select="onUserAction" />
  </div>
</template>

<script>
import { getLibraryList } from '@/api/library'
import { getAvailableSeats } from '@/api/seat'
import { createReservation } from '@/api/reservation'
import { logout } from '@/api/auth'
import { formatDate, combineDateTime, generateTimeOptions, isValidTime } from '@/utils/time'

export default {
  name: 'MobileHome',
  data() {
    return {
      currentStep: 0,
      activeTab: 0,
      
      // 时间选择
      reservationDate: '',
      startTime: '',
      endTime: '',
      showDatePicker: false,
      showStartTimePicker: false,
      showEndTimePicker: false,
      currentDate: new Date(),
      minDate: new Date(),
      
      // 时间选项（只到小时）
      timeColumns: generateTimeOptions(),
      
      // 图书馆和座位
      libraries: [],
      selectedLibrary: null,
      availableSeats: [],
      selectedSeat: null,
      
      // 用户菜单
      showUserMenu: false,
      userActions: [
        { name: '退出登录', color: '#ee0a24' }
      ]
    }
  },
  
  computed: {
    isTimeValid() {
      return isValidTime(this.reservationDate, this.startTime, this.endTime)
    }
  },
  
  async created() {
    await this.loadLibraries()
    // 初始化当前日期
    this.reservationDate = formatDate(new Date())
  },
  
  methods: {

    
    async loadLibraries() {
      try {
        const res = await getLibraryList()
        this.libraries = res.data
      } catch (error) {
        this.$toast.fail('加载图书馆列表失败')
      }
    },
    
    async loadAvailableSeats() {
      try {
        const startDateTime = combineDateTime(this.reservationDate, this.startTime)
        const endDateTime = combineDateTime(this.reservationDate, this.endTime)
        
        const res = await getAvailableSeats({
          libraryId: this.selectedLibrary.id,
          startTime: startDateTime,
          endTime: endDateTime
        })
        this.availableSeats = res.data
      } catch (error) {
        this.$toast.fail('加载可用座位失败')
      }
    },
    
    nextStep() {
      if (this.currentStep < 2) {
        this.currentStep++
      }
    },
    
    prevStep() {
      if (this.currentStep > 0) {
        this.currentStep--
      }
    },
    
    async selectLibrary(library) {
      this.selectedLibrary = library
      await this.loadAvailableSeats()
      this.nextStep()
    },
    
    selectSeat(seat) {
      this.selectedSeat = seat
    },
    
    async confirmReservation() {
      try {
        const startDateTime = combineDateTime(this.reservationDate, this.startTime)
        const endDateTime = combineDateTime(this.reservationDate, this.endTime)
        
        const reservationData = {
          libraryId: this.selectedLibrary.id,
          seatId: this.selectedSeat.id,
          startTime: startDateTime,
          endTime: endDateTime
        }
        
        const res = await createReservation(reservationData)
        this.$toast.success('预约成功')
        
        // 跳转到二维码页面
        this.$router.push(`/mobile/qrcode/${res.data.orderNo}`)
      } catch (error) {
        this.$toast.fail(error.message || '预约失败')
      }
    },
    
    onDateConfirm(date) {
      this.reservationDate = formatDate(date)
      this.showDatePicker = false
    },
    
    onStartTimeConfirm(value) {
      this.startTime = value
      this.showStartTimePicker = false
    },
    
    onEndTimeConfirm(value) {
      this.endTime = value
      this.showEndTimePicker = false
    },
    
    getTimeRange() {
      return `${this.reservationDate} ${this.startTime} - ${this.endTime}`
    },
    
    onTabChange(index) {
      if (index === 1) {
        this.$router.push('/mobile/my-reservations')
      }
    },
    
    async onUserAction(action) {
      if (action.name === '退出登录') {
        try {
          await logout()
          this.$store.dispatch('clearUser')
          this.$router.push('/mobile/login')
        } catch (error) {
          this.$toast.fail('退出登录失败')
        }
      }
      this.showUserMenu = false
    }
  }
}
</script>

<style scoped>
.mobile-home {
  padding-top: 46px;
  padding-bottom: 50px;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.content {
  padding: 20px;
}

.step-container {
  margin-top: 20px;
}

.button-container {
  margin-top: 30px;
  display: flex;
  gap: 10px;
}

.button-container .van-button {
  flex: 1;
}

.library-info {
  background: white;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.library-info h3 {
  margin: 0 0 5px 0;
  font-size: 18px;
}

.library-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.seat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-bottom: 20px;
}

.seat-item {
  background: white;
  border: 2px solid #e8e8e8;
  border-radius: 8px;
  padding: 15px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.seat-item:hover {
  border-color: #1989fa;
}

.seat-item.selected {
  background: #1989fa;
  color: white;
  border-color: #1989fa;
}
</style>