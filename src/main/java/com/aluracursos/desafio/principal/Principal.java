package com.aluracursos.desafio.principal;

import com.aluracursos.desafio.model.*;
import com.aluracursos.desafio.repository.AutorRepository;
import com.aluracursos.desafio.repository.LibroRepository;
import com.aluracursos.desafio.service.ConsumoAPI;
import com.aluracursos.desafio.service.ConvierteDatos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    @Autowired
    private final LibroRepository repositorio;
    @Autowired
    private final AutorRepository autorRepositorio;

    private static final String URL_BASE = "https://gutendex.com/books/";
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private Scanner teclado = new Scanner(System.in);
    private String json;

    private String menu = """
            ------------
            Elija la opción a través de su número:
            1- buscar libro por título
            2- listar libros registrados
            3- listar autores registrados
            4- listar autores vivos en un determinado año
            5- listar libros por idioma
            0 - salir
            """;

    public Principal(LibroRepository repositorio, AutorRepository autorRepository) {
        this.repositorio = repositorio;
        this.autorRepositorio = autorRepository;
    }

    public void muestraElMenu() {

        var opcionElegida = -1;
        while (opcionElegida != 0) {
            json = consumoAPI.obtenerDatos(URL_BASE);
            System.out.println(menu);
            opcionElegida = teclado.nextInt();
            teclado.nextLine();
            switch (opcionElegida) {
                case 1 -> buscarLibroPorTitulo();
                case 2 -> listarLibrosRegistrados();
                case 3 -> listarAutoresRegistrados();
                case 4 -> listarAutoresVivosEnAnoEspecifico();
                case 5 -> listarLibrosPorIdioma();
                case 0 -> System.out.println("Hasta luego...");
                default -> System.out.println("Opcion invalida");
            }
        }
    }

    private void buscarLibroPorTitulo() {
        DatosLibros datos = getDatosLibro();
        if (datos != null) {
            Libro libro;
            DatosAutor datosAutor = datos.autor().get(0);
            Autor autorExistente = autorRepositorio.findByNombre(datosAutor.nombre());

            if (autorExistente != null) {
                libro = new Libro(datos, autorExistente);
            } else {
                Autor autor = new Autor(datos.autor().get(0));
                libro = new Libro(datos, autor);
                autorRepositorio.save(autor);
            }
            try {
                repositorio.save(libro);
                System.out.println(libro);
            } catch (Exception e) {
                System.out.println("No se puede registrar el mismo libro más de una vez");
            }

        } else {
            System.out.println("Libro no encontrado");
        }
    }

    private DatosLibros getDatosLibro() {
        System.out.println("Ingrese el nombre del libro que desea buscar");
        var tituloLibro = teclado.nextLine();

        if (!tituloLibro.isBlank()) {
            json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + tituloLibro.replace(" ", "+"));
            var datosBusqueda = conversor.obtenerDatos(json, Datos.class);
            Optional<DatosLibros> libroBuscado = datosBusqueda.resultados().stream()
                    .filter(l -> l.titulo().toUpperCase().contains(tituloLibro.toUpperCase()))
                    .findFirst();
            if (libroBuscado.isPresent()) {
                return libroBuscado.get();
            }
        } else {
            System.out.println("Campo de texto vacío, por favor, inténtelo de nuevo e ingrese un texto válido.");
        }
        return null;
    }

    private void listarLibrosRegistrados() {
        repositorio.findAll(Sort.by(Sort.Direction.ASC, "titulo")).forEach(System.out::println);
    }

    private void listarAutoresRegistrados() {
        autorRepositorio.findAll().forEach(System.out::println);
    }

    private void listarAutoresVivosEnAnoEspecifico() {
        System.out.println("Ingrese el año vivo de autor(es) que desea buscar");
        var anoVivo = teclado.nextLine();

        if (!anoVivo.isBlank()) {
            List<Autor> autoresBuscados = autorRepositorio.findByFechaDeNacimientoLessThanEqualAndFechaDeFallecimientoGreaterThan(
                    Integer.valueOf(anoVivo),
                    Integer.valueOf(anoVivo));

            if (!autoresBuscados.isEmpty()) {
                autoresBuscados.forEach(autor -> System.out.println(autor.toString()));
            } else {
                System.out.println("No se han encontrado autores vivos este año.");
            }
        } else {
            System.out.println("Campo de texto vacío, por favor, inténtelo de nuevo e ingrese un numero entero válido.");
        }
    }
    private void listarLibrosPorIdioma() {
        System.out.println("Ingrese el idioma que desea buscar los libros");
        var idioma = teclado.nextLine();

        if (!idioma.isBlank()) {
            List<Libro> librosPorIdioma = repositorio.findByIdiomaContainingIgnoreCase(idioma);
            librosPorIdioma.forEach(libro -> {
                System.out.println(libro.toString());
            });
        } else {
            System.out.println("Campo de texto vacío, por favor, inténtelo de nuevo e ingrese un numero entero válido.");
        }
    }
}