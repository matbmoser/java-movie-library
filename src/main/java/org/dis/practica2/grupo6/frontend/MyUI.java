package org.dis.practica2.grupo6.frontend;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.Position;
import com.vaadin.ui.*;
import org.dis.practica2.grupo6.backend.Lector;
import org.dis.practica2.grupo6.backend.VDException;
import org.dis.practica2.grupo6.backend.Videoteca;

import java.util.ArrayList;
import java.util.List;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of an HTML page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
public class MyUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        final HorizontalLayout gridContainer = new HorizontalLayout();
        final HorizontalLayout importContainer = new HorizontalLayout();
        final HorizontalLayout saveContainer = new HorizontalLayout();
        final VerticalLayout formContainer = new VerticalLayout();
        TabSheet tabsheet = new TabSheet();
        TextField textField = new TextField();
        List<Videoteca> Videotecas = new ArrayList<>();
        importar(Videotecas, "peliculas.json");
        Grid<Videoteca> gridVideotecas = new Grid<>();
        gridVideotecas.setItems(Videotecas);
        gridVideotecas.addColumn(Videoteca::getId).setCaption("Id");
        gridVideotecas.addColumn(Videoteca::getNombre).setCaption("Nombre");
        gridVideotecas.addColumn(Videoteca::getGeneros).setCaption("Generos");
        gridVideotecas.addColumn(Videoteca::getUbicacion).setCaption("Ubicación");
        gridVideotecas.addColumn(Videoteca::getFecha).setCaption("Modified");
        gridContainer.setSizeFull();
        gridVideotecas.setSizeFull();
        gridContainer.addComponent(gridVideotecas);

        tabsheet.addTab(gridContainer, "Select", VaadinIcons.CHECK_SQUARE);
        tabsheet.addTab(importContainer, "Importar", VaadinIcons.UPLOAD);
        tabsheet.addTab(saveContainer, "Guardar", VaadinIcons.DOWNLOAD);

        layout.addComponents(tabsheet);
        setContent(layout);
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
    //Inicio Función importar
    public static void importar(List<Videoteca> videotecas, String NOM_FICHERO){
        try {
            Lector.importar(videotecas, NOM_FICHERO);
            Notification notif = new Notification("<span style='color:green'>Sucess</span>","Videotecas cargadas correctamente ",Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(20000);
            notif.setHtmlContentAllowed(true);
            notif.setPosition(Position.BOTTOM_RIGHT);
            notif.setIcon(VaadinIcons.CHECK);
            notif.show(Page.getCurrent());
        }catch(VDException e) {
            Notification notif = new Notification("Lo sentimos",e.getMessage()+" Intente otra vez...",Notification.Type.WARNING_MESSAGE);
            notif.setDelayMsec(20000);
            notif.setPosition(Position.TOP_CENTER);
            notif.setIcon(VaadinIcons.WARNING);
            notif.show(Page.getCurrent());
        }

    }
}
