package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UbicacionResponseDTO {
	private Long id;
    private Double latitud;
    private Double longitud;
    private String direccion;
    private String colonia;
    private String ciudad;
}
