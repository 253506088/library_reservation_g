<template>
  <div class="user-management">
    <div class="page-header">
      <h2>用户管理</h2>
    </div>
    
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="searchForm.realName" placeholder="请输入真实姓名" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item label="用户类型">
          <el-select v-model="searchForm.userType" placeholder="请选择用户类型" clearable style="width: 120px;">
            <el-option label="学生" value="学生" />
            <el-option label="管理员" value="管理员" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px;">
            <el-option label="正常" value="正常" />
            <el-option label="禁用" value="禁用" />
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
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="真实姓名" width="120" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="email" label="邮箱" width="180" />
      <el-table-column prop="userType" label="用户类型" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.userType === '管理员' ? 'danger' : 'primary'">
            {{ scope.row.userType }}
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
      <el-table-column prop="createTime" label="注册时间" width="180" />
      <el-table-column label="操作" width="220">
        <template slot-scope="scope">
          <el-button
            size="mini"
            :type="scope.row.status === '正常' ? 'danger' : 'success'"
            @click="handleToggleStatus(scope.row)"
          >
            {{ scope.row.status === '正常' ? '禁用' : '启用' }}
          </el-button>
          <el-button
            size="mini"
            type="warning"
            @click="handleChangeUserType(scope.row)"
          >
            改类型
          </el-button>
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
  </div>
</template>

<script>
import { getUserPage, toggleUserStatus, changeUserType } from '@/api/user'

export default {
  name: 'UserManagement',
  data() {
    return {
      loading: false,
      tableData: [],
      pagination: {
        current: 1,
        size: 10,
        total: 0
      },
      searchForm: {
        username: '',
        realName: '',
        userType: '',
        status: ''
      }
    }
  },
  
  created() {
    this.loadData()
  },
  
  methods: {
    async loadData() {
      this.loading = true
      try {
        const params = {
          current: this.pagination.current,
          size: this.pagination.size,
          username: this.searchForm.username,
          realName: this.searchForm.realName,
          userType: this.searchForm.userType,
          status: this.searchForm.status
        }
        const res = await getUserPage(params)
        this.tableData = res.data.records
        this.pagination.total = res.data.total
      } catch (error) {
        this.$message.error('加载数据失败')
      } finally {
        this.loading = false
      }
    },
    
    handleSearch() {
      this.pagination.current = 1
      this.loadData()
    },
    
    handleReset() {
      this.searchForm.username = ''
      this.searchForm.realName = ''
      this.searchForm.userType = ''
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
    
    async handleToggleStatus(row) {
      const action = row.status === '正常' ? '禁用' : '启用'
      try {
        await this.$confirm(`确定要${action}用户"${row.realName}"吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await toggleUserStatus(row.id)
        this.$message.success(`${action}成功`)
        this.loadData()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(`${action}失败`)
        }
      }
    },
    
    async handleChangeUserType(row) {
      const newUserType = row.userType === '学生' ? '管理员' : '学生'
      try {
        await this.$confirm(
          `确定要将用户"${row.realName}"的类型修改为【${newUserType}】吗？`, 
          '修改用户类型', 
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        await changeUserType(row.id, newUserType)
        this.$message.success('用户类型修改成功')
        this.loadData()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('用户类型修改失败')
        }
      }
    }
  }
}
</script>

<style scoped>
.user-management {
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