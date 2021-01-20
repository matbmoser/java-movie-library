package org.dis.practica2.grupo6.frontend;

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.WrappedSession;
import com.vaadin.ui.UI;
import org.dis.practica2.grupo6.backend.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.servlet.http.Cookie;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.*;

import static org.junit.Assert.*;

public class MyUITest {
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void hasDigits() {
        assertTrue(MyUI.hasDigits("ejemp1o cadena"));
    }

    @Test
    public void isNumeric() {
        assertTrue(MyUI.isNumeric("121012"));
    }

    @Test(expected = VDException.class)
    public void select() throws VDException {
        MyUI ui = new MyUI();
        VaadinRequest vaadinRequest = new VaadinRequest() {

            public String getParameter(String s) {
                return null;
            }

            public Map<String, String[]> getParameterMap() {
                return null;
            }

            public int getContentLength() {
                return 0;
            }

            public InputStream getInputStream() throws IOException {
                return null;
            }

            public Object getAttribute(String s) {
                return null;
            }

            public void setAttribute(String s, Object o) {

            }

            public String getPathInfo() {
                return null;
            }

            public String getContextPath() {
                return null;
            }

            public WrappedSession getWrappedSession() {
                return null;
            }

            public WrappedSession getWrappedSession(boolean b) {
                return null;
            }

            public String getContentType() {
                return null;
            }

            public Locale getLocale() {
                return null;
            }

            public String getRemoteAddr() {
                return null;
            }

            public boolean isSecure() {
                return false;
            }

            public String getHeader(String s) {
                return null;
            }

            public VaadinService getService() {
                return null;
            }

            public Cookie[] getCookies() {
                return new Cookie[0];
            }

            public String getAuthType() {
                return null;
            }

            public String getRemoteUser() {
                return null;
            }

            public Principal getUserPrincipal() {
                return null;
            }

            public boolean isUserInRole(String s) {
                return false;
            }

            public void removeAttribute(String s) {

            }

            public Enumeration<String> getAttributeNames() {
                return null;
            }

            public Enumeration<Locale> getLocales() {
                return null;
            }

            public String getRemoteHost() {
                return null;
            }

            public int getRemotePort() {
                return 0;
            }

            public String getCharacterEncoding() {
                return null;
            }

            public BufferedReader getReader() throws IOException {
                return null;
            }

            public String getMethod() {
                return null;
            }

            public long getDateHeader(String s) {
                return 0;
            }

            public Enumeration<String> getHeaderNames() {
                return null;
            }

            public Enumeration<String> getHeaders(String s) {
                return null;
            }
        };
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

        ui.select(vaadinRequest,-1, videotecas);

        expectedEx.expect(VDException.class);
        expectedEx.expectMessage("La Videoteca seleccionada no es válida!");

        throw new VDException("La Videoteca seleccionada no es válida!");
    }
}