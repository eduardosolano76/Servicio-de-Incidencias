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
public class HistorialEstadosDTO {
	
	private Long id;
    private Long incidenciaId; 
    private Estado estado;
    private LocalDateTime fechaCambio;
    private String comentarios;
    private Long usuarioModificador;
    private String alertaClima;

}
