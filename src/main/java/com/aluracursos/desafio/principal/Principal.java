package com.aluracursos.desafio.principal;

import com.aluracursos.desafio.model.*;
import com.aluracursos.desafio.repository.AutorRepository;
import com.aluracursos.desafio.repository.BookRepository;
import com.aluracursos.desafio.service.ConsumoAPI;
import com.aluracursos.desafio.service.ConvierteDatos;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Year;
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
            8- listar autores por año de nacimiento
            9- listar autores por intervalo de años
            10- listar autores por año vivo
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
                case 8 -> listarAutoresPorAnoDeNacimiento();
                case 9 -> listarAutoresPorIntervaloDeAnos();
                case 10 -> listarAutoresVivosEnAnoEspecifico();
                case 0 -> System.out.println("Hasta luego...");
                default -> System.out.println("Opcion invalida");
            }
        }

    }

    private void listarAutoresRegistrados() {
       autorRepository.findAll().forEach(
               autor -> {
                   System.out.println(
                           "\n Autor: " + autor.getNombre() +
                           "\n Fecha de nacimiento: " + autor.getFechaDeNacimiento() +
                           "\n Fecha de fallecimiento: " + autor.getFechaDeFallecimiento() + "\n"
                   );
               });
    }

    private void exhibirEstadisticasDeDescargasDeLosLibrosRegistrados() {
//        DoubleSummaryStatistics est = repositorio.findAll().stream().filter(d -> d.getNumeroDeDescargas() > 0)
//                .collect(Collectors.summarizingDouble(Libro::getNumeroDeDescargas));
//        System.out.println("Cantidad media de descargas: %.2f" + est.getAverage());
//        System.out.println("Cantidad máxima de descargas: %.2f" + est.getMax());
//        System.out.println("Cantidad mínima de descargas: %.2f" + est.getMin());
//        System.out.println("Cantidad de registros evaluados para calcular las estadisticas: " + est.getCount());

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
        } else {
            System.out.println("Libro no encontrado. Le sugiero que compruebe si escribió el título del libro correctamente e intente otra vez.");
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
        repositorio.findAll().stream().forEach(
                libro -> {
                    System.out.println(
                                    "----- LIBRO -----" +
                                    "\n Titulo: " + libro.getTitulo() +
                                    "\n Autor: " + libro.getAutor().getNombre() +
                                    "\n Idiomas: " + libro.getIdiomas() +
                                    "\n Numero de descargas: " + libro.getNumeroDeDescargas() +
                                    "\n-----------------\n"
                    );
                }
        );
    }

    /* Consultas con fechas (Autores) */

    private void listarAutoresPorAnoDeNacimiento (){
        System.out.println("Ingrese el año de nacimiento del autor(es) que desea buscar");
        var anoNacimiento = teclado.nextLine();
        Year anoNacimientoConvertido = Year.parse(anoNacimiento);

        if(anoNacimientoConvertido!=null){
            List<Autor> autoresBuscados = autorRepository.findByFechaDeNacimiento(anoNacimientoConvertido);

            if(!autoresBuscados.isEmpty()){
                autoresBuscados.forEach(autor -> {
                    System.out.println(autor.toString());
                });
            } else {
                System.out.println("No se han encontrado autores nacidos este año.");
            }
        } else {
            System.out.println("Formato de año no válido. Introduzca un valor entero (positivo o negativo)");
        }
    }

    private void listarAutoresVivosEnAnoEspecifico(){
        System.out.println("Ingrese el año vivo de autor(es) que desea buscar");
        var anoVivo = teclado.nextLine();
        Year anoVivoConvertido = Year.parse(anoVivo);

        if(anoVivoConvertido!=null){
            List<Autor> autoresBuscados = autorRepository.findByFechaDeNacimientoLessThanEqualAndFechaDeFallecimientoGreaterThan(anoVivoConvertido, anoVivoConvertido);

            if(!autoresBuscados.isEmpty()){
                autoresBuscados.forEach(autor -> {
                    System.out.println(autor.toString());
                });
            } else {
                System.out.println("No se han encontrado autores vivos este año.");
            }
        } else {
            System.out.println("Formato de año no válido. Introduzca un valor entero (positivo o negativo)");
        }
    }

    private void listarAutoresPorIntervaloDeAnos(){
        System.out.println("Ingrese el año inicial del intervalo");
        var anoInicial = teclado.nextLine();
        Year anoInicialConvertido = Year.parse(anoInicial);

        System.out.println("Ingrese el año final del intervalo");
        var anoFinal = teclado.nextLine();
        Year anoFinalConvertido = Year.parse(anoFinal);

        if(anoInicialConvertido!=null && anoFinalConvertido!=null){
            List<Autor> autoresBuscados = autorRepository.findByFechaDeNacimientoGreaterThanEqualAndFechaDeFallecimientoLessThan(anoInicialConvertido, anoFinalConvertido);

            if(!autoresBuscados.isEmpty()){
                autoresBuscados.forEach(autor -> {
                    System.out.println(autor.toString());
                });
            } else {
                System.out.println("No se han encontrado autores vivos en este intervalo de años.");
            }
        } else {
            System.out.println("Formato de año no válido. Introduzca un valor entero (positivo o negativo)");
        }
    }
}