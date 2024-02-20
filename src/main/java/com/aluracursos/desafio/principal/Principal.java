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
            2- top 10 libros más descargados
            3- exhibir estadísticas de descargas totales
            4- buscar un autor
            5- listar libros registrados
            6- listar autores registrados
            7- listar autores por año de nacimiento
            8- listar autores vivos por año
            9- listar autores por intervalo de años
            10- exhibir estadísticas de descargas de los libros registrados
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
                case 2 -> listarTop10LibrosMasDescargados();
                case 3 -> exhibirEstadisticasDeDescargas();
                case 4 -> buscarAutor();
                case 5 -> listarLibrosRegistrados();
                case 6 -> listarAutoresRegistrados();
                case 7 -> listarAutoresPorAnoDeNacimiento();
                case 8 -> listarAutoresPorIntervaloDeAnos();
                case 9 -> listarAutoresVivosEnAnoEspecifico();
                case 10 -> exhibirEstadisticasDeDescargasDeLosLibrosRegistrados();
                case 0 -> System.out.println("Hasta luego...");
                default -> System.out.println("Opcion invalida");
            }
        }

    }

    private void buscarLibroPorTitulo() {
        DatosLibros datos = getDatosLibro();
        if (datos != null) {
            DatosAutor autor = datos.autor().get(0);
            Autor autorExistente = autorRepositorio.findByNombre(autor.nombre());

            Libro libro;
            if (autorExistente != null) {
                System.out.println(autorExistente);
                libro = new Libro();
                libro.setTitulo(datos.titulo());
                libro.setIdiomas(datos.idiomas());
                libro.setNumeroDeDescargas(datos.numeroDeDescargas());
                libro.setAutor(autorExistente);
                repositorio.save(libro);
            } else {
                libro = new Libro();
                Autor nuevoAutor = new Autor(autor);
                libro.setTitulo(datos.titulo());
                libro.setIdiomas(datos.idiomas());
                libro.setNumeroDeDescargas(datos.numeroDeDescargas());
                libro.setAutor(nuevoAutor);
                autorRepositorio.save(nuevoAutor);
                repositorio.save(libro);
            }
            System.out.println(libro);
        } else {
            System.out.println("Libro no encontrado. Le sugiero que compruebe si escribiste el título del libro correctamente e intente otra vez.");
        }
    }

    private DatosLibros getDatosLibro() {
        System.out.println("Ingrese el nombre del libro que desea buscar");
        var tituloLibro = teclado.nextLine();

        if(!tituloLibro.isBlank()){
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

    private void listarTop10LibrosMasDescargados() {
        System.out.println("Top 10 libros más descargados");

        Datos datos = getDadosWeb();
        datos.resultados().stream()
                .sorted(Comparator.comparing(DatosLibros::numeroDeDescargas).reversed())
                .limit(10)
                .map(l -> l.titulo().toUpperCase())
                .forEach(System.out::println);
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

    private void buscarAutor() {
        System.out.println("Ingrese el nombre del autor que desea buscar");
        var nombreAutor = teclado.nextLine();

        if(!nombreAutor.isBlank()){
            List<Autor> autores = autorRepositorio.findAllByNombreContainingIgnoreCase(nombreAutor);

            if (!autores.isEmpty()) {
                autores.forEach(System.out::println);
            } else {
                System.out.println("No existen autores con este nombre en nuestra base de datos");
            }
        } else {
            System.out.println("Campo de texto vacío, por favor, inténtelo de nuevo e ingrese un texto válido.");
        }
    }

    private void listarLibrosRegistrados() {
        repositorio.findAll(Sort.by(Sort.Direction.ASC, "titulo")).forEach(
                libro -> System.out.println(
                        "----- LIBRO -----" +
                                "\n Titulo: " + libro.getTitulo() +
                                "\n Autor: " + libro.getAutor().getNombre() +
                                "\n Idiomas: " + libro.getIdiomas() +
                                "\n Numero de descargas: " + libro.getNumeroDeDescargas() +
                                "\n-----------------\n"
                )
        );
    }

    private void listarAutoresRegistrados() {
        autorRepositorio.findAll().forEach(
                autor -> System.out.println(
                        "\n Autor: " + autor.getNombre() +
                                "\n Fecha de nacimiento: " + autor.getFechaDeNacimiento() +
                                "\n Fecha de fallecimiento: " + autor.getFechaDeFallecimiento()
                ));
    }

    private Datos getDadosWeb() {
        return conversor.obtenerDatos(json, Datos.class);
    }

    /* Consultas con fechas (Autores) */

    private void listarAutoresPorAnoDeNacimiento() {
        System.out.println("Ingrese el año de nacimiento del autor(es) que desea buscar");
        var anoNacimiento = teclado.nextLine();

        if(!anoNacimiento.isBlank()){
            List<Autor> autoresBuscados = autorRepositorio.findByFechaDeNacimiento(Integer.valueOf(anoNacimiento));

            if (!autoresBuscados.isEmpty()) {
                autoresBuscados.forEach(autor -> System.out.println(autor.toString()));
            } else {
                System.out.println("No se han encontrado autores nacidos este año.");
            }
        } else {
            System.out.println("Campo de texto vacío, por favor, inténtelo de nuevo e ingrese un numero entero válido.");
        }
    }

    private void listarAutoresVivosEnAnoEspecifico() {
        System.out.println("Ingrese el año vivo de autor(es) que desea buscar");
        var anoVivo = teclado.nextLine();

        if(!anoVivo.isBlank()){
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

    private void listarAutoresPorIntervaloDeAnos() {
        System.out.println("Ingrese el año inicial del intervalo");
        var anoInicial = teclado.nextLine();

        System.out.println("Ingrese el año final del intervalo");
        var anoFinal = teclado.nextLine();

        if(!(anoInicial.isBlank() || anoFinal.isBlank())){
            List<Autor> autoresBuscados = autorRepositorio.findByFechaDeNacimientoGreaterThanEqualAndFechaDeFallecimientoLessThan(
                    Integer.valueOf(anoInicial), Integer.valueOf(anoFinal));

            if (!autoresBuscados.isEmpty()) {
                autoresBuscados.forEach(autor -> System.out.println(autor.toString()));
            } else {
                System.out.println("No se han encontrado autores vivos en este intervalo de años.");
            }
        } else {
            System.out.println("Campo(s) de texto vacío(s), por favor, inténtelo de nuevo e ingrese un numero entero válido en cada campo.");
        }
    }

    private void exhibirEstadisticasDeDescargasDeLosLibrosRegistrados() {
        // Cantidad de libros por idiomas
        Map<List<String>, Long> librosPorIdioma = repositorio.findAll().stream()
                .collect(Collectors.groupingBy(Libro::getIdiomas, Collectors.counting()
                ));

        System.out.println("Cantidad de libros por idiomas");
        librosPorIdioma.forEach((idioma, totalLibros) ->
                System.out.println(idioma + ": " + totalLibros)); //

        // Cantidad de descargas por idiomas
        Map<List<String>, Double> descargasPorIdioma = repositorio.findAll().stream()
                .collect(Collectors.groupingBy(Libro::getIdiomas, Collectors.summingDouble(Libro::getNumeroDeDescargas)
                ));

        System.out.println("Cantidad de libros descargados por idiomas");
        descargasPorIdioma.forEach((idioma, totalDescargas) ->
                System.out.println(idioma + ": " + totalDescargas));
    }
}