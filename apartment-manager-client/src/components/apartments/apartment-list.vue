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
        <el-select v-model="status" placeholder="房屋状态" size="medium" @change="load(page)">
          <el-option label="全部" value></el-option>
          <el-option label="应续费" value="EXPIRED"></el-option>
          <el-option label="未出租" value="IDLE"></el-option>
          <el-option label="已出租" value="OCCUPIED"></el-option>
        </el-select>
      </el-col>
      <el-col :span="2">
        <el-button size="mini" type="primary" round @click="load(page)">搜索</el-button>
      </el-col>
      <el-col :span="2">
        <el-button size="mini" type="primary" round @click="cleanFilter()">清空筛选</el-button>
      </el-col>
    </el-row>
    <el-table
      :data="apartments"
      border
      stripe
      style="width: 100%"
      height="420"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="30" align="center"></el-table-column>
      <el-table-column prop="name" label="房间名" width="100"></el-table-column>
      <el-table-column prop="price" label="价格" width="150"></el-table-column>
      <el-table-column prop="primaryCustomerName" label="客户姓名" width="115">
        <template v-if="scope.row.primaryCustomerName" slot-scope="scope">
          <el-popover placement="top">
            <el-tag
              slot="reference"
              size="medium"
              @click="displayCustomerInfo(scope.row)"
            >{{ scope.row.primaryCustomerName }}</el-tag>
            <customer-info-popover
              v-if="scope.row.seenCumtomerPopover"
              :customerId="scope.row.primaryCustomerId"
            ></customer-info-popover>
          </el-popover>
        </template>
      </el-table-column>
      <el-table-column label="到租日期" width="150">
        <template v-if="scope.row.toDate != null" slot-scope="scope">
          <i class="el-icon-date"></i>
          <span style="margin-left: 10px">{{ scope.row.toDate }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="premiums" label="计数" width="150"></el-table-column>
      <el-table-column prop="unsettledPrice" label="结余" width="130"></el-table-column>
      <el-table-column align="center" label="操作">
        <template slot-scope="scope">
          <el-tooltip class="item" effect="dark" content="编辑公寓信息" placement="top-start">
            <el-button
              type="primary"
              icon="el-icon-edit"
              round
              size="mini"
              @click="edit(scope.row.apartmentId)"
            ></el-button>
          </el-tooltip>

          <el-tooltip
            v-if="scope.row.status == 'IDLE'"
            class="item"
            effect="dark"
            content="入住登记"
            placement="top-start"
          >
            <el-button
              size="mini"
              type="primary"
              icon="el-icon-s-home"
              round
              @click="register(scope.row.apartmentId, scope.row.name)"
            ></el-button>
          </el-tooltip>

          <el-tooltip
            v-if="scope.row.status == 'OCCUPIED'"
            class="item"
            effect="dark"
            content="续费"
            placement="top-start"
          >
            <el-button
              size="mini"
              type="primary"
              icon="el-icon-s-finance"
              round
              @click="renew(scope.row.apartmentId, scope.row.primaryCustomerId, scope.row.name)"
            ></el-button>
          </el-tooltip>

          <el-tooltip
            v-if="scope.row.status == 'OCCUPIED'"
            class="item"
            effect="dark"
            content="退房"
            placement="top-start"
          >
            <el-button
              size="mini"
              type="primary"
              icon="el-icon-s-release"
              round
              @click="exit(scope.row.apartmentId, scope.row.name)"
            ></el-button>
          </el-tooltip>

          <el-tooltip
            v-if="scope.row.status == 'IDLE'"
            class="item"
            effect="dark"
            content="删除公寓"
            placement="top-start"
          >
            <el-button
              size="mini"
              type="danger"
              icon="el-icon-delete"
              round
              @click="removeApartment(scope.row.apartmentId)"
            ></el-button>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="displayRegistryForm">
      <el-dialog :title="dialogTitle" :visible.sync="displayRegistryForm">
        <customer-registry-form :apartmentId="selectedId"></customer-registry-form>
      </el-dialog>
    </div>
    <div v-if="displayRenewalForm">
      <el-dialog :title="dialogTitle" :visible.sync="displayRenewalForm">
        <customer-renewal-form :apartmentId="selectedId" :customerId="customerId"></customer-renewal-form>
      </el-dialog>
    </div>
    <div v-if="displayExitForm">
      <el-dialog :title="dialogTitle" :visible.sync="displayExitForm">
        <customer-exit-form :apartmentId="selectedId"></customer-exit-form>
      </el-dialog>
    </div>
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
import CustomerRegistryForm from "@/components/customers/customer-registry-form.vue";
import CustomerRenewalForm from "@/components/customers/customer-renewal-form.vue";
import CustomerExitForm from "@/components/customers/customer-exit-form.vue";
import CustomerInfoPopover from "@/components/customers/customer-info-popover.vue";

export default {
  name: "apartment-list",
  components: {
    CustomerRegistryForm,
    CustomerRenewalForm,
    CustomerExitForm,
    CustomerInfoPopover
  },
  inject: ["reload"],
  data() {
    return {
      suggestions: [],
      loading: true,
      name: null,
      status: null,
      selectedId: 0,
      customerId: 0,
      dialogTitle: "入住登记",
      isRouterAlive: true,
      displayRegistryForm: false,
      displayRenewalForm: false,
      displayExitForm: false,
      total: 1,
      page: 1,
      pageSize: 5,
      apartments: [
        {
          apartmentId: 1,
          toDate: "2016-01-01",
          name: "202",
          price: 1000,
          status: "IDLE",
          primaryCustomerName: "李四",
          premiums: "电表: 2909.9",
          unsettledPrice: "0"
        }
      ]
    };
  },
  mounted() {
    this.load(this.page);
  },
  methods: {
    load(page) {
      this.$api
        .get("/apartments", {
          page: page,
          pageSize: this.pageSize,
          name: this.name,
          status: this.status
        })
        .then(response => {
          const that = this;
          this.total = response.data.total;
          this.apartments = response.data.content.map(a => {
            return {
              apartmentId: a.apartmentId,
              name: a.name,
              status: a.status,
              toDate: a.latestPayment.toDate,
              price: that.priceToDisplay(a),
              primaryCustomerId: a.primaryCustomerId,
              primaryCustomerName: a.primaryCustomerName,
              unsettledPrice: that.unsettledPrice(a),
              premiums: that.premiumsToDisplay(a)
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
    register(apartmentId, apartmentName) {
      this.selectedId = apartmentId;
      this.dialogTitle = apartmentName + "入住登记";
      this.displayRegistryForm = true;
    },
    renew(apartmentId, primaryCustomerId, apartmentName) {
      this.customerId = primaryCustomerId;
      this.selectedId = apartmentId;
      this.dialogTitle = apartmentName + "续费";
      this.displayRenewalForm = true;
    },
    exit(apartmentId, apartmentName) {
      this.selectedId = apartmentId;
      this.dialogTitle = apartmentName + "退房";
      this.displayExitForm = true;
    },
    edit(apartmentId) {
      this.$router.push("/apartmentForm/" + apartmentId);
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
    unsettledPrice(a) {
      let unsettledPrice = 0;
      if (a.latestPayment.extension != null) {
        unsettledPrice =
          a.latestPayment.extension.receipts -
          a.latestPayment.extension.totalPrice;
      }
      if (Math.round(unsettledPrice) === 0) {
        return (unsettledPrice = "-");
      } else {
        return unsettledPrice < 0
          ? "未结清" + Math.round(0 - unsettledPrice) + "元"
          : "应扣除" + Math.round(unsettledPrice) + "元";
      }
    },
    handleSelectionChange(val) {
      console.log(val);
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

    removeApartment(id) {
      const that = this;
      this.$confirm("此操作将删除该信息, 是否继续?", "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      })
        .then(() => {
          that.$api
            .delete("/apartments/" + id)
            .then(function(res) {
              that.$message({
                message: "删除成功",
                type: "success"
              });
              that.reload();
            })
            .catch(function(err) {
              that.$message.error("删除失败，请联系管理员");
            });
        })
        .catch(() => {
          that.$message({
            type: "info",
            message: "已取消删除"
          });
        });
    },

    premiumsToDisplay(a) {
      let premiums = "";
      if (a.latestPayment.extension != null) {
        //Load premiums from latest payment
        a.latestPayment.extension.premiumPayments.forEach(
          p => (premiums += this.premiumToDisplay(p))
        );
      } else if (a.premiums != null) {
        //Load premiums from apartment extension
        a.premiums.forEach(p => (premiums += this.premiumToDisplay(p)));
      }
      return premiums;
    },
    premiumToDisplay(p) {
      let val = "";
      if (p.premiumFlag.indexOf("E_") >= 0) {
        val =
          p.premiumFlag.replace("E_", "电表") +
          "：" +
          p.currentMeasurement +
          "\n";
      } else if (p.premiumFlag.indexOf("W_") >= 0) {
        val =
          p.premiumFlag.replace("W_", "水表") +
          "：" +
          p.currentMeasurement +
          "\n";
      }
      return val;
    },
    displayCustomerInfo(row) {
      this.$set(row, "seenCumtomerPopover", true);
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
