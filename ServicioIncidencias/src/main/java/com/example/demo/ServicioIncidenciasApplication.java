package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
	    "com.example.demo",         // Tu paquete actual
	    "com.ayuntamiento.security_lib"   // El paquete de tu LIBRERÍA
	})
@EnableFeignClients
public class ServicioIncidenciasApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServicioIncidenciasApplication.class, args);
	}
}
