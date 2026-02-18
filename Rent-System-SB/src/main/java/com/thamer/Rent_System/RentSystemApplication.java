package com.thamer.Rent_System;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.filter.CharacterEncodingFilter;

@SpringBootApplication
@EnableAsync // تفعيل العمليات المتزامنة في الخلفية
@EnableScheduling
public class RentSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentSystemApplication.class, args);
	}

}
