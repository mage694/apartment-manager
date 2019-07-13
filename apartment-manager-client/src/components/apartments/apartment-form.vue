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
    <el-row>
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
    <el-form-item
      v-for="(measurement, index) in apartmentForm.eleMeasurements"
      :label="'电表' + (index + 1)"
      :key="measurement.key"
      :prop="'eleMeasurements.' + index + '.value'"
      :rules="{required: true, message: '刻度为空', trigger: 'blur'}"
    >
      <el-row>
        <el-col :span="10">
          <el-input type="number" v-model="measurement.value" min="0" placeholder="刻度"></el-input>
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
      v-for="(measurement, index) in apartmentForm.waterMeasurements"
      :label="'水表' + (index + 1)"
      :key="measurement.key"
      :prop="'waterMeasurements.' + index + '.value'"
      :rules="{required: true, message: '刻度为空', trigger: 'blur'}"
    >
      <el-row>
        <el-col :span="10">
          <el-input v-model="measurement.value" min="0" type="number" placeholder="刻度"></el-input>
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
    <el-form-item label="描述" prop="desc">
      <el-col :span="20">
        <el-input type="textarea" v-model="apartmentForm.description"></el-input>
      </el-col>
    </el-form-item>
    <el-form-item>
      <el-col :span="20">
        <el-button type="primary" @click="submitForm('apartmentForm')">立即创建</el-button>
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
      fileUploadUrl: process.env.BASE_URL + "/files",
      module: "apartment",
      imageDialogVisible: false,
      eleMeasurementIndexCounter: 1,
      waterMeasurementIndexCounter: 1,
      apartmentForm: {
        name: "",
        pricePerDay: "",
        pricePerWeek: "",
        pricePerMonth: "",
        eleMeasurements: [
          {
            key: "E_1",
            value: "",
            unitPrice: 0.8
          }
        ],
        waterMeasurements: [
          {
            key: "W_1",
            value: "",
            unitPrice: 7
          }
        ],
        premiums: [],
        fileIds: [],
        description: ""
      },
      rules: {
        name: [
          { required: true, message: "请输入房间名称", trigger: "blur" },
          { max: 20, message: "长度小于20个字符", trigger: "blur" }
        ],
        pricePerMonth: [
          { required: true, message: "请输入月价格", trigger: "blur" }
        ]
      }
    };
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
          this.processFormData();
          this.loading = true;
          this.$api
            .post("/apartments", this.apartmentForm)
            .then(function(res) {
              that.$message({
                message: "添加成功",
                type: "success"
              });
              that.$router.push("/apartments");
            })
            .catch(function(err) {
              that.$message.error("添加失败，请联系管理员");
              that.loading = false;
            });
        } else {
          return false;
        }
      });
    },
    processFormData() {
      let premiums = [];
      this.apartmentForm.eleMeasurements.forEach(m => {
        premiums.push({
          paymentType: "S",
          premiumType: "E",
          premiumFlag: m.key,
          currentMeasurement: m.value,
          unitPrice: m.unitPrice
        });
      });
      delete this.apartmentForm.eleMeasurements;

      this.apartmentForm.waterMeasurements.forEach(m => {
        premiums.push({
          paymentType: "S",
          premiumType: "W",
          premiumFlag: m.key,
          currentMeasurement: m.value,
          unitPrice: m.unitPrice
        });
      });
      delete this.apartmentForm.waterMeasurements;

      this.apartmentForm.premiums = premiums;
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
    },
    removeEleMeasurement(item) {
      var index = this.apartmentForm.eleMeasurements.indexOf(item);
      if (index !== -1) {
        this.eleMeasurementIndexCounter--;
        this.apartmentForm.eleMeasurements.splice(index, 1);
      }
    },
    addEleMeasurement() {
      const key = "E_" + ++this.eleMeasurementIndexCounter;
      this.apartmentForm.eleMeasurements.push({
        value: "",
        key: key,
        unitPrice: 0.8
      });
    },
    removeWaterMeasurement(item) {
      var index = this.apartmentForm.waterMeasurements.indexOf(item);
      if (index !== -1) {
        this.waterMeasurementIndexCounter--;
        this.apartmentForm.waterMeasurements.splice(index, 1);
      }
    },
    addWaterMeasurement() {
      const key = "W_" + ++this.waterMeasurementIndexCounter;
      this.apartmentForm.waterMeasurements.push({
        value: "",
        key: key,
        unitPrice: 7
      });
    }
  }
};
</script>