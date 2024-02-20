package com.aluracursos.desafio.repository;

import com.aluracursos.desafio.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AutorRepository extends JpaRepository<Autor, Long> {
    Autor findByNombre(String nombre);
    List<Autor> findByFechaDeNacimientoLessThanEqualAndFechaDeFallecimientoGreaterThan(Integer fechaDeNacimiento, Integer fechaDeFallecimiento);

}