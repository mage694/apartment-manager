import apartmentApi from './apartment-api';

export default {
    validateImg(file) {
        const isJPG = file.type === "image/png"
            || file.type === "image/jpg"
            || file.type === "image/jpeg"
            || file.type === "image/gif";
        const isLt2M = file.size / 1024 / 1024 < 2;
        if (!isJPG) {
            this.$message.error("上传头像只能是图片格式!");
        }
        if (!isLt2M) {
            this.$message.error("上传头像图片大小不能超过 2MB!");
        }
        return isJPG && isLt2M;
    },
    upload(form, successCallback) {
        apartmentApi.post("/files", form).then(successCallback).catch(err => console.log("附件上传失败" + err));
    },
    removeFile(file, successCallback) {
        apartmentApi.delete("/files/" + file.id)
            .then(successCallback)
            .catch(err => console.log("附件删除失败：" + err));
    }
}