package com.example.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.dto.CatalogoTipoIncidenciasDTO;
import com.example.demo.entity.CatalogoTipoIncidencias;
import com.example.demo.repository.CatalogoTipoIncidenciasRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CatalogoTipoIncidenciasService {
	
	private final CatalogoTipoIncidenciasRepository repository;
	
	// Obtener todos los tipos de incidencia del catálogo
    public List<CatalogoTipoIncidenciasDTO> obtenerTodos() {
        return repository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // Método de ayuda para mapear Entidad a DTO
    private CatalogoTipoIncidenciasDTO convertirADTO(CatalogoTipoIncidencias entidad) {
        CatalogoTipoIncidenciasDTO dto = new CatalogoTipoIncidenciasDTO();
        dto.setId(entidad.getId());
        dto.setNombre(entidad.getNombre());
        dto.setPrioridadBase(entidad.getPrioridadBase());
        return dto;
    }
}
