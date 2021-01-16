package org.dis.practica2.grupo6.frontend;

import javax.servlet.annotation.WebServlet;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;


import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.*;
import com.vaadin.shared.Position;
import com.vaadin.ui.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.TextField;
import org.dis.practica2.grupo6.backend.Lector;
import org.dis.practica2.grupo6.backend.VDException;
import org.dis.practica2.grupo6.backend.Videoteca;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        final VerticalLayout optContainer = new VerticalLayout();
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
        gridVideotecas.addItemClickListener(e -> {
           //select(Videotecas, e.getItem().getId());
        });
        gridContainer.addComponent(gridVideotecas);
        FormLayout form = new FormLayout();


        Upload upload = new Upload();
        upload.setSizeFull();
        upload.setImmediateMode(false);
        //Bloqueamos el control de errores original, ya que lo realizamos nosotros.
        upload.setErrorHandler(errorEvent -> {});
        //Fin bloqueo de errores
        upload.addStartedListener(startedEvent -> {

            if(startedEvent.getMIMEType().equals("application/json")){ //Comprobamos si el formato es JSON
                importar(Videotecas, startedEvent.getFilename());
            }else if(startedEvent.getFilename().equals("")){ //En el caso que no se importe ningún archivo
                Notification notif = new Notification("Warning","Ningún archivo seleccionado!",Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(2000);
                notif.setPosition(Position.TOP_CENTER);
                notif.setIcon(VaadinIcons.WARNING);
                notif.show(Page.getCurrent());
            }
            else{//En el caso que se importe un archivo no JSON
                Notification notif = new Notification("Warning","El archivo no tiene formato JSON, Intente otra vez...",Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(2000);
                notif.setPosition(Position.TOP_CENTER);
                notif.setIcon(VaadinIcons.WARNING);
                notif.show(Page.getCurrent());
            }
        });
        upload.setCaptionAsHtml(true);
        upload.setButtonCaption("Importar");
        upload.setAcceptMimeTypes("application/json");
        form.addComponent(upload);
        importContainer.addComponent(form);
        FormLayout form2 = new FormLayout();
        Upload save = new Upload();
        save.setButtonCaption("Guardar");
        save.setId("saveDirectorio");
        save.setCaption("Seleccione un directorio");
        save.setImmediateMode(false);
        Upload saveArchivo = new Upload();
        saveArchivo.setButtonCaption("Guardar");
        saveArchivo.setCaption("Seleccione un Archivo para Guardar");
        saveArchivo.setAcceptMimeTypes("application/json");
        TextField nombreFich = new TextField();
        nombreFich.setCaption("Crear un fichero:");
        nombreFich.setPlaceholder("Nombre del Fichero");

            ListSelect<String> select = new ListSelect<>("Elige una opción de guardado: ");

            select.setItems("Nuevo Fichero", "Fichero Existente");

            select.setRows(2);
            select.select("Nuevo Fichero");
            optContainer.removeAllComponents();
            optContainer.addComponents(nombreFich, save);
            select.addValueChangeListener(event -> {
                if (event.getValue().toString().equals("[Nuevo Fichero]")) {
                    optContainer.removeAllComponents();
                    optContainer.addComponents(nombreFich, save);

                }else{
                    optContainer.removeAllComponents();
                    optContainer.addComponents(saveArchivo);
                }
            });

        saveContainer.addComponents(select, optContainer);
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
            int oldSize = videotecas.size();
            Lector.importar(videotecas, NOM_FICHERO);
            if((videotecas.size() - oldSize) == 0){
                Notification notif = new Notification("Error", "Estas videotecas ya están añadidas!",Notification.Type.ERROR_MESSAGE);
                notif.setDelayMsec(20000);
                notif.setPosition(Position.TOP_CENTER);
                notif.setIcon(VaadinIcons.EXCLAMATION);
                notif.show(Page.getCurrent());
            }else {
                Notification notif = new Notification("<span style='color:green'>Sucess</span>", "[" + (videotecas.size() - oldSize) + "] Videotecas cargadas correctamente ", Notification.Type.HUMANIZED_MESSAGE);
                notif.setDelayMsec(10000);
                notif.setHtmlContentAllowed(true);
                notif.setPosition(Position.BOTTOM_RIGHT);
                notif.setIcon(VaadinIcons.CHECK);
                notif.show(Page.getCurrent());
            }
        }catch(VDException e) {
            Notification notif = new Notification("Lo sentimos",e.getMessage()+" Intente otra vez...",Notification.Type.WARNING_MESSAGE);
            notif.setDelayMsec(20000);
            notif.setPosition(Position.TOP_CENTER);
            notif.setIcon(VaadinIcons.WARNING);
            notif.show(Page.getCurrent());
        }


    }
}
