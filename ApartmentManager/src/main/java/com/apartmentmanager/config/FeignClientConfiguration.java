package com.apartmentmanager.config;

import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

@Configuration
public class FeignClientConfiguration {
    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConvertersObjectFactory;

    @Bean
    @Primary
    @Scope("prototype")
    public SpringFormEncoder multipartFormEncoder() {
        return new SpringFormEncoder(new SpringEncoder(messageConvertersObjectFactory));
    }

    @Bean
    public feign.Logger.Level multipartLoggerLevel() {
        return feign.Logger.Level.FULL;
    }
}
