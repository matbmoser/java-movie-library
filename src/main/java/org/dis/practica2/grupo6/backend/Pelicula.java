package org.dis.practica2.grupo6.backend;

import java.util.List;

public class Pelicula {

    //Atributos
    int id;
    String titulo;
    String sinopsis;
    List<Actor> reparto;
    Atributos atributos;
    String genero;
    String enlaceIMDB;

    //Si no hay genero
    public Pelicula(int id, String titulo, String sinopsis, List<Actor> reparto, Atributos atributos, String enlaceIMDB) {
        this.id = id;
        this.titulo = titulo;
        this.sinopsis = sinopsis;
        this.reparto = reparto;
        this.genero = "";
        this.atributos = atributos;
        this.enlaceIMDB = enlaceIMDB;
    }

    //Si hay genero
    public Pelicula(int id, String titulo, String sinopsis, List<Actor> reparto, Atributos atributos, String genero, String enlaceIMDB) {
        this.id = id;
        this.titulo = titulo;
        this.sinopsis = sinopsis;
        this.reparto = reparto;
        this.atributos = atributos;
        this.genero = genero;
        this.enlaceIMDB = enlaceIMDB;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Atributos getAtributos() {
        return atributos;
    }

    public void setAtributos(Atributos atributos) {
        this.atributos = atributos;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getSinopsis() {
        return sinopsis;
    }

    public void setSinopsis(String sinopsis) {
        this.sinopsis = sinopsis;
    }

    public List<Actor> getReparto() {
        return reparto;
    }

    public String getActores(){
        StringBuilder actores = null;
        for (int i = 0; i < reparto.size(); i++) {
            actores.insert(i,reparto.get(i).nombre);
        }
        return actores.toString();
    }

    public void setReparto(List<Actor> reparto) {
        this.reparto = reparto;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getEnlace() {
        return enlaceIMDB;
    }

    public void setEnlace(String enlaceIMDB) {
        this.enlaceIMDB = enlaceIMDB;
    }

    public Pelicula() {
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
}
