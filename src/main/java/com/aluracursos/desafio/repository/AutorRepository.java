package com.aluracursos.desafio.repository;

import com.aluracursos.desafio.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Year;
import java.util.List;

public interface AutorRepository extends JpaRepository<Autor, Long> {
    List<Autor> findFirstByNombreContainingIgnoreCase(String nombre);
    Autor findByNombre(String nombre);
    List<Autor> findByFechaDeNacimiento(Year fechaDeNacimiento);
    List<Autor> findByFechaDeNacimientoGreaterThanEqualAndFechaDeFallecimientoLessThan(Year fechaDeNacimiento, Year fechaDeFallecimiento);
    List<Autor> findByFechaDeNacimientoLessThanEqualAndFechaDeFallecimientoGreaterThan(Year fechaDeNacimiento, Year fechaDeFallecimiento);
}