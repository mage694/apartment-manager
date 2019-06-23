package com.filemanager.service.baidu;

import com.baidu.aip.ocr.AipOcr;
import com.filemanager.service.IOcrService;
import com.filemanager.dto.IdCardInfo;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;

@Service
@Primary
public class AipOcrService implements IOcrService {

    @Value("${platform.baidu.appId}")
    private String appId;

    @Value("${platform.baidu.apiKey}")
    private String apiKey;

    @Value("${platform.baidu.secret}")
    private String secret;

    @Override
    public IdCardInfo parse(MultipartFile file) {
        IdCardInfo result = new IdCardInfo();
        AipOcr client = new AipOcr(appId, apiKey, secret);
        HashMap<String, String> options = new HashMap<>();
        options.put("detect_direction", "true");
        options.put("detect_risk", "false");

        try {
            byte[] bytes =  file.getBytes();
            JSONObject json = client.idcard(bytes, "front", options);
            result.setIdNumber(json.getJSONObject("words_result").getJSONObject("公民身份号码").getString("words"));
            result.setName(json.getJSONObject("words_result").getJSONObject("姓名").getString("words"));
            result.setAddress(json.getJSONObject("words_result").getJSONObject("住址").getString("words"));
            result.setBirthDate(json.getJSONObject("words_result").getJSONObject("出生").getString("words"));
            result.setGender(json.getJSONObject("words_result").getJSONObject("性别").getString("words"));
        } catch (Exception e) {
            e.printStackTrace();
        }


        return result;
    }
}
