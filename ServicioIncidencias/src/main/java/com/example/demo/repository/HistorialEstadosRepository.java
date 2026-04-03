package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.HistorialEstados;

@Repository
public interface HistorialEstadosRepository extends JpaRepository<HistorialEstados, Long> {
	
	List<HistorialEstados> findByIncidenciaIdOrderByFechaCambioDesc(Long incidenciaId);

}
