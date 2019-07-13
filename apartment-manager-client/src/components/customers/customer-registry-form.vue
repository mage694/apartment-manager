<template>
  <el-form
    v-loading="loading"
    :element-loading-text="loadingText"
    :rules="rules"
    :model="form"
    ref="registoryForm"
    label-position="right"
    label-width="80px"
  >
    <el-tabs type="border-card" v-model="tabView">
      <el-tab-pane label="房间信息" name="apartment-info-tab">
        <el-form-item label="支付形式">
          <el-radio-group v-model="form.paymentType" @change="chosePaymentType">
            <el-radio
              :key="key"
              v-for="(price, key) in apartmentInfo.prices"
              :label="paymentTypeCode(key)"
            >{{paymentTypeLabel(key)}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="价格" prop="concertedPrice">
          <el-input v-model.number="form.concertedPrice" @change="calculateTotal" type="number"></el-input>
        </el-form-item>
        <el-form-item label="押金" prop="deposit">
          <el-input v-model.number="form.deposit" @change="calculateTotal" type="number"></el-input>
        </el-form-item>
        <el-form-item label="实收款" prop="receipts">
          <el-input v-model.number="form.receipts" type="number"></el-input>
        </el-form-item>
        <measurement-form-item
          :key="premium.premiumFlag"
          v-for="premium in form.premiums"
          :premium="premium"
        ></measurement-form-item>
        <el-form-item label="入住日期" prop="enrollDate" required>
          <el-col :span="5">
            <el-date-picker
              v-model="form.enrollDate"
              type="date"
              value-format="yyyy-MM-dd"
              placeholder="入住日期"
            ></el-date-picker>
          </el-col>
        </el-form-item>
        <el-form-item label="支付数量" prop="quantity">
          <el-col :span="7">
            <el-input-number
              v-model.number="form.quantity"
              @change="calculateTotal"
              :min="1"
              :max="12"
            ></el-input-number>
          </el-col>
        </el-form-item>
        <el-button type="primary" @click="nextStep">下一步</el-button>
      </el-tab-pane>
      <el-tab-pane label="客户信息" name="customer-info-tab">
        <el-upload
          list-type="picture-card"
          :action="fileUploadUrl"
          name="file"
          :show-file-list="true"
          :file-list="imgList"
          :before-upload="beforeFileUpload"
          :http-request="upload"
          :on-preview="previewImage"
          :on-remove="removeImage"
        >
          <i class="el-icon-plus"></i>
        </el-upload>
        <el-dialog :visible.sync="imageDialogVisible">
          <img width="100%" v-if="imgUrl" :src="imgUrl">
        </el-dialog>
        <br>
        <el-form-item label="身份证号" required>
          <el-input v-model="form.customers[0].idNum"></el-input>
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="form.customers[0].name"></el-input>
        </el-form-item>
        <el-form-item label="性别" required>
          <el-radio-group v-model="form.customers[0].gender">
            <el-radio label="MALE">男</el-radio>
            <el-radio label="FEMALE">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="电话" required>
          <el-input v-model="form.customers[0].contacts.P"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="register">登记</el-button>
          <el-button>取消</el-button>
        </el-form-item>
      </el-tab-pane>
    </el-tabs>
  </el-form>
</template>
<script>
import FileUtil from "../../js/utils/file-util";
import MeasurementFormItem from "@/components/apartments/measurement-form-item.vue";

export default {
  name: "customer-registry-form",
  inject: ["reload"],
  props: {
    apartmentId: {
      type: Number,
      required: true
    }
  },
  components: { MeasurementFormItem },
  computed: {
    fileUploadUrl() {
      if (this.form.customers[0].idNum.trim() === "") {
        return process.env.BASE_URL + "/files/idCard/upload";
      } else {
        return process.env.BASE_URL + "/files";
      }
    }
  },
  data() {
    return {
      file: null,
      loading: true,
      loadingText: "",
      imageDialogVisible: false,
      tabView: "apartment-info-tab",
      apartmentInfo: {
        prices: {}
      },
      imgUrl: "",
      imgList: [],
      form: {
        concertedPrice: 0,
        enrollDate: new Date(),
        deposit: 0,
        paymentType: "",
        quantity: 1,
        receipts: 0,
        customers: [
          {
            idNum: "",
            name: "",
            gender: "",
            isPrimary: true,
            fileIds: [],
            contacts: {
              P: "13800000"
            }
          }
        ],
        premiums: []
      },
      rules: {
        concertedPrice: [
          {
            required: true,
            message: "请输入价格",
            trigger: "blur",
            type: "number"
          },
          { min: 1, message: "最小为1", trigger: "blur", type: "number" }
        ],
        deposit: [
          {
            required: true,
            message: "请输入押金",
            trigger: "blur",
            type: "number"
          },
          { min: 1, message: "最小为1", trigger: "blur", type: "number" }
        ],
        quantity: [
          {
            required: true,
            message: "请输入支付数",
            trigger: "blur",
            type: "number"
          },
          { min: 1, message: "最小为1", trigger: "blur", type: "number" }
        ],
        receipts: [
          {
            required: true,
            message: "请输入实收款",
            trigger: "blur",
            type: "number"
          },
          { min: 1, message: "最小为1", trigger: "blur", type: "number" }
        ]
      }
    };
  },
  mounted() {
    this.$api.get("/apartments/" + this.apartmentId).then(response => {
      this.apartmentInfo = response.data;
      if (this.apartmentInfo.premiums != null) {
        this.form.premiums = this.apartmentInfo.premiums.map(this.parsePremium);
      }
      this.choseDefaultPaymentType(this.apartmentInfo.prices);
      this.calculateTotal();
      this.loading = false;
    });
  },
  methods: {
    beforeFileUpload(file) {
      const result = FileUtil.validateImg(file);
      if (result && this.form.customers[0].idNum === "") {
        this.loading = true;
        this.loadingText = "解析中";
      }
      return result;
    },
    upload(param) {
      let callback = this.parseSuccessfully;
      const form = new FormData();
      form.append("file", param.file);
      if (!this.fileUploadUrl.includes("/idCard/")) {
        form.append("module", "customer");
        callback = this.uploadSuccessfully;
      }
      this.$api.post(this.fileUploadUrl, form)
        .then(callback)
        .catch(err => console.log("上传失败：" + err));
    },
    parseSuccessfully(response) {
      this.form.customers[0].idNum = response.data.idNumber;
      this.form.customers[0].name = response.data.name;
      this.form.customers[0].gender =
        response.data.gender == "男" ? "MALE" : "FEMALE";
      this.form.customers[0].fileIds.push(response.data.imgUuid);
      this.imgList.push({
        id: response.data.imgUuid,
        name: response.data.name,
        url: process.env.FILE_MANAGER_URL + "/" + response.data.uri
      });
      this.loading = false;
      this.loadingText = "";
    },
    uploadSuccessfully(response) {
      this.imgList.push({
        id: response.data.id,
        name: response.data.fileName,
        url: process.env.FILE_MANAGER_URL + "/" + response.data.location
      });
      this.form.customers[0].fileIds.push(response.data.id);
    },
    removeImage(file) {
      FileUtil.removeFile(file, resp => {
        const index = this.form.customers[0].fileIds.indexOf(file.id);
        if (index > -1) {
          this.form.customers[0].fileIds.splice(index, 1);
        }
      });
    },
    parsePremium(p) {
      if (p.hasOwnProperty("currentMeasurement")) {
        return {
          premiumFlag: p.premiumFlag,
          paymentType: "S",
          premiumType: p.premiumType == "WATER" ? "W" : "E",
          unitPrice: p.unitPrice,
          currentMeasurement: p.currentMeasurement,
          selected: true
        };
      } else {
        return {
          premiumFlag: p.premiumFlag,
          paymentType: "M",
          premiumType: "M",
          unitPrice: p.unitPrice,
          expiredDate: p.expiredDate,
          selected: true
        };
      }
    },
    nextStep() {
      this.tabView = "customer-info-tab";
    },
    register() {
      this.$refs["registoryForm"].validate(valid => {
        if (valid) {
          this.loading = true;
          this.$api
            .put("/apartments/" + this.apartmentId + "/enroll", this.form)
            .then(res => {
              this.$message({
                message: "登记成功",
                type: "success"
              });
              this.reload();
            })
            .catch(err => {
              this.$message.error("登记失败，请联系管理员:" + err);
            });
        } else {
          return false;
        }
      });
    },
    choseDefaultPaymentType(prices) {
      let paymentType = "";
      if (prices.hasOwnProperty("PER_MONTH")) {
        paymentType = "M";
      } else if (prices.hasOwnProperty("PER_DAY")) {
        paymentType = "D";
      } else if (prices.hasOwnProperty("PER_WEEK")) {
        paymentType = "W";
      }
      this.form.paymentType = paymentType;
      this.chosePaymentType(paymentType);
    },
    paymentTypeCode(val) {
      switch (val) {
        case "PER_MONTH":
          return "M";
        case "PER_WEEK":
          return "W";
        case "PER_DAY":
          return "D";
      }
    },
    paymentTypeLabel(val) {
      switch (val) {
        case "PER_MONTH":
          return "月付";
        case "PER_WEEK":
          return "周付";
        case "PER_DAY":
          return "天付";
      }
    },
    chosePaymentType(val, prices) {
      switch (val) {
        case "M":
          this.form.deposit = this.apartmentInfo.prices["PER_MONTH"];
          this.form.concertedPrice = this.apartmentInfo.prices["PER_MONTH"];
          break;
        case "W":
          this.form.deposit = this.apartmentInfo.prices["PER_WEEK"];
          this.form.concertedPrice = this.apartmentInfo.prices["PER_WEEK"];
          break;
        case "D":
          this.form.deposit = this.apartmentInfo.prices["PER_DAY"];
          this.form.concertedPrice = this.apartmentInfo.prices["PER_DAY"];
          break;
      }
      this.calculateTotal();
    },
    calculateTotal() {
      this.form.receipts =
        this.form.concertedPrice * this.form.quantity +
        parseInt(this.form.deposit);
    },
    previewImage(file) {
      this.imgUrl = file.url;
      this.imageDialogVisible = true;
    }
  }
};
</script>