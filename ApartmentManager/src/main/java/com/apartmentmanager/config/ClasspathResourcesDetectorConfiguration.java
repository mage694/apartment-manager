package com.apartmentmanager.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
public class ClasspathResourcesDetectorConfiguration {
    private PropertySourcesPlaceholderConfigurer config(Resource... resources) {
        PropertySourcesPlaceholderConfigurer config = new PropertySourcesPlaceholderConfigurer();
        YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();
        factoryBean.setResources(resources);
        config.setProperties(factoryBean.getObject());
        return config;
    }

    @Bean
    @Profile("dev")
    public PropertySourcesPlaceholderConfigurer dev() {
        return config(new ClassPathResource("config/dev/apartment-dev.yml"));
    }

    @Bean
    @Profile("prod")
    public PropertySourcesPlaceholderConfigurer prod() {
        return config(new ClassPathResource("config/prod/apartment-prod.yml"));
    }
}
