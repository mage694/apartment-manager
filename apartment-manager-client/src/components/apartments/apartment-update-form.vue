<template>
  <el-form
    :model="apartmentForm"
    :rules="rules"
    ref="apartmentForm"
    label-width="100px"
    v-loading="loading"
  >
    <el-row>
      <el-upload
        list-type="picture-card"
        :action="fileUploadUrl"
        :show-file-list="true"
        :file-list="imgList"
        :before-upload="beforeFileUpload"
        :http-request="upload"
        :on-preview="previewImage"
        :on-remove="removeImage"
      >
        <i class="el-icon-plus avatar-uploader-icon"></i>
      </el-upload>
    </el-row>
    <el-dialog :visible.sync="imageDialogVisible">
      <img width="100%" v-if="imgUrl" :src="imgUrl">
    </el-dialog>
    <br>

    <el-form-item label="名称" prop="name">
      <el-col :span="21">
        <el-input v-model="apartmentForm.name"></el-input>
      </el-col>
    </el-form-item>
    <el-row v-if="status == 'IDLE'">
      <el-col :span="7">
        <el-form-item label="价格" prop="pricePerMonth">
          <el-input v-model="apartmentForm.pricePerMonth" placeholder="元/月"></el-input>
        </el-form-item>
      </el-col>
      <el-col :span="7">
        <el-form-item prop="pricePerWeek">
          <el-input v-model="apartmentForm.pricePerWeek" placeholder="元/周"></el-input>
        </el-form-item>
      </el-col>
      <el-col :span="7">
        <el-form-item prop="pricePerDay">
          <el-input v-model="apartmentForm.pricePerDay" placeholder="元/天"></el-input>
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item v-if="status =='OCCUPIED'" label="押金" prop="deposit">
      <el-col :span="21">
        <el-input v-model="apartmentForm.deposit"></el-input>
      </el-col>
    </el-form-item>

    <el-form-item
      v-for="(measurement, index) in eleMeasurements"
      :label="'电表' + (index + 1)"
      :key="measurement.key"
      :rules="{required: true, message: '刻度为空', trigger: 'blur'}"
    >
      <el-row>
        <el-col :span="10">
          <el-input type="number" v-model="measurement.currentMeasurement" min="0" placeholder="刻度"></el-input>
        </el-col>
        <el-col :span="1">-</el-col>
        <el-col :span="10">
          <el-input v-model="measurement.unitPrice" type="number" min="0" placeholder="单价"></el-input>
        </el-col>
        <el-col :span="3">
          <el-button
            @click.prevent="removeEleMeasurement(measurement)"
            type="danger"
            icon="el-icon-delete"
            circle
          ></el-button>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item>
      <el-col :span="7">
        <el-button @click="addEleMeasurement" type="success" icon="el-icon-plus" round>电表</el-button>
      </el-col>
    </el-form-item>
    <el-form-item
      v-for="(measurement, index) in waterMeasurements"
      :label="'水表' + (index + 1)"
      :key="measurement.key"
      :rules="{required: true, message: '刻度为空', trigger: 'blur'}"
    >
      <el-row>
        <el-col :span="10">
          <el-input v-model="measurement.currentMeasurement" min="0" type="number" placeholder="刻度"></el-input>
        </el-col>
        <el-col :span="1">-</el-col>
        <el-col :span="10">
          <el-input v-model="measurement.unitPrice" min="0" type="number" placeholder="单价"></el-input>
        </el-col>
        <el-col :span="3">
          <el-button
            @click.prevent="removeWaterMeasurement(measurement)"
            type="danger"
            icon="el-icon-delete"
            circle
          ></el-button>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item>
      <el-col :span="7">
        <el-button @click="addWaterMeasurement" type="success" icon="el-icon-plus" round>水表</el-button>
      </el-col>
    </el-form-item>
    <el-form-item v-if="status == 'OCCUPIED'" label="到期时间" prop="toDate">
      <el-col :span="5">
        <el-date-picker v-model="apartmentForm.toDate" type="date" value-format="yyyy-MM-dd"></el-date-picker>
      </el-col>
    </el-form-item>
    <el-form-item label="描述" prop="desc">
      <el-col :span="21">
        <el-input type="textarea" v-model="apartmentForm.description"></el-input>
      </el-col>
    </el-form-item>
    <el-form-item>
      <el-col :span="21">
        <el-button type="primary" @click="submitForm('apartmentForm')">更新</el-button>
        <el-button @click="resetForm('apartmentForm')">重置</el-button>
      </el-col>
    </el-form-item>
  </el-form>
</template>
<script>
import FileUtil from "../../js/utils/file-util";

export default {
  data() {
    return {
      loading: false,
      imgUrl: "",
      imgList: [],
      status: "",
      fileUploadUrl: process.env.BASE_URL + "/files",
      module: "apartment",
      imageDialogVisible: false,
      eleMeasurementIndexCounter: 1,
      waterMeasurementIndexCounter: 1,
      apartmentForm: {
        name: "",
        pricePerDay: 0,
        pricePerWeek: 0,
        pricePerMonth: 0,
        deposit: null,
        premiums: [],
        fileIds: [],
        toDate: null,
        description: ""
      },
      rules: {
        name: [
          { required: true, message: "请输入房间名称", trigger: "blur" },
          { max: 20, message: "长度小于20个字符", trigger: "blur" }
        ],
        toDate: [
          { required: true, message: "请输入到期时间", trigger: "blur" }
        ],
        pricePerMonth: [
          { required: true, message: "请输入月价格", trigger: "blur" }
        ]
      }
    };
  },
  computed: {
    eleMeasurements() {
      return this.apartmentForm.premiums.filter(
        p => p.premiumFlag.indexOf("E_") === 0
      );
    },
    waterMeasurements() {
      return this.apartmentForm.premiums.filter(
        p => p.premiumFlag.indexOf("W_") === 0
      );
    }
  },
  mounted() {
    this.loading = true;
    this.$api.get("/apartments/" + this.$route.params.id).then(response => {
      this.apartmentForm.name = response.data.name;
      this.status = response.data.status;
      this.apartmentForm.description = response.data.description;
      this.apartmentForm.deposit = response.data.deposit;
      this.imgList = response.data.files.map(f => {
        return {
          id: f.id,
          name: f.fileName,
          url: process.env.FILE_MANAGER_URL + "/" + f.location
        };
      });
      this.apartmentForm.fileIds = response.data.files.map(f => f.id);

      if (this.status === "IDLE") {
        const prices = response.data.prices;
        for (const key in prices) {
          const price = prices[key];
          switch (key) {
            case "PER_MONTH":
              this.apartmentForm.pricePerMonth = price;
              break;
            case "PER_WEEK":
              this.apartmentForm.pricePerWeek = price;
              break;
            case "PER_DAY":
              this.apartmentForm.pricePerDay = price;
              break;
          }
        }
      }

      if (response.data.latestPayment != null) {
        this.apartmentForm.toDate = response.data.latestPayment.toDate;
        this.apartmentForm.premiums = response.data.latestPayment.extension.premiumPayments.map(
          p => {
            return {
              premiumFlag: p.premiumFlag,
              unitPrice: p.unitPrice,
              currentMeasurement: p.currentMeasurement
            };
          }
        );
      } else if (response.data.premiums != null) {
        this.apartmentForm.premiums = response.data.premiums;
      }

      this.apartmentForm.premiums.forEach(p => {
        this.$set(p, "paymentType", p.hasOwnProperty("currentMeasurement") ? "S" : "M");
        if (p.premiumFlag.indexOf("E_") >= 0) {
          this.$set(p, "premiumType", "E");
        } else {
          this.$set(p, "premiumType", "W");
        }
      });
      this.eleMeasurementIndexCounter = this.eleMeasurements.length;
      this.waterMeasurementIndexCounter = this.waterMeasurements.length;
      this.loading = false;
    });
  },
  methods: {
    beforeFileUpload(file) {
      return FileUtil.validateImg(file);
    },
    previewImage(file) {
      this.imgUrl = file.url;
      this.imageDialogVisible = true;
    },
    upload(param) {
      const fileForm = new FormData();
      fileForm.append("file", param.file);
      fileForm.append("module", this.module);
      FileUtil.upload(fileForm, this.uploadSuccessfully);
    },
    uploadSuccessfully(response) {
      this.imgList.push({
        id: response.data.id,
        name: response.data.fileName,
        url: process.env.FILE_MANAGER_URL + "/" + response.data.location
      });
      this.apartmentForm.fileIds.push(response.data.id);
    },
    removeImage(file) {
      FileUtil.removeFile(file, resp => {
        const index = this.apartmentForm.fileIds.indexOf(file.id);
        if (index > -1) {
          this.apartmentForm.fileIds.splice(index, 1);
        }
      });
    },
    submitForm(formName) {
      const that = this;
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.loading = true;
          this.$api
            .put("/apartments/" + this.$route.params.id, this.apartmentForm)
            .then(function(res) {
              that.$message({
                message: "更新成功",
                type: "success"
              });
              that.$router.push("/apartments");
            })
            .catch(function(err) {
              that.$message.error("更新失败，请联系管理员. " + err);
              that.loading = false;
            });
        } else {
          return false;
        }
      });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
    },
    removeEleMeasurement(item) {
      var index = this.apartmentForm.premiums.indexOf(item);
      if (index !== -1) {
        this.eleMeasurementIndexCounter--;
        this.apartmentForm.premiums.splice(index, 1);
      }
    },
    addEleMeasurement() {
      const key = "E_" + ++this.eleMeasurementIndexCounter;
      this.apartmentForm.premiums.push({
        paymentType: "S",
        premiumType: "E",
        currentMeasurement: 0,
        premiumFlag: key
      });
    },
    removeWaterMeasurement(item) {
      var index = this.apartmentForm.premiums.indexOf(item);
      if (index !== -1) {
        this.waterMeasurementIndexCounter--;
        this.apartmentForm.premiums.splice(index, 1);
      }
    },
    addWaterMeasurement() {
      const key = "W_" + ++this.waterMeasurementIndexCounter;
      this.apartmentForm.premiums.push({
        paymentType: "S",
        premiumType: "W",
        currentMeasurement: 0,
        premiumFlag: key
      });
    }
  }
};
</script>