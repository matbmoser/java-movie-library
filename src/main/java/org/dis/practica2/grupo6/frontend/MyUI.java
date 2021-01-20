package org.dis.practica2.grupo6.frontend;

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.server.UserError;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.Position;
import com.vaadin.ui.*;
import org.dis.practica2.grupo6.backend.*;

import javax.servlet.annotation.WebServlet;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        Label label = new Label("*Haz click sobre cualquier campo de una videoteca para ver y editar sus peliculas");
        VerticalLayout v1 = new VerticalLayout();
        gridContainer.addComponents(gridVideotecas);
        v1.addComponents(gridContainer,label);
        FormLayout form = new FormLayout();
        // Implement both receiver that saves upload in a file and
        // listener for successful upload
        Upload upload = new Upload();
        upload.setSizeFull();
        upload.setImmediateMode(false);
        //Bloqueamos el control de errores original, ya que lo realizamos nosotros.
        upload.setErrorHandler(errorEvent -> {});
        // Show uploaded file in this placeholder

        //Fin bloqueo de errores
        upload.addFinishedListener(startedEvent -> {

            if("application/json".equals(startedEvent.getMIMEType())){ //Comprobamos si el formato es JSON
                importar(Videotecas, startedEvent.getFilename());
            }else if("".equals(startedEvent.getFilename())){ //En el caso que no se importe ningún archivo
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

            if("application/json".equals(startedEvent.getMIMEType())) { //Comprobamos si el formato es JSON
                if (Videotecas.size() <= 0) {
                    Notification notif = new Notification("Error", "No hay videotecas para guardar!", Notification.Type.ERROR_MESSAGE);
                    notif.setDelayMsec(20000);
                    notif.setPosition(Position.TOP_CENTER);
                    notif.setIcon(VaadinIcons.EXCLAMATION);
                    notif.show(Page.getCurrent());
                } else {
                    guardar(Videotecas, startedEvent.getFilename(),true);
                    String NOM_FICHERO = startedEvent.getFilename();
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
                    downloadButton.addClickListener(event -> {
                        Notification notif = new Notification("Guardando en Fichero", "Descargando: " + NOM_FICHERO + " en 10 segundos se borrará", Notification.Type.HUMANIZED_MESSAGE);
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
                }else if ("".equals(startedEvent.getFilename())) { //En el caso que no se importe ningún archivo
                    Notification notif = new Notification("Warning", "Ningún archivo seleccionado!", Notification.Type.WARNING_MESSAGE);
                    notif.setDelayMsec(2000);
                    notif.setPosition(Position.TOP_CENTER);
                    notif.setIcon(VaadinIcons.WARNING);
                    notif.show(Page.getCurrent());
                } else {//En el caso que se importe un archivo no JSON
                    Notification notif = new Notification("Warning", "El archivo no tiene formato JSON, Intente otra vez...", Notification.Type.WARNING_MESSAGE);
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
            }else if(Videotecas.size() <= 0){
                Notification notif = new Notification("Error", "No hay videotecas para guardar!", Notification.Type.ERROR_MESSAGE);
                notif.setDelayMsec(20000);
                notif.setPosition(Position.TOP_CENTER);
                notif.setIcon(VaadinIcons.EXCLAMATION);
                notif.show(Page.getCurrent());
            }
            else{
                guardar(Videotecas, nombreFich.getValue() + ".json",true);
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
                if ("[Nuevo Fichero]".equals(event.getValue().toString())) {
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
        tabsheet.addTab(v1, "Select", VaadinIcons.CHECK_SQUARE);
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
    private void peliculas(Videoteca videoteca, VaadinRequest vaadinRequest, List<Videoteca> videotecas){
        Page.getCurrent().setTitle("Videoteca: "+videoteca.getNombre());
        final VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.addStyleName("scrollable");
        layout.setResponsive(true);
        final HorizontalLayout gridContainer = new HorizontalLayout();
        final VerticalLayout addContainer = new VerticalLayout();
        addContainer.addStyleName("scrollable");
        final HorizontalLayout returnContainer = new HorizontalLayout();
        final VerticalLayout actoresContainer = new VerticalLayout();
        actoresContainer.addStyleName("scrollable");
        gridContainer.setCaption("Ver");
        List<Pelicula> peliculas = videoteca.getPeliculas();
        Grid<Pelicula> gridPeliculas = new Grid<>();
        gridPeliculas.setItems(peliculas);
        gridPeliculas.addColumn(Pelicula::getId).setCaption("Id");
        gridPeliculas.addColumn(Pelicula::getTitulo).setCaption("Titulo");
        gridPeliculas.addColumn(Pelicula::getSinopsis).setCaption("Sinopsis");
        gridPeliculas.addColumn(Pelicula::getGenero).setCaption("Género");
        gridPeliculas.addColumn(Pelicula::getActores).setCaption("Actores");
        gridPeliculas.addColumn(Pelicula::getEnlace).setCaption("Enlace");
        gridContainer.setSizeFull();
        gridPeliculas.setSizeFull();
        gridPeliculas.getEditor().setEnabled(true);
        gridPeliculas.addItemClickListener(e->{
            Atributos att = e.getItem().getAtributos();
            Window subWindow = new Window("Detalles Pelicula: " +e.getItem().getTitulo());
            subWindow.setModal(true);
            subWindow.setDraggable(false);
            subWindow.setResizable(false);
            subWindow.setWidth("500px");
            subWindow.setHeight("300px");
            VerticalLayout subContent = new VerticalLayout();
            subWindow.setContent(subContent);

            // Put some components in it
            subContent.addComponent(new Label("Detalles:"));
            subContent.addComponent(new Label("Duración: "+ att.getDuracion() + " mins"));
            subContent.addComponent(new Label("Año de Estreno: "+ att.getAno()));

            Button button = new Button("Editar/Borrar");
            button.addClickListener(d->{
                subWindow.close();
                editar(e.getItem(),videoteca,vaadinRequest,videotecas);
            });
            subContent.addComponent(button);
            // Center it in the browser window
            subWindow.center();

            // Open it in the UI
            addWindow(subWindow);
        });
        gridPeliculas.getEditor().setSaveCaption("Editar/Borrar");
        gridPeliculas.getEditor().setCancelCaption("Cancelar");
        gridPeliculas.getEditor().addSaveListener(e->{
            editar(e.getBean(), videoteca,vaadinRequest, videotecas);
        });
        gridContainer.addComponent(gridPeliculas);
        Label titulo = new Label("Añadir una pelicula:");
        TextField tituloP = new TextField();
        tituloP.setPlaceholder("Titulo");
        com.vaadin.ui.TextArea sinopsisP = new com.vaadin.ui.TextArea();
        sinopsisP.setRows(4);
        sinopsisP.setPlaceholder("Sinopsis");
        TextField genero = new TextField();
        genero.setPlaceholder("Género");
        TextField enlace = new TextField();
        enlace.setPlaceholder("Enlace");
        TextField min = new TextField();
        min.setCaption("Atributos:");
        min.setValue("0");
        min.setPlaceholder("Minutos de Duración");
        min.setId("minutos");
        com.vaadin.ui.JavaScript.getCurrent().execute("document.getElementById('minutos').setAttribute('type', 'number')");
        TextField ano = new TextField();
        ano.setPlaceholder("Año de estreno");
        ano.setValue("0");
        ano.setId("anos");
        com.vaadin.ui.JavaScript.getCurrent().execute("document.getElementById('anos').setAttribute('type', 'number')");
        TextField numActores = new TextField();
        numActores.setCaption("[1-20] Actores");
        numActores.setPlaceholder("Número de Actores");
        numActores.setValue("1");
        numActores.setId("numActores");
        com.vaadin.ui.JavaScript.getCurrent().execute("document.getElementById('numActores').setAttribute('type', 'number')");
        List<Actor> actores = new ArrayList<>();
        FormLayout actorForm = new FormLayout();
        actorForm.setCaption("Actor 1");
        TextField nomActor = new TextField();
        nomActor.setPlaceholder("Nombre Actor");
        TextField enlaceActor = new TextField();
        enlaceActor.setPlaceholder("Enlace Wikipedia");
        actores.add(0,new Actor("",""));
        nomActor.setId(""+0);
        nomActor.addValueChangeListener(l->{
            if(!nomActor.getValue().isEmpty()) {
                if (hasDigits(nomActor.getValue())) {
                    nomActor.setIcon(VaadinIcons.WARNING);
                    nomActor.setStyleName("fail");
                    nomActor.setComponentError(new UserError("No se admiten números"));
                    actores.get(Integer.parseInt(nomActor.getId())).setNombre("");
                } else {
                    nomActor.setIcon(VaadinIcons.CHECK);
                    nomActor.setStyleName("sucess");
                    nomActor.setComponentError(null);
                    actores.get(Integer.parseInt(nomActor.getId())).setNombre(nomActor.getValue());
                }
            }else{
                nomActor.setIcon(VaadinIcons.WARNING);
                nomActor.setStyleName("fail");
                nomActor.setComponentError(new UserError("No puede estar vacio"));
                actores.get(Integer.parseInt(nomActor.getId())).setNombre("");
            }
        });
        nomActor.setPlaceholder("Nombre Actor");
        enlaceActor.setPlaceholder("Enlace Wikipedia");
        enlaceActor.setId(""+0);
        enlaceActor.addValueChangeListener(l->{
            if(!enlaceActor.getValue().isEmpty()) {
                enlaceActor.setIcon(VaadinIcons.CHECK);
                enlaceActor.setStyleName("sucess");
                enlaceActor.setComponentError(null);
                actores.get(Integer.parseInt(enlaceActor.getId())).setEnlace(enlaceActor.getValue());
            }else{
                enlaceActor.setIcon(VaadinIcons.WARNING);
                enlaceActor.setStyleName("fail");
                enlaceActor.setComponentError(new UserError("No puede estar vacio"));
                actores.get(Integer.parseInt(enlaceActor.getId())).setEnlace("");
            }
        });

        actoresContainer.setResponsive(true);
        actorForm.addComponents(nomActor, enlaceActor);
        numActores.addValueChangeListener(e ->{
            try{
                int num = Integer.parseInt(numActores.getValue());
                if(num > 0 && num <= 20) {
                    actoresContainer.removeAllComponents();
                    actores.clear();
                    for (int i = 0; i < num; i++) {
                        Actor actor = new Actor("","");
                        actores.add(i,actor);
                        FormLayout actorForm1 = new FormLayout();
                        actorForm1.setCaption("Actor "+ (i+1));
                        TextField nomActor1 = new TextField();
                        nomActor1.setId(""+i);
                        nomActor1.addValueChangeListener(l->{
                            if(!nomActor1.getValue().isEmpty()) {
                                if (hasDigits(nomActor1.getValue())) {
                                    nomActor1.setIcon(VaadinIcons.WARNING);
                                    nomActor1.setStyleName("fail");
                                    nomActor1.setComponentError(new UserError("No se admiten números"));
                                    actores.get(Integer.parseInt(nomActor1.getId())).setNombre("");
                                } else {
                                    nomActor1.setIcon(VaadinIcons.CHECK);
                                    nomActor1.setStyleName("sucess");
                                    nomActor1.setComponentError(null);
                                    actores.get(Integer.parseInt(nomActor1.getId())).setNombre(nomActor1.getValue());
                                }
                            }else{
                                nomActor1.setIcon(VaadinIcons.WARNING);
                                nomActor1.setStyleName("fail");
                                nomActor1.setComponentError(new UserError("No puede estar vacio"));
                                actores.get(Integer.parseInt(nomActor1.getId())).setNombre("");
                            }
                        });
                        nomActor1.setPlaceholder("Nombre Actor");
                        TextField enlaceActor1 = new TextField();
                        enlaceActor1.setPlaceholder("Enlace Wikipedia");
                        enlaceActor1.setId(""+i);
                        enlaceActor1.addValueChangeListener(l->{
                            if(!enlaceActor1.getValue().isEmpty()) {
                                    enlaceActor1.setIcon(VaadinIcons.CHECK);
                                    enlaceActor1.setStyleName("sucess");
                                    enlaceActor1.setComponentError(null);
                                    actores.get(Integer.parseInt(enlaceActor1.getId())).setEnlace(enlaceActor1.getValue());
                            }else{
                                enlaceActor1.setIcon(VaadinIcons.WARNING);
                                enlaceActor1.setStyleName("fail");
                                enlaceActor1.setComponentError(new UserError("No puede estar vacio"));
                                actores.get(Integer.parseInt(enlaceActor1.getId())).setEnlace("");
                            }
                        });
                        actorForm1.addComponents(nomActor1, enlaceActor1);
                        actoresContainer.addComponent(actorForm1);
                    }
                }else{
                    Notification notif = new Notification("Warning","El valor debe estar entre 1 y 20",Notification.Type.WARNING_MESSAGE);
                    notif.setDelayMsec(1000);
                    notif.setPosition(Position.TOP_CENTER);
                    notif.setIcon(VaadinIcons.WARNING);
                    notif.show(Page.getCurrent());
                    actoresContainer.removeAllComponents();
                    actoresContainer.addComponents(actorForm);
                }
            }catch(Exception exception){
                Notification notif = new Notification("Warning","No se admiten caracteres o que este vacio!",Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1000);
                notif.setPosition(Position.TOP_CENTER);
                notif.setIcon(VaadinIcons.WARNING);
                notif.show(Page.getCurrent());
                actoresContainer.removeAllComponents();
                actoresContainer.addComponents(actorForm);
            }
        });
        actoresContainer.addComponents(actorForm);
        Button addButton = new Button("Añadir Pelicula");
        addContainer.addComponents(titulo,tituloP, sinopsisP, genero,enlace,min, ano, numActores,addButton);
        HorizontalLayout formulario = new HorizontalLayout();
        formulario.addComponents(addContainer, actoresContainer);
        formulario.setSizeFull();
        addButton.addClickListener(click->{
            int camposValidos = 0;
            if(tituloP.getValue().isEmpty()){
                tituloP.setIcon(VaadinIcons.WARNING);
                tituloP.setStyleName("fail");
                tituloP.setComponentError(new UserError("No puede estar vacio"));
            }else{
                tituloP.setIcon(VaadinIcons.CHECK);
                tituloP.setStyleName("sucess");
                tituloP.setComponentError(null);
                camposValidos++;
            }
            if(sinopsisP.getValue().isEmpty()){
                sinopsisP.setIcon(VaadinIcons.WARNING);
                sinopsisP.setStyleName("fail");
                sinopsisP.setComponentError(new UserError("No puede estar vacio"));
            }else{
                sinopsisP.setIcon(VaadinIcons.CHECK);
                sinopsisP.setStyleName("sucess");
                sinopsisP.setComponentError(null);
                camposValidos++;
            }
            if(isNumeric(genero.getValue()) && !genero.getValue().isEmpty()){
                genero.setIcon(VaadinIcons.WARNING);
                genero.setStyleName("fail");
                genero.setComponentError(new UserError("No puede contener números"));
            }else{
                genero.setIcon(VaadinIcons.CHECK);
                genero.setStyleName("sucess");
                genero.setComponentError(null);
                camposValidos++;
            }
            if(enlace.getValue().isEmpty()){
                enlace.setIcon(VaadinIcons.WARNING);
                enlace.setStyleName("fail");
                enlace.setComponentError(new UserError("No puede estar vacio"));
            }else{
                enlace.setIcon(VaadinIcons.CHECK);
                enlace.setStyleName("sucess");
                enlace.setComponentError(null);
                camposValidos++;
            }
            if(!min.getValue().isEmpty()) {
                if (!isNumeric(min.getValue())) {
                    min.setIcon(VaadinIcons.WARNING);
                    min.setStyleName("fail");
                    min.setComponentError(new UserError("Solo se admiten numeros"));
                } else {
                    if (Integer.parseInt(min.getValue()) < 1 && Integer.parseInt(min.getValue()) > 1000) {
                        min.setIcon(VaadinIcons.WARNING);
                        min.setStyleName("fail");
                        min.setComponentError(new UserError("No puede ser 0 o negativo o mayor que 1000"));
                    } else {
                        min.setIcon(VaadinIcons.CHECK);
                        min.setStyleName("sucess");
                        min.setComponentError(null);
                        camposValidos++;
                    }
                }
            }else{
                min.setIcon(VaadinIcons.WARNING);
                min.setStyleName("fail");
                min.setComponentError(new UserError("No puede estar vacio"));
            }
            if(!ano.getValue().isEmpty()) {
                if (!isNumeric(ano.getValue())){
                    ano.setIcon(VaadinIcons.WARNING);
                    ano.setStyleName("fail");
                    ano.setComponentError(new UserError("Solo se admiten numeros"));
                }else{
                    if(Integer.parseInt(ano.getValue()) < 1850 || Integer.parseInt(ano.getValue()) > 3000) {
                        ano.setIcon(VaadinIcons.WARNING);
                        ano.setStyleName("fail");
                        ano.setComponentError(new UserError("No puede ser menor que 1850 y mayor que 3000"));
                    }else {
                        ano.setComponentError(null);
                        ano.setIcon(VaadinIcons.CHECK);
                        ano.setStyleName("sucess");
                        camposValidos++;
                    }
                }
            }else{
                ano.setIcon(VaadinIcons.WARNING);
                ano.setStyleName("fail");
                ano.setComponentError(new UserError("No puede estar vacio"));
            }
            if(!numActores.getValue().isEmpty()) {
                if (!isNumeric(numActores.getValue())){
                    numActores.setIcon(VaadinIcons.WARNING);
                    numActores.setStyleName("fail");
                    numActores.setComponentError(new UserError("Solo se admiten numeros"));
                }else{
                    if(Integer.parseInt(numActores.getValue()) < 1 || Integer.parseInt(numActores.getValue()) > 20) {
                        numActores.setIcon(VaadinIcons.WARNING);
                        numActores.setStyleName("fail");
                        numActores.setComponentError(new UserError("No puede ser menor que 1 mayor que 20"));
                    }else {
                        if(actores.isEmpty()){
                            numActores.setIcon(VaadinIcons.WARNING);
                            numActores.setStyleName("fail");
                            numActores.setComponentError(new UserError("Rellene los actores"));
                        }else{
                            int valid = 0;
                            for (Actor actor : actores) {
                                if (!("".equals(actor.getNombre())) && !isNumeric(actor.getNombre()) && !("".equals(actor.getEnlace()))) {
                                    valid++;
                                }
                            }
                            if(valid == actores.size()) {
                                numActores.setComponentError(null);
                                numActores.setIcon(VaadinIcons.CHECK);
                                numActores.setStyleName("sucess");
                                camposValidos++;
                            }else{
                                numActores.setIcon(VaadinIcons.WARNING);
                                numActores.setStyleName("fail");
                                numActores.setComponentError(new UserError("Rellene los actores correctamente"));
                            }
                        }
                    }
                }
            }else{
                numActores.setIcon(VaadinIcons.WARNING);
                numActores.setStyleName("fail");
                numActores.setComponentError(new UserError("No puede estar vacio o ser cero"));
            }
            if(camposValidos == 7){
                Notification notif = new Notification("Añadida la Pelicula", "["+tituloP.getValue()+"]", Notification.Type.HUMANIZED_MESSAGE);
                notif.setDelayMsec(10000);
                notif.setPosition(Position.BOTTOM_LEFT);
                notif.setIcon(VaadinIcons.CHECK_SQUARE);
                notif.show(Page.getCurrent());
                Pelicula peli = new Pelicula();
                peli.setTitulo(tituloP.getValue());
                peli.setSinopsis(sinopsisP.getValue());
                if(genero.getValue().isEmpty()){
                    peli.setGenero("");
                }else {
                    peli.setGenero(genero.getValue());
                }
                peli.setAtributos(new Atributos(Integer.parseInt(min.getValue()),Integer.parseInt(ano.getValue())));
                peli.setReparto(actores);
                peli.setEnlace(enlace.getValue());
                peli.setId(peli.hashCode());
                videoteca.getPeliculas().add(peli);
                videoteca.setFecha(new SimpleDateFormat("dd/MM/yyyy").format(new Date())); //Añadimos la fecha actual y la formateamos
                videotecas.add(videoteca.getId()-1,videoteca);
                guardar(videotecas, "peliculas.json",false);
                peliculas(videoteca,vaadinRequest,videotecas);
            }
        });
        returnContainer.setCaption("Volver");
        TabSheet tabpelis = new TabSheet();
        tabpelis.addTab(gridContainer, "Ver", VaadinIcons.CHECK_SQUARE);
        tabpelis.addTab(formulario, "Añadir", VaadinIcons.PLUS);
        tabpelis.addTab(returnContainer, "Volver", VaadinIcons.ARROW_LEFT);
        tabpelis.addSelectedTabChangeListener(listener->{
            if("Volver".equals(tabpelis.getSelectedTab().getCaption())){
                init(vaadinRequest);
            }
        });
        layout.addComponents(tabpelis);
        setContent(layout);
    }
    public void editar(Pelicula peli, Videoteca videoteca, VaadinRequest vaadinRequest, List<Videoteca> videotecas){
        Page.getCurrent().setTitle("Pelicula: "+peli.getTitulo());
        final VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.addStyleName("scrollable");
        layout.setResponsive(true);
        final HorizontalLayout deleteContainer = new HorizontalLayout();
        final VerticalLayout addContainer = new VerticalLayout();
        addContainer.addStyleName("scrollable");
        final HorizontalLayout returnContainer = new HorizontalLayout();
        final VerticalLayout actoresContainer = new VerticalLayout();
        actoresContainer.addStyleName("scrollable");

        Label titulo = new Label("Editar pelicula: ["+peli.getTitulo()+"]");
        TextField tituloP = new TextField();
        tituloP.setPlaceholder("Titulo");
        tituloP.setValue(peli.getTitulo());
        com.vaadin.ui.TextArea sinopsisP = new com.vaadin.ui.TextArea();
        sinopsisP.setRows(4);
        sinopsisP.setPlaceholder("Sinopsis");
        sinopsisP.setValue(peli.getSinopsis());
        TextField genero = new TextField();
        genero.setPlaceholder("Género");
        genero.setValue(peli.getGenero());
        TextField enlace = new TextField();
        enlace.setPlaceholder("Enlace");
        enlace.setValue(peli.getEnlace());
        TextField min = new TextField();
        Atributos att = peli.getAtributos();
        min.setCaption("Atributos:");
        min.setPlaceholder("Minutos de Duración");
        min.setId("minutos");
        min.setValue(String.valueOf(att.getDuracion()));
        com.vaadin.ui.JavaScript.getCurrent().execute("document.getElementById('minutos').setAttribute('type', 'number')");
        TextField ano = new TextField();
        ano.setPlaceholder("Año de estreno");
        ano.setId("anos");
        ano.setValue(String.valueOf(att.getAno()));
        com.vaadin.ui.JavaScript.getCurrent().execute("document.getElementById('anos').setAttribute('type', 'number')");
        List<Actor> actores = peli.getReparto();
        TextField numActores = new TextField();
        numActores.setCaption("[1-20] Actores");
        numActores.setPlaceholder("Número de Actores");
        numActores.setValue(String.valueOf(actores.size()));
        numActores.setId("numActores");
        com.vaadin.ui.JavaScript.getCurrent().execute("document.getElementById('numActores').setAttribute('type', 'number')");
        actoresContainer.setResponsive(true);
        for( int i = 0;i < actores.size();i++) {
            FormLayout actorForm = new FormLayout();
            actorForm.setCaption("Actor "+ i);
            TextField nomActor = new TextField();
            nomActor.setValue(actores.get(i).getNombre());
            nomActor.setPlaceholder("Nombre Actor");
            TextField enlaceActor = new TextField();
            enlaceActor.setPlaceholder("Enlace Wikipedia");
            enlaceActor.setValue(actores.get(i).getEnlace());
            nomActor.setId("" + i);
            nomActor.addValueChangeListener(l -> {
                if (!nomActor.getValue().isEmpty()) {
                    if (hasDigits(nomActor.getValue())) {
                        nomActor.setIcon(VaadinIcons.WARNING);
                        nomActor.setStyleName("fail");
                        nomActor.setComponentError(new UserError("No se admiten números"));
                        actores.get(Integer.parseInt(nomActor.getId())).setNombre("");
                    } else {
                        nomActor.setIcon(VaadinIcons.CHECK);
                        nomActor.setStyleName("sucess");
                        nomActor.setComponentError(null);
                        actores.get(Integer.parseInt(nomActor.getId())).setNombre(nomActor.getValue());
                    }
                } else {
                    nomActor.setIcon(VaadinIcons.WARNING);
                    nomActor.setStyleName("fail");
                    nomActor.setComponentError(new UserError("No puede estar vacio"));
                    actores.get(Integer.parseInt(nomActor.getId())).setNombre("");
                }
            });
            nomActor.setPlaceholder("Nombre Actor");
            enlaceActor.setPlaceholder("Enlace Wikipedia");
            enlaceActor.setId("" + 0);
            enlaceActor.addValueChangeListener(l -> {
                if (!enlaceActor.getValue().isEmpty()) {
                    enlaceActor.setIcon(VaadinIcons.CHECK);
                    enlaceActor.setStyleName("sucess");
                    enlaceActor.setComponentError(null);
                    actores.get(Integer.parseInt(enlaceActor.getId())).setEnlace(enlaceActor.getValue());
                } else {
                    enlaceActor.setIcon(VaadinIcons.WARNING);
                    enlaceActor.setStyleName("fail");
                    enlaceActor.setComponentError(new UserError("No puede estar vacio"));
                    actores.get(Integer.parseInt(enlaceActor.getId())).setEnlace("");
                }
            });
            actorForm.addComponents(nomActor, enlaceActor);
            actoresContainer.addComponent(actorForm);
        }
        numActores.addValueChangeListener(e ->{
            try{
                int num = Integer.parseInt(numActores.getValue());
                if(num > 0 && num <= 20) {
                    actoresContainer.removeAllComponents();
                    actores.clear();
                    for (int i = 0; i < num; i++) {
                        Actor actor = new Actor("","");
                        actores.add(i,actor);
                        FormLayout actorForm1 = new FormLayout();
                        actorForm1.setCaption("Actor "+ (i+1));
                        TextField nomActor1 = new TextField();
                        nomActor1.setId(""+i);
                        nomActor1.addValueChangeListener(l->{
                            if(!nomActor1.getValue().isEmpty()) {
                                if (hasDigits(nomActor1.getValue())) {
                                    nomActor1.setIcon(VaadinIcons.WARNING);
                                    nomActor1.setStyleName("fail");
                                    nomActor1.setComponentError(new UserError("No se admiten números"));
                                    actores.get(Integer.parseInt(nomActor1.getId())).setNombre("");
                                } else {
                                    nomActor1.setIcon(VaadinIcons.CHECK);
                                    nomActor1.setStyleName("sucess");
                                    nomActor1.setComponentError(null);
                                    actores.get(Integer.parseInt(nomActor1.getId())).setNombre(nomActor1.getValue());
                                }
                            }else{
                                nomActor1.setIcon(VaadinIcons.WARNING);
                                nomActor1.setStyleName("fail");
                                nomActor1.setComponentError(new UserError("No puede estar vacio"));
                                actores.get(Integer.parseInt(nomActor1.getId())).setNombre("");
                            }
                        });
                        nomActor1.setPlaceholder("Nombre Actor");
                        TextField enlaceActor1 = new TextField();
                        enlaceActor1.setPlaceholder("Enlace Wikipedia");
                        enlaceActor1.setId(""+i);
                        enlaceActor1.addValueChangeListener(l->{
                            if(!enlaceActor1.getValue().isEmpty()) {
                                enlaceActor1.setIcon(VaadinIcons.CHECK);
                                enlaceActor1.setStyleName("sucess");
                                enlaceActor1.setComponentError(null);
                                actores.get(Integer.parseInt(enlaceActor1.getId())).setEnlace(enlaceActor1.getValue());
                            }else{
                                enlaceActor1.setIcon(VaadinIcons.WARNING);
                                enlaceActor1.setStyleName("fail");
                                enlaceActor1.setComponentError(new UserError("No puede estar vacio"));
                                actores.get(Integer.parseInt(enlaceActor1.getId())).setEnlace("");
                            }
                        });
                        actorForm1.addComponents(nomActor1, enlaceActor1);
                        actoresContainer.addComponent(actorForm1);
                    }
                }else{
                    Notification notif = new Notification("Warning","El valor debe estar entre 1 y 20",Notification.Type.WARNING_MESSAGE);
                    notif.setDelayMsec(1000);
                    notif.setPosition(Position.TOP_CENTER);
                    notif.setIcon(VaadinIcons.WARNING);
                    notif.show(Page.getCurrent());
                }
            }catch(Exception exception){
                Notification notif = new Notification("Warning","No se admiten caracteres o que este vacio!",Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1000);
                notif.setPosition(Position.TOP_CENTER);
                notif.setIcon(VaadinIcons.WARNING);
                notif.show(Page.getCurrent());
            }
        });
        Button addButton = new Button("Editar Pelicula");
        addContainer.addComponents(titulo,tituloP, sinopsisP, genero,enlace,min, ano, numActores,addButton);
        HorizontalLayout formulario = new HorizontalLayout();
        formulario.addComponents(addContainer, actoresContainer);
        formulario.setSizeFull();
        addButton.addClickListener(click->{
            int camposValidos = 0;
            if(tituloP.getValue().isEmpty()){
                tituloP.setIcon(VaadinIcons.WARNING);
                tituloP.setStyleName("fail");
                tituloP.setComponentError(new UserError("No puede estar vacio"));
            }else{
                tituloP.setIcon(VaadinIcons.CHECK);
                tituloP.setStyleName("sucess");
                tituloP.setComponentError(null);
                camposValidos++;
            }
            if(sinopsisP.getValue().isEmpty()){
                sinopsisP.setIcon(VaadinIcons.WARNING);
                sinopsisP.setStyleName("fail");
                sinopsisP.setComponentError(new UserError("No puede estar vacio"));
            }else{
                sinopsisP.setIcon(VaadinIcons.CHECK);
                sinopsisP.setStyleName("sucess");
                sinopsisP.setComponentError(null);
                camposValidos++;
            }
            if(isNumeric(genero.getValue()) && !genero.getValue().isEmpty()){
                genero.setIcon(VaadinIcons.WARNING);
                genero.setStyleName("fail");
                genero.setComponentError(new UserError("No puede contener números"));
            }else{
                genero.setIcon(VaadinIcons.CHECK);
                genero.setStyleName("sucess");
                genero.setComponentError(null);
                camposValidos++;
            }
            if(enlace.getValue().isEmpty()){
                enlace.setIcon(VaadinIcons.WARNING);
                enlace.setStyleName("fail");
                enlace.setComponentError(new UserError("No puede estar vacio"));
            }else{
                enlace.setIcon(VaadinIcons.CHECK);
                enlace.setStyleName("sucess");
                enlace.setComponentError(null);
                camposValidos++;
            }
            if(!min.getValue().isEmpty()) {
                if (!isNumeric(min.getValue())) {
                    min.setIcon(VaadinIcons.WARNING);
                    min.setStyleName("fail");
                    min.setComponentError(new UserError("Solo se admiten numeros"));
                } else {
                    if (Integer.parseInt(min.getValue()) < 1 && Integer.parseInt(min.getValue()) > 1000) {
                        min.setIcon(VaadinIcons.WARNING);
                        min.setStyleName("fail");
                        min.setComponentError(new UserError("No puede ser 0 o negativo o mayor que 1000"));
                    } else {
                        min.setIcon(VaadinIcons.CHECK);
                        min.setStyleName("sucess");
                        min.setComponentError(null);
                        camposValidos++;
                    }
                }
            }else{
                min.setIcon(VaadinIcons.WARNING);
                min.setStyleName("fail");
                min.setComponentError(new UserError("No puede estar vacio"));
            }
            if(!ano.getValue().isEmpty()) {
                if (!isNumeric(ano.getValue())){
                    ano.setIcon(VaadinIcons.WARNING);
                    ano.setStyleName("fail");
                    ano.setComponentError(new UserError("Solo se admiten numeros"));
                }else{
                    if(Integer.parseInt(ano.getValue()) < 1850 || Integer.parseInt(ano.getValue()) > 3000) {
                        ano.setIcon(VaadinIcons.WARNING);
                        ano.setStyleName("fail");
                        ano.setComponentError(new UserError("No puede ser menor que 1850 y mayor que 3000"));
                    }else {
                        ano.setComponentError(null);
                        ano.setIcon(VaadinIcons.CHECK);
                        ano.setStyleName("sucess");
                        camposValidos++;
                    }
                }
            }else{
                ano.setIcon(VaadinIcons.WARNING);
                ano.setStyleName("fail");
                ano.setComponentError(new UserError("No puede estar vacio"));
            }
            if(!numActores.getValue().isEmpty()) {
                if (!isNumeric(numActores.getValue())){
                    numActores.setIcon(VaadinIcons.WARNING);
                    numActores.setStyleName("fail");
                    numActores.setComponentError(new UserError("Solo se admiten numeros"));
                }else{
                    if(Integer.parseInt(numActores.getValue()) < 1 || Integer.parseInt(numActores.getValue()) > 20) {
                        numActores.setIcon(VaadinIcons.WARNING);
                        numActores.setStyleName("fail");
                        numActores.setComponentError(new UserError("No puede ser menor que 1 mayor que 20"));
                    }else {
                        if(actores.isEmpty()){
                            numActores.setIcon(VaadinIcons.WARNING);
                            numActores.setStyleName("fail");
                            numActores.setComponentError(new UserError("Rellene los actores"));
                        }else{
                            int valid = 0;
                            for (Actor actor : actores) {
                                if (!("".equals(actor.getNombre())) && !isNumeric(actor.getNombre()) && !("".equals(actor.getEnlace()))) {
                                    valid++;
                                }
                            }
                            if(valid == actores.size()) {
                                numActores.setComponentError(null);
                                numActores.setIcon(VaadinIcons.CHECK);
                                numActores.setStyleName("sucess");
                                camposValidos++;
                            }else{
                                numActores.setIcon(VaadinIcons.WARNING);
                                numActores.setStyleName("fail");
                                numActores.setComponentError(new UserError("Rellene los actores correctamente"));
                            }
                        }
                    }
                }
            }else{
                numActores.setIcon(VaadinIcons.WARNING);
                numActores.setStyleName("fail");
                numActores.setComponentError(new UserError("No puede estar vacio o ser cero"));
            }
            if(camposValidos == 7){
                Notification notif = new Notification("Pelicula Editada!", "["+tituloP.getValue()+"]", Notification.Type.HUMANIZED_MESSAGE);
                notif.setDelayMsec(10000);
                notif.setPosition(Position.BOTTOM_LEFT);
                notif.setIcon(VaadinIcons.EDIT);
                notif.show(Page.getCurrent());
                Pelicula peli1 = new Pelicula();
                peli1.setTitulo(tituloP.getValue());
                peli1.setSinopsis(sinopsisP.getValue());
                if(genero.getValue().isEmpty()){
                    peli1.setGenero("");
                }else {
                    peli1.setGenero(genero.getValue());
                }
                peli1.setAtributos(new Atributos(Integer.parseInt(min.getValue()),Integer.parseInt(ano.getValue())));
                peli1.setReparto(actores);
                peli1.setEnlace(enlace.getValue());
                int num = 0;
                while(num < videoteca.getPeliculas().size()) {
                    if(videoteca.getPeliculas().get(num).getId() == peli.getId()){
                        break;
                    }
                    num++;
                }
                peli1.setId(peli.getId());
                videoteca.getPeliculas().remove(num);
                videoteca.getPeliculas().add(num, peli1);
                videoteca.setFecha(new SimpleDateFormat("dd/MM/yyyy").format(new Date())); //Añadimos la fecha actual y la formateamos
                videotecas.add(videoteca.getId()-1,videoteca);
                guardar(videotecas, "peliculas.json",false);
                peliculas(videoteca,vaadinRequest,videotecas);
            }
        });
        TabSheet tabedit = new TabSheet();
        deleteContainer.setCaption("Borrar");
        returnContainer.setCaption("Volver");
        tabedit.addTab(formulario, "Editar", VaadinIcons.EDIT);
        tabedit.addTab(deleteContainer, "Borrar", VaadinIcons.TRASH);
        tabedit.addTab(returnContainer, "Volver", VaadinIcons.ARROW_LEFT);
        tabedit.addSelectedTabChangeListener(listener->{
            if("Volver".equals(tabedit.getSelectedTab().getCaption())){
                peliculas(videoteca,vaadinRequest,videotecas);
            }
            if("Borrar".equals(tabedit.getSelectedTab().getCaption())){
                // Create a sub-window and set the content
                Window subWindow = new Window("Borrar Pelicula");
                subWindow.setModal(true);
                subWindow.addCloseListener(e->{
                    subWindow.close();
                    peliculas(videoteca,vaadinRequest,videotecas);
                });
                subWindow.setDraggable(false);
                subWindow.setResizable(false);

                VerticalLayout subContent = new VerticalLayout();
                subWindow.setContent(subContent);

                // Put some components in it
                subContent.addComponent(new Label("¿Estás seguro que quieres borrar?"));
                subContent.addComponent(new Label("Pelicula: " +peli.getTitulo()));
                Button bb = new Button("Borrar");
                bb.addClickListener(e->{
                    int num = 0;
                    while(num < videoteca.getPeliculas().size()) {
                        if(videoteca.getPeliculas().get(num).getId() == peli.getId()){
                            break;
                        }
                        num++;
                    }
                    videoteca.getPeliculas().remove(num);
                    videoteca.setFecha(new SimpleDateFormat("dd/MM/yyyy").format(new Date())); //Añadimos la fecha actual y la formateamos
                    videotecas.add(videoteca.getId()-1,videoteca);
                    guardar(videotecas, "peliculas.json",false);
                    subWindow.close();
                    Notification notif = new Notification("Pelicula Borrada!", "["+tituloP.getValue()+"]", Notification.Type.HUMANIZED_MESSAGE);
                    notif.setDelayMsec(10000);
                    notif.setPosition(Position.BOTTOM_LEFT);
                    notif.setIcon(VaadinIcons.EXIT);
                    notif.show(Page.getCurrent());
                    peliculas(videoteca,vaadinRequest,videotecas);
                });
                bb.setStyleName("delete");
                Button bb2 = new Button("No");
                bb2.addClickListener(e->{
                    subWindow.close();
                    peliculas(videoteca,vaadinRequest,videotecas);
                });
                subContent.addComponents(bb2,bb);

                // Center it in the browser window
                subWindow.center();

                // Open it in the UI
                addWindow(subWindow);

            }
        });
        layout.addComponents(tabedit);
        setContent(layout);
    }

    public static boolean hasDigits(String strNum){
        for(int i = 0; i < strNum.length(); i++){
            if(Character.isDigit(strNum.charAt(i))){
                return true;
            }
        }
        return false;
    }
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
    private void select(VaadinRequest vaadinRequest, int id, List<Videoteca> videotecas) throws VDException {
        if(id < 0){
            throw new VDException("La Videoteca seleccionada no es válida!");
        }else{
            try{
                if(videotecas.get(id) != null){
                    peliculas(videotecas.get(id), vaadinRequest, videotecas);
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
    public static void guardar(List<Videoteca> videotecas, String NOM_FICHERO, boolean b) {

        if(b){
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
        }else{
            try {
                if (videotecas.size() > 0) {
                    Lector.guardar(videotecas, NOM_FICHERO);
                    Notification notif = new Notification("<span style='color:green'>Sucess</span>", "["+NOM_FICHERO+"] Actualizado Correctamente", Notification.Type.HUMANIZED_MESSAGE);
                    notif.setDelayMsec(10000);
                    notif.setHtmlContentAllowed(true);
                    notif.setPosition(Position.BOTTOM_RIGHT);
                    notif.setIcon(VaadinIcons.CHECK);
                    notif.show(Page.getCurrent());
                } else {
                    Notification notif = new Notification("Error", "No hay videotecas para actualizar!", Notification.Type.ERROR_MESSAGE);
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

}
