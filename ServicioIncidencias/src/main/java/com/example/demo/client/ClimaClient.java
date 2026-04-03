package com.example.demo.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.dto.ClimaResponseDTO;


@FeignClient(name = "clima-api", url = "${api.clima.url}")
public interface ClimaClient {
	
	@GetMapping("/data/2.5/weather")
    ClimaResponseDTO obtenerClimaActual(
            @RequestParam("lat") Double latitud,
            @RequestParam("lon") Double longitud,
            @RequestParam("appid") String apiKey,
            @RequestParam(value = "lang", defaultValue = "es") String idioma,
            @RequestParam(value = "units", defaultValue = "metric") String unidades
    );

}
