<template>
  <el-form
    :model="customerForm"
    :rules="rules"
    ref="customerForm"
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
    <el-form-item label="身份证号" prop="idNum">
      <el-input v-model="customerForm.idNum"></el-input>
    </el-form-item>
    <el-form-item label="名称" prop="name">
      <el-input v-model="customerForm.name"></el-input>
    </el-form-item>
    <el-form-item label="性别">
      <el-radio-group v-model="customerForm.gender">
        <el-radio label="MALE">男</el-radio>
        <el-radio label="FEMALE">女</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item>
      <el-date-picker
        v-model="customerForm.birthday"
        type="date"
        value-format="yyyy-MM-dd"
        placeholder="生日"
      ></el-date-picker>
    </el-form-item>
    <el-form-item label="电话">
      <el-input v-model="customerForm.contacts.P"></el-input>
    </el-form-item>

    <el-form-item>
      <el-col :span="20">
        <el-button type="primary" @click="submitForm('customerForm')">更新</el-button>
        <el-button @click="resetForm('customerForm')">重置</el-button>
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
      module: "customer",
      fileUploadUrl: process.env.BASE_URL + "/files",
      imageDialogVisible: false,
      customerForm: {
        idNum: null,
        name: null,
        birthday: "",
        gender: "MALE",
        fileIds: [],
        contacts: {}
      },
      rules: {
        name: [
          { required: true, message: "请输入客户名称", trigger: "blur" },
          { max: 20, message: "长度小于20个字符", trigger: "blur" }
        ]
      }
    };
  },
  mounted() {
    this.loading = true;
    this.$api.get("/customers/" + this.$route.params.id).then(response => {
      this.customerForm.idNum = response.data.idNum;
      this.customerForm.name = response.data.name;
      this.customerForm.gender = response.data.gender;
      this.customerForm.birthday = response.data.birthday;
      this.customerForm.contacts.P = response.data.contacts.forEach(c => {
        if ("PHONE" === c.contactType) {
          this.$set(this.customerForm.contacts, 'P', c.detail);
        }
      })
      this.imgList = response.data.files.map(f => {
        return {
          id: f.id,
          name: f.fileName,
          url: process.env.FILE_MANAGER_URL + "/" + f.location
        };
      });
      this.customerForm.fileIds = response.data.files.map(f => f.id);
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
      const form = new FormData();
      form.append("file", param.file);
      form.append("module", this.module);
      FileUtil.upload(form, this.uploadSuccessfully);
    },
    uploadSuccessfully(response) {
      this.imgList.push({
        id: response.data.id,
        name: response.data.fileName,
        url: process.env.FILE_MANAGER_URL + "/" + response.data.location
      });
      this.customerForm.fileIds.push(response.data.id);
    },
    removeImage(file) {
      FileUtil.removeFile(file, resp => {
        const index = this.customerForm.fileIds.indexOf(file.id);
        if (index > -1) {
          this.customerForm.fileIds.splice(index, 1);
        }
      });
    },
    submitForm(formName) {
      const that = this;
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.loading = true;
          this.$api
            .put("/customers/" + this.$route.params.id, this.customerForm)
            .then(function(res) {
              that.$message({
                message: "更新成功",
                type: "success"
              });
              that.$router.push("/customers");
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
    }
  }
};
</script>