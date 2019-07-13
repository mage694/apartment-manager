<template>
  <el-form label-position="right" v-loading="loading">
    <el-form-item v-if="seenUnsettledPrice" :label="unsettledPriceLabel">
      <el-input-number :value="unsettledPrice" disabled></el-input-number>
    </el-form-item>
    <el-form-item label="应补费用">
      <el-input-number v-model="form.compensation" type="number" @change="calculateTotal"></el-input-number>
    </el-form-item>
    <el-form-item label="押金">
      <el-input-number v-model="form.deposit" type="number" disabled></el-input-number>
    </el-form-item>
    <el-form-item
      :label="premiumLabel(latestPremium)"
      :key="latestPremium.premiumFlag"
      v-for="(latestPremium, index) in apartmentInfo.latestPayment.extension.premiumPayments"
    >
      <el-tooltip content="新数值">
        <el-input-number v-model="form.premiums[index].currentMeasurement" @change="calculateTotal"></el-input-number>
      </el-tooltip>
      <el-tooltip content="原数值">
        <el-input-number :value="latestPremium.currentMeasurement" disabled></el-input-number>
      </el-tooltip>
    </el-form-item>
    <el-form-item label="应退款">
      <el-input-number v-model="form.refund" type="number"></el-input-number>
    </el-form-item>
    <el-button type="primary" @click="exit">退房</el-button>
  </el-form>
</template>
<script>
export default {
  name: "customer-registry-form",
  inject: ["reload"],
  props: {
    apartmentId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      loading: true,
      apartmentInfo: {
        latestPayment: {
          extension: {}
        }
      },
      form: {
        deposit: 0,
        compensation: 0,
        refund: 0,
        premiums: [
          {
            premiumFlag: "",
            paymentType: "",
            premiumType: "",
            unitPrice: 0,
            currentMeasurement: 0,
            expiredDate: ""
          }
        ]
      }
    };
  },
  computed: {
    paymentTypeLabel() {
      switch (this.apartmentInfo.chosenPaymentType) {
        case "PER_MONTH":
          return "月";
        case "PER_WEEK":
          return "周";
        case "PER_DAY":
          return "天";
      }
    },
    seenUnsettledPrice() {
      return this.unsettledPrice != 0;
    },
    unsettledPrice() {
      return Math.round(
        this.apartmentInfo.latestPayment.extension.totalPrice -
          this.apartmentInfo.latestPayment.extension.receipts
      );
    },
    unsettledPriceLabel() {
      if (this.unsettledPrice > 0) {
        return "未结清款";
      } else {
        return "应扣除款";
      }
    }
  },
  mounted() {
    this.$api.get("/apartments/" + this.apartmentId).then(response => {
      this.apartmentInfo = response.data;
      this.form.deposit = this.apartmentInfo.deposit;
      this.form.refund = this.apartmentInfo.deposit;
      this.form.premiums = this.apartmentInfo.latestPayment.extension.premiumPayments.map(
        p => {
          return {
            paymentType: p.paymentType == null ? (p.hasOwnProperty("currentMeasurement") ? "S" : "M") : p.paymentType,
            premiumType: p.premiumType == null ? (p.premiumFlag.indexOf("E_") >= 0 ? "E" : "W") : p.premiumType, 
            premiumFlag: p.premiumFlag,
            unitPrice: p.unitPrice,
            currentMeasurement: p.currentMeasurement
          };
        }
      );
      this.form.concertedPrice = response.data.concertedPrice;
      this.calculateTotal();
      this.loading = false;
    });
  },
  methods: {
    exit() {
      this.$confirm("确认退房?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      })
        .then(() => {
          this.$api
            .put("/apartments/" + this.apartmentId + "/exit", this.form)
            .then(res => {
              this.$message({
                message: "退房成功",
                type: "success"
              });
              this.reload();
            })
            .catch(err => {
              this.$message.error("退房失败，请联系管理员:" + err);
            });
        })
        .catch(() => {
          this.$message({
            type: "info",
            message: "已取消"
          });
        });
    },
    premiumLabel(premium) {
      if (premium.premiumFlag.indexOf("E_") >= 0) {
        return premium.premiumFlag.replace("E_", "电表");
      } else if (premium.premiumFlag.indexOf("W_") >= 0) {
        return premium.premiumFlag.replace("W_", "水表");
      }
    },
    calculateTotal() {
      let premiumTotal = 0;
      this.form.premiums.forEach(p => {
        this.apartmentInfo.latestPayment.extension.premiumPayments.forEach(
          lp => {
            if (p.premiumFlag === lp.premiumFlag) {
              if (lp.hasOwnProperty("currentMeasurement")) {
                premiumTotal +=
                  (p.currentMeasurement - lp.currentMeasurement) * lp.unitPrice;
              }
            }
          }
        );
      });
      this.form.refund = Math.round(
        this.form.deposit -
          this.unsettledPrice -
          this.form.compensation -
          premiumTotal
      );
    }
  }
};
</script>