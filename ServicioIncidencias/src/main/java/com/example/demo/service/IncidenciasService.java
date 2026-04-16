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
		UbicacionResponseDTO ubicacion = ubicacionClient.obtenerUbicacionPorId(dto.getUbicacionId());
		
		ClimaResponseDTO clima = climaClient.obtenerClimaActual(
				ubicacion.getLatitud(), ubicacion.getLongitud(), apiKey, "es", "metric"
		);
		
		String condicion = clima.getCondicionPrincipal().toLowerCase();
		String alerta = null;
		if (condicion.contains("tormenta") || condicion.contains("lluvia")) {
			alerta = "Alerta climática: No se recomienda enviar cuadrillas para reparación debido al clima (" + condicion + ").";
		}
		
		Incidencias incidencia = new Incidencias();
		incidencia.setTitulo(dto.getTitulo());
		incidencia.setDescripcion(dto.getDescripcion());
		incidencia.setTipoIncidencia(tipoIncidenciasRepository.findById(dto.getTipoIncidenciaId())
				.orElseThrow(() -> new RuntimeException("Tipo de incidencia no encontrado")));
		incidencia.setFechaReporte(LocalDateTime.now());
		incidencia.setEstado(Estado.REPORTADO); 
		incidencia.setUsuarioId(dto.getUsuarioId());
		incidencia.setUbicacionId(dto.getUbicacionId());
		
		Incidencias incidenciaGuardada = incidenciasRepository.save(incidencia);
		
		// AÑADIDO: Registrar el primer paso en el Historial
		registrarHistorial(incidenciaGuardada, Estado.REPORTADO, "Reporte inicial creado", dto.getUsuarioId());
		
		IncidenciasDTO respuesta = convertirADTO(incidenciaGuardada);
		respuesta.setAlertaClima(alerta); 
		return respuesta;
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

        incidencia.setEstado(nuevoEstado);
        Incidencias actualizada = incidenciasRepository.save(incidencia);

        registrarHistorial(actualizada, nuevoEstado, comentarios, usuarioModificador);
        
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
    public IncidenciasDTO asignarResponsable(Long id, Long departamentoId, Long personalId, Long usuarioAsignador) {
        
        // VALIDACIÓN 1: Verificar que el Departamento existe
        try {
            gestionClient.obtenerDepartamento(departamentoId);
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Error: El departamento especificado no existe en el sistema.");
        }

        // VALIDACIÓN 2: Verificar que el Personal existe y está disponible
        PersonalResponseDTO personal;
        try {
            personal = gestionClient.obtenerPersonal(personalId);
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Error: El personal especificado no existe en el sistema.");
        }

        if (personal.getDisponible() != null && !personal.getDisponible()) {
            throw new RuntimeException("Error: El empleado " + personal.getNombre() + " no está disponible en este momento.");
        }

        // Si pasamos las validaciones, procedemos con la asignación
        Incidencias incidencia = incidenciasRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));

        incidencia.setDepartamentoId(departamentoId);
        incidencia.setPersonalId(personalId);
        incidencia.setEstado(Estado.EN_PROCESO); 

        Incidencias actualizada = incidenciasRepository.save(incidencia);

        // Registramos en el historial usando el nombre real del empleado
        registrarHistorial(actualizada, Estado.EN_PROCESO, "Personal asignado: " + personal.getNombre(), usuarioAsignador);

        gestionClient.cambiarDisponibilidad(personalId, false);
        
        return convertirADTO(actualizada);
    }
    
    // Método de ayuda para registrar en el historial
    private void registrarHistorial(Incidencias incidencia, Estado estado, String comentarios, Long usuarioModificador) {
        HistorialEstados historial = new HistorialEstados();
        historial.setIncidencia(incidencia);
        historial.setEstado(estado);
        historial.setFechaCambio(LocalDateTime.now());
        historial.setComentarios(comentarios);
        historial.setUsuarioModificador(usuarioModificador);
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
        return dto;
    }
}
