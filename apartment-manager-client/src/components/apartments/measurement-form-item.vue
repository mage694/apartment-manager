<template>
  <el-row>
    <el-form-item :label="measurementLabel">
      <el-col :span="3">
        <el-switch v-model="premium.selected"></el-switch>
      </el-col>
      <el-col :span="8">
        <el-input v-if="premium.selected" v-model="premium.currentMeasurement"></el-input>
      </el-col>
      <el-col :span="2">&nbsp;&nbsp;&nbsp;单价</el-col>
      <el-col :span="8">
        <el-input v-if="premium.selected" v-model="premium.unitPrice"></el-input>
      </el-col>
    </el-form-item>
  </el-row>
</template>
<script>
export default {
  name: "measurement-form-item",
  props: {
    premium: {
      type: Object,
      required: true
    }
  },
  computed: {
    measurementLabel() {
      if (this.premium.premiumFlag.indexOf("E_") >= 0) {
        return this.premium.premiumFlag.replace("E_", "电表");
      } else if (this.premium.premiumFlag.indexOf("W_") >= 0) {
        return this.premium.premiumFlag.replace("W_", "水表");
      }
    }
  }
};
</script>
