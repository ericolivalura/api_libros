package com.aluracursos.desafio.repository;

import com.aluracursos.desafio.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Libro, Long> {
}
