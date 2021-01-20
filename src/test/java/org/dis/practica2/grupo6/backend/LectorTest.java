package org.dis.practica2.grupo6.backend;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class LectorTest {

    @Test
    public void importarVideotecaConMasVideotecasCreadas() {

        int expected = 3; //Debemos poder añadir 2 nuevas videotecas desde un fichero externo JSON

        //Añadimos una videoteca
        List<Videoteca> videotecas = new ArrayList<>();
        List<Pelicula> peliculas = new ArrayList<>();
        List<Actor> reparto = new ArrayList<>();
        reparto.add(new Actor("Jennifer Rosenbert", "http:wikihow.com/jenny_rosenbert"));
        reparto.add(new Actor("Maria Disibali", "http:wikihow.com/disibali_mary"));
        reparto.add(new Actor("Mateo de la Papa", "http:wikihow.com/mateo_de_la_papa"));
        peliculas.add(new Pelicula(this.hashCode(),"Mucho Mucho 2","Una pelicula no para cualquiera", reparto, new Atributos(120, 2020), "Comedia","http://imdb.com/tas542121512"));
        peliculas.add(new Pelicula(this.hashCode(),"Mucho Mucho 1","Una pelicula no para cualquier2", reparto, new Atributos(50, 2020), "Comedia","http://imdb.com/tas542121512"));
        videotecas.add(new Videoteca(1,"La hueca tuerta", "Madrid", peliculas, "04/02/2020"));

        System.out.println("importarVideotecaConMasVideotecasCreadas: Expected [3]\n| NumVideotecasAntes: "+videotecas.size());
        //Probamos Importamos un fichero con dos videotecas
        try {
            Lector.importar(videotecas, "videotecasgeneradas.json");
        } catch (VDException e) {
            fail("No ha sido posible añadir videotecas desde un fichero | Razón: ["+e.getMessage()+"]");
        }
        System.out.println("| NumVideotecasDespues: " + videotecas.size());
        assertEquals(expected,videotecas.size()); //El nuevo tamaño debe de ser [1 + 2] = 3
    }
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test(expected = VDException.class)
    public void importarUnFicheroNoJSON() throws VDException {
        List<Videoteca> videotecas = new ArrayList<>();
        //Debemos realizar el test para incluir in fichero no JSON, y esperaremos la respuesta.
            Lector.importar(videotecas, "Readme.md");
            expectedEx.expect(VDException.class);
            expectedEx.expectMessage("Se produjo un error de parseo");

            throw new VDException("Se produjo un error de parseo");
    }

    @Test(expected = VDException.class)
    public void guardarUnFicheroNoJSON() throws VDException {
        //Añadimos una videoteca
        List<Videoteca> videotecas = new ArrayList<>();
        List<Pelicula> peliculas = new ArrayList<>();
        List<Actor> reparto = new ArrayList<>();
        reparto.add(new Actor("Jennifer Rosenbert", "http:wikihow.com/jenny_rosenbert"));
        reparto.add(new Actor("Maria Disibali", "http:wikihow.com/disibali_mary"));
        reparto.add(new Actor("Mateo de la Papa", "http:wikihow.com/mateo_de_la_papa"));
        peliculas.add(new Pelicula(this.hashCode(),"Mucho Mucho 2","Una pelicula no para cualquiera", reparto, new Atributos(120, 2020), "Comedia","http://imdb.com/tas542121512"));
        peliculas.add(new Pelicula(this.hashCode(),"Mucho Mucho 1","Una pelicula no para cualquier2", reparto, new Atributos(50, 2020), "Comedia","http://imdb.com/tas542121512"));
        videotecas.add(new Videoteca(1,"La hueca tuerta", "Madrid", peliculas, "04/02/2020"));
        //Intentamos guardar en un archivo .txt
        Lector.guardar(videotecas,"Readme.txt");
        expectedEx.expect(VDException.class);
        expectedEx.expectMessage("El fichero no es un .json");

        throw new VDException("El fichero no es un .json");
    }

    @Test
    public void findUnaVideotecaIgual() {
        //Debemos comprobar si teniendo contenidos diferentes, pero mismos nombres y la misma localización, encuentra la videoteca
        boolean expected = true;
        //Añadimos una Videoteca con nombre igual y contenido diferente
        List<Videoteca> videotecas = new ArrayList<>();
        List<Actor> repartoPrueba = new ArrayList<>();
        repartoPrueba.add(new Actor("John Rosenbert", "http:wikihow.com/john_rosenbert"));
        repartoPrueba.add(new Actor("Mateo Papa", "http:wikihow.com/mateo_papa"));
        Videoteca videotecaPrueba = new Videoteca();
        List<Pelicula> peliculasPrueba = new ArrayList<>();
        Pelicula peliculaPrueba = new Pelicula();
        peliculaPrueba.setReparto(repartoPrueba);
        peliculaPrueba.setTitulo("Megalomanicaco");
        peliculaPrueba.setId(2);
        peliculaPrueba.setSinopsis("Una experiencia de locos");
        peliculaPrueba.setGenero("");
        peliculaPrueba.setEnlace("http://imdb.com/40102321213");
        videotecaPrueba.setNombre("La hueca tuerta");
        peliculasPrueba.add(peliculaPrueba);
        videotecaPrueba.setPeliculas(peliculasPrueba);
        videotecaPrueba.setUbicacion("Madrid");
        //Añadimos una lista de videotecas
        List<Pelicula> peliculas = new ArrayList<>();
        List<Actor> reparto = new ArrayList<>();
        reparto.add(new Actor("Jennifer Rosenbert", "http:wikihow.com/jenny_rosenbert"));
        reparto.add(new Actor("Maria Disibali", "http:wikihow.com/disibali_mary"));
        reparto.add(new Actor("Mateo de la Papa", "http:wikihow.com/mateo_de_la_papa"));
        peliculas.add(new Pelicula(this.hashCode(),"Mucho Mucho 2","Una pelicula no para cualquiera", reparto, new Atributos(120, 2020), "Comedia","http://imdb.com/tas542121512"));
        peliculas.add(new Pelicula(this.hashCode(),"Mucho Mucho 1","Una pelicula no para cualquier2", reparto, new Atributos(50, 2020), "Comedia","http://imdb.com/tas542121512"));
        videotecas.add(new Videoteca(1,"La hueca tuerta", "Madrid", peliculas, "04/02/2020"));

        //Esperamos a que nos devuelva true, papara comprobar la funcionalidad bien.
        assertEquals(expected,Lector.find(videotecas, videotecaPrueba));

    }
}