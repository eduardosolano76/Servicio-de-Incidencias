package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CatalogoTipoIncidenciasDTO {
	
	private Long id;
    private String nombre;
    private Integer prioridadBase;

}
