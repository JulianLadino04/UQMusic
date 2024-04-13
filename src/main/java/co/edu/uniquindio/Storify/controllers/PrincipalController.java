package co.edu.uniquindio.Storify.controllers;

import co.edu.uniquindio.Storify.model.Autor;
import co.edu.uniquindio.Storify.model.Cancion;
import co.edu.uniquindio.Storify.model.Storify;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.web.WebView;

import java.util.ArrayList;

public class PrincipalController {

    @FXML
    private TableView<Cancion> tablaCanciones;
    @FXML
    private TextField buscador;
    @FXML
    private RadioButton bttO, bttArtista, bttY;
    @FXML
    private TableColumn<Cancion, String> columnaCaratula;
    @FXML
    private TableColumn<Cancion, String> columnaNombreCancion;
    @FXML
    private TableColumn<Cancion, String> columnaNombreAlbum;
    @FXML
    private TableColumn<Cancion, String> columnaArtistas;
    @FXML
    private TableColumn<Cancion, Double> columnaDuracion;
    @FXML
    private ImageView imagenView;
    @FXML
    private Label labelCancion;
    @FXML
    private Label resultadoBusqueda;

    private ToggleGroup toggleGroup;
    private final Storify storify = Storify.getInstance();
    private ArrayList<Cancion> cancionesSistema = storify.enviarCanciones();

    @FXML
    public void initialize() {
        toggleGroup = new ToggleGroup();

        // Asigna el grupo de toogle a los RadioButtons
        bttO.setToggleGroup(toggleGroup);
        bttArtista.setToggleGroup(toggleGroup);
        bttY.setToggleGroup(toggleGroup);

        // Configura un listener para los eventos de selección
        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                // Si no hay RadioButton seleccionado, selecciona el anterior
                oldValue.setSelected(true);
            }
        });

        // Configurar la columna de carátula para mostrar imágenes
        columnaCaratula.setCellValueFactory(new PropertyValueFactory<>("caratula"));
        columnaCaratula.setCellFactory(param -> new javafx.scene.control.TableCell<>() {
        private final ImageView imageView = new ImageView();

        @Override
        protected void updateItem(String caratulaPath, boolean empty) {
           super.updateItem(caratulaPath, empty);
           if (empty || caratulaPath == null) {
               setGraphic(null);
           } else {
               try {
                   Image image = new Image(caratulaPath);
                   imageView.setImage(image);
                   imageView.setFitWidth(50); // Tamaño de la imagen
                   imageView.setPreserveRatio(true);
                   setGraphic(imageView);
               } catch (Exception e) {
                        System.out.println("Error al cargar la imagen: " + caratulaPath);
                        System.out.println("Excepción: " + e.getMessage());
                        e.printStackTrace();
                        setGraphic(null); // Mostrar celda vacía si no se puede cargar la imagen
               }
           }
        }
    });

        // Configurar otras columnas para mostrar propiedades de la canción
        columnaNombreCancion.setCellValueFactory(new PropertyValueFactory<>("nombreCancion"));
        columnaNombreAlbum.setCellValueFactory(new PropertyValueFactory<>("nombreAlbum"));
        columnaArtistas.setCellValueFactory(new PropertyValueFactory<>("artistas"));
        columnaDuracion.setCellValueFactory(new PropertyValueFactory<>("duracion"));

        // Agregar listener para cambios de selección en la tabla
        tablaCanciones.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Obtener la canción seleccionada
                Cancion cancionSeleccionada = newValue;

                // Actualizar la ImageView con la carátula de la canción
                if (cancionSeleccionada.getCaratula() != null) {
                    Image caratulaImage = new Image(cancionSeleccionada.getCaratula());
                    imagenView.setImage(caratulaImage);
                } else {
                    imagenView.setImage(null); // Limpiar la ImageView si no hay carátula
                }

                // Actualizar el Label con el nombre de la canción y los artistas
                String nombreCancion = cancionSeleccionada.getNombreCancion();
                String artistas = cancionSeleccionada.getArtistas();
                labelCancion.setText(nombreCancion + " - " + artistas);
            } else {
                // Limpiar la ImageView y el Label si no hay canción seleccionada
                imagenView.setImage(null);
                labelCancion.setText("");
            }
        });

        // Asignar las canciones al TableView
        tablaCanciones.getItems().addAll(cancionesSistema);
    }
    @FXML
    void play() {
        Cancion cancionSeleccionada = tablaCanciones.getSelectionModel().getSelectedItem();
        if (cancionSeleccionada != null) {
            String youtubeEmbed = convertToEmbedUrl(cancionSeleccionada.getUrl());
            System.out.println(youtubeEmbed);

            try {
                Stage stage = new Stage();
                WebView webView = new WebView();
                webView.getEngine().load(youtubeEmbed);

                stage.setScene(new javafx.scene.Scene(webView, 640, 390));
                stage.show();
            } catch (Exception e) {
                System.out.println("Error al reproducir la canción: " + e.getMessage());
            }
        }
    }

    public static String convertToEmbedUrl(String youtubeUrl) {
        // Verifica si la URL es válida
        if (youtubeUrl != null && youtubeUrl.trim().length() > 0) {
            // Verifica si la URL contiene el formato de un enlace de YouTube
            if (youtubeUrl.contains("youtube.com") || youtubeUrl.contains("youtu.be")) {
                // Extrae el ID del vídeo de la URL
                String videoId = extractVideoId(youtubeUrl);
                // Crea el enlace embebido usando el ID del vídeo
                return "https://www.youtube.com/embed/" + videoId;
            }
        }
        return null;
    }

    // Método auxiliar para extraer el ID del vídeo de la URL
    private static String extractVideoId(String youtubeUrl) {
        String videoId = null;
        if (youtubeUrl != null && youtubeUrl.trim().length() > 0) {
            String[] splitUrl = youtubeUrl.split("(vi/|v=|/v/|youtu.be/|/embed/)");
            if (splitUrl.length > 1) {
                videoId = splitUrl[1].split("[^0-9a-zA-Z_-]")[0];
            }
        }
        return videoId;
    }

    // Método para buscar por artistas
    private ObservableList<Cancion> buscarPorArtistas(String nombre) {
        ObservableList<Cancion> artistasFiltrados = FXCollections.observableArrayList();
        for (Autor artista : storify.enviarAutores()) {
            if (artista.getNombre().toLowerCase().contains(nombre.toLowerCase())) {
                artistasFiltrados.addAll(artista.getListaCanciones().toArrayList());
            }
        }
        return artistasFiltrados;
    }

    // Método para buscar por O
    private ObservableList<Cancion> buscarPorO(String[] atributos) {
        ObservableList<Cancion> cancionesCoincidentes = FXCollections.observableArrayList();
        if (atributos.length == 0) {
            return FXCollections.observableArrayList(cancionesSistema);
        }
        for (Cancion cancion : cancionesSistema) {
            for (String atributo : atributos) {
                if (cancion.coincideAtributo(atributo)) {
                    cancionesCoincidentes.add(cancion);
                }
            }
        }
        return cancionesCoincidentes;
    }

    // Método para buscar por Y
    private ObservableList<Cancion> buscarPorY(String[] atributos) {
        ObservableList<Cancion> cancionesCoincidentes = FXCollections.observableArrayList();
        if (atributos.length == 0) {
            return FXCollections.observableArrayList(cancionesSistema);
        }
        for (Cancion cancion : cancionesSistema) {
            int atributosCoincidentes = 0;
            for (String atributo : atributos) {
                if (cancion.coincideAtributo(atributo)) {
                    atributosCoincidentes+=1;
                }
            }
            if (atributosCoincidentes == atributos.length) {
                cancionesCoincidentes.add(cancion);
            }
        }
        return cancionesCoincidentes;
    }

    public void buscar (ActionEvent actionEvent) {
        if (bttArtista.isSelected()){
            System.out.println("BUSQUEDA POR ARTISTA");
            buscador.textProperty().addListener((observable, oldValue, newValue) ->
                    tablaCanciones.setItems(buscarPorArtistas(newValue)));
        }
        if (bttO.isSelected()){
            System.out.println("BUSQUEDA POR O");
            buscador.textProperty().addListener((observable, oldValue, newValue) ->
                    tablaCanciones.setItems(buscarPorO(newValue.split(","))));
        }
        if (bttY.isSelected()){
            System.out.println("BUSQUEDA POR Y");
            buscador.textProperty().addListener((observable, oldValue, newValue) ->
                    tablaCanciones.setItems(buscarPorY(newValue.split(","))));
        }
    }

}
