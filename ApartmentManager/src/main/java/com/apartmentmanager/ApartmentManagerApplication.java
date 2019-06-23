package com.apartmentmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;


@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ApartmentManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApartmentManagerApplication.class, args);
	}
}
