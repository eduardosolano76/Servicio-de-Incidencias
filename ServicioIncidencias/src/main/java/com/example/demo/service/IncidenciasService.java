package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.client.ClimaClient;
import com.example.demo.client.GestionClient;
import com.example.demo.client.UbicacionClient;
import com.example.demo.dto.ClimaResponseDTO;
import com.example.demo.dto.CuadrillaResponseDTO;
import com.example.demo.dto.IncidenciasDTO;
import com.example.demo.dto.PersonalResponseDTO;
import com.example.demo.dto.UbicacionResponseDTO;
import com.example.demo.entity.Estado;
import com.example.demo.entity.HistorialEstados;
import com.example.demo.entity.Incidencias;
import com.example.demo.repository.CatalogoTipoIncidenciasRepository;
import com.example.demo.repository.HistorialEstadosRepository;
import com.example.demo.repository.IncidenciasRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IncidenciasService {
	
	private final IncidenciasRepository incidenciasRepository;
	private final CatalogoTipoIncidenciasRepository tipoIncidenciasRepository;
	private final ClimaClient climaClient;
    private final UbicacionClient ubicacionClient; 
    private final HistorialEstadosRepository historialRepository;
    private final GestionClient gestionClient;
    
    @Value("${api.clima.key}")
    private String apiKey;
  
    @Transactional
	public IncidenciasDTO crearIncidencia(IncidenciasDTO dto) {
    	
		String alerta = evaluarAlertaClimatica(dto.getUbicacionId());
	
		Incidencias incidencia = new Incidencias();
		incidencia.setTitulo(dto.getTitulo());
		incidencia.setDescripcion(dto.getDescripcion());
		incidencia.setTipoIncidencia(tipoIncidenciasRepository.findById(dto.getTipoIncidenciaId())
				.orElseThrow(() -> new RuntimeException("Tipo de incidencia no encontrado")));
		incidencia.setFechaReporte(LocalDateTime.now());
		incidencia.setEstado(Estado.REPORTADO); 
		incidencia.setUsuarioId(dto.getUsuarioId());
		incidencia.setUbicacionId(dto.getUbicacionId());
		incidencia.setAlertaClima(alerta);
		
		Incidencias incidenciaGuardada = incidenciasRepository.save(incidencia);
		
		registrarHistorial(incidenciaGuardada, Estado.REPORTADO, "Reporte inicial creado", dto.getUsuarioId(), alerta);

		return convertirADTO(incidenciaGuardada);
	}
    
    // Consultar todas las incidencias
    public List<IncidenciasDTO> obtenerTodas() {
        return incidenciasRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    // Consultar incidencia por ID
    public IncidenciasDTO obtenerPorId(Long id) {
        Incidencias incidencia = incidenciasRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada con ID: " + id));
        return convertirADTO(incidencia);
    }
    
    // Cambiar el estado de la incidencia y registrar en bitácora
    @Transactional
    public IncidenciasDTO cambiarEstado(Long id, Estado nuevoEstado, String comentarios, Long usuarioModificador) {
        Incidencias incidencia = incidenciasRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));
        
        if (incidencia.getPersonalId() == null) {
            throw new RuntimeException("Error: No se puede cambiar el estado a " + nuevoEstado + " porque la incidencia aún no tiene un responsable asignado.");
        }
        
        incidencia.setEstado(nuevoEstado);
        
        String nuevaAlerta = evaluarAlertaClimatica(incidencia.getUbicacionId());
        incidencia.setAlertaClima(nuevaAlerta);
        
        Incidencias actualizada = incidenciasRepository.save(incidencia);

        registrarHistorial(actualizada, nuevoEstado, comentarios, usuarioModificador, nuevaAlerta);
        
        if ((nuevoEstado == Estado.RESUELTO || nuevoEstado == Estado.CERRADO) && incidencia.getPersonalId() != null) {
            try {
                gestionClient.cambiarDisponibilidad(incidencia.getPersonalId(), true);
            } catch (Exception e) {
                // Solo imprimimos el error para no detener el flujo si Gestión falla
                System.out.println("No se pudo liberar al personal: " + e.getMessage());
            }
        }

        return convertirADTO(actualizada);
    }
    
    // Asignar responsable a la incidencia
    @Transactional
    public IncidenciasDTO asignarResponsable(Long id, Long personalId, Long usuarioAsignador) {
        
        // 1. Verificar que el Personal existe y está disponible
        PersonalResponseDTO personal;
        try {
            personal = gestionClient.obtenerPersonal(personalId);
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Error: El personal especificado no existe en el sistema.");
        }

        if (personal.getDisponible() != null && !personal.getDisponible()) {
            throw new RuntimeException("Error: El empleado " + personal.getNombre() + " no está disponible en este momento.");
        }

        // 2. Obtener la cuadrilla para sacar el departamentoId
        if (personal.getCuadrillaId() == null) {
            throw new RuntimeException("Error: El personal devuelto no tiene una cuadrilla asignada.");
        }
        
        CuadrillaResponseDTO cuadrilla;
        try {
            cuadrilla = gestionClient.obtenerCuadrilla(personal.getCuadrillaId());
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Error: La cuadrilla del empleado no existe en el sistema.");
        }

        // 3. Proceder con la asignación
        Incidencias incidencia = incidenciasRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));

        incidencia.setPersonalId(personalId);
        incidencia.setDepartamentoId(cuadrilla.getDepartamentoId()); // Se asigna el extraído de la cuadrilla

        Incidencias actualizada = incidenciasRepository.save(incidencia);

        // 4. Guardar historial y bloquear personal
        registrarHistorial(actualizada, actualizada.getEstado(), "Personal asignado: " + personal.getNombre(), usuarioAsignador, actualizada.getAlertaClima());
        gestionClient.cambiarDisponibilidad(personalId, false);
        
        return convertirADTO(actualizada);
    }
    
    // Método de ayuda para registrar en el historial
    private void registrarHistorial(Incidencias incidencia, Estado estado, String comentarios, Long usuarioModificador, String alertaClima) {
        HistorialEstados historial = new HistorialEstados();
        historial.setIncidencia(incidencia);
        historial.setEstado(estado);
        historial.setFechaCambio(LocalDateTime.now());
        historial.setComentarios(comentarios);
        historial.setUsuarioModificador(usuarioModificador);
        historial.setAlertaClima(alertaClima);
        historialRepository.save(historial);
    }
    
    // Método de ayuda para mapear Entidad a DTO
    private IncidenciasDTO convertirADTO(Incidencias incidencia) {
        IncidenciasDTO dto = new IncidenciasDTO();
        dto.setId(incidencia.getId());
        dto.setTitulo(incidencia.getTitulo());
        dto.setDescripcion(incidencia.getDescripcion());
        dto.setTipoIncidenciaId(incidencia.getTipoIncidencia().getId());
        dto.setFechaReporte(incidencia.getFechaReporte());
        dto.setEstado(incidencia.getEstado());
        dto.setUsuarioId(incidencia.getUsuarioId());
        dto.setUbicacionId(incidencia.getUbicacionId());
        dto.setDepartamentoId(incidencia.getDepartamentoId());
        dto.setPersonalId(incidencia.getPersonalId());
        dto.setAlertaClima(incidencia.getAlertaClima());
        return dto;
    }
    
    private String evaluarAlertaClimatica(Long ubicacionId) {
        try {
            UbicacionResponseDTO ubicacion = ubicacionClient.obtenerUbicacionPorId(ubicacionId);
            
            ClimaResponseDTO clima = climaClient.obtenerClimaActual(
                    ubicacion.getLatitud(), ubicacion.getLongitud(), apiKey, "es", "metric"
            );
            
            String condicion = clima.getCondicionPrincipal().toLowerCase();
            
            if (condicion.contains("tormenta") || condicion.contains("lluvia")) {
                return "Alerta climática: No se recomienda enviar cuadrillas para reparación debido al clima (" + condicion + ").";
            }
        } catch (Exception e) {
            // Buena práctica: Si falla la API del clima, no bloqueamos la operación
            System.out.println("No se pudo obtener el clima: " + e.getMessage());
        }
        return null; // Si no hay lluvia/tormenta o si falló la API, retorna null
    }
   
}
