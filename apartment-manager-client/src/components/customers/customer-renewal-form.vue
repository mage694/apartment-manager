<template>
  <el-form v-loading="loading" :label-position="labelAlign">
    <el-form-item label="价格">
      <span>{{ apartmentInfo.concertedPrice }}元/{{ paymentTypeLabel }}</span>
    </el-form-item>
    <el-form-item v-if="seenUnsettledPrice" :label="unsettledPriceLabel">
      <el-input-number :value="unsettledPrice" disabled></el-input-number>
    </el-form-item>
    <el-form-item :label="'支付' + paymentTypeLabel + '数'">
      <el-input-number v-model="form.quantity" @change="calculateTotal" :min="1" :max="12"></el-input-number>
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
    <el-form-item label="水电费">
      <span>{{ premiumTotal }}元</span>
    </el-form-item>
    <el-form-item label="实收款">
      <el-input v-model="form.receipts" type="number"></el-input>
    </el-form-item>
    <el-button type="primary" @click="renew">续租</el-button>
  </el-form>
</template>
<script>
export default {
  name: "customer-renewal-form",
  inject: ["reload"],
  props: {
    apartmentId: {
      type: Number,
      required: true
    },
    customerId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      labelAlign: "right",
      premiumTotal: 0,
      loading: true,
      apartmentInfo: {
        latestPayment: {
          extension: {}
        }
      },
      form: {
        quantity: 1,
        receipts: 0,
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
      this.form.premiums = this.apartmentInfo.latestPayment.extension.premiumPayments.map(
        p => {
          return {
            premiumFlag: p.premiumFlag,
            paymentType: p.paymentType == null ? (p.hasOwnProperty("currentMeasurement") ? "S" : "M") : p.paymentType,
            premiumType: p.premiumType == null ? (p.premiumFlag.indexOf("E_") >= 0 ? "E" : "W") : p.premiumType, 
            unitPrice: p.unitPrice,
            currentMeasurement: p.currentMeasurement,
            expiredDate: p.toDate
          };
        }
      );
      this.form.concertedPrice = response.data.concertedPrice;
      this.calculateTotal();
      this.loading = false;
    });
  },
  methods: {
    renew() {
      this.$api
        .put(
          "/apartments/" +
            this.apartmentId +
            "/pay?customerId=" +
            this.customerId,
          this.form
        )
        .then(res => {
          this.$message({
            message: "缴费成功",
            type: "success"
          });
          this.reload();
        })
        .catch(err => {
          this.$message.error("缴费失败，请联系管理员:" + err);
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
      let premium = 0;
      this.form.premiums.forEach(p => {
        this.apartmentInfo.latestPayment.extension.premiumPayments.forEach(
          lp => {
            if (p.premiumFlag === lp.premiumFlag) {
              if (lp.hasOwnProperty("currentMeasurement")) {
                premium += Math.round((p.currentMeasurement - lp.currentMeasurement) * lp.unitPrice);
              }
            }
          }
        );
      });
      this.premiumTotal = premium;
      this.form.receipts =
        Math.round(this.form.concertedPrice * this.form.quantity + this.unsettledPrice + this.premiumTotal);
    }
  }
};
</script>