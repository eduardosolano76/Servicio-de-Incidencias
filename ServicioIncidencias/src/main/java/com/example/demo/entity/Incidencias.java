package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "incidencias")
public class Incidencias {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(insertable = false, updatable = false)
	private Long id;
	
	@Column(nullable = false, length = 50)
	private String titulo;
	
	@Column(nullable = false, length = 250)
	private String descripcion;
	
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_incidencia_id", nullable = false)
	private CatalogoTipoIncidencias tipoIncidencia;
    
    private LocalDateTime fechaReporte;
    
    private Long usuarioId;
    
    private Long ubicacionId;
    
    private Long departamentoId;
    
    private Long personalId;
}
