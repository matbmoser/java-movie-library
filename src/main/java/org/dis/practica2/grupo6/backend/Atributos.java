package org.dis.practica2.grupo6.backend;

import java.util.ArrayList;
import java.util.List;

public class Atributos {

    int min_dur; //Minutos de duración
    int ano_estreno; //Año de entreno

    public int getDuracion() {
        return min_dur;
    }

    public void setDuracion(int min_dur) {
        this.min_dur = min_dur;
    }

    public int getAno() {
        return ano_estreno;
    }

    public void setAno(int ano_estreno) {
        this.ano_estreno = ano_estreno;
    }


    public Atributos(int min_dur, int ano_estreno) {
        this.min_dur = min_dur;
        this.ano_estreno = ano_estreno;
    }

    public Atributos() {
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
