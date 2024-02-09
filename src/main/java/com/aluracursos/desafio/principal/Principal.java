package com.aluracursos.desafio.principal;

import com.aluracursos.desafio.model.*;
import com.aluracursos.desafio.repository.AutorRepository;
import com.aluracursos.desafio.repository.BookRepository;
import com.aluracursos.desafio.service.ConsumoAPI;
import com.aluracursos.desafio.service.ConvierteDatos;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private static final String URL_BASE = "https://gutendex.com/books/";
    @Autowired
    private final BookRepository repositorio;
    @Autowired
    private final AutorRepository autorRepository;
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
            3- exhibir estadísticas de descargas totales
            4- buscar un autor
            5- listar libros registrados
            6- exhibir estadísticas de descargas de los libros registrados
            7- listar autores registrados
            0 - salir
            """;

    public Principal(BookRepository repositorio, AutorRepository autorRepository) {
        this.repositorio = repositorio;
        this.autorRepository = autorRepository;
    }

    public void muestraElMenu() {

        //System.out.println(json);
        //var datos = conversor.obtenerDatos(json,Datos.class);
        //System.out.println(datos);
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
                case 5 -> listarLibrosRegistrados();
                case 6 -> exhibirEstadisticasDeDescargasDeLosLibrosRegistrados();
                case 7 -> listarAutoresRegistrados();
                case 0 -> System.out.println("Hasta luego...");
                default -> System.out.println("Opcion invalida");
            }
        }

    }

    private void listarAutoresRegistrados() {
       autorRepository.findAll().forEach(System.out::println);
    }

    private void exhibirEstadisticasDeDescargasDeLosLibrosRegistrados() {
        //a implementar
    }

    private void buscarAutor() {
        System.out.println("Ingrese el nombre del autor que desea buscar");
        var nombreAutor = teclado.nextLine();
        List<Autor> autores = autorRepository.findFirstByNombreContainingIgnoreCase(nombreAutor);
        autores.stream().forEach(System.out::println);
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
        DatosLibros datos = getDatosLibro();
        if (datos.titulo() != null) {
            DatosAutor autor = datos.autor().get(0);
            Autor autorExistente = autorRepository.findByNombre(autor.nombre());

            Libro libro;
            if(autorExistente != null){
                System.out.println(autorExistente);
                libro = new Libro();
                libro.setTitulo(datos.titulo());
                libro.setIdiomas(datos.idiomas());
                libro.setNumeroDeDescargas(datos.numeroDeDescargas());
                libro.setAutor(autorExistente);
            }
            else {
                libro = new Libro(datos);
            }
            repositorio.save(libro);
            System.out.println(libro);
        }

    }

    private DatosLibros getDatosLibro() {
        System.out.println("Ingrese el nombre del libro que desea buscar");
        var tituloLibro = teclado.nextLine();
        json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + tituloLibro.replace(" ", "+"));
        var datosBusqueda = conversor.obtenerDatos(json, Datos.class);
        Optional<DatosLibros> libroBuscado = datosBusqueda.resultados().stream()
                .filter(l -> l.titulo().toUpperCase().contains(tituloLibro.toUpperCase()))
                .findFirst();
        if (libroBuscado.isPresent()) {
            DatosLibros datosLibros = libroBuscado.get();
            return datosLibros;
        }
        return null;
    }

    private void listarLibrosRegistrados() {
        repositorio.findAll().stream().forEach(System.out::println);
    }

}
