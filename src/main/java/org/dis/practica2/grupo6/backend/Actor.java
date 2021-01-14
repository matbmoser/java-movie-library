package org.dis.practica2.grupo6.backend;

public class Actor {

    //Atributos
    String nombre;
    String URL_Wiki;

    //Constructores, getters/setters y overrides
    public Actor(String nombre, String URL_Wiki) {
        this.nombre = nombre;
        this.URL_Wiki = URL_Wiki;
    }

    public Actor() {
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEnlace() {
        return URL_Wiki;
    }

    public void setEnlace(String URL_Wiki) {
        this.URL_Wiki = URL_Wiki;
    }
}
