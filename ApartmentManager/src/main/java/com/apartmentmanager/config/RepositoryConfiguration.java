package com.apartmentmanager.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import javax.persistence.EntityManager;

@Configuration
@EnableMongoRepositories(basePackages = "com.apartmentmanager.dao.ext")
public class RepositoryConfiguration {

    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager)  {
        return new JPAQueryFactory(entityManager);
    }
}
