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
          <van-button 
            type="info" 
            block 
            :disabled="!isTimeValid" 
            @click="nextStep"
          >
            下一步
          </van-button>
        </div>
      </div>
      
      <!-- 步骤2：选择图书馆 -->
      <div v-if="currentStep === 1" class="step-container">
        <!-- 搜索框 -->
        <div class="search-container">
          <van-search
            v-model="librarySearchKeyword"
            placeholder="搜索图书馆名称、地址"
            show-action
            @search="onLibrarySearch"
            @clear="onLibrarySearchClear"
            @input="onLibrarySearchInput"
          >
            <template #action>
              <div @click="onLibrarySearch">搜索</div>
            </template>
          </van-search>
        </div>
        
        <!-- 搜索结果统计 -->
        <div v-if="librarySearchKeyword" class="search-result-info">
          找到 {{ filteredLibraries.length }} 个图书馆
        </div>
        
        <!-- 图书馆列表 -->
        <van-list>
          <van-cell
            v-for="library in filteredLibraries"
            :key="library.id"
            is-link
            @click="selectLibrary(library)"
          >
            <template #title>
              <div v-html="highlightText(library.name, librarySearchKeyword)"></div>
            </template>
            <template #label>
              <div v-html="highlightText(library.address, librarySearchKeyword)"></div>
            </template>
          </van-cell>
        </van-list>
        
        <!-- 无搜索结果提示 -->
        <div v-if="librarySearchKeyword && filteredLibraries.length === 0" class="no-result-container">
          <div class="empty-icon">🔍</div>
          <h3>未找到相关图书馆</h3>
          <p>请尝试其他关键词</p>
        </div>
        
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
        
        <!-- 加载中状态 -->
        <div v-if="loadingSeats" class="loading-container">
          <van-loading size="24px" vertical>正在查询可用座位...</van-loading>
        </div>
        
        <!-- 有可用座位时显示座位选择区域 -->
        <div v-else-if="availableSeats.length > 0" class="seat-selection-area">
          <!-- 座位类型图例 -->
          <div class="seat-legend">
            <div class="legend-item">
              <div class="legend-icon normal-seat"></div>
              <span>普通座位</span>
            </div>
            <div class="legend-item">
              <div class="legend-icon computer-seat"></div>
              <span>电脑座位</span>
            </div>
            <div class="legend-item">
              <div class="legend-icon quiet-seat"></div>
              <span>静音座位</span>
            </div>
          </div>

          <!-- 按座位类型分组显示 -->
          <div class="seats-by-type">
          <!-- 普通座位区域 -->
          <div v-if="normalSeats.length > 0" class="seat-type-section">
            <h4 class="seat-type-title">
              <span class="type-icon normal-seat"></span>
              普通座位区 ({{ normalSeats.length }}个)
            </h4>
            <div class="seat-grid">
              <div
                v-for="seat in normalSeats"
                :key="seat.id"
                class="seat-item normal-seat"
                :class="{ selected: selectedSeat && selectedSeat.id === seat.id }"
                @click="selectSeat(seat)"
              >
                {{ seat.seatNumber }}
              </div>
            </div>
          </div>

          <!-- 电脑座位区域 -->
          <div v-if="computerSeats.length > 0" class="seat-type-section">
            <h4 class="seat-type-title">
              <span class="type-icon computer-seat"></span>
              电脑座位区 ({{ computerSeats.length }}个)
            </h4>
            <div class="seat-grid">
              <div
                v-for="seat in computerSeats"
                :key="seat.id"
                class="seat-item computer-seat"
                :class="{ selected: selectedSeat && selectedSeat.id === seat.id }"
                @click="selectSeat(seat)"
              >
                {{ seat.seatNumber }}
              </div>
            </div>
          </div>

          <!-- 静音座位区域 -->
          <div v-if="quietSeats.length > 0" class="seat-type-section">
            <h4 class="seat-type-title">
              <span class="type-icon quiet-seat"></span>
              静音座位区 ({{ quietSeats.length }}个)
            </h4>
            <div class="seat-grid">
              <div
                v-for="seat in quietSeats"
                :key="seat.id"
                class="seat-item quiet-seat"
                :class="{ selected: selectedSeat && selectedSeat.id === seat.id }"
                @click="selectSeat(seat)"
              >
                {{ seat.seatNumber }}
              </div>
            </div>
          </div>
        </div>
        </div>
        
        <!-- 无可用座位时显示满员提示 -->
        <div v-else class="no-seats-container">
          <div class="empty-icon">😔</div>
          <h3>该时间段该图书馆已满员</h3>
          <p>请尝试选择其他时间段或其他图书馆</p>
          <van-button round type="primary" class="bottom-button" @click="prevStep">
            重新选择时间
          </van-button>
        </div>
        
        <div v-if="availableSeats.length > 0" class="button-container">
          <van-button plain @click="prevStep">上一步</van-button>
          <van-button type="info" :disabled="!selectedSeat" @click="confirmReservation">
            确认预约
          </van-button>
        </div>
        
        <div v-else class="button-container">
          <van-button plain @click="prevStep">上一步</van-button>
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
        title="选择预约日期"
        :min-date="minDate"
        @confirm="onDateConfirm"
        @cancel="showDatePicker = false"
      />
    </van-popup>
    
    <!-- 开始时间选择器 -->
    <van-action-sheet
      v-model="showStartTimePicker"
      title="选择开始时间"
      :actions="timeActions"
      @select="onStartTimeSelect"
      cancel-text="取消"
    />
    
    <!-- 结束时间选择器 -->
    <van-action-sheet
      v-model="showEndTimePicker"
      title="选择结束时间"
      :actions="timeActions"
      @select="onEndTimeSelect"
      cancel-text="取消"
    />
    
    <!-- 用户菜单 -->
    <van-action-sheet v-model="showUserMenu" :actions="userActions" @select="onUserAction" />
  </div>
</template>

<script>
import { getLibraryList } from '@/api/library'
import { getAvailableSeats } from '@/api/seat'
import { createReservation } from '@/api/reservation'
import { logout } from '@/api/auth'
import { formatDate, combineDateTime, generateTimeOptions } from '@/utils/time'

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
      timeActions: [],
      
      // 图书馆和座位
      libraries: [],
      librarySearchKeyword: '',
      selectedLibrary: null,
      availableSeats: [],
      selectedSeat: null,
      loadingSeats: false,
      
      // 用户菜单
      showUserMenu: false,
      userActions: [
        { name: '退出登录', color: '#ee0a24' }
      ]
    }
  },
  
  computed: {
    isTimeValid() {
      // 简单验证：只要三个字段都有值且结束时间晚于开始时间
      const hasAllFields = this.reservationDate && this.startTime && this.endTime
      
      if (!hasAllFields) {
        return false
      }
      
      // 简单比较时间字符串（因为都是HH:00格式）
      const startHour = parseInt(this.startTime.split(':')[0])
      const endHour = parseInt(this.endTime.split(':')[0])
      const isTimeOrderValid = endHour > startHour
      
      // console.log('时间验证:', { hasAllFields, isTimeOrderValid })
      
      return hasAllFields && isTimeOrderValid
    },
    
    filteredLibraries() {
      if (!this.librarySearchKeyword) {
        return this.libraries
      }
      
      const keyword = this.librarySearchKeyword.toLowerCase().trim()
      if (!keyword) {
        return this.libraries
      }
      
      return this.libraries.filter(library => {
        const name = library.name.toLowerCase()
        const address = library.address ? library.address.toLowerCase() : ''
        const description = library.description ? library.description.toLowerCase() : ''
        
        // 支持多个关键词搜索（空格分隔）
        const keywords = keyword.split(/\s+/)
        
        return keywords.every(kw => 
          name.includes(kw) || 
          address.includes(kw) || 
          description.includes(kw)
        )
      })
    },

    // 按座位类型分组
    normalSeats() {
      return this.availableSeats.filter(seat => seat.seatType === '普通座位')
    },

    computerSeats() {
      return this.availableSeats.filter(seat => seat.seatType === '电脑座位')
    },

    quietSeats() {
      return this.availableSeats.filter(seat => seat.seatType === '静音座位')
    }
  },
  
  async created() {
    await this.loadLibraries()
    // 初始化当前日期
    this.reservationDate = formatDate(new Date())
    // 初始化时间选项
    this.timeActions = this.generateTimeActions()
  },
  
  methods: {
    // 生成时间选择动作
    generateTimeActions() {
      return generateTimeOptions().map(time => ({
        name: time,
        value: time
      }))
    },
    
    async loadLibraries() {
      try {
        const res = await getLibraryList()
        this.libraries = res.data
      } catch (error) {
        this.$toast.fail('加载图书馆列表失败')
      }
    },
    
    onLibrarySearch() {
      // 搜索功能已通过计算属性 filteredLibraries 实现
      // 这里可以添加搜索统计或其他逻辑
      if (this.filteredLibraries.length === 0 && this.librarySearchKeyword) {
        this.$toast('未找到相关图书馆')
      }
    },
    
    onLibrarySearchClear() {
      this.librarySearchKeyword = ''
    },
    
    onLibrarySearchInput() {
      // 实时搜索，无需额外处理，计算属性会自动更新
    },
    
    highlightText(text, keyword) {
      if (!keyword || !text) return text
      
      const keywords = keyword.toLowerCase().trim().split(/\s+/)
      let result = text
      
      keywords.forEach(kw => {
        if (kw) {
          const regex = new RegExp(`(${kw})`, 'gi')
          result = result.replace(regex, '<span class="highlight">$1</span>')
        }
      })
      
      return result
    },
    
    async loadAvailableSeats() {
      this.loadingSeats = true
      try {
        const startDateTime = combineDateTime(this.reservationDate, this.startTime)
        const endDateTime = combineDateTime(this.reservationDate, this.endTime)
        
        const res = await getAvailableSeats({
          libraryId: this.selectedLibrary.id,
          startTime: startDateTime,
          endTime: endDateTime
        })
        
        this.availableSeats = res.data
        
        // 如果没有可用座位，显示提示
        if (this.availableSeats.length === 0) {
          this.$toast('该时间段该图书馆已满员')
        }
        
      } catch (error) {
        this.$toast.fail('加载可用座位失败')
        // 如果加载失败，返回上一步
        this.prevStep()
      } finally {
        this.loadingSeats = false
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
      this.selectedSeat = null // 清除之前选择的座位
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
    
    onStartTimeSelect(action) {
      this.startTime = action.value
      this.showStartTimePicker = false
    },
    
    onEndTimeSelect(action) {
      this.endTime = action.value
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

/* 座位选择区域 */
.seat-selection-area {
  margin-bottom: 20px;
}

/* 座位类型图例 */
.seat-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 20px;
  padding: 15px;
  background: white;
  border-radius: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.legend-icon {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  border: 2px solid;
}

/* 座位类型分组 */
.seats-by-type {
  margin-bottom: 20px;
}

.seat-type-section {
  margin-bottom: 25px;
}

.seat-type-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 500;
  color: #323233;
}

.type-icon {
  width: 16px;
  height: 16px;
  border-radius: 3px;
  border: 2px solid;
}

.seat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.seat-item {
  border-radius: 8px;
  padding: 15px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
  position: relative;
  border: 2px solid;
}

/* 普通座位样式 */
.seat-item.normal-seat {
  background: #f0f9ff;
  border-color: #0ea5e9;
  color: #0369a1;
}

.seat-item.normal-seat:hover {
  background: #e0f2fe;
  border-color: #0284c7;
}

.seat-item.normal-seat.selected {
  background: #0ea5e9;
  color: white;
  border-color: #0284c7;
}

.legend-icon.normal-seat,
.type-icon.normal-seat {
  background: #f0f9ff;
  border-color: #0ea5e9;
}

/* 电脑座位样式 */
.seat-item.computer-seat {
  background: #f0fdf4;
  border-color: #22c55e;
  color: #15803d;
}

.seat-item.computer-seat:hover {
  background: #dcfce7;
  border-color: #16a34a;
}

.seat-item.computer-seat.selected {
  background: #22c55e;
  color: white;
  border-color: #16a34a;
}

.legend-icon.computer-seat,
.type-icon.computer-seat {
  background: #f0fdf4;
  border-color: #22c55e;
}

/* 为电脑座位添加图标 */
.seat-item.computer-seat::before {
  content: '💻';
  position: absolute;
  top: 3px;
  right: 5px;
  font-size: 12px;
}

/* 静音座位样式 */
.seat-item.quiet-seat {
  background: #fef3f2;
  border-color: #f97316;
  color: #c2410c;
}

.seat-item.quiet-seat:hover {
  background: #fed7d1;
  border-color: #ea580c;
}

.seat-item.quiet-seat.selected {
  background: #f97316;
  color: white;
  border-color: #ea580c;
}

.legend-icon.quiet-seat,
.type-icon.quiet-seat {
  background: #fef3f2;
  border-color: #f97316;
}

/* 为静音座位添加图标 */
.seat-item.quiet-seat::before {
  content: '🤫';
  position: absolute;
  top: 3px;
  right: 5px;
  font-size: 12px;
}

/* 为电脑座位添加图标 */
.seat-item.computer-seat::before {
  content: '💻';
  position: absolute;
  top: 3px;
  right: 5px;
  font-size: 12px;
}

/* 为普通座位添加图标 */
.seat-item.normal-seat::before {
  content: '📚';
  position: absolute;
  top: 3px;
  right: 5px;
  font-size: 12px;
}

.no-seats-container {
  background: white;
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
  margin-bottom: 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.no-seats-container h3 {
  margin: 0 0 8px 0;
  color: #323233;
  font-size: 16px;
  font-weight: 500;
}

.no-seats-container p {
  margin: 0 0 24px 0;
  color: #969799;
  font-size: 14px;
  line-height: 20px;
}

.bottom-button {
  margin-top: 20px;
}

.loading-container {
  background: white;
  border-radius: 8px;
  padding: 60px 20px;
  text-align: center;
  margin-bottom: 20px;
}

.search-container {
  background: white;
  border-radius: 8px;
  margin-bottom: 15px;
  overflow: hidden;
}

.search-result-info {
  padding: 10px 16px;
  background: #f7f8fa;
  color: #646566;
  font-size: 14px;
  border-radius: 4px;
  margin-bottom: 10px;
}

.no-result-container {
  background: white;
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
  margin-bottom: 20px;
}

.no-result-container .empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.no-result-container h3 {
  margin: 0 0 8px 0;
  color: #323233;
  font-size: 16px;
  font-weight: 500;
}

.no-result-container p {
  margin: 0;
  color: #969799;
  font-size: 14px;
  line-height: 20px;
}

.highlight {
  background-color: #fff3cd;
  color: #856404;
  padding: 1px 2px;
  border-radius: 2px;
  font-weight: 500;
}
</style>