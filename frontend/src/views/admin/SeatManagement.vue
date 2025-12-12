<template>
  <div class="seat-management">
    <div class="page-header">
      <h2>座位管理</h2>
      <el-button type="primary" @click="showAddDialog">新增座位</el-button>
    </div>
    
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="图书馆">
          <el-select v-model="searchForm.libraryId" placeholder="请选择图书馆" clearable>
            <el-option
              v-for="library in libraries"
              :key="library.id"
              :label="library.name"
              :value="library.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="座位编号">
          <el-input v-model="searchForm.seatNumber" placeholder="请输入座位编号" clearable />
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
      <el-table-column prop="seatType" label="座位类型" />
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
          <el-select v-model="form.libraryId" placeholder="请选择图书馆">
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
        seatNumber: ''
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
          seatNumber: this.searchForm.seatNumber
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
</style>