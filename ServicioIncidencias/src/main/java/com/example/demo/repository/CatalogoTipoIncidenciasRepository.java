package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.CatalogoTipoIncidencias;

@Repository
public interface CatalogoTipoIncidenciasRepository extends JpaRepository<CatalogoTipoIncidencias, Long> {
	
}
