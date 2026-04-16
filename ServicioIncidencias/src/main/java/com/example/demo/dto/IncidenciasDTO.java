package com.example.demo.dto;

import java.time.LocalDateTime;

import com.example.demo.entity.Estado;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IncidenciasDTO {

	private Long id;
    private String titulo;
    private String descripcion;
    
    // Reemplazamos el objeto completo por su ID para evitar problemas de recursividad en JSON
    private Long tipoIncidenciaId; 
    
    private LocalDateTime fechaReporte;
    private Long usuarioId;
    private Long ubicacionId;
    private Long departamentoId;
    private Long personalId;
    
    private Estado estado;
    private String alertaClima;
}
