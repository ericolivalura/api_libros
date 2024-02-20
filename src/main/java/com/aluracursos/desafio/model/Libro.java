package com.aluracursos.desafio.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "libros")
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String titulo;
    @ManyToOne()
    private Autor autor;
    private List<String> idiomas;
    private Double numeroDeDescargas;

    public Libro() {
    }

    public Libro(DatosLibros datos, Autor autor) {
        this.titulo = datos.titulo();
        this.autor = autor;
        this.idiomas = datos.idiomas();
        this.numeroDeDescargas = datos.numeroDeDescargas();
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public List<String> getIdiomas() {
        return idiomas;
    }

    public Double getNumeroDeDescargas() {
        return numeroDeDescargas;
    }

    @Override
    public String toString() {
        return "----- LIBRO -----" +
                "\n Titulo: " + titulo +
                "\n Autor: " + autor.getNombre() +
                "\n Idiomas: " + idiomas +
                "\n Numero de descargas: " + numeroDeDescargas +
                "\n-----------------\n";
    }
}
