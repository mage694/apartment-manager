<template>
  <div v-loading="loading">
    <el-row style="margin-bottom:10px;">
      <el-col :span="4">
        <el-autocomplete
          v-model="name"
          :fetch-suggestions="fetchAdviser"
          placeholder="请输入房间名或姓名"
          :trigger-on-focus="false"
          @select="load(page)"
          size="medium"
        ></el-autocomplete>
      </el-col>
      <el-col :span="0.7">&nbsp;</el-col>
      <el-col :span="4">
        <el-select v-model="status" placeholder="客户状态" size="medium" @change="load(page)">
          <el-option label="入住中" value="ENROLLED"></el-option>
          <el-option label="已退房" value="EXITED"></el-option>
          <el-option label="全部" value></el-option>
        </el-select>
      </el-col>
      <el-col :span="2">
        <el-button size="mini" type="primary" round @click="load(page)">搜索</el-button>
      </el-col>
      <el-col :span="2">
        <el-button size="mini" type="primary" round @click="cleanFilter()">清空筛选</el-button>
      </el-col>
    </el-row>
    <el-table :data="customers" border stripe style="width: 100%" height="360">
      <el-table-column type="selection" width="35"></el-table-column>
      <el-table-column prop="apartmentName" label="房间名" width="200"></el-table-column>
      <el-table-column prop="idNum" label="身份证" width="250"></el-table-column>
      <el-table-column prop="name" label="客户姓名" width="150"></el-table-column>
      <el-table-column prop="enrollDate" label="入住日期" width="200">
        <template slot-scope="scope">
          <i class="el-icon-date"></i>
          <span>{{ scope.row.enrollDate }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="操作">
        <template slot-scope="scope">
          <el-tooltip effect="dark" content="编辑" placement="top-start">
            <el-button
              type="primary"
              icon="el-icon-edit"
              round
              size="mini"
              @click="edit(scope.row.id)"
            >编辑</el-button>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>
    <br>
    <el-pagination
      background
      layout="prev, pager, next"
      :page-size="pageSize"
      :total="total"
      @current-change="jumpPage"
      @previous-click="jumpPage"
      @next-click="jumpPage"
    ></el-pagination>
  </div>
</template>
<style>
.el-table .cell {
  white-space: pre-line;
}
</style>

<script>
export default {
  name: "apartment-list",
  inject: ["reload"],
  data() {
    return {
      suggestions: [],
      loading: true,
      name: null,
      status: "ENROLLED",
      isRouterAlive: true,
      total: 1,
      page: 1,
      pageSize: 5,
      total: 0,
      customers: [
        {
          id: 0,
          apartmentId: 0,
          apartmentName: "202",
          paymentType: "PER_MONTH",
          name: "李四",
          idNum: "34546456452",
          isPrimary: true,
          enrollDate: null
        }
      ]
    };
  },
  mounted() {
    this.load(this.page);
  },
  methods: {
    load(page) {
      const that = this;
      this.$api
        .get("/customers", {
          page: page,
          pageSize: this.pageSize,
          name: this.name,
          status: this.status
        })
        .then(response => {
          this.total = response.data.total;
          this.customers = response.data.content.map(c => {
            return {
              id: c.id,
              apartmentId: c.apartmentId,
              apartmentName: c.apartmentName,
              name: c.name,
              idNum: c.idNum,
              enrollDate: c.enrollDate
            };
          });
          this.$api.get("/adviser/names").then(response => {
            this.suggestions = response.data.map(d => {
              return {
                value: d
              };
            });
          });
          this.loading = false;
        });
    },
    jumpPage(page) {
      this.loading = true;
      this.load(page);
    },
    cleanFilter() {
      this.name = null;
      this.status = "";
      this.load(1);
    },
    edit(id) {
      this.$router.push("/customerForm/" + id);
    },
    fetchAdviser(queryString, cb) {
      let names = this.suggestions;
      const results = queryString
        ? names.filter(this.createFilter(queryString))
        : names;

      cb(results);
    },
    createFilter(queryString) {
      return n => {
        return n.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0;
      };
    }
  }
};
</script>
