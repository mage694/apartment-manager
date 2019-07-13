<template>
  <el-form label-position="left" inline class="table-expand">
    <el-form-item label="房价" v-if="!fix">
      <span>{{ row.unitPrice }} * {{ row.quantity }} = {{ row.unitPrice * row.quantity }}元</span>
    </el-form-item>
    <div v-for="premium in form.premiums" :key="premium.premiumFlag">
      <el-form-item v-if="premium.premiumTotal > 0" :label="premiumPaymentDetailLabel(premium)">
        <el-row v-if="fix">
          <el-col :span="7">
            <el-input
              @change="calculateTotal"
              v-model="premium.currentMeasurement"
              size="small"
              min="0"
              type="number"
              placeholder="刻度"
            ></el-input>
          </el-col>
          <el-col :span="2">&nbsp;</el-col>
          <el-col :span="7">
            <el-input
              v-model="premium.previousMeasurement"
              size="small"
              min="0"
              type="number"
              placeholder="刻度"
              disabled
            ></el-input>
          </el-col>
        </el-row>
        <span v-else>{{ premiumPaymentDetail(premium) }}</span>
      </el-form-item>
    </div>
    <el-form-item label="总价">
      <el-input v-if="fix" v-model="form.totalPrice" size="small" diabled></el-input>
      <span v-else>{{ form.totalPrice }}元</span>
    </el-form-item>
    <el-form-item label="实收">
      <el-input v-if="fix" v-model="form.receipts" size="small"></el-input>
      <span v-else>{{ form.receipts }}元</span>
    </el-form-item>
    <el-form-item>
      <el-row>
        <el-col>
          <el-button v-if="!fix" type="primary" size="small" @click="fix=true">更正</el-button>
          <el-button v-if="fix" type="primary" size="small" @click="update()">确定</el-button>
          <el-button v-if="fix" size="small" @click="fix=false">取消</el-button>
        </el-col>
      </el-row>
    </el-form-item>
  </el-form>
</template>
<style>
.table-expand {
  font-size: 0;
}
.table-expand label {
  width: 90px;
  color: #99a9bf;
}
.table-expand .el-form-item {
  margin-right: 0;
  margin-bottom: 0;
  width: 100%;
}
</style>
<script>
export default {
  inject: ["reload"],
  props: {
    row: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      fix: false,
      form: {
        totalPrice: 0,
        receipts: 0,
        premiums: []
      }
    };
  },

  mounted() {
    this.form.totalPrice = this.row.totalPrice;
    this.form.receipts = this.row.receipts;
    this.form.premiums = this.row.premiums.map(p => {
      return {
        premiumFlag: p.premiumFlag,
        paymentType:
          p.paymentType == null
            ? p.hasOwnProperty("currentMeasurement")
              ? "S"
              : "M"
            : p.paymentType,
        premiumType:
          p.premiumType == null
            ? p.premiumFlag.indexOf("E_") >= 0
              ? "E"
              : "W"
            : p.premiumType,
        unitPrice: p.unitPrice,
        currentMeasurement: p.currentMeasurement,
        previousMeasurement: p.previousMeasurement,
        expiredDate: p.toDate,
        premiumTotal: p.premiumTotal
      };
    });
  },

  methods: {
    update() {
      this.$api
        .put("/payments/" + this.row.id, this.form)
        .then(res => {
          this.$message({
            message: "更新成功",
            type: "success"
          });
          this.reload();
        })
        .catch(err => {
          this.$message.error("更新失败，请联系管理员. " + err);
          this.loading = false;
        });
    },
    calculateTotal() {
      let premiumTotal = 0;
      this.form.premiums.forEach(p => {
        premiumTotal += Math.round(
          (p.currentMeasurement - p.previousMeasurement) * p.unitPrice
        );
      });
      this.form.totalPrice = Math.round(
        this.row.unitPrice * this.row.quantity + premiumTotal
      );
      this.form.receipts = this.form.totalPrice;
    },
    premiumPaymentDetailLabel(p) {
      if (p.premiumFlag.indexOf("E_") >= 0) {
        return p.premiumFlag.replace("E_", "电表");
      } else if (p.premiumFlag.indexOf("W_") >= 0) {
        return p.premiumFlag.replace("W_", "水表");
      }
    },
    premiumPaymentDetail(p) {
      let val = "";
      if (p.premiumFlag.indexOf("E_") >= 0) {
        val =
          p.currentMeasurement +
          " - " +
          p.previousMeasurement +
          " = " +
          (p.currentMeasurement - p.previousMeasurement) +
          " * " +
          p.unitPrice +
          " = " +
          p.premiumTotal +
          "元";
      } else if (p.premiumFlag.indexOf("W_") >= 0) {
        val =
          p.currentMeasurement +
          " - " +
          p.previousMeasurement +
          " = " +
          (p.currentMeasurement - p.previousMeasurement) +
          " * " +
          p.unitPrice +
          " = " +
          p.premiumTotal +
          "元";
      }
      return val;
    }
  }
};
</script>
