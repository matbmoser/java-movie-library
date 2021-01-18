package org.dis.practica2.grupo6.frontend;

import javax.lang.model.type.TypeMirror;
import javax.servlet.annotation.WebServlet;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;

import com.vaadin.annotations.*;
import com.vaadin.annotations.JavaScript;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.*;
import com.vaadin.shared.Position;
import com.vaadin.ui.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import org.dis.practica2.grupo6.backend.*;


import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of an HTML page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@JavaScript("https://code.jquery.com/jquery-3.5.1.slim.min.js")
@JavaScript("https://cdn.jsdelivr.net/npm/popper.js@1.16.1/dist/umd/popper.min.js")
@StyleSheet("https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css")
@JavaScript("https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js")
@SuppressWarnings("serial")
@Theme("mytheme")
public class MyUI extends UI {
    @Override
    protected void init(VaadinRequest vaadinRequest) {
        Page.getCurrent().setTitle("Gestor de Videotecas");

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
            try {
                select(vaadinRequest,e.getItem().getId()-1,Videotecas);
            } catch (VDException exception) {
                Notification notif = new Notification("Lo sentimos",exception.getMessage()+" Intente otra vez...",Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(20000);
                notif.setPosition(Position.TOP_CENTER);
                notif.setIcon(VaadinIcons.WARNING);
                notif.show(Page.getCurrent());
            }
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
        Button save = new Button("Guardar");
        Upload saveArchivo = new Upload();
        saveArchivo.setButtonCaption("Guardar");
        //Bloqueamos el control de errores original, ya que lo realizamos nosotros.
        saveArchivo.setErrorHandler(errorEvent -> {});
        saveArchivo.setCaption("Seleccione un Archivo para Guardar");
        saveArchivo.setAcceptMimeTypes("application/json");
        saveArchivo.setImmediateMode(false);
        saveArchivo.addStartedListener(startedEvent -> {

            if(startedEvent.getMIMEType().equals("application/json")){ //Comprobamos si el formato es JSON
                guardar(Videotecas, startedEvent.getFilename());
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
        TextField nombreFich = new TextField();
        nombreFich.setCaption("Crear un fichero:");
        nombreFich.setPlaceholder("Nombre del Fichero");
        save.addClickListener(e->{
            if(nombreFich.isEmpty()){
                Notification notif = new Notification("Warning","Ningún nombre de archivo seleccionado!",Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(2000);
                notif.setPosition(Position.TOP_CENTER);
                notif.setIcon(VaadinIcons.WARNING);
                notif.show(Page.getCurrent());
            }else{
                guardar(Videotecas, nombreFich.getValue() + ".json");
                String NOM_FICHERO = nombreFich.getValue()+".json";
                Button downloadButton = new Button("Download File");
                downloadButton.setIcon(VaadinIcons.DOWNLOAD);
                final AdvancedFileDownloader downloader = new AdvancedFileDownloader();
                downloader.addAdvancedDownloaderListener(new AdvancedFileDownloader.AdvancedDownloaderListener() {

                            /**
                             * This method will be invoked just before the download
                             * starts. Thus, a new file path can be set.
                             *
                             * @param downloadEvent
                             */
                            @Override
                            public void beforeDownload(AdvancedFileDownloader.DownloaderEvent downloadEvent) {

                                String filePath = NOM_FICHERO;
                                downloader.setFilePath(filePath);
                            }

                        });
                downloader.extend(downloadButton);
                downloadButton.addClickListener(event ->{
                        Notification notif = new Notification("Generando Fichero", "Descargando: " + NOM_FICHERO+ " en 10 segundos se borrará", Notification.Type.HUMANIZED_MESSAGE);
                        notif.setDelayMsec(2000);
                        notif.setPosition(Position.BOTTOM_LEFT);
                        notif.setIcon(VaadinIcons.SPINNER);
                        notif.show(Page.getCurrent());
                        //Creamos un hilo que será responsable de borrar el archivo creado despues de 10000 milisegundos
                        Thread thread = new Thread(() -> {
                            try {
                                Thread.sleep(10000);
                                new File(NOM_FICHERO).delete();

                            } catch (InterruptedException interruptedException) {
                                new File(NOM_FICHERO).delete();
                            }
                            optContainer.removeAllComponents();
                            optContainer.addComponent(form2);
                        });
                        thread.start();
                });
                optContainer.addComponent(downloadButton);
            }
        });
        form2.addComponents(nombreFich,save);
            ListSelect<String> select = new ListSelect<>("Elige una opción de guardado: ");

            select.setItems("Nuevo Fichero", "Fichero Existente");

            select.setRows(2);
            select.select("Nuevo Fichero");
            optContainer.removeAllComponents();
            select.addValueChangeListener(event -> {
                if (event.getValue().toString().equals("[Nuevo Fichero]")) {
                    optContainer.removeAllComponents();
                    optContainer.addComponents(form2);

                }else{
                    optContainer.removeAllComponents();
                    optContainer.addComponents(saveArchivo);
                }

            });
        // Shorthand
        // Execute JavaScript in the currently processed page
        optContainer.addComponent(form2);
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
    //Añade el layout de peliculas
    private void peliculas(Videoteca videoteca, VaadinRequest vaadinRequest){
        Page.getCurrent().setTitle("Videoteca: "+videoteca.getNombre());
        final VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        final HorizontalLayout gridContainer = new HorizontalLayout();
        final HorizontalLayout addContainer = new HorizontalLayout();
        final HorizontalLayout returnContainer = new HorizontalLayout();
        List<Pelicula> peliculas = videoteca.getPeliculas();
        Grid<Pelicula> gridPeliculas = new Grid<>();
        gridPeliculas.setItems(peliculas);
        gridPeliculas.addColumn(Pelicula::getId).setCaption("Id");
        gridPeliculas.addColumn(Pelicula::getTitulo).setCaption("Titulo");
        gridPeliculas.addColumn(Pelicula::getSinopsis).setCaption("Sinopsis");
        gridPeliculas.addColumn(Pelicula::getGenero).setCaption("Género");
        gridPeliculas.addColumn(Pelicula::getEnlace).setCaption("Enlace");
        gridContainer.setSizeFull();
        gridPeliculas.setSizeFull();
        gridContainer.addComponent(gridPeliculas);
        gridContainer.setCaption("Ver");
        addContainer.setCaption("Añadir");
        returnContainer.setCaption("Volver");
        TabSheet tabpelis = new TabSheet();
        tabpelis.addTab(gridContainer, "Ver", VaadinIcons.CHECK_SQUARE);
        tabpelis.addTab(addContainer, "Añadir", VaadinIcons.PLUS);
        tabpelis.addTab(returnContainer, "Volver", VaadinIcons.ARROW_LEFT);
        tabpelis.addSelectedTabChangeListener(listener->{
            if(listener.getTabSheet().getSelectedTab().getCaption().equals("Volver")){
                init(vaadinRequest);
            }
        });
        layout.addComponents(tabpelis);
        setContent(layout);
    }
    private void select(VaadinRequest vaadinRequest, int id, List<Videoteca> videotecas) throws VDException {
        if(id < 0){
            throw new VDException("La Videoteca seleccionada no es válida!");
        }else{
            try{
                if(videotecas.get(id) != null){
                    peliculas(videotecas.get(id), vaadinRequest);
                }else{
                    throw new VDException("La Videoteca seleccionada no existe!");
                }
            }catch (Exception e){
                throw new VDException("La Videoteca seleccionada no existe!");
            }
        }
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
    public static void guardar(List<Videoteca> videotecas, String NOM_FICHERO) {
        try {
            if (videotecas.size() > 0) {
                Lector.guardar(videotecas, NOM_FICHERO);
                Notification notif = new Notification("<span style='color:green'>Sucess</span>", "["+NOM_FICHERO+"] Añadido Correctamente", Notification.Type.HUMANIZED_MESSAGE);
                notif.setDelayMsec(10000);
                notif.setHtmlContentAllowed(true);
                notif.setPosition(Position.BOTTOM_RIGHT);
                notif.setIcon(VaadinIcons.CHECK);
                notif.show(Page.getCurrent());
            } else {
                Notification notif = new Notification("Error", "No hay videotecas para añadir!", Notification.Type.ERROR_MESSAGE);
                notif.setDelayMsec(20000);
                notif.setPosition(Position.TOP_CENTER);
                notif.setIcon(VaadinIcons.EXCLAMATION);
                notif.show(Page.getCurrent());
            }

        } catch (VDException e) {
            Notification notif = new Notification("Lo sentimos", e.getMessage() + " Intente otra vez...", Notification.Type.WARNING_MESSAGE);
            notif.setDelayMsec(20000);
            notif.setPosition(Position.TOP_CENTER);
            notif.setIcon(VaadinIcons.WARNING);
            notif.show(Page.getCurrent());
        }
    }

}
