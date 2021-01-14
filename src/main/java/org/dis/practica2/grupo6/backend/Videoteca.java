package org.dis.practica2.grupo6.backend;

import java.util.ArrayList;
import java.util.List;

public class Videoteca {

    //Atributos
    int id;
    String nombre;
    String ubicacion;
    List<Pelicula> peliculas;
    String fecha_ultima_actualizacion;

    //Constructores, getters/setters y overrides
    public Videoteca(int id,String nombre, String ubicacion, List<Pelicula> peliculas, String fecha_ultima_actualizacion) {
        this.id = id;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.peliculas = peliculas;
        this.fecha_ultima_actualizacion = fecha_ultima_actualizacion;
    }

    public Videoteca() {
        super();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
    public void setId(int id) { this.id = id; }

    public int getId() { return id; }

    public String getNombre() {
        return nombre;
    }

    public List<String> getGeneros() {
        List<String> generos = new ArrayList<>();
        if(!peliculas.isEmpty()){
            for(Pelicula pelicula:peliculas){
                if(pelicula.getGenero().isEmpty()){
                    if(!generos.contains("No calificada")){
                        generos.add("No calificada");
                    }
                }else{
                    if(!generos.contains(pelicula.getGenero())) {
                        generos.add(pelicula.getGenero());
                    }
                }
            }
        }else{
            generos.add("No calificada");
        }
        return generos;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public List<Pelicula> getPeliculas() {
        return peliculas;
    }

    public void setPeliculas(List<Pelicula> peliculas) {
        this.peliculas = peliculas;
    }

    public String getFecha() {
        return fecha_ultima_actualizacion;
    }

    public void setFecha(String fecha_ultima_actualizacion) {
        this.fecha_ultima_actualizacion = fecha_ultima_actualizacion;
    }
}
