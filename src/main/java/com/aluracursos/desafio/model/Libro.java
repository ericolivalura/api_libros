package com.aluracursos.desafio.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Libro {
    private Long id;
    private String titulo;
    private List<Autor> autores;
    private List<String> idiomas;
    private Double numeroDeDescargas;
}
