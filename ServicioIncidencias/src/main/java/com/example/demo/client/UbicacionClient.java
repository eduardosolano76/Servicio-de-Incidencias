package com.example.demo.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.demo.dto.UbicacionResponseDTO;

@FeignClient(name = "UBICACION") 
public interface UbicacionClient {

    @GetMapping("/api/ubicaciones/{id}")
    UbicacionResponseDTO obtenerUbicacionPorId(@PathVariable("id") Long id);

}
