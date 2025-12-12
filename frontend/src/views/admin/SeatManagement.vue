<template>
  <div class="seat-management">
    <div class="page-header">
      <h2>座位管理</h2>
      <div class="header-buttons">
        <el-button type="primary" @click="showAddDialog">新增座位</el-button>
        <el-button type="success" @click="showBatchAddDialog">批量新增座位</el-button>
      </div>
    </div>
    
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="图书馆">
          <el-select 
            v-model="searchForm.libraryId" 
            placeholder="请选择图书馆" 
            clearable 
            filterable
            style="width: 150px;"
          >
            <el-option
              v-for="library in libraries"
              :key="library.id"
              :label="library.name"
              :value="library.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="座位编号">
          <el-input v-model="searchForm.seatNumber" placeholder="请输入座位编号" clearable style="width: 120px;" />
        </el-form-item>
        <el-form-item label="座位类型">
          <el-select v-model="searchForm.seatType" placeholder="请选择类型" clearable style="width: 120px;">
            <el-option label="普通座位" value="普通座位" />
            <el-option label="电脑座位" value="电脑座位" />
            <el-option label="静音座位" value="静音座位" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 100px;">
            <el-option label="正常" value="正常" />
            <el-option label="维修" value="维修" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 表格 -->
    <el-table :data="tableData" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="libraryId" label="图书馆" width="150">
        <template slot-scope="scope">
          {{ getLibraryName(scope.row.libraryId) }}
        </template>
      </el-table-column>
      <el-table-column prop="seatNumber" label="座位编号" />
      <el-table-column prop="seatType" label="座位类型" width="120">
        <template slot-scope="scope">
          <el-tag 
            :type="getSeatTypeTagType(scope.row.seatType)"
            :icon="getSeatTypeIcon(scope.row.seatType)"
          >
            {{ scope.row.seatType }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '正常' ? 'success' : 'danger'">
            {{ scope.row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200">
        <template slot-scope="scope">
          <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.current"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
      />
    </div>
    
    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="500px"
      @close="resetForm"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="图书馆" prop="libraryId">
          <el-select v-model="form.libraryId" placeholder="请选择图书馆" filterable>
            <el-option
              v-for="library in libraries"
              :key="library.id"
              :label="library.name"
              :value="library.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="座位编号" prop="seatNumber">
          <el-input v-model="form.seatNumber" placeholder="请输入座位编号" />
        </el-form-item>
        <el-form-item label="座位类型" prop="seatType">
          <el-select v-model="form.seatType" placeholder="请选择座位类型">
            <el-option label="普通座位" value="普通座位" />
            <el-option label="电脑座位" value="电脑座位" />
            <el-option label="静音座位" value="静音座位" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="正常" value="正常" />
            <el-option label="维修" value="维修" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </div>
    </el-dialog>
    
    <!-- 批量新增对话框 -->
    <el-dialog
      title="批量新增座位"
      :visible.sync="batchDialogVisible"
      width="600px"
      @close="resetBatchForm"
    >
      <el-form ref="batchForm" :model="batchForm" :rules="batchRules" label-width="120px">
        <el-form-item label="图书馆" prop="libraryId">
          <el-select v-model="batchForm.libraryId" placeholder="请选择图书馆" filterable style="width: 100%;">
            <el-option
              v-for="library in libraries"
              :key="library.id"
              :label="library.name"
              :value="library.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="座位编号区间">
          <el-row :gutter="10">
            <el-col :span="11">
              <el-form-item prop="startNumber">
                <el-input-number 
                  v-model="batchForm.startNumber" 
                  :min="1" 
                  :max="9999"
                  placeholder="起始编号"
                  style="width: 100%;"
                  @change="updatePreview"
                />
              </el-form-item>
            </el-col>
            <el-col :span="2" style="text-align: center; line-height: 40px;">
              至
            </el-col>
            <el-col :span="11">
              <el-form-item prop="endNumber">
                <el-input-number 
                  v-model="batchForm.endNumber" 
                  :min="batchForm.startNumber + 1" 
                  :max="9999"
                  placeholder="结束编号"
                  style="width: 100%;"
                  @change="updatePreview"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form-item>
        
        <el-form-item label="座位类型" prop="seatType">
          <el-select v-model="batchForm.seatType" placeholder="请选择座位类型" style="width: 100%;">
            <el-option label="普通座位" value="普通座位" />
            <el-option label="电脑座位" value="电脑座位" />
            <el-option label="静音座位" value="静音座位" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-select v-model="batchForm.status" placeholder="请选择状态" style="width: 100%;">
            <el-option label="正常" value="正常" />
            <el-option label="维修" value="维修" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
        
        <!-- 预览区域 -->
        <el-form-item label="预览" v-if="previewSeats.length > 0">
          <div class="preview-area">
            <div class="preview-info">
              <span>将生成 {{ previewSeats.length }} 个座位：</span>
            </div>
            <div class="preview-seats">
              <el-tag 
                v-for="seat in previewSeats.slice(0, 10)" 
                :key="seat"
                size="small"
                style="margin: 2px;"
              >
                {{ seat }}
              </el-tag>
              <span v-if="previewSeats.length > 10" class="more-indicator">
                ... 还有 {{ previewSeats.length - 10 }} 个
              </span>
            </div>
          </div>
        </el-form-item>
      </el-form>
      
      <div slot="footer">
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleBatchSubmit" 
          :loading="batchSubmitLoading"
          :disabled="previewSeats.length === 0"
        >
          批量新增 ({{ previewSeats.length }}个)
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getSeatPage, createSeat, updateSeat, deleteSeat } from '@/api/seat'
import { getLibraryList } from '@/api/library'

export default {
  name: 'SeatManagement',
  data() {
    return {
      loading: false,
      tableData: [],
      libraries: [],
      pagination: {
        current: 1,
        size: 10,
        total: 0
      },
      searchForm: {
        libraryId: null,
        seatNumber: '',
        seatType: '',
        status: ''
      },
      dialogVisible: false,
      dialogTitle: '',
      isEdit: false,
      submitLoading: false,
      form: {
        id: null,
        libraryId: null,
        seatNumber: '',
        seatType: '普通座位',
        status: '正常'
      },
      
      // 批量新增相关
      batchDialogVisible: false,
      batchSubmitLoading: false,
      batchForm: {
        libraryId: null,
        startNumber: 1,
        endNumber: null,
        seatType: '普通座位',
        status: '正常'
      },
      previewSeats: [],
      rules: {
        libraryId: [
          { required: true, message: '请选择图书馆', trigger: 'change' }
        ],
        seatNumber: [
          { required: true, message: '请输入座位编号', trigger: 'blur' }
        ],
        seatType: [
          { required: true, message: '请选择座位类型', trigger: 'change' }
        ],
        status: [
          { required: true, message: '请选择状态', trigger: 'change' }
        ]
      },
      
      // 批量新增验证规则
      batchRules: {
        libraryId: [
          { required: true, message: '请选择图书馆', trigger: 'change' }
        ],
        startNumber: [
          { required: true, message: '请输入起始编号', trigger: 'blur' },
          { type: 'number', min: 1, message: '起始编号不能小于1', trigger: 'blur' }
        ],
        endNumber: [
          { required: true, message: '请输入结束编号', trigger: 'blur' },
          { 
            validator: (rule, value, callback) => {
              if (!value) {
                callback(new Error('请输入结束编号'))
              } else if (value <= this.batchForm.startNumber) {
                callback(new Error('结束编号必须大于起始编号'))
              } else {
                callback()
              }
            }, 
            trigger: 'blur' 
          }
        ],
        seatType: [
          { required: true, message: '请选择座位类型', trigger: 'change' }
        ],
        status: [
          { required: true, message: '请选择状态', trigger: 'change' }
        ]
      }
    }
  },
  
  async created() {
    await this.loadLibraries()
    this.loadData()
  },
  
  methods: {
    async loadLibraries() {
      try {
        const res = await getLibraryList()
        this.libraries = res.data
      } catch (error) {
        this.$message.error('加载图书馆列表失败')
      }
    },
    
    async loadData() {
      this.loading = true
      try {
        const params = {
          current: this.pagination.current,
          size: this.pagination.size,
          libraryId: this.searchForm.libraryId,
          seatNumber: this.searchForm.seatNumber,
          seatType: this.searchForm.seatType,
          status: this.searchForm.status
        }
        const res = await getSeatPage(params)
        this.tableData = res.data.records
        this.pagination.total = res.data.total
      } catch (error) {
        this.$message.error('加载数据失败')
      } finally {
        this.loading = false
      }
    },
    
    getLibraryName(libraryId) {
      const library = this.libraries.find(lib => lib.id === libraryId)
      return library ? library.name : '未知图书馆'
    },
    
    handleSearch() {
      this.pagination.current = 1
      this.loadData()
    },
    
    handleReset() {
      this.searchForm.libraryId = null
      this.searchForm.seatNumber = ''
      this.searchForm.seatType = ''
      this.searchForm.status = ''
      this.pagination.current = 1
      this.loadData()
    },
    
    handleSizeChange(val) {
      this.pagination.size = val
      this.loadData()
    },
    
    handleCurrentChange(val) {
      this.pagination.current = val
      this.loadData()
    },
    
    showAddDialog() {
      this.dialogTitle = '新增座位'
      this.isEdit = false
      this.dialogVisible = true
    },
    
    handleEdit(row) {
      this.dialogTitle = '编辑座位'
      this.isEdit = true
      this.form = { ...row }
      this.dialogVisible = true
    },
    
    async handleDelete(row) {
      try {
        await this.$confirm('确定要删除这个座位吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await deleteSeat(row.id)
        this.$message.success('删除成功')
        this.loadData()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    },
    
    handleSubmit() {
      this.$refs.form.validate(async (valid) => {
        if (valid) {
          this.submitLoading = true
          try {
            if (this.isEdit) {
              await updateSeat(this.form.id, this.form)
              this.$message.success('更新成功')
            } else {
              await createSeat(this.form)
              this.$message.success('新增成功')
            }
            this.dialogVisible = false
            this.loadData()
          } catch (error) {
            this.$message.error(error.message || (this.isEdit ? '更新失败' : '新增失败'))
          } finally {
            this.submitLoading = false
          }
        }
      })
    },
    
    resetForm() {
      this.form = {
        id: null,
        libraryId: null,
        seatNumber: '',
        seatType: '普通座位',
        status: '正常'
      }
      if (this.$refs.form) {
        this.$refs.form.resetFields()
      }
    },
    
    // 批量新增相关方法
    showBatchAddDialog() {
      this.batchDialogVisible = true
      this.updatePreview()
    },
    
    resetBatchForm() {
      this.batchForm = {
        libraryId: null,
        startNumber: 1,
        endNumber: null,
        seatType: '普通座位',
        status: '正常'
      }
      this.previewSeats = []
      if (this.$refs.batchForm) {
        this.$refs.batchForm.resetFields()
      }
    },
    
    updatePreview() {
      this.previewSeats = []
      
      if (this.batchForm.startNumber && this.batchForm.endNumber && 
          this.batchForm.endNumber > this.batchForm.startNumber) {
        
        const seats = []
        for (let i = this.batchForm.startNumber; i <= this.batchForm.endNumber; i++) {
          // 格式化座位编号为三位数，如 001, 002, 010
          const seatNumber = String(i).padStart(3, '0')
          seats.push(seatNumber)
        }
        this.previewSeats = seats
      }
    },
    
    async handleBatchSubmit() {
      this.$refs.batchForm.validate(async (valid) => {
        if (valid) {
          if (this.previewSeats.length === 0) {
            this.$message.warning('请设置正确的座位编号区间')
            return
          }
          
          // 确认批量新增
          try {
            await this.$confirm(
              `确定要批量新增 ${this.previewSeats.length} 个座位吗？`, 
              '批量新增确认', 
              {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
              }
            )
            
            this.batchSubmitLoading = true
            
            // 构建批量新增的座位数据
            const seats = this.previewSeats.map(seatNumber => ({
              libraryId: this.batchForm.libraryId,
              seatNumber: seatNumber,
              seatType: this.batchForm.seatType,
              status: this.batchForm.status
            }))
            
            // 调用批量新增API
            await this.batchCreateSeats(seats)
            
            this.$message.success(`成功新增 ${seats.length} 个座位`)
            this.batchDialogVisible = false
            this.loadData()
            
          } catch (error) {
            if (error !== 'cancel') {
              this.$message.error('批量新增失败：' + (error.message || '未知错误'))
            }
          } finally {
            this.batchSubmitLoading = false
          }
        }
      })
    },
    
    async batchCreateSeats(seats) {
      // 分批处理，避免一次性创建太多座位导致请求超时
      const batchSize = 10
      const batches = []
      
      for (let i = 0; i < seats.length; i += batchSize) {
        batches.push(seats.slice(i, i + batchSize))
      }
      
      let successCount = 0
      let errorCount = 0
      
      for (const batch of batches) {
        try {
          // 并发创建当前批次的座位
          const promises = batch.map(seat => createSeat(seat))
          await Promise.all(promises)
          successCount += batch.length
        } catch (error) {
          errorCount += batch.length
          console.error('批次创建失败:', error)
        }
      }
      
      if (errorCount > 0) {
        throw new Error(`成功创建 ${successCount} 个，失败 ${errorCount} 个`)
      }
    },

    // 获取座位类型标签类型
    getSeatTypeTagType(seatType) {
      switch (seatType) {
        case '电脑座位':
          return 'success'
        case '静音座位':
          return 'warning'
        case '普通座位':
        default:
          return 'primary'
      }
    },

    // 获取座位类型图标
    getSeatTypeIcon(seatType) {
      switch (seatType) {
        case '电脑座位':
          return 'el-icon-monitor'
        case '静音座位':
          return 'el-icon-bell'
        case '普通座位':
        default:
          return 'el-icon-reading'
      }
    }
  }
}
</script>

<style scoped>
.seat-management {
  background: white;
  padding: 20px;
  border-radius: 4px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #333;
}

.search-area {
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 4px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.header-buttons {
  display: flex;
  gap: 10px;
}

.preview-area {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 15px;
  background-color: #fafafa;
}

.preview-info {
  margin-bottom: 10px;
  font-weight: 500;
  color: #606266;
}

.preview-seats {
  max-height: 120px;
  overflow-y: auto;
}

.more-indicator {
  color: #909399;
  font-size: 12px;
  margin-left: 5px;
}
</style>