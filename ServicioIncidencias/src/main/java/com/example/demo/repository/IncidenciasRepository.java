package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.dto.ZonaEstadisticaDTO;
import com.example.demo.entity.Incidencias;

@Repository
public interface IncidenciasRepository extends JpaRepository<Incidencias, Long> {
	
    List<Incidencias> findByDepartamentoId(Long departamentoId);
    
    List<Incidencias> findByPersonalId(Long personalId);
    
    List<Incidencias> findByTipoIncidenciaId(Long tipoIncidenciaId);
    
    List<Incidencias> findByUbicacionId(Long ubicacionId);
    
    // Consulta para contar incidencias de un tipo específico agrupadas por colonia/ubicación
    @Query("SELECT new com.example.demo.dto.ZonaEstadisticaDTO(i.ubicacionId, COUNT(i)) " +
           "FROM Incidencias i " +
           "WHERE i.tipoIncidencia.id = :tipoId " +
           "GROUP BY i.ubicacionId " +
           "ORDER BY COUNT(i) DESC")
    List<ZonaEstadisticaDTO> countIncidenciasPorZona(@Param("tipoId") Long tipoId);
}
