package org.dis.practica2.grupo6.backend;

import com.google.gson.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Lector {

    public static void importar(List<Videoteca> videotecas, String NOM_FICHERO) throws VDException { //Parsea el documento json si existe

        //Instanciamos Gson
        File archivo = new File(NOM_FICHERO);
        if (!archivo.exists()) { //Si no existe
            throw new VDException("El fichero no existe!");
        }
        else { //Si existe
            try { //Probamos parsear el documento
                JsonElement Jsonobj = JsonParser.parseReader(new FileReader(NOM_FICHERO));
                JsonObject jsonObject = Jsonobj.getAsJsonObject();
                JsonArray jsonVideotecas = jsonObject.get("videotecas").getAsJsonArray(); //Recogemos las videotecas
                for (JsonElement jsonElementoV : jsonVideotecas) {
                    Videoteca videoteca = new Videoteca();
                    List<Pelicula> peliculas = new ArrayList<>(); //Creamos una lista de peliculas
                    videoteca.setNombre(jsonElementoV.getAsJsonObject().get("nombre").getAsString());
                    videoteca.setUbicacion(jsonElementoV.getAsJsonObject().get("ubicacion").getAsString());
                    JsonArray jsonPelis = jsonElementoV.getAsJsonObject().get("peliculas").getAsJsonArray(); //Recogemos las peliculas
                    for (JsonElement jsonElementoP : jsonPelis) { //Añadimos las pelis
                        Pelicula pelicula = new Pelicula();
                        List<Actor> reparto = new ArrayList<>(); //Creamos una nueva lista de actores
                        Atributos atributos = new Atributos();
                        pelicula.setTitulo(jsonElementoP.getAsJsonObject().get("titulo").getAsString());
                        pelicula.setSinopsis(jsonElementoP.getAsJsonObject().get("sinopsis").getAsString());
                        JsonObject jsonAtributos = jsonElementoP.getAsJsonObject().get("atributos").getAsJsonObject();
                        atributos.setDuracion(jsonAtributos.get("min_dur").getAsInt());
                        atributos.setAno(jsonAtributos.get("ano_estreno").getAsInt());
                        pelicula.setAtributos(atributos);
                        JsonArray jsonActores = jsonElementoP.getAsJsonObject().get("reparto").getAsJsonArray(); //Recogemos las peliculas
                        for (JsonElement jsonElementoA : jsonActores) { //Añadimos los actores
                            Actor actor = new Actor();
                            actor.setNombre(jsonElementoA.getAsJsonObject().get("nombre").getAsString());
                            actor.setEnlace(jsonElementoA.getAsJsonObject().get("URL_Wiki").getAsString());
                            reparto.add(actor);
                        }
                        pelicula.setReparto(reparto); //Añadimos el reparto
                        pelicula.setGenero(jsonElementoP.getAsJsonObject().get("genero").getAsString());
                        pelicula.setEnlace(jsonElementoP.getAsJsonObject().get("enlaceIMDB").getAsString());
                        pelicula.setId(pelicula.hashCode()); //Generamos una id unica

                        peliculas.add(pelicula);
                    }
                    videoteca.setPeliculas(peliculas); //Añadimos las peliculas
                    videoteca.setFecha(jsonElementoV.getAsJsonObject().get("fecha_ultima_actualizacion").getAsString());
                    if(!find(videotecas, videoteca)) {
                        videotecas.add(videoteca);
                        videoteca.setId(videotecas.size());
                    }
                }
            } catch (Exception e) { //Si se produce un error de parseo lo captamos
                throw new VDException(e.getMessage());
            }
        }
    }
    public static List<Videoteca> guardar(List<Videoteca> videotecas,String NOM_FICHERO) throws VDException {

        //Instanciamos Gson
        Gson gson = new Gson();
        String nombre = NOM_FICHERO;
        //Pasamos el objeto java a un string json
        boolean b = false;
        String misvideotecas = gson.toJson(videotecas);
        misvideotecas = "{ \"videotecas\":"+ misvideotecas +"}";
        try {
            File file = new File(nombre);
            try(FileWriter writer = new FileWriter(file, false);
            BufferedWriter bw = new BufferedWriter(writer)){
                bw.write(misvideotecas);
                //Si se añade
            }catch (Exception e){
                throw new VDException("Error al escribir fichero", e.getCause());
            }
        }catch (Exception e){
            throw new VDException("¡El fichero no existe!", e.getCause());
        }
        return videotecas;
    }
    //Función responsable de verificar si una videoteca ya ha sido añadida o no independientemente de la ID
    public static boolean find(List<Videoteca> videotecas, Videoteca videoteca){
        for (Videoteca value : videotecas) {
            if (value.getNombre().equals(videoteca.getNombre()) && value.getUbicacion().equals(videoteca.getUbicacion())) {
                return true;
            }
        }
        return false;
    }
}

