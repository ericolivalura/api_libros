package com.aluracursos.desafio.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "libros")
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titulo;
    @OneToMany(mappedBy = "libro", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Autor autor;
    private List<String> idiomas;
    private Double numeroDeDescargas;

    public Libro(DatosLibros datosLibros) {
        this.titulo = datosLibros.titulo();
        this.autor = new Autor(datosLibros.autor());
        this.idiomas = datosLibros.idiomas();
        this.numeroDeDescargas = datosLibros.numeroDeDescargas();
    }

    public Libro() {
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public Autor getAutor() {
        return autor;
    }

    public List<String> getIdiomas() {
        return idiomas;
    }

    public Double getNumeroDeDescargas() {
        return numeroDeDescargas;
    }

    @Override
    public String toString() {
        return "Libro{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", autor=" + autor.getNombre() +
                ", idiomas=" + idiomas +
                ", numeroDeDescargas=" + numeroDeDescargas +
                '}';
    }
}
