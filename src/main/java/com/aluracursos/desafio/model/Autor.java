package com.aluracursos.desafio.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "autores")
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private Integer fechaDeNacimiento;
    private Integer fechaDeFallecimiento;

    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Libro> libros = new ArrayList<>();

    public Autor(DatosAutor datosAutor) {
        this.nombre = String.valueOf(datosAutor.nombre());
        this.fechaDeNacimiento = Integer.valueOf(datosAutor.fechaDeNacimiento());
        this.fechaDeFallecimiento = Integer.valueOf(datosAutor.fechaDeFallecimiento());
    }

    public Autor() {
    }

    public String getNombre() {
        return nombre;
    }

    public Long getId() {
        return id;
    }

    private List<String> getNombreDeLibro() {
        return libros.stream().map(Libro::getTitulo).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "\n Autor: " + nombre +
                "\n Fecha de nacimiento: " + fechaDeNacimiento +
                "\n Fecha de fallecimiento: " + fechaDeFallecimiento +
                "\n Libros: " + getNombreDeLibro()
                ;
    }
}
