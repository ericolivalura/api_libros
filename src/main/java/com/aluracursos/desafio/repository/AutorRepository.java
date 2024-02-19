package com.aluracursos.desafio.repository;

import com.aluracursos.desafio.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AutorRepository extends JpaRepository<Autor, Long> {
    Autor findByNombre(String nombre);
    List<Autor> findByFechaDeNacimiento(Integer fechaDeNacimiento);
    List<Autor> findByFechaDeNacimientoGreaterThanEqualAndFechaDeFallecimientoLessThan(Integer fechaDeNacimiento, Integer fechaDeFallecimiento);
    List<Autor> findByFechaDeNacimientoLessThanEqualAndFechaDeFallecimientoGreaterThan(Integer fechaDeNacimiento, Integer fechaDeFallecimiento);
    List<Autor> findAllByNombreContainingIgnoreCase(String nombreAutor);

}