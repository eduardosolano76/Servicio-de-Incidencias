package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ServicioIncidenciasApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServicioIncidenciasApplication.class, args);
	}

}
