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
      <el-col :span="2">
        <el-button size="mini" type="primary" round @click="load(page)">搜索</el-button>
      </el-col>
      <el-col :span="2">
        <el-button size="mini" type="primary" round @click="cleanFilter()">清空筛选</el-button>
      </el-col>
    </el-row>
    <el-table :data="payments" border stripe style="width: 100%" height="450">
      <el-table-column type="expand">
        <template slot-scope="scope">
          <payment-detail-form :row="scope.row"></payment-detail-form>
        </template>
      </el-table-column>
      <el-table-column prop="apartmentName" label="房间名" width="150"></el-table-column>
      <el-table-column prop="primaryCustomerName" label="客户姓名" width="150"></el-table-column>
      <el-table-column label="日期" width="230">
        <template v-if="scope.row.toDate != null" slot-scope="scope">
          <i class="el-icon-date"></i>
          <span>{{scope.row.fromDate}} 至 {{ scope.row.toDate }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="totalPrice" label="总价" width="150"></el-table-column>
      <el-table-column prop="receipts" label="实收" width="150"></el-table-column>
      <el-table-column align="center" label="操作">
        <template v-if="scope.row.canBeReverted" slot-scope="scope">
          <el-tooltip class="item" effect="dark" content="退回此缴费记录" placement="top-start">
            <el-button
              type="primary"
              icon="el-icon-refresh-left"
              round
              size="mini"
              @click="revert(scope.row.id)"
            >撤销</el-button>
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
import PaymentDetailForm from "@/components/payments/payment-detail-form.vue";
export default {
  name: "apartment-list",
  inject: ["reload"],
  components: {PaymentDetailForm},
  data() {
    return {
      suggestions: [],
      loading: true,
      name: null,
      isRouterAlive: true,
      total: 1,
      page: 1,
      pageSize: 5,
      total: 0,
      payments: [
        {
          id: 0,
          apartmentId: 0,
          apartmentName: "202",
          paymentType: "PER_MONTH",
          quantity: 1,
          primaryCustomerName: "李四",
          fromDate: "2019-01-01",
          toDate: "2019-02-01",
          totalPrice: 0,
          receipts: 0,
          premiums: []
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
        .get("/payments", {
          page: page,
          pageSize: this.pageSize,
          name: this.name
        })
        .then(response => {
          this.total = response.data.total;
          this.payments = response.data.content.map(p => {
            return {
              id: p.id,
              apartmentId: p.apartmentId,
              apartmentName: p.apartmentName,
              primaryCustomerName: p.primaryCustomerName,
              fromDate: p.fromDate,
              toDate: p.toDate,
              unitPrice: p.unitPrice,
              quantity: p.quantity,
              paymentType: p.paymentType,
              totalPrice: p.totalPrice,
              receipts: p.receipts,
              premiums: p.premiums,
              canBeReverted: p.canBeReverted
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
    revert(id) {
      this.$confirm("此操作将撤销该记录, 是否继续?", "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      })
        .then(() => {
          this.$api
            .delete("/payments/" + id + "/revert")
            .then(res => {
              this.$message({
                message: "撤销成功",
                type: "success"
              });
              this.reload();
            })
            .catch(err => {
              this.$message.error("撤销失败，请联系管理员");
            });
        })
        .catch(() => {
          this.$message({
            type: "info",
            message: "已取消"
          });
        });
    },

    priceToDisplay(a) {
      let price = "";
      if (a.concertedPrice != null) {
        price = a.concertedPrice;
        switch (a.chosenPaymentType) {
          case "PER_MONTH":
            price += "元/月";
            break;
          case "PER_WEEK":
            price += "元/周";
            break;
          case "PER_DAY":
            price += "元/天";
            break;
        }
      } else {
        for (let key in a.prices) {
          switch (key) {
            case "PER_MONTH":
              price += a.prices[key] != null ? a.prices[key] + "元/月\n" : "";
              break;
            case "PER_WEEK":
              price += a.prices[key] != null ? a.prices[key] + "元/周\n" : "";
              break;
            case "PER_DAY":
              price += a.prices[key] != null ? a.prices[key] + "元/天\n" : "";
              break;
          }
        }
      }
      return price;
    },
    jumpPage(page) {
      this.loading = true;
      this.load(page);
    },
    cleanFilter() {
      this.name = null;
      this.load(1);
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
