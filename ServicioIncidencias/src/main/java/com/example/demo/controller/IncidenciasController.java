package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.CatalogoTipoIncidenciasDTO;
import com.example.demo.dto.IncidenciasDTO;
import com.example.demo.dto.ZonaEstadisticaDTO;
import com.example.demo.entity.Estado;
import com.example.demo.service.CatalogoTipoIncidenciasService;
import com.example.demo.service.IncidenciasService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/incidencias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Permitir solicitudes desde cualquier origen (CORS)
public class IncidenciasController {
	
	private final IncidenciasService incidenciasService;
	private final CatalogoTipoIncidenciasService service;
	
    // 1. Crear incidencia
	@PostMapping
	public ResponseEntity<IncidenciasDTO> crearReporte(@RequestBody IncidenciasDTO dto) {
		IncidenciasDTO nuevaIncidencia = incidenciasService.crearIncidencia(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(nuevaIncidencia);
	}

    // 2. Obtener todas las incidencias
    //@PreAuthorize("hasRole('ADMIN') or hasRole('SISTEMA')")
	@GetMapping
    public ResponseEntity<List<IncidenciasDTO>> obtenerTodas() {
        return ResponseEntity.ok(incidenciasService.obtenerTodas());
    }

    // 3. Obtener incidencia por ID
    @GetMapping("/{id}")
    public ResponseEntity<IncidenciasDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(incidenciasService.obtenerPorId(id));
    }

    // 4. Cambiar estado (Usamos PATCH porque solo actualizamos una parte del recurso)
    @PatchMapping("/{id}/estado")
    public ResponseEntity<IncidenciasDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam Estado nuevoEstado,
            @RequestParam String comentarios,
            @RequestParam Long usuarioModificador) {
        
        IncidenciasDTO actualizada = incidenciasService.cambiarEstado(id, nuevoEstado, comentarios, usuarioModificador);
        return ResponseEntity.ok(actualizada);
    }

    // 5. Asignar responsable
    @PatchMapping("/{id}/asignar")
    public ResponseEntity<IncidenciasDTO> asignarResponsable(
            @PathVariable Long id,
            @RequestParam Long personalId,
            @RequestParam Long usuarioAsignador) {
        
        IncidenciasDTO actualizada = incidenciasService.asignarResponsable(id, personalId, usuarioAsignador);
        return ResponseEntity.ok(actualizada);
    }
    
    // Obtener todos los tipos de incidencias (Bache, Fuga, etc.)
    @GetMapping("/catalogos-tipos")
    public ResponseEntity<List<CatalogoTipoIncidenciasDTO>> obtenerTipos() {
        return ResponseEntity.ok(service.obtenerTodos());
    }	
    
    // Obtener incidencias por Tipo (Bache, Fuga, etc.)
    @GetMapping("/tipo/{tipoId}")
    public ResponseEntity<List<IncidenciasDTO>> obtenerPorTipo(@PathVariable Long tipoId) {
        return ResponseEntity.ok(incidenciasService.obtenerPorTipo(tipoId));
    }
    
    // Obtener incidencias por Ubicación/Colonia
    @GetMapping("/incidencias-por-colonia/{ubicacionId}")
    public ResponseEntity<List<IncidenciasDTO>> obtenerPorUbicacion(@PathVariable Long ubicacionId) {
        return ResponseEntity.ok(incidenciasService.obtenerPorUbicacion(ubicacionId));
    }
    
    // Obtener ranking de zonas con más baches
    @GetMapping("/estadisticas/zonas-baches")
    public ResponseEntity<List<ZonaEstadisticaDTO>> obtenerRankingBaches() {
        return ResponseEntity.ok(incidenciasService.obtenerZonasConMasBaches());
    }
}