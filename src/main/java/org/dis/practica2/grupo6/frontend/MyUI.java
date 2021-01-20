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
//Declaramos la libreria de Jquery
@JavaScript("https://code.jquery.com/jquery-3.5.1.slim.min.js")
@SuppressWarnings("serial")
@Theme("mytheme") //Cargamos los temas .scss
public class MyUI extends UI {
    @Override
    protected void init(VaadinRequest vaadinRequest) {
        Page.getCurrent().setTitle("Gestor de Videotecas");
        //Declaramos los contenedores principales de nuestra página
        final VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        final HorizontalLayout gridContainer = new HorizontalLayout();
        final HorizontalLayout importContainer = new HorizontalLayout();
        final HorizontalLayout saveContainer = new HorizontalLayout();
        final VerticalLayout optContainer = new VerticalLayout();
        TabSheet tabsheet = new TabSheet();

        List<Videoteca> Videotecas = new ArrayList<>();

        //Importamos las videotecas iniciales.
        importar(Videotecas, "peliculas.json");

        //Creamos un grid con las videotecas
        Grid<Videoteca> gridVideotecas = new Grid<>();
        gridVideotecas.setItems(Videotecas);
        gridVideotecas.addColumn(Videoteca::getId).setCaption("Id");
        gridVideotecas.addColumn(Videoteca::getNombre).setCaption("Nombre");
        gridVideotecas.addColumn(Videoteca::getGeneros).setCaption("Generos");
        gridVideotecas.addColumn(Videoteca::getUbicacion).setCaption("Ubicación");
        gridVideotecas.addColumn(Videoteca::getFecha).setCaption("Modified");
        gridContainer.setSizeFull();
        gridVideotecas.setSizeFull();

        //Añadimos un listener para la selección de una videoteca, para poder ver y editar sus peliculas
        gridVideotecas.addItemClickListener(e -> {
            try {
                select(vaadinRequest,e.getItem().getId()-1,Videotecas); //Seleccionamos
            } catch (VDException exception) {
                Notification notif = new Notification("Lo sentimos",exception.getMessage()+" Intente otra vez...",Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(20000);
                notif.setPosition(Position.TOP_CENTER);
                notif.setIcon(VaadinIcons.WARNING);
                notif.show(Page.getCurrent());
            }
        });

        //Definimos un layout para el siguiente punto
        Label label = new Label("*Haz click sobre cualquier campo de una videoteca para ver y editar sus peliculas");
        VerticalLayout v1 = new VerticalLayout();
        gridContainer.addComponents(gridVideotecas);
        v1.addComponents(gridContainer,label);
        FormLayout form = new FormLayout();

        //Creamos un pequeño formulario de upload
        Upload upload = new Upload();
        upload.setSizeFull();
        upload.setImmediateMode(false);
        //Bloqueamos el control de errores original, ya que lo realizamos nosotros.
        upload.setErrorHandler(errorEvent -> {});
        //Fin bloqueo de errores

        //Cuando termine de subirse el fichero recogemos su tipo
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

        //Realizamos un formulario 2 para la segunda opción un documento ya existente
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
                    //Una vez se ha podido guardar debemos disponibilizar un enlace para la descarga, con advanvced download listener
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
                            downloader.setFilePath(filePath); //Indicamos el nombre del fichero
                        }

                    });
                    downloader.extend(downloadButton);
                    //Si se guarda el fichero
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
                                new File(NOM_FICHERO).delete(); //Borramos el fichero generado

                            } catch (InterruptedException interruptedException) {
                                new File(NOM_FICHERO).delete(); //Borramos el fichero generado
                            }
                            optContainer.removeAllComponents();
                            optContainer.addComponent(form2);
                        });
                        thread.start(); //Empezamos el hilo
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

        //En el caso que queramos crear un nuevo fichero JSON
        TextField nombreFich = new TextField();
        nombreFich.setCaption("Crear un fichero:");
        nombreFich.setPlaceholder("Nombre del Fichero");
        //Primero el usuario debe introducir un nombre de fichero para guardar
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
                //Luego si es posible guardamos y disponibilizamos un boton para download
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
                                downloader.setFilePath(filePath); //Damos el nombre del documento
                            }

                        });
                downloader.extend(downloadButton);
                //Una vez iniciado la función a lo lasgo de 10 segundos se borrará el fichero.
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
                                new File(NOM_FICHERO).delete(); //Borramos el fichero

                            } catch (InterruptedException interruptedException) {
                                new File(NOM_FICHERO).delete(); //Borramos el fichero
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
        //Creamos una lista de selección para disponibilizar las opciones
            ListSelect<String> select = new ListSelect<>("Elige una opción de guardado: ");

            select.setItems("Nuevo Fichero", "Fichero Existente");

            //En el caso que sea igual que Nuevo Fichero
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

        //Gurdamos los contendores en las tabsheet
        optContainer.addComponent(form2);
        saveContainer.addComponents(select, optContainer);
        tabsheet.addTab(v1, "Select", VaadinIcons.CHECK_SQUARE);
        tabsheet.addTab(importContainer, "Importar", VaadinIcons.UPLOAD);
        tabsheet.addTab(saveContainer, "Guardar", VaadinIcons.DOWNLOAD);

        layout.addComponents(tabsheet);
        setContent(layout); //Contenedo principal
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
    //Esta función Añade el layout de peliculas para poder verlas y añadirlas asi como editarlas y leerlas
    private void peliculas(Videoteca videoteca, VaadinRequest vaadinRequest, List<Videoteca> videotecas){
        //Definimos las estructuras de nuestra web
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
        List<Pelicula> peliculas = videoteca.getPeliculas();
        Grid<Pelicula> gridPeliculas = new Grid<>();
        gridPeliculas.setItems(peliculas);

        //Añadimos las columnas del grid
        gridPeliculas.addColumn(Pelicula::getId).setCaption("Id");
        gridPeliculas.addColumn(Pelicula::getTitulo).setCaption("Titulo");
        gridPeliculas.addColumn(Pelicula::getSinopsis).setCaption("Sinopsis");
        gridPeliculas.addColumn(Pelicula::getGenero).setCaption("Género");
        gridPeliculas.addColumn(Pelicula::getActores).setCaption("Actores");
        gridPeliculas.addColumn(Pelicula::getEnlace).setCaption("Enlace");
        gridContainer.setSizeFull();
        gridPeliculas.setSizeFull();
        gridPeliculas.getEditor().setEnabled(true);

        //En el caso de selección mostramos más información
        gridPeliculas.addItemClickListener(e->{

            //Abrimos un modal
            Atributos att = e.getItem().getAtributos();
            Window subWindow = new Window("Detalles Pelicula: " +e.getItem().getTitulo());
            subWindow.setModal(true);
            subWindow.setDraggable(false);
            subWindow.setResizable(false);
            subWindow.setWidth("500px");
            subWindow.setHeight("300px");
            VerticalLayout subContent = new VerticalLayout();
            subWindow.setContent(subContent);
            //Añadimos los componentes basicos
            subContent.addComponent(new Label("Detalles:"));
            subContent.addComponent(new Label("Duración: "+ att.getDuracion() + " mins"));
            subContent.addComponent(new Label("Año de Estreno: "+ att.getAno()));

            Button button = new Button("Editar/Borrar");
            button.addClickListener(d->{
                subWindow.close();
                editar(e.getItem(),videoteca,vaadinRequest,videotecas);
            });
            subContent.addComponent(button);
            subWindow.center();
            addWindow(subWindow);
        });
        //Añado las opciónes para editar
        gridPeliculas.getEditor().setSaveCaption("Editar/Borrar");
        gridPeliculas.getEditor().setCancelCaption("Cancelar");
        gridPeliculas.getEditor().addSaveListener(e->{
            editar(e.getBean(), videoteca,vaadinRequest, videotecas); //Llamamos a editar
        });
        //Creamos los componentes para el formulario add pelicula
        gridContainer.addComponent(gridPeliculas);
        Label label = new Label("**Haz click sobre cualquier campo de una pelicula para ver los detalles u editar o borrar");
        VerticalLayout v1 = new VerticalLayout();
        v1.addComponents(gridContainer,label);
        Label titulo = new Label("Añadir una pelicula:");
        TextField tituloP = new TextField();
        tituloP.setPlaceholder("Titulo");
        //Titulo
        com.vaadin.ui.TextArea sinopsisP = new com.vaadin.ui.TextArea();
        sinopsisP.setRows(4);
        sinopsisP.setPlaceholder("Sinopsis");
        //Sinopsis
        TextField genero = new TextField();
        genero.setPlaceholder("Género");
        //Genero
        TextField enlace = new TextField();
        enlace.setPlaceholder("Enlace");
        //Enlace
        TextField min = new TextField();
        min.setCaption("Atributos:");
        //Atributos
        min.setPlaceholder("Minutos de Duración");
        min.setId("minutos");

        com.vaadin.ui.JavaScript.getCurrent().execute("document.getElementById('minutos').setAttribute('type', 'number')");
        TextField ano = new TextField();
        ano.setPlaceholder("Año de estreno");
        ano.setId("anos");
        //El numero de actores
        com.vaadin.ui.JavaScript.getCurrent().execute("document.getElementById('anos').setAttribute('type', 'number')");
        TextField numActores = new TextField();
        numActores.setCaption("[1-20] Actores");
        numActores.setPlaceholder("Número de Actores");
        numActores.setValue(String.valueOf(1));
        numActores.setId("numActores");
        //Modificamos el tipo de input mediante JS
        com.vaadin.ui.JavaScript.getCurrent().execute("document.getElementById('numActores').setAttribute('type', 'number')");

        List<Actor> actores = new ArrayList<>();
        FormLayout actorForm = new FormLayout();
        actorForm.setCaption("Actor 1");
        TextField nomActor = new TextField();
        nomActor.setPlaceholder("Nombre Actor");
        TextField enlaceActor = new TextField();
        enlaceActor.setPlaceholder("Enlace Wikipedia");

        //Añadimos un pequeño formulario para añadir los actores
        actores.add(0,new Actor("",""));
        nomActor.setId(""+0);
        nomActor.addValueChangeListener(l->{
            if(!nomActor.getValue().isEmpty()) {
                if (hasDigits(nomActor.getValue())) { //Si contiene números
                    nomActor.setIcon(VaadinIcons.WARNING);
                    nomActor.setStyleName("fail");
                    nomActor.setComponentError(new UserError("No se admiten números"));
                    actores.get(Integer.parseInt(nomActor.getId())).setNombre("");
                } else { //Si no es numerico
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
        //Repetimos el mismo proceso con el enlace del actor
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

        //Para no realizar la validación una sola vez, se mantiene actualizado filtrando las letras, ya que es un type numeric
        numActores.addValueChangeListener(e ->{
            try{
                int num = Integer.parseInt(numActores.getValue());
                if(num > 0 && num <= 20) {
                    actoresContainer.removeAllComponents();
                    actores.clear();
                    //Recogemos todas las opciones

                    for (int i = 0; i < num; i++) {
                        Actor actor = new Actor("","");
                        actores.add(i,actor);
                        //Creamos un actor nuevo
                        FormLayout actorForm1 = new FormLayout();
                        actorForm1.setCaption("Actor "+ (i+1));
                        TextField nomActor1 = new TextField();
                        //Establecemos lo mismo que más arriba, pero con la diferencia es que puede ser hasta 20 o más
                        nomActor1.setId(""+i);
                        nomActor1.addValueChangeListener(l->{
                            if(!nomActor1.getValue().isEmpty()) {
                                if (hasDigits(nomActor1.getValue())) { //Si no esta vacio y no tiene digitos
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
                        //Repetimos el processo con el enlace
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
                }else{//En caso de sobrepasar el rango
                    Notification notif = new Notification("Warning","El valor debe estar entre 1 y 20",Notification.Type.WARNING_MESSAGE);
                    notif.setDelayMsec(1000);
                    notif.setPosition(Position.TOP_CENTER);
                    notif.setIcon(VaadinIcons.WARNING);
                    notif.show(Page.getCurrent());
                    actoresContainer.removeAllComponents();
                    actoresContainer.addComponents(actorForm);
                }
            }catch(Exception exception){ //En caso de que salga una excepción
                Notification notif = new Notification("Warning","No se admiten caracteres o que este vacio!",Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1000);
                notif.setPosition(Position.TOP_CENTER);
                notif.setIcon(VaadinIcons.WARNING);
                notif.show(Page.getCurrent());
                actoresContainer.removeAllComponents();
                actoresContainer.addComponents(actorForm);
            }
        });
        //Realizamos la validación del formulario
        actoresContainer.addComponents(actorForm);
        Button addButton = new Button("Añadir Pelicula");
        addContainer.addComponents(titulo,tituloP, sinopsisP, genero,enlace,min, ano, numActores,addButton);
        HorizontalLayout formulario = new HorizontalLayout();
        formulario.addComponents(addContainer, actoresContainer);
        formulario.setSizeFull();
        addButton.addClickListener(click->{
            int camposValidos = 0;
            //Debemos obtener 7 campos válidos para poder enviar el formulario
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
            //---------------------------------------------------------------
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
            //---------------------------------------------------------------
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
            //---------------------------------------------------------------
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
            //---------------------------------------------------------------
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
            //---------------------------------------------------------------
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
            //---------------------------------------------------------------
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
            //---------------------------------------------------------------
            //Si son todos correctos lo añadimos
            if(camposValidos == 7){
                Notification notif = new Notification("Añadida la Pelicula", "["+tituloP.getValue()+"]", Notification.Type.HUMANIZED_MESSAGE);
                notif.setDelayMsec(10000);
                notif.setPosition(Position.BOTTOM_LEFT);
                notif.setIcon(VaadinIcons.CHECK_SQUARE);
                notif.show(Page.getCurrent());
                //Creamos una nueva pelicula
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
                //Subimos la pelicula y actualizamos la fecha y guardamos.
                videoteca.setFecha(new SimpleDateFormat("dd/MM/yyyy").format(new Date())); //Añadimos la fecha actual y la formateamos
                videotecas.add(videoteca.getId()-1,videoteca);
                guardar(videotecas, "peliculas.json",false);
                peliculas(videoteca,vaadinRequest,videotecas);
            }
        });

        //Definimos los contenedores
        returnContainer.setCaption("Volver");
        TabSheet tabpelis = new TabSheet();
        tabpelis.addTab(v1, "Ver", VaadinIcons.CHECK_SQUARE);
        tabpelis.addTab(formulario, "Añadir", VaadinIcons.PLUS);
        tabpelis.addTab(returnContainer, "Volver", VaadinIcons.ARROW_LEFT);

        //Si el usuario desea volver, volvemos a la interfaz original
        tabpelis.addSelectedTabChangeListener(listener->{
            if("Volver".equals(tabpelis.getSelectedTab().getCaption())){
                init(vaadinRequest);
            }
        });
        layout.addComponents(tabpelis);
        setContent(layout);
    }
    //Función que permite editar una pelicula, asi como borrarla
    public void editar(Pelicula peli, Videoteca videoteca, VaadinRequest vaadinRequest, List<Videoteca> videotecas){
        //Declaramos todos los contenedores
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

        //Disponibilizamos el formulario para poder editar
        Label titulo = new Label("Editar pelicula: ["+peli.getTitulo()+"]");
        TextField tituloP = new TextField();
        tituloP.setPlaceholder("Titulo");
        tituloP.setValue(peli.getTitulo());
        //---------------------------------------------------------------
        com.vaadin.ui.TextArea sinopsisP = new com.vaadin.ui.TextArea();
        sinopsisP.setRows(4);
        sinopsisP.setPlaceholder("Sinopsis");
        sinopsisP.setValue(peli.getSinopsis());
        TextField genero = new TextField();
        genero.setPlaceholder("Género");
        genero.setValue(peli.getGenero());
        //---------------------------------------------------------------
        TextField enlace = new TextField();
        enlace.setPlaceholder("Enlace");
        enlace.setValue(peli.getEnlace());
        //---------------------------------------------------------------
        TextField min = new TextField();
        Atributos att = peli.getAtributos();
        min.setCaption("Atributos:");
        min.setPlaceholder("Minutos de Duración");
        min.setId("minutos");
        min.setValue(String.valueOf(att.getDuracion()));
        com.vaadin.ui.JavaScript.getCurrent().execute("document.getElementById('minutos').setAttribute('type', 'number')");
        //---------------------------------------------------------------
        TextField ano = new TextField();
        ano.setPlaceholder("Año de estreno");
        ano.setId("anos");
        ano.setValue(String.valueOf(att.getAno()));
        com.vaadin.ui.JavaScript.getCurrent().execute("document.getElementById('anos').setAttribute('type', 'number')");
        //---------------------------------------------------------------
        List<Actor> actores = peli.getReparto();
        TextField numActores = new TextField();
        numActores.setCaption("[1-20] Actores");
        numActores.setPlaceholder("Número de Actores");
        numActores.setValue(String.valueOf(actores.size()));
        numActores.setId("numActores");
        com.vaadin.ui.JavaScript.getCurrent().execute("document.getElementById('numActores').setAttribute('type', 'number')");
        //---------------------------------------------------------------
        actoresContainer.setResponsive(true);
        for( int i = 0;i < actores.size();i++) {
            FormLayout actorForm = new FormLayout();
            actorForm.setCaption("Actor "+ (i+1));
            TextField nomActor = new TextField();
            nomActor.setValue(actores.get(i).getNombre());
            nomActor.setPlaceholder("Nombre Actor");
            TextField enlaceActor = new TextField();
            enlaceActor.setPlaceholder("Enlace Wikipedia");
            enlaceActor.setValue(actores.get(i).getEnlace());
            nomActor.setId("" + i);
            //Definimos un listener para cuando el valor cambie, y asi se valida automaticamente
            nomActor.addValueChangeListener(l -> {
                if (!nomActor.getValue().isEmpty()) {
                    if (hasDigits(nomActor.getValue())) { //Si tiene digitos
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
            //Validamos el enlace tambien
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
        //Si el numero de actores cambia tambien volvemos a cambiar
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

                        //---------------------------------------------------------------
                        //Recogemos el nombre del actor
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

                        //---------------------------------------------------------------
                        //Recogemos el enlace del actor
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
                }else{ //Realizamos otro control de errores
                    Notification notif = new Notification("Warning","El valor debe estar entre 1 y 20",Notification.Type.WARNING_MESSAGE);
                    notif.setDelayMsec(1000);
                    notif.setPosition(Position.TOP_CENTER);
                    notif.setIcon(VaadinIcons.WARNING);
                    notif.show(Page.getCurrent());
                }
            }catch(Exception exception){ //Captamos las excepciones
                Notification notif = new Notification("Warning","No se admiten caracteres o que este vacio!",Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1000);
                notif.setPosition(Position.TOP_CENTER);
                notif.setIcon(VaadinIcons.WARNING);
                notif.show(Page.getCurrent());
            }
        });
        //---------------------------------------------------------------
        //Añadimos un botón para enviar el formulario
        Button addButton = new Button("Editar Pelicula");
        addContainer.addComponents(titulo,tituloP, sinopsisP, genero,enlace,min, ano, numActores,addButton);
        HorizontalLayout formulario = new HorizontalLayout();
        formulario.addComponents(addContainer, actoresContainer);
        formulario.setSizeFull();
        //Realizamos un filtrado para comprobar si la información esta de acorde
        addButton.addClickListener(click->{
            int camposValidos = 0; //Se espera obtener

            //---------------------------------------------------------------
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

            //---------------------------------------------------------------
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

            //---------------------------------------------------------------
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

            //---------------------------------------------------------------
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

            //---------------------------------------------------------------
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

            //---------------------------------------------------------------
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

            //---------------------------------------------------------------
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
                            //Realizamos una valiación de todos los actores, si sus inputs son válidos
                            int valid = 0;
                            for (Actor actor : actores) {
                                if (!("".equals(actor.getNombre())) && !isNumeric(actor.getNombre()) && !("".equals(actor.getEnlace()))) {
                                    valid++;
                                }
                            }
                            if(valid == actores.size()) { //Si son validos podemos añadir
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
            //---------------------------------------------------------------
            //Una vez que tengamos todo podemos añadir
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
                //Recogemos la id y la posición actual de nuestra peli, y la sobrescribirmos con una que tiene el contenido nuevo
                peli1.setId(peli.getId());
                videoteca.getPeliculas().remove(num);
                videoteca.getPeliculas().add(num, peli1);

                //---------------------------------------------------------------
                //Volvemos a actualizar la fecha
                videoteca.setFecha(new SimpleDateFormat("dd/MM/yyyy").format(new Date())); //Añadimos la fecha actual y la formateamos
                videotecas.add(videoteca.getId()-1,videoteca);
                guardar(videotecas, "peliculas.json",false);
                peliculas(videoteca,vaadinRequest,videotecas);
            }
        });

        //Añadimos la tabshee para poder ofrecer más opciones
        TabSheet tabedit = new TabSheet();
        deleteContainer.setCaption("Borrar");
        returnContainer.setCaption("Volver");
        tabedit.addTab(formulario, "Editar", VaadinIcons.EDIT);
        tabedit.addTab(deleteContainer, "Borrar", VaadinIcons.TRASH);
        tabedit.addTab(returnContainer, "Volver", VaadinIcons.ARROW_LEFT);

        //En el caso que cambie
        tabedit.addSelectedTabChangeListener(listener->{

            //---------------------------------------------------------------
            //Si el usuario desea volver
            if("Volver".equals(tabedit.getSelectedTab().getCaption())){
                peliculas(videoteca,vaadinRequest,videotecas);
            }

            //---------------------------------------------------------------
            //Si el usuario desea borrar la pelicula
            if("Borrar".equals(tabedit.getSelectedTab().getCaption())){
                // Declaramos el modal
                Window subWindow = new Window("Borrar Pelicula");
                subWindow.setModal(true);
                //Si cierra volvemos a las peliculas
                subWindow.addCloseListener(e->{
                    subWindow.close();
                    peliculas(videoteca,vaadinRequest,videotecas);
                });
                subWindow.setDraggable(false);
                subWindow.setResizable(false);

                VerticalLayout subContent = new VerticalLayout();
                subWindow.setContent(subContent);


                //Preguntamos si el usuario esta seguro de que quiere borrar
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
                    //---------------------------------------------------------------
                    //Borramos la pelicula mediante su ID y posición.
                    videoteca.getPeliculas().remove(num);
                    videoteca.setFecha(new SimpleDateFormat("dd/MM/yyyy").format(new Date())); //Añadimos la fecha actual y la formateamos
                    videotecas.add(videoteca.getId()-1,videoteca);
                    guardar(videotecas, "peliculas.json",false);
                    subWindow.close();

                    //---------------------------------------------------------------
                    //Notificamos al usuario
                    Notification notif = new Notification("Pelicula Borrada!", "["+tituloP.getValue()+"]", Notification.Type.HUMANIZED_MESSAGE);
                    notif.setDelayMsec(10000);
                    notif.setPosition(Position.BOTTOM_LEFT);
                    notif.setIcon(VaadinIcons.EXIT);
                    notif.show(Page.getCurrent());
                    peliculas(videoteca,vaadinRequest,videotecas);
                });
                bb.setStyleName("delete");
                Button bb2 = new Button("No");
                //Si el usuario no quiere borrar volvemos al listado de peliculas
                bb2.addClickListener(e->{
                    subWindow.close();
                    peliculas(videoteca,vaadinRequest,videotecas);
                });
                subContent.addComponents(bb2,bb);

                subWindow.center();

                addWindow(subWindow);

            }
        });
        //---------------------------------------------------------------
        //Añado el contenido del layout
        layout.addComponents(tabedit);
        setContent(layout);
    }

    //Función que detecta si una cadena tiene caracteres numericos
    public static boolean hasDigits(String strNum){
        for(int i = 0; i < strNum.length(); i++){
            if(Character.isDigit(strNum.charAt(i))){
                return true;
            }
        }
        return false;
    }
    //Función que detecta si los caracteres no son numericos, o que no se pueden parsear
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
    //Función responsable de seleccionar una viedeoteca y abrir su menu
    public void select(VaadinRequest vaadinRequest, int id, List<Videoteca> videotecas) throws VDException {
        if(id < 0){ //Si la ID es negativa
            throw new VDException("La Videoteca seleccionada no es válida!");
        }else{
            try{
                if(videotecas.get(id) != null){ //Si existe
                    peliculas(videotecas.get(id), vaadinRequest, videotecas);
                }else{ //Si no
                    throw new VDException("La Videoteca seleccionada no existe!");
                }
            }catch (Exception e){ //Si hay algun exeption
                throw new VDException("La Videoteca seleccionada no existe!");
            }
        }
    }
    //Inicio Función importar
    public static void importar(List<Videoteca> videotecas, String NOM_FICHERO){
        try {
            int oldSize = videotecas.size(); //Tamaño actual para comparar
            Lector.importar(videotecas, NOM_FICHERO); //Importamos
            if((videotecas.size() - oldSize) == 0){ //Si se puede importar se compara y vemos si se ha importado algo.
                Notification notif = new Notification("Error", "Estas videotecas ya están añadidas!",Notification.Type.ERROR_MESSAGE);
                notif.setDelayMsec(20000);
                notif.setPosition(Position.TOP_CENTER);
                notif.setIcon(VaadinIcons.EXCLAMATION);
                notif.show(Page.getCurrent());
            }else { //Si se puede añadir
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
    //Funcioón responsable de guardar un fichero o actualizar, b = true [Crear], b= false, actualizar
    public static void guardar(List<Videoteca> videotecas, String NOM_FICHERO, boolean b) {

        if(b){ //Dividimos los criterios
            try {
                if (videotecas.size() > 0) { //Si hay vidotecas que guardar
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

            } catch (VDException e) { //Si se produce algun error
                Notification notif = new Notification("Lo sentimos", e.getMessage() + " Intente otra vez...", Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(20000);
                notif.setPosition(Position.TOP_CENTER);
                notif.setIcon(VaadinIcons.WARNING);
                notif.show(Page.getCurrent());
            }
        }else{ //Si queremos actualizar
            try {
                if (videotecas.size() > 0) { //Si hay videotecas para actualizar
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

            } catch (VDException e) { //Si hay algún error
                Notification notif = new Notification("Lo sentimos", e.getMessage() + " Intente otra vez...", Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(20000);
                notif.setPosition(Position.TOP_CENTER);
                notif.setIcon(VaadinIcons.WARNING);
                notif.show(Page.getCurrent());
            }
        }
    }

}
