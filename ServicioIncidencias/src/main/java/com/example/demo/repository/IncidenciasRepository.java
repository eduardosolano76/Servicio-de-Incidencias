package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Incidencias;

@Repository
public interface IncidenciasRepository extends JpaRepository<Incidencias, Long> {
	
    List<Incidencias> findByDepartamentoId(Long departamentoId);
    
    List<Incidencias> findByPersonalId(Long personalId);
    
    

}
