package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    
    @Enumerated(EnumType.STRING)
    private Estado estado;
    
    @Column(name = "fecha_reporte")
    private LocalDateTime fechaReporte;
    
    @Column(name = "usuario_id")
    private Long usuarioId;
    
    @Column(name = "ubicacion_id")
    private Long ubicacionId;
    
    @Column(name = "departamento_id")
    private Long departamentoId;
    
    @Column(name = "personal_id")
    private Long personalId;
    
    @Column(name = "alerta_clima", length = 250)
    private String alertaClima;
}
