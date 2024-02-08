package com.aluracursos.desafio.principal;

import com.aluracursos.desafio.model.Datos;
import com.aluracursos.desafio.model.DatosLibros;
import com.aluracursos.desafio.service.ConsumoAPI;
import com.aluracursos.desafio.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private static final String URL_BASE = "https://gutendex.com/books/";
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private Scanner teclado = new Scanner(System.in);
    private String json;
//    private Datos datos = conversor.obtenerDatos(json,Datos.class);

    private String menu = """
            ------------
            Elija la opción a través de su número:
            1- buscar libro por título
            2- top 10 libros más descargados
            3- exhibir estadísticas de descargas
            4- buscar un autor
            0 - salir
            """;

    public void muestraElMenu() {

        var opcionElegida = -1;
        while (opcionElegida != 0) {
            json = consumoAPI.obtenerDatos(URL_BASE);
            System.out.println(menu);
            opcionElegida = teclado.nextInt();
            teclado.nextLine();
            switch (opcionElegida) {
                case 1 -> buscarLibroPorTitulo();
                case 2 -> listarTop10LibrosMasDescargados();
                case 3 -> exhibirEstadisticasDeDescargas();
                case 4 -> buscarAutor();
                case 0 -> System.out.println("Hasta luego...");
                default -> System.out.println("Opcion invalida");
            }
        }

    }

    private void buscarAutor() {
        System.out.println("Ingrese el nombre del autor que desea buscar");
        var nombreAutor = teclado.nextLine();
        json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + nombreAutor.replace(" ", "+"));
        var datosBusqueda = conversor.obtenerDatos(json, Datos.class);
        Optional<DatosLibros> first = datosBusqueda.resultados().stream().filter(l -> l.autor().get(0).nombre().toUpperCase().contains(nombreAutor.toUpperCase())).findFirst();
        DatosLibros datosLibros = encontrarLibro(first);
        System.out.println(datosLibros.autor());
    }


    private Datos getDadosWeb() {
        return conversor.obtenerDatos(json, Datos.class);
    }

    private void exhibirEstadisticasDeDescargas() {
        Datos datos = getDadosWeb();
        DoubleSummaryStatistics est = datos.resultados().stream()
                .filter(d -> d.numeroDeDescargas() > 0)
                .collect(Collectors.summarizingDouble(DatosLibros::numeroDeDescargas));
        System.out.println("Cantidad media de descargas: " + est.getAverage());
        System.out.println("Cantidad máxima de descargas: " + est.getMax());
        System.out.println("Cantidad mínima de descargas: " + est.getMin());
        System.out.println("Cantidad de registros evaluados para calcular las estadisticas: " + est.getCount());
    }

    private void listarTop10LibrosMasDescargados() {
        System.out.println("Top 10 libros más descargados");

        Datos datos = getDadosWeb();
        datos.resultados().stream()
                .sorted(Comparator.comparing(DatosLibros::numeroDeDescargas).reversed())
                .limit(10)
                .map(l -> l.titulo().toUpperCase())
                .forEach(System.out::println);
    }

    private void buscarLibroPorTitulo() {
        System.out.println("Ingrese el nombre del libro que desea buscar");
        var tituloLibro = teclado.nextLine();
        json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + tituloLibro.replace(" ", "+"));
        var datosBusqueda = conversor.obtenerDatos(json, Datos.class);
        Optional<DatosLibros> libroBuscado = datosBusqueda.resultados().stream()
                .filter(l -> l.titulo().toUpperCase().contains(tituloLibro.toUpperCase()))
                .findFirst();
        DatosLibros datosLibros = encontrarLibro(libroBuscado);
        System.out.println(datosLibros);
    }

    private DatosLibros encontrarLibro(Optional<DatosLibros> libroBuscado) {
        if (libroBuscado.isPresent()) {
            System.out.println("Libro Encontrado ");
            return libroBuscado.get();
        } else {
            System.out.println("Libro no encontrado");
            return null;
        }
    }

}
