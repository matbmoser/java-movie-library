package org.dis.practica2.grupo6.backend;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Lector {

    public static List<Videoteca> importar(List<Videoteca> videotecas, String NOM_FICHERO) throws VDException { //Parsea el documento json si existe

        //Instanciamos Gson
        File archivo = new File(NOM_FICHERO);
        if (!archivo.exists()) { //Si no existe
            throw new VDException("El fichiero no existe!");
        }
        else { //Si existe
            int num = 0;
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
                    videotecas.add(videoteca);
                    videoteca.setId(videotecas.size());
                    num++; //Añadimos una videoteca más
                }
                //System.out.println("[Videotecas Importadas] " + num + " Videotecas importadas correctamente");
            } catch (Exception e) { //Si se produce un error de parseo lo captamos
                throw new VDException("Estructura Incorrecta");
                //System.out.println("[ERROR] No ha sido posible añadir las videotecas por estructura incorrecta\n");
            }
        }
        return videotecas;
    }
    public static List<Videoteca> guardar(List<Videoteca> videotecas,String NOM_FICHERO) throws IOException, VDException {

        //Instanciamos Gson
        Gson gson = new Gson();
        String nombre = NOM_FICHERO;
        Scanner sc = new Scanner(System.in); //Creamos un scanner de consola
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
                //System.out.println("[ERROR] No ha sido posible guardar el fichero\n");
            }
        }catch (Exception e){
            throw new VDException("¡El fichero no existe!", e.getCause());
            //System.out.println("[ERROR] No ha sido posible guardar el fichero\n");
        }
        return videotecas;
    }
    public static int getID(List<Videoteca> videotecas, String name){
        int id = -1;
        for(int i = 0;i<videotecas.size(); i++){
            if(videotecas.get(i).getNombre().equals(name)) {
                id = i;
                i = videotecas.size();
            }
        }
        return id;
    }
}

