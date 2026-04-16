package com.example.demo.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.dto.DepartamentoResponseDTO;
import com.example.demo.dto.PersonalResponseDTO;

@FeignClient(name = "SERVICIO-GESTION-INSTITUCIONAL")
public interface GestionClient {
	
	@GetMapping("/api/gestion/departamentos/{id}")
    DepartamentoResponseDTO obtenerDepartamento(@PathVariable("id") Long id);

    @GetMapping("/api/gestion/personal/{id}")
    PersonalResponseDTO obtenerPersonal(@PathVariable("id") Long id);
    
    @PatchMapping("/api/gestion/personal/{id}/disponibilidad")
    PersonalResponseDTO cambiarDisponibilidad(
            @PathVariable("id") Long id, 
            @RequestParam("disponible") Boolean disponible
    );
}
