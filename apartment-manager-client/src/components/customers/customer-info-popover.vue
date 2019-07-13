<template>
  <div>
  <p>姓名: {{ name }}</p>
  <p>性别: {{ genderForDisplay }}</p>
  <p>电话: {{ phone }}</p>
  </div>
</template>
<script>
export default {
  props: {
    customerId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      idNum: 0,
      name: "",
      gender: "",
      phone: ""
    };
  },
  computed: {
    genderForDisplay() {
      return this.gender === "MALE" ? "男" : "女";
    }
  },
  mounted() {
    this.$api.get("/customers/" + this.customerId).then(response => {
      this.name = response.data.name;
      this.gender = response.data.gender;
      response.data.contacts.forEach(c => {
          if(c.contactType === "PHONE") {
              this.phone = c.detail;
          }
      });;
    });
  }
};
</script>

